/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.waypoint;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.io.FileHandler;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.model.Waypoint;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

/**
 * Disk-backed cache for Waypoints.
 */
public class WaypointStore
{
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Cache<String, Waypoint> cache = CacheBuilder.newBuilder().build();
    private final Set<Integer> dimensions = new HashSet<Integer>();
    private boolean loaded = false;
    private WaypointStore()
    {
    }

    public static WaypointStore instance()
    {
        return Holder.INSTANCE;
    }

    private boolean writeToFile(Waypoint waypoint)
    {
        File waypointFile = null;
        try
        {
            // Write to file
            waypointFile = new File(FileHandler.getWaypointDir(), waypoint.getFileName());
            FileWriter fw = new FileWriter(waypointFile);
            fw.write(gson.toJson(waypoint));
            fw.flush();
            fw.close();
            return true;
        }
        catch (Exception e)
        {
            JourneyMap.getLogger().severe(String.format("Can't save waypoint file %s: %s", waypointFile, LogFormatter.toString(e)));
            return false;
        }
    }

    public Collection<Waypoint> getAll()
    {
        return cache.asMap().values();
    }

    public void save(Waypoint waypoint)
    {
        cache.put(waypoint.getId(), waypoint);
        boolean saved = writeToFile(waypoint);
        if (saved)
        {
            waypoint.setDirty(false);
        }
    }

    public void bulkSave()
    {
        for (Waypoint waypoint : cache.asMap().values())
        {
            if (waypoint.isDirty())
            {
                boolean saved = writeToFile(waypoint);
                if (saved)
                {
                    waypoint.setDirty(false);
                }
            }
        }
    }

    public void remove(Waypoint waypoint)
    {
        cache.invalidate(waypoint.getId());
        File waypointFile = null;
        waypointFile = new File(FileHandler.getWaypointDir(), waypoint.getFileName());
        remove(waypointFile);
    }

    private void remove(File waypointFile)
    {
        try
        {
            waypointFile.delete();
        }
        catch (Exception e)
        {
            JourneyMap.getLogger().warning(String.format("Can't delete waypoint file %s: %s", waypointFile, e.getMessage()));
            waypointFile.deleteOnExit();
        }
    }

    public void reset()
    {
        cache.invalidateAll();
        dimensions.clear();
        loaded = false;
    }

    public void load()
    {
        synchronized (cache)
        {
            cache.invalidateAll();

            try
            {
                File waypointDir = FileHandler.getWaypointDir();

                ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
                waypoints.addAll(new ReiReader().loadWaypoints(waypointDir, true));
                waypoints.addAll(new VoxelReader().loadWaypoints(waypointDir, true));
                waypoints.addAll(new JmReader().loadWaypoints(waypointDir));

                load(waypoints, false);

                JourneyMap.getLogger().info(String.format("Loaded %s waypoints for world", cache.size()));
            }
            catch (Exception e)
            {
                JourneyMap.getLogger().severe(String.format("Error loading waypoints: %s", LogFormatter.toString(e)));
            }
        }
    }

    public void load(Collection<Waypoint> waypoints, boolean forceSave)
    {
        for (Waypoint waypoint : waypoints)
        {
            if (forceSave || (!waypoint.isReadOnly() && waypoint.isDirty()))
            {
                save(waypoint);
            }
            else
            {
                cache.put(waypoint.getId(), waypoint);
            }

            dimensions.addAll(waypoint.getDimensions());
        }
        loaded = true;
    }

    public boolean hasLoaded()
    {
        return loaded;
    }

    public List<Integer> getLoadedDimensions()
    {
        return new ArrayList<Integer>(dimensions);
    }

    private static class Holder
    {
        private static final WaypointStore INSTANCE = new WaypointStore();
    }


}
