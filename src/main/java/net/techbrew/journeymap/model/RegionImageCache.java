/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.model;

import com.google.common.cache.*;
import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.data.DataCache;
import net.techbrew.journeymap.io.FileHandler;
import net.techbrew.journeymap.io.RegionImageHandler;
import net.techbrew.journeymap.thread.JMThreadFactory;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RegionImageCache
{
    public static final long flushInterval = TimeUnit.SECONDS.toMillis(30);
    public static final long regionCacheAge = flushInterval / 2;
    static final Logger logger = JourneyMap.getLogger();
    final LoadingCache<RegionImageSet.Key, RegionImageSet> regionImageSetsCache;
    private volatile long lastFlush;
    private Minecraft minecraft = FMLClientHandler.instance().getClient();

    /**
     * Underlying caches are to be managed by the DataCache.
     */
    private RegionImageCache()
    {
        this.regionImageSetsCache = DataCache.instance().getRegionImageSets();
        lastFlush = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(5);

        // Add shutdown hook to flush cache to disk
        Runtime.getRuntime().addShutdownHook(new JMThreadFactory("rcache").newThread(new Runnable()
        {
            @Override
            public void run()
            {
                flushToDisk();
                if (logger.isEnabled(Level.DEBUG))
                {
                    logger.debug("RegionImageCache flushing to disk on shutdown"); //$NON-NLS-1$
                }
            }
        }));
    }

    /**
     * Singleton
     */
    public static RegionImageCache instance()
    {
        return Holder.INSTANCE;
    }

    public static LoadingCache<RegionImageSet.Key, RegionImageSet> initRegionImageSetsCache(CacheBuilder<Object, Object> builder)
    {
        return builder
                .expireAfterAccess(regionCacheAge, TimeUnit.SECONDS)
                .removalListener(new RemovalListener<RegionImageSet.Key, RegionImageSet>()
                {
                    @Override
                    @ParametersAreNonnullByDefault
                    public void onRemoval(RemovalNotification<RegionImageSet.Key, RegionImageSet> notification)
                    {
                        RegionImageSet regionImageSet = notification.getValue();
                        if (regionImageSet != null)
                        {
                            regionImageSet.writeToDisk(false);
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

    public RegionImageSet getRegionImageSet(RegionCoord rCoord)
    {
        return regionImageSetsCache.getUnchecked(RegionImageSet.Key.from(rCoord));
    }

    // Doesn't trigger access on cache
    private Collection<RegionImageSet> getRegionImageSets()
    {
        return regionImageSetsCache.asMap().values();
    }

    public void updateTextures(boolean forceFlush)
    {
        for (RegionImageSet regionImageSet : getRegionImageSets())
        {
            if (regionImageSet.hasChunkUpdates())
            {
                regionImageSet.finishChunkUpdates();
                //System.out.println("UPDATED: " + regionImageSet.simpleRCoord.full);
            }
            else
            {
                //System.out.println("Should expire eventually: " + regionImageSet.simpleRCoord.full);
            }
        }

        // Write to disk if needed
        if (forceFlush)
        {
            flushToDisk();
        }
        else
        {
            autoFlush();
        }
    }

    private void autoFlush()
    {
        if (lastFlush + flushInterval < System.currentTimeMillis())
        {
            if (logger.isEnabled(Level.DEBUG))
            {
                logger.debug("RegionImageCache auto-flushing"); //$NON-NLS-1$
            }
            flushToDisk();
        }
    }

    public void flushToDisk()
    {
        for (RegionImageSet regionImageSet : getRegionImageSets())
        {
            regionImageSet.writeToDisk(false);
        }
        lastFlush = System.currentTimeMillis();
    }

    /**
     * Get a list of regions in the cache that are dirty.
     * This won't include regions which changed but have already been removed from the cache.
     *
     * @param time
     * @return
     */
    public List<RegionCoord> getDirtySince(final MapType mapType, long time)
    {
        if (time <= lastFlush)
        {
            if (logger.isEnabled(Level.DEBUG))
            {
                logger.debug("Nothing dirty, last flush was " + (time - lastFlush) + "ms before " + time);
            }
            return Collections.EMPTY_LIST;
        }
        else
        {
            ArrayList<RegionCoord> list = new ArrayList<RegionCoord>();
            for (RegionImageSet regionImageSet : getRegionImageSets())
            {
                ImageHolder imageHolder = regionImageSet.imageHolders.get(mapType);
                if (imageHolder.getImageTimestamp() > time)
                {
                    list.add(regionImageSet.getRegionCoord());
                }
            }
            if (logger.isEnabled(Level.DEBUG))
            {
                logger.debug("Dirty regions: " + list.size() + " of " + regionImageSetsCache.size());
            }
            return list;
        }
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
        regionImageSetsCache.invalidateAll();
        regionImageSetsCache.cleanUp();
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
            // Toss all images and textures without flushing to disk
            for (RegionImageSet regionImageSet : getRegionImageSets())
            {
                regionImageSet.clear();
            }

            // Clear cache
            this.clear();

            // Remove directories
            boolean result = true;
            for (File dir : dirs)
            {
                FileHandler.delete(dir);
                logger.info(String.format("Deleted image directory %s: %s", dir, !dir.exists()));
                if (dir.exists())
                {
                    result = false;
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

    private static class Holder
    {
        private static final RegionImageCache INSTANCE = new RegionImageCache();
    }
}
