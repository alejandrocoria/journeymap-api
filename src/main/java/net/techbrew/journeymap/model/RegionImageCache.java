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
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.Constants.MapType;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.data.DataCache;
import net.techbrew.journeymap.io.FileHandler;
import net.techbrew.journeymap.io.RegionImageHandler;
import net.techbrew.journeymap.render.texture.DelayedTexture;
import net.techbrew.journeymap.render.texture.TextureImpl;
import net.techbrew.journeymap.thread.JMThreadFactory;
import org.apache.logging.log4j.Level;

import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

public class RegionImageCache
{
    public static final long flushInterval = TimeUnit.SECONDS.toMillis(30);
    public static final long regionCacheAge = flushInterval / 2;
    private static boolean logCacheActions = false;
    final LoadingCache<RegionCoord, RegionImageSet> regionImageSetsCache;
    private volatile long lastFlush;
    private MapType lastRequestedMapType = MapType.day;
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
                if (JourneyMap.getLogger().isEnabled(Level.DEBUG))
                {
                    JourneyMap.getLogger().debug("RegionImageCache flushing to disk on shutdown"); //$NON-NLS-1$
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

    public static LoadingCache<RegionCoord, RegionImageSet> initRegionImageSetsCache(CacheBuilder<Object, Object> builder)
    {
        return builder
                .removalListener(new RemovalListener<RegionCoord, RegionImageSet>()
                {
                    @Override
                    @ParametersAreNonnullByDefault
                    public void onRemoval(RemovalNotification<RegionCoord, RegionImageSet> notification)
                    {
                        RegionImageSet regionImageSet = notification.getValue();
                        if (regionImageSet != null)
                        {
                            regionImageSet.writeToDisk(false);
                            regionImageSet.clear();
                        }
                    }
                }).build(new CacheLoader<RegionCoord, RegionImageSet>()
                {
                    @Override
                    @ParametersAreNonnullByDefault
                    public RegionImageSet load(RegionCoord key) throws Exception
                    {
                        return new RegionImageSet(key);
                    }
                });
    }

    private static void logCacheAction(String action, TextureImpl texture)
    {
        if (texture != null)
        {
            String time = texture.getLastUpdated() == 0 ? "Never" : new SimpleDateFormat("HH:mm:ss.SSS").format(new Date(texture.getLastUpdated()));
            JourneyMap.getLogger().info(String.format("RegionImageCache: %s %s (GLID %s) on %s",
                    action, texture.getDescription(), texture.getSafeGlTextureId(), time));
        }
        else
        {
            JourneyMap.getLogger().info(String.format("RegionImageCache: %s on NULL?", action));
        }
    }

    /**
     * Must be called on GL Context thread.
     */
    public Integer getBoundRegionTextureId(RegionCoord rCoord, Constants.MapType mapType)
    {
        if (lastRequestedMapType != mapType)
        {
            lastRequestedMapType = mapType;
        }
        DelayedTexture texture = getRegionImageSet(rCoord).touch().getHolder(mapType).getTexture();
        texture.bindTexture();
        return texture.getSafeGlTextureId();
    }

    public RegionImageSet getRegionImageSet(RegionCoord rCoord)
    {
        return regionImageSetsCache.getUnchecked(rCoord);
    }

    public boolean contains(RegionCoord rCoord)
    {
        return regionImageSetsCache.getIfPresent(rCoord) != null;
    }

    public BufferedImage getGuaranteedImage(RegionCoord rCoord, Constants.MapType mapType)
    {
        RegionImageSet ris = getRegionImageSet(rCoord).touch();
        return ris.getImage(mapType);
    }

    // Doesn't trigger access on cache
    private Set<Entry<RegionCoord, RegionImageSet>> getRegionImageSets()
    {
        return regionImageSetsCache.asMap().entrySet();
    }

    public void updateTextures(boolean forceFlush, EnumSet<MapType> mapTypes)
    {
        for (Map.Entry<RegionCoord, RegionImageSet> entry : getRegionImageSets())
        {
            RegionImageSet regionImageSet = entry.getValue();
            ImageHolder imageHolder = regionImageSet.getHolder(lastRequestedMapType);
            if (imageHolder.updateTexture())
            {
                if (logCacheActions)
                {
                    JourneyMap.getLogger().info("MAPTASK UPDATE " + regionImageSet.rCoord + " " + imageHolder.mapType);
                }
            }
            else
            {
                checkExpired(regionImageSet);
                if (logCacheActions)
                {
                    JourneyMap.getLogger().info("MAPTASK SKIP " + regionImageSet.rCoord + " " + imageHolder.mapType);
                }
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

    private void checkExpired(RegionImageSet regionImageSet)
    {
        if (System.currentTimeMillis() - regionImageSet.getLastTouched() > regionCacheAge)
        {
            if (minecraft.isGamePaused())
            {
                // Keep alive
                regionImageSet.touch();
            }
            else
            {
                regionImageSetsCache.invalidate(regionImageSet.rCoord);
            }
        }
        //System.out.println(System.currentTimeMillis() - regionImageSet.getLastTouched());
    }

    private void autoFlush()
    {
        if (lastFlush + flushInterval < System.currentTimeMillis())
        {
            if (JourneyMap.getLogger().isEnabled(Level.DEBUG))
            {
                JourneyMap.getLogger().debug("RegionImageCache auto-flushing"); //$NON-NLS-1$
            }
            flushToDisk();
        }
    }

    public void flushToDisk()
    {
        for (Map.Entry<RegionCoord, RegionImageSet> entry : getRegionImageSets())
        {
            entry.getValue().writeToDisk(false);
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
            if (JourneyMap.getLogger().isEnabled(Level.DEBUG))
            {
                JourneyMap.getLogger().debug("Nothing dirty, last flush was " + (time - lastFlush) + "ms before " + time);
            }
            return Collections.EMPTY_LIST;
        }
        else
        {
            ArrayList<RegionCoord> list = new ArrayList<RegionCoord>();
            for (Map.Entry<RegionCoord, RegionImageSet> entry : getRegionImageSets())
            {
                RegionImageSet regionImageSet = entry.getValue();
                if (regionImageSet.updatedSince(mapType, time))
                {
                    list.add(regionImageSet.rCoord);
                }
            }
            if (JourneyMap.getLogger().isEnabled(Level.DEBUG))
            {
                JourneyMap.getLogger().debug("Dirty regions: " + list.size() + " of " + regionImageSetsCache.size());
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
        RegionCoord fakeRc = new RegionCoord(state.getWorldDir(), 0, null, 0, state.getDimension());
        File imageDir = RegionImageHandler.getImageDir(fakeRc, MapType.day).getParentFile();
        if (!imageDir.getName().startsWith("DIM"))
        {
            JourneyMap.getLogger().error("Expected DIM directory, got " + imageDir);
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
                FileHandler.delete(dir);
                JourneyMap.getLogger().info(String.format("Deleted image directory %s: %s", dir, !dir.exists()));
                if (dir.exists())
                {
                    result = false;
                }
            }

            JourneyMap.getLogger().info("Done deleting directories");
            return result;
        }
        else
        {
            JourneyMap.getLogger().info("Found no DIM directories in " + imageDir);
            return true;
        }
    }

    private static class Holder
    {
        private static final RegionImageCache INSTANCE = new RegionImageCache();
    }
}
