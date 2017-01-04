/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.model;

import com.google.common.cache.*;
import journeymap.client.data.DataCache;
import journeymap.client.io.FileHandler;
import journeymap.client.io.RegionImageHandler;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

public enum RegionImageCache
{
    INSTANCE;

    public long firstFileFlushIntervalSecs = 5;
    public long flushFileIntervalSecs = 60;
    public long textureCacheAgeSecs = 30;
    static final Logger logger = Journeymap.getLogger();
    private volatile long lastFlush;

    /**
     * Underlying caches are to be managed by the DataCache.
     */
    private RegionImageCache()
    {
        lastFlush = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(firstFileFlushIntervalSecs);
    }

    public LoadingCache<RegionImageSet.Key, RegionImageSet> initRegionImageSetsCache(CacheBuilder<Object, Object> builder)
    {
        return builder
                .expireAfterAccess(textureCacheAgeSecs, TimeUnit.SECONDS)
                .removalListener(new RemovalListener<RegionImageSet.Key, RegionImageSet>()
                {
                    @Override
                    @ParametersAreNonnullByDefault
                    public void onRemoval(RemovalNotification<RegionImageSet.Key, RegionImageSet> notification)
                    {
                        RegionImageSet regionImageSet = notification.getValue();
                        if (regionImageSet != null)
                        {
                            // Don't force it, but do it synchronously to ensure things aren't GCd before it's done.
                            int count = regionImageSet.writeToDisk(false);
                            if (count > 0 && Journeymap.getLogger().isDebugEnabled())
                            {
                                Journeymap.getLogger().debug("Wrote to disk before removal from cache: " + regionImageSet);
                            }
                            regionImageSet.clear();
                        }
                    }
                }).build(new CacheLoader<RegionImageSet.Key, RegionImageSet>()
                {
                    @Override
                    @ParametersAreNonnullByDefault
                    public RegionImageSet load(RegionImageSet.Key key) throws Exception
                    {
                        return new RegionImageSet(key);
                    }
                });
    }

    public RegionImageSet getRegionImageSet(ChunkMD chunkMd, MapType mapType)
    {
        if (chunkMd.hasChunk())
        {
            Minecraft mc = FMLClientHandler.instance().getClient();
            Chunk chunk = chunkMd.getChunk();
            RegionCoord rCoord = RegionCoord.fromChunkPos(FileHandler.getJMWorldDir(mc), mapType, chunk.xPosition, chunk.zPosition);
            return getRegionImageSet(rCoord);
        }
        else
        {
            return null;
        }
    }

    public RegionImageSet getRegionImageSet(RegionCoord rCoord)
    {
        return DataCache.INSTANCE.getRegionImageSets().getUnchecked(RegionImageSet.Key.from(rCoord));
    }

    public RegionImageSet getRegionImageSet(RegionImageSet.Key rCoordKey)
    {
        return DataCache.INSTANCE.getRegionImageSets().getUnchecked(rCoordKey);
    }

    // Doesn't trigger access on cache
    private Collection<RegionImageSet> getRegionImageSets()
    {
        return DataCache.INSTANCE.getRegionImageSets().asMap().values();
    }

    /**
     * Finalize images before they can be bound as textures or written to disk
     *
     * @param forceFlush whether to force images to be written to disk
     * @param async      whether to do file writes on a different thread
     */
    public void updateTextures(boolean forceFlush, boolean async)
    {
        for (RegionImageSet regionImageSet : getRegionImageSets())
        {
            regionImageSet.finishChunkUpdates();
        }

        // Write to disk if needed
        if (forceFlush || (lastFlush + TimeUnit.SECONDS.toMillis(flushFileIntervalSecs) < System.currentTimeMillis()))
        {
            if (!forceFlush && logger.isEnabled(Level.DEBUG))
            {
                logger.debug("RegionImageCache auto-flushing"); //$NON-NLS-1$
            }

            if (async)
            {
                flushToDiskAsync(false);
            }
            else
            {
                flushToDisk(false);
            }
        }
    }

