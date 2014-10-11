/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.model;

import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.Constants.MapType;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.io.RegionImageHandler;
import net.techbrew.journeymap.thread.JMThreadFactory;
import org.apache.logging.log4j.Level;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

public class RegionImageCache
{

    private static final int SIZE = 25;
    private static final long flushInterval = TimeUnit.SECONDS.toMillis(30);
    //private volatile Set<RegionCoord> dirty;
    private final Object lock = new Object();
    private volatile Map<RegionCoord, RegionImageSet> imageSets;
    private volatile long lastFlush;

    // Private constructor
    private RegionImageCache()
    {
        imageSets = Collections.synchronizedMap(new CacheMap(SIZE));

        //dirty = Collections.synchronizedSet(new HashSet<RegionCoord>(SIZE));
        lastFlush = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(5);

        // Init thread factory
        JMThreadFactory tf = new JMThreadFactory("rcache");

        // Add shutdown hook to flush cache to disk
        Runtime.getRuntime().addShutdownHook(tf.newThread(new Runnable()
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
        final RegionImageHandler rfh = RegionImageHandler.getInstance();
        synchronized (lock)
        {
            for (ChunkImageSet cis : chunkImageSets)
            {
                final RegionCoord rCoord = cis.getCCoord().getRegionCoord();
                final RegionImageSet ris = getRegionImageSet(rCoord);
                ris.insertChunk(cis, forceFlush);
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
        RegionImageHandler rfh = RegionImageHandler.getInstance();
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
