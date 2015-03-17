/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.model;

import com.google.common.cache.*;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.Constants.MapType;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.io.FileHandler;
import net.techbrew.journeymap.io.RegionImageHandler;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.render.texture.DelayedTexture;
import net.techbrew.journeymap.render.texture.TextureCache;
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
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class RegionImageCache
{
    public static final long flushInterval = TimeUnit.SECONDS.toMillis(30);
    public static final long regionCacheAge = flushInterval / 2;
    private static boolean logCacheActions = false;
    final Cache<Integer, Future<DelayedTexture>> futureTextureCache;
    final LoadingCache<Integer, DelayedTexture> regionTextureCache;
    final LoadingCache<RegionCoord, RegionImageSet> regionImageSetsCache;
    private volatile long lastFlush;
    private MapType lastRequestedMapType = MapType.day;

    /**
     * Underlying caches are to be managed by the DataCache.
     */
    public RegionImageCache(final Cache<Integer, Future<DelayedTexture>> futureTextureCache,
                            final LoadingCache<Integer, DelayedTexture> regionTextureCache,
                            final LoadingCache<RegionCoord, RegionImageSet> regionImageSetsCache)
    {
        this.futureTextureCache = futureTextureCache;
        this.regionTextureCache = regionTextureCache;
        this.regionImageSetsCache = regionImageSetsCache;

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

    public static Cache<Integer, Future<DelayedTexture>> initFutureTextureCache(CacheBuilder<Object, Object> builder)
    {
        return builder.expireAfterAccess(regionCacheAge, TimeUnit.MILLISECONDS).build();
    }

    public static LoadingCache<Integer, DelayedTexture> initRegionTextureCache(CacheBuilder<Object, Object> builder)
    {
        return builder
                .expireAfterAccess(regionCacheAge, TimeUnit.MILLISECONDS)
                .removalListener(new RemovalListener<Integer, DelayedTexture>()
                {
                    @Override
                    @ParametersAreNonnullByDefault
                    public void onRemoval(RemovalNotification<Integer, DelayedTexture> notification)
                    {
                        if (notification != null && notification.getValue() != null)
                        {
                            if (logCacheActions)
                            {
                                logCacheAction("REMOVE", notification.getValue());
                            }
                            TextureCache.instance().expireTexture(notification.getValue());
                        }
                    }
                })
                .build(new CacheLoader<Integer, DelayedTexture>()
                {
                    @Override
                    @ParametersAreNonnullByDefault
                    public DelayedTexture load(Integer key) throws Exception
                    {
                        return new DelayedTexture();
                    }
                });
    }

    public static LoadingCache<RegionCoord, RegionImageSet> initRegionImageSetsCache(CacheBuilder<Object, Object> builder)
    {
        return builder
                .expireAfterAccess(regionCacheAge, TimeUnit.MILLISECONDS)
                .removalListener(new RemovalListener<RegionCoord, RegionImageSet>()
                {
                    @Override
                    @ParametersAreNonnullByDefault
                    public void onRemoval(RemovalNotification<RegionCoord, RegionImageSet> notification)
                    {
                        if (notification.getValue() != null)
                        {
                            notification.getValue().writeToDisk(false);
                            //System.out.println(notification.getKey() + " last touched: " + (System.currentTimeMillis() - notification.getValue().getLastTouched()));
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

    public void updateRegionTexture(final RegionCoord rCoord, final Constants.MapType mapType, boolean aSync)
    {
        final int hash = Objects.hash(rCoord, mapType);
        final DelayedTexture existing = getRegionTextureByHash(hash);

        if (existing == null)
        {
            JourneyMap.getLogger().error(String.format("RegionImageCache error during updateRegionTexture(): No existing texture for %s %s", rCoord, mapType));
            return;
        }

        synchronized (futureTextureCache)
        {
            Future<DelayedTexture> future = futureTextureCache.getIfPresent(hash);
            if (future != null)
            {
                if (!future.isDone())
                {
                    if (logCacheActions)
                    {
                        logCacheAction("ASYNC UPDATE PENDING", existing);
                    }
                    return;
                }
            }

            long imageTime = getRegionImageSet(rCoord).touch().getWrapper(mapType).getTimestamp();
            if (existing.getLastUpdated() >= imageTime)
            {
                if (logCacheActions)
                {
                    logCacheAction("ASYNC UPDATE UNNECESSARY", existing);
                }
                return;
            }

            if (existing.getDescription() == null)
            {
                existing.setDescription(String.format("%s (%s) %s", rCoord, rCoord.vSlice, mapType));
            }

            if (aSync)
            {

                futureTextureCache.put(hash,
                        TextureCache.instance().scheduleTextureTask(new Callable<DelayedTexture>()
                        {
                            @Override
                            public DelayedTexture call() throws Exception
                            {
                                try
                                {
                                    ImageSet.Wrapper wrapper = getRegionImageSet(rCoord).getWrapper(mapType);
                                    existing.setImage(wrapper.getImage(), false);
                                    existing.setLastUpdated(wrapper.getTimestamp());

                                    if (logCacheActions)
                                    {
                                        logCacheAction("ASYNC UPDATE DONE", existing);
                                    }
                                    return existing;
                                }
                                catch (Exception e)
                                {
                                    JourneyMap.getLogger().error(String.format("RegionImageCache error during updateRegionTexture() texture for %s %s: %s", rCoord, mapType,
                                            LogFormatter.toString(e)));
                                    return null;
                                }
                            }
                        })
                );
            }
            else
            {
                ImageSet.Wrapper wrapper = getRegionImageSet(rCoord).getWrapper(mapType);
                existing.setImage(wrapper.getImage(), false);
                existing.setLastUpdated(wrapper.getTimestamp());
                if (logCacheActions)
                {
                    logCacheAction("SYNC UPDATE DONE", existing);
                }
            }
        }
    }

    private DelayedTexture getRegionTextureByHash(int hash)
    {
        try
        {
            return regionTextureCache.get(hash);
        }
        catch (Throwable e)
        {
            JourneyMap.getLogger().error(String.format("RegionImageCache error in getRegionTextureByHash(): %s", LogFormatter.toString(e)));
            return null;
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
        int hash = Objects.hash(rCoord, mapType);
        DelayedTexture texture = getRegionTextureByHash(hash);
        if (texture == null)
        {
            updateRegionTexture(rCoord, mapType, true);
            return null;
        }
        getRegionImageSet(rCoord).touch(); // touch
        texture.bindTexture();
        return texture.getSafeGlTextureId();
    }

    /**
     * Must be called on GL Context thread.
     */
    public DelayedTexture getRegionTexture(RegionCoord rCoord, Constants.MapType mapType)
    {
        int hash = Objects.hash(rCoord, mapType);
        DelayedTexture texture = getRegionTextureByHash(hash);
        if (lastRequestedMapType != mapType)
        {
            lastRequestedMapType = mapType;
        }

        if (texture == null || textureNeedsUpdate(rCoord, mapType, 0))
        {
            updateRegionTexture(rCoord, mapType, true);
            if (logCacheActions)
            {
                logCacheAction("HIT AUTOUPDATE", texture);
            }
        }
        else
        {
            if (logCacheActions)
            {
                JourneyMap.getLogger().warn(String.format("RegionImageCache: %s %s (GLID %s) on %s",
                        "MISS", String.format("%s %s", rCoord, mapType), "?", "?"));
            }
        }

        return texture;
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
            RegionImageSet ris = entry.getValue();
            for (MapType mapType : mapTypes)
            {
                ImageSet.Wrapper wrapper = ris.getWrapper(mapType);
                if (textureNeedsUpdate(ris.rCoord, wrapper.mapType, 0))
                {
                    updateRegionTexture(ris.rCoord, wrapper.mapType, wrapper.mapType == lastRequestedMapType);
                    if (logCacheActions)
                    {
                        JourneyMap.getLogger().info("MAPTASK UPDATE " + ris.rCoord + " " + wrapper.mapType);
                    }
                }
                else
                {
                    checkExpired(ris);
                    if (logCacheActions)
                    {
                        JourneyMap.getLogger().info("MAPTASK SKIP " + ris.rCoord + " " + wrapper.mapType);
                    }
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
            regionImageSetsCache.invalidate(regionImageSet.rCoord);
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
     * Check whether a given Region's Texture needs to be updated
     */
    public boolean textureNeedsUpdate(final RegionCoord rCoord, final MapType mapType, int marginOfError)
    {
        DelayedTexture texture = getRegionTextureByHash(Objects.hash(rCoord, mapType));
        if (texture != null)
        {
            if (texture.isUnused())
            {
                return true;
            }
            if (getRegionImageSet(rCoord).updatedSince(mapType, texture.getLastUpdated() + marginOfError))
            {
                return true;
            }
        }
        else
        {
            return true;
        }
        return false;
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

        futureTextureCache.invalidateAll();
        futureTextureCache.cleanUp();

        regionTextureCache.invalidateAll();
        regionTextureCache.cleanUp();
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
}