    /**
     * Write all dirty images to disk asynchronously.
     *
     * @param force Whether to force writes even if not needed.
     */
    public void flushToDiskAsync(boolean force)
    {
        int count = 0;
        for (RegionImageSet regionImageSet : getRegionImageSets())
        {
            // Don't force writes that aren't necessary
            count += regionImageSet.writeToDiskAsync(force);
        }
        lastFlush = System.currentTimeMillis();
    }

    /**
     * Write all dirty images to disk immediately.
     *
     * @param force Whether to force writes even if not needed.
     */
    public void flushToDisk(boolean force)
    {
        for (RegionImageSet regionImageSet : getRegionImageSets())
        {
            // Don't force writes that aren't necessary
            regionImageSet.writeToDisk(force);
        }
        lastFlush = System.currentTimeMillis();
    }

    /**
     * lol
     */
    public long getLastFlush()
    {
        return lastFlush;
    }

    /**
     * Get a list of region images in the cache updated since the time specified.
     * This won't include regions which changed but have already been removed from the cache.
     *
     * @param time
     * @return
     */
    public List<RegionCoord> getChangedSince(final MapType mapType, long time)
    {
        ArrayList<RegionCoord> list = new ArrayList<RegionCoord>();
        for (RegionImageSet regionImageSet : getRegionImageSets())
        {
            if (regionImageSet.updatedSince(mapType, time))
            {
                list.add(regionImageSet.getRegionCoord());
            }
        }
        if (logger.isEnabled(Level.DEBUG))
        {
            logger.debug("Dirty regions: " + list.size() + " of " + DataCache.INSTANCE.getRegionImageSets().size());
        }
        return list;
    }

    /**
     * Check whether a given RegionCoord is dirty since a given time
     *
     * @param time
     * @return
     */
    public boolean isDirtySince(final RegionCoord rc, final MapType mapType, final long time)
    {
        RegionImageSet ris = getRegionImageSet(rc);
        if (ris == null)
        {
            return false;
        }
        else
        {
            return ris.updatedSince(mapType, time);
        }
    }

    public void clear()
    {
        for (RegionImageSet regionImageSet : getRegionImageSets())
        {
            // Ensures textures are properly disposed of
            regionImageSet.clear();
        }
        DataCache.INSTANCE.getRegionImageSets().invalidateAll();
        DataCache.INSTANCE.getRegionImageSets().cleanUp();
    }

    public boolean deleteMap(MapState state, boolean allDims)
    {
        RegionCoord fakeRc = new RegionCoord(state.getWorldDir(), 0, 0, state.getDimension());
        File imageDir = RegionImageHandler.getImageDir(fakeRc, MapType.day(state.getDimension())).getParentFile();
        if (!imageDir.getName().startsWith("DIM"))
        {
            logger.error("Expected DIM directory, got " + imageDir);
            return false;
        }

        File[] dirs;
        if (allDims)
        {
            dirs = imageDir.getParentFile().listFiles(new FilenameFilter()
            {
                @Override
                public boolean accept(File dir, String name)
                {
                    return dir.isDirectory() && name.startsWith("DIM");
                }
            });
        }
        else
        {
            dirs = new File[]{imageDir};
        }

        if (dirs != null && dirs.length > 0)
        {
            // Clear cache
            this.clear();

            // Remove directories
            boolean result = true;
            for (File dir : dirs)
            {
                if (dir.exists())
                {
                    FileHandler.delete(dir);
                    logger.info(String.format("Deleted image directory %s: %s", dir, !dir.exists()));
                    if (dir.exists())
                    {
                        result = false;
                    }
                }
            }

            logger.info("Done deleting directories");
            return result;
        }
        else
        {
            logger.info("Found no DIM directories in " + imageDir);
            return true;
        }
    }
}
