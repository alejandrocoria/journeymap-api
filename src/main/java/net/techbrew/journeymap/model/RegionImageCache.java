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
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.render.texture.DelayedTexture;
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.thread.JMThreadFactory;
import org.apache.logging.log4j.Level;

import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class RegionImageCache
{
    private static final int SIZE = 25;
    private static final long flushInterval = TimeUnit.SECONDS.toMillis(30);
    private final Object lock = new Object();

    private final Cache<Integer, Future<DelayedTexture>> futureTextureCache;
    private final LoadingCache<Integer, DelayedTexture> regionTextureCache;
    private final List<DelayedTexture> expiredTextures;

    private volatile Map<RegionCoord, RegionImageSet> imageSets;
    private volatile long lastFlush;
    private boolean logCacheActions = false;

    private MapType lastRequestedMapType = MapType.day;

    // Private constructor
    private RegionImageCache()
    {
        imageSets = Collections.synchronizedMap(new CacheMap(SIZE));

        //dirty = Collections.synchronizedSet(new HashSet<RegionCoord>(SIZE));
        lastFlush = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(5);

        this.expiredTextures = Collections.synchronizedList(new ArrayList<DelayedTexture>());

        this.futureTextureCache = CacheBuilder.newBuilder()
                .expireAfterAccess(10, TimeUnit.SECONDS)
                .build();

        this.regionTextureCache = CacheBuilder.newBuilder()
                .expireAfterAccess(15, TimeUnit.SECONDS)
                .removalListener(new RemovalListener<Integer, DelayedTexture>()
                {
                    @Override
                    public void onRemoval(RemovalNotification<Integer, DelayedTexture> notification)
                    {
                        if (notification != null && notification.getValue() != null)
                        {
                            //if (logCacheActions)
                            {
                                JourneyMap.getLogger().info(String.format("RegionImageCache: Recycling %s", notification.getValue()));
                            }
                            expiredTextures.add(notification.getValue());
                        }
                    }
                })
                .build(new CacheLoader<Integer, DelayedTexture>()
                {
                    @Override
                    public DelayedTexture load(Integer key) throws Exception
                    {
                        return new DelayedTexture();
                    }
                });

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

    // Get singleton instance.  Concurrency-safe.
    public static RegionImageCache getInstance()
    {
        return Holder.INSTANCE;
    }

    private void updateRegionTexture(final RegionCoord rCoord, final Constants.MapType mapType, boolean aSync)
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

            long imageTime = getRegionImageSet(rCoord).getWrapper(mapType).getTimestamp();
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
                existing.setDescription(String.format("%s %s", rCoord, mapType));
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

    /**
     * Must be called on GL Context thread.
     */
    private DelayedTexture getRegionTextureByHash(int hash)
    {
        try
        {
            return regionTextureCache.get(hash);
        }
        catch (ExecutionException e)
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
        int hash = Objects.hash(rCoord, mapType);
        DelayedTexture texture = getRegionTextureByHash(hash);
        if (texture == null)
        {
            return null;
        }
        if (texture.isUnused())
        {
            updateRegionTexture(rCoord, mapType, false);
        }
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

        if (textureNeedsUpdate(rCoord, mapType))
        {
            updateRegionTexture(rCoord, mapType, true);
            if (logCacheActions)
            {
                logCacheAction("HIT AUTOUPDATE", texture);
            }
        }
        else
        {
            //if (logCacheActions)
            {
                JourneyMap.getLogger().warn(String.format("RegionImageCache: %s %s (GLID %s) on %s",
                        "ERROR MISSING", String.format("%s %s", rCoord, mapType), "?", "?"));
            }
        }

        clearExpiredTextures();

        return texture;
    }

    private RegionImageSet getRegionImageSet(RegionCoord rCoord)
    {
        synchronized (lock)
        {
            RegionImageSet ris = imageSets.get(rCoord);
            if (ris == null)
            {
                ris = new RegionImageSet(rCoord);
                imageSets.put(rCoord, ris);
            }
            return ris;
        }
    }

    public boolean contains(RegionCoord rCoord)
    {
        synchronized (lock)
        {
            return imageSets.containsKey(rCoord);
        }
    }

    public List<RegionCoord> getRegions()
    {
        synchronized (lock)
        {
            return new ArrayList<RegionCoord>(imageSets.keySet());
        }
    }

    public BufferedImage getGuaranteedImage(RegionCoord rCoord, Constants.MapType mapType)
    {
        RegionImageSet ris = getRegionImageSet(rCoord);
        return ris.getImage(mapType);
    }

    public void putAll(final Collection<ChunkImageSet> chunkImageSets, boolean forceFlush)
    {
        synchronized (lock)
        {
            for (ChunkImageSet cis : chunkImageSets)
            {
                final RegionCoord rCoord = cis.getCCoord().getRegionCoord();
                final RegionImageSet ris = getRegionImageSet(rCoord);
                ris.insertChunk(cis);
            }

            if (forceFlush)
            {
                flushToDisk();
            }
        }

        if (!forceFlush)
        {
            autoFlush();
        }
    }

    private void autoFlush()
    {
        synchronized (lock)
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
    }

    public void flushToDisk()
    {
        synchronized (lock)
        {
            for (RegionImageSet ris : imageSets.values())
            {
                ris.writeToDisk(false);
            }
            lastFlush = System.currentTimeMillis();
        }
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
            ArrayList<RegionCoord> list = new ArrayList<RegionCoord>(imageSets.size());
            synchronized (lock)
            {
                for (Entry<RegionCoord, RegionImageSet> entry : imageSets.entrySet())
                {
                    if (entry.getValue().updatedSince(mapType, time))
                    {
                        list.add(entry.getKey());
                    }
                }
                if (JourneyMap.getLogger().isEnabled(Level.DEBUG))
                {
                    JourneyMap.getLogger().debug("Dirty regions: " + list.size() + " of " + imageSets.size());
                }
            }
            return list;
        }
    }

    /**
     * Check whether a given Region's Texture needs to be updated
     */
    public boolean textureNeedsUpdate(final RegionCoord rCoord, final MapType mapType)
    {
        DelayedTexture texture = getRegionTextureByHash(Objects.hash(rCoord, mapType));
        if (texture != null)
        {
            if (texture.isUnused() || getRegionImageSet(rCoord).updatedSince(mapType, texture.getLastUpdated()))
            {
                return true;
            }
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
        synchronized (lock)
        {
            for (ImageSet imageSet : imageSets.values())
            {
                imageSet.clear();
            }
            imageSets.clear();
        }

        futureTextureCache.invalidateAll();
        futureTextureCache.cleanUp();

        regionTextureCache.invalidateAll();
        regionTextureCache.cleanUp();

        clearExpiredTextures();
    }

    public void clearExpiredTextures()
    {
        for (DelayedTexture expired : expiredTextures)
        {
            if (logCacheActions)
            {
                logCacheAction("DELETE", expired);
            }
            expired.deleteTexture();
        }
        expiredTextures.clear();
    }

    private void logCacheAction(String action, DelayedTexture delayedTexture)
    {
        if (delayedTexture != null)
        {
            String time = delayedTexture.getLastUpdated() == 0 ? "Never" : new SimpleDateFormat("HH:mm:ss.SSS").format(new Date(delayedTexture.getLastUpdated()));
            JourneyMap.getLogger().info(String.format("RegionImageCache: %s %s (GLID %s) on %s",
                    action, delayedTexture.getDescription(), delayedTexture.getSafeGlTextureId(), time));
        }
        else
        {
            JourneyMap.getLogger().info(String.format("RegionImageCache: %s on NULL?", action));
        }
    }

    // On-demand-holder for instance
    private static class Holder
    {
        private static final RegionImageCache INSTANCE = new RegionImageCache();
    }

    /**
     * LinkedHashMap serves as a LRU cache that can write a RIS to disk
     * when it's removed.
     *
     * @author mwoodman
     */
    class CacheMap extends LinkedHashMap<RegionCoord, RegionImageSet>
    {

        private final int capacity;

        CacheMap(int capacity)
        {
            super(capacity + 1, 1.1f, true);
            this.capacity = capacity;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<RegionCoord, RegionImageSet> entry)
        {
            Boolean remove = size() > capacity;
            if (remove)
            {
                if (JourneyMap.getLogger().isEnabled(Level.DEBUG))
                {
                    JourneyMap.getLogger().debug("RegionImageCache purging " + entry.getKey()); //$NON-NLS-1$
                }
                entry.getValue().writeToDisk(false);
            }
            if (JourneyMap.getLogger().isEnabled(Level.DEBUG))
            {
                JourneyMap.getLogger().debug("RegionImageCache size: " + (this.size() - 1)); //$NON-NLS-1$
            }
            return remove;
        }
    }
}
