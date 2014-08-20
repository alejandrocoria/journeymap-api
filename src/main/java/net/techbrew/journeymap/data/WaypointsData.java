/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.data;

import com.google.common.cache.CacheLoader;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.model.Waypoint;
import net.techbrew.journeymap.properties.WaypointProperties;
import net.techbrew.journeymap.waypoint.ReiReader;
import net.techbrew.journeymap.waypoint.VoxelReader;
import net.techbrew.journeymap.waypoint.WaypointStore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Provides waypoint data
 *
 * @author mwoodman
 */
public class WaypointsData extends CacheLoader<Class, Collection<Waypoint>>
{
    /**
     * Reset state so classes can be checked again. Useful
     * after post-init of all mods.
     */
    public static void reset()
    {
        ReiReader.modLoaded = null;
        VoxelReader.modLoaded = null;
    }

    /**
     * Whether native waypoint management is enabled.
     *
     * @return
     */
    public static boolean isManagerEnabled()
    {
        return JourneyMap.getWaypointProperties().managerEnabled.get();
    }

    /**
     * Whether Rei is loaded.
     *
     * @return
     */
    public static boolean isReiMinimapEnabled()
    {
        if (ReiReader.modLoaded == null)
        {
            try
            {
                ReiReader.modLoaded = waypointClassesFound(ReiReader.classNames);
            }
            catch (Throwable t)
            {
                ReiReader.modLoaded = false;
                JourneyMap.getLogger().warn("Rei's Minimap Waypoints can't be used: " + LogFormatter.toString(t));
            }
            if (ReiReader.modLoaded)
            {
                JourneyMap.getLogger().info("Rei's Minimap Waypoints enabled.");
            }
        }
        return ReiReader.modLoaded;
    }

    /**
     * Whether Voxel Mod is loaded.
     *
     * @return
     */
    public static boolean isVoxelMapEnabled()
    {
        if (VoxelReader.modLoaded == null)
        {
            try
            {
                VoxelReader.modLoaded = waypointClassesFound(VoxelReader.classNames);
            }
            catch (Throwable t)
            {
                VoxelReader.modLoaded = false;
                JourneyMap.getLogger().warn("VoxelMap Waypoints can't be used: " + t.getMessage());
            }

            if (VoxelReader.modLoaded)
            {
                JourneyMap.getLogger().info("VoxelMap Waypoints enabled.");
            }
        }
        return VoxelReader.modLoaded;
    }

    /**
     * Get waypoints from whatever sources are supported.
     *
     * @return
     */
    protected static List<net.techbrew.journeymap.model.Waypoint> getWaypoints()
    {
        ArrayList<Waypoint> list = new ArrayList<net.techbrew.journeymap.model.Waypoint>(0);

        if (isReiMinimapEnabled())
        {
            list.addAll(ReiReader.loadWaypoints());
        }

        if (isVoxelMapEnabled())
        {
            list.addAll(VoxelReader.loadWaypoints());
        }

        if (isManagerEnabled())
        {
            list.addAll(WaypointStore.instance().getAll());
        }

        return list;
    }

    /**
     * Check to see whether one or more class names have been classloaded.
     *
     * @param names
     * @return
     */
    private static boolean waypointClassesFound(String... names) throws Exception
    {
        boolean loaded = true;

        for (String name : names)
        {
            if (!loaded)
            {
                break;
            }
            try
            {
                loaded = false;
                Class.forName(name);
                loaded = true;
                JourneyMap.getLogger().debug("Class found: " + name);
            }
            catch (NoClassDefFoundError e)
            {
                throw new Exception("Class detected, but is obsolete: " + e.getMessage());
            }
            catch (ClassNotFoundException e)
            {
                JourneyMap.getLogger().debug("Class not found: " + name);
            }
            catch (VerifyError v)
            {
                throw new Exception("Class detected, but is obsolete: " + v.getMessage());
            }
            catch (Throwable t)
            {
                throw new Exception("Class detected, but produced errors.", t);
            }
        }

        return loaded;
    }

    @Override
    public Collection<Waypoint> load(Class aClass) throws Exception
    {
        return getWaypoints();
    }

    public long getTTL()
    {
        return 5000;
    }
}
