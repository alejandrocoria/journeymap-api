/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.data;

import com.google.common.cache.CacheLoader;
import journeymap.client.JourneymapClient;
import journeymap.client.log.LogFormatter;
import journeymap.client.model.Waypoint;
import journeymap.client.waypoint.ReiReader;
import journeymap.client.waypoint.VoxelReader;
import journeymap.client.waypoint.WaypointStore;
import journeymap.common.Journeymap;

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
    public static void enableRecheck()
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
        return JourneymapClient.getWaypointProperties().managerEnabled.get();
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
                Journeymap.getLogger().warn("Rei's Minimap Waypoints can't be used: " + LogFormatter.toString(t));
            }
            if (ReiReader.modLoaded)
            {
                Journeymap.getLogger().info("Rei's Minimap Waypoints enabled.");
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
                Journeymap.getLogger().warn("VoxelMap Waypoints can't be used: " + t.getMessage());
            }

            if (VoxelReader.modLoaded)
            {
                Journeymap.getLogger().info("VoxelMap Waypoints enabled.");
            }
        }
        return VoxelReader.modLoaded;
    }

    /**
     * Get waypoints from whatever sources are supported.
     *
     * @return
     */
    protected static List<journeymap.client.model.Waypoint> getWaypoints()
    {
        ArrayList<Waypoint> list = new ArrayList<journeymap.client.model.Waypoint>(0);

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
                Journeymap.getLogger().debug("Class found: " + name);
            }
            catch (NoClassDefFoundError e)
            {
                throw new Exception("Class detected, but is obsolete: " + e.getMessage());
            }
            catch (ClassNotFoundException e)
            {
                Journeymap.getLogger().debug("Class not found: " + name);
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
