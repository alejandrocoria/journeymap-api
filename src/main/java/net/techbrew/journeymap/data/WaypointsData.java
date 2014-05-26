package net.techbrew.journeymap.data;

import net.minecraft.client.Minecraft;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.model.Waypoint;
import net.techbrew.journeymap.properties.WaypointProperties;
import net.techbrew.journeymap.waypoint.ReiReader;
import net.techbrew.journeymap.waypoint.VoxelReader;
import net.techbrew.journeymap.waypoint.WaypointStore;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Provides waypoint data
 *
 * @author mwoodman
 */
public class WaypointsData implements IDataProvider
{
    private static long TTL = TimeUnit.SECONDS.toMillis(5);

    private static WaypointProperties waypointProperties = JourneyMap.getInstance().waypointProperties;

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
     * Whether any waypoint management is enabled.
     *
     * @return
     */
    public static boolean isAnyEnabled()
    {
        return WaypointsData.isNativeEnabled() || WaypointsData.isReiMinimapEnabled() || WaypointsData.isVoxelMapEnabled();
    }

    /**
     * Whether any other mod's waypoint management is enabled.
     *
     * @return
     */
    public static boolean isOtherModEnabled()
    {
        return WaypointsData.isReiMinimapEnabled() || WaypointsData.isVoxelMapEnabled();
    }


    /**
     * Whether native waypoint management is enabled.
     *
     * @return
     */
    public static boolean isNativeEnabled()
    {
        return waypointProperties.enabled.get();
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
            ReiReader.modLoaded = waypointClassesFound(ReiReader.classNames);
            if (ReiReader.modLoaded)
            {
                JourneyMap.getLogger().info("Rei's Minimap detected.  Waypoints enabled.");
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
            VoxelReader.modLoaded = waypointClassesFound(VoxelReader.classNames);
            if (VoxelReader.modLoaded)
            {
                JourneyMap.getLogger().info("VoxelMap detected.  Waypoints enabled.");
            }
        }
        return VoxelReader.modLoaded;
    }

    /**
     * Get waypoints from whatever sources are supported.
     *
     * @return
     */
    public static List<net.techbrew.journeymap.model.Waypoint> getWaypoints()
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

        if (isNativeEnabled())
        {
            list.addAll(WaypointStore.instance().getAll());
        }

        return list;
    }

    /**
     * Get cached waypoints to avoid load overhead.
     *
     * @return
     */
    public static Collection<Waypoint> getCachedWaypoints()
    {
        if (isReiMinimapEnabled() || isVoxelMapEnabled())
        {
            return ((HashMap<String, Waypoint>) DataCache.instance().get(WaypointsData.class).get(EntityKey.root)).values();
        }
        else
        {
            if (isNativeEnabled()) // No caching needed
            {
                return WaypointStore.instance().getAll();
            }
            else
            {
                return Collections.EMPTY_LIST;
            }
        }
    }


    /**
     * Check to see whether one or more class names have been classloaded.
     *
     * @param names
     * @return
     */
    private static boolean waypointClassesFound(String... names)
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
                JourneyMap.getLogger().fine("Class found: " + name);
            }
            catch (NoClassDefFoundError e)
            {
                JourneyMap.getLogger().warning("Class detected, but is obsolete. Can't use waypoints: " + e.getMessage());
            }
            catch (ClassNotFoundException e)
            {
                JourneyMap.getLogger().fine("Class not found: " + name);
            }
            catch (VerifyError v)
            {
                JourneyMap.getLogger().warning("Class detected, but is obsolete. Can't use waypoints: " + v.getMessage());
            }
            catch (Throwable t)
            {
                JourneyMap.getLogger().warning("Class detected, but with errors. Can't use waypoints: " + LogFormatter.toString(t));
            }
        }

        return loaded;
    }

    /**
     * Return map of waypoints data.
     */
    @Override
    public Map getMap(Map optionalParams)
    {

        List<Waypoint> waypoints = getWaypoints();
        Map<String, Waypoint> map = new HashMap<String, Waypoint>(waypoints.size());
        final int dimension = Minecraft.getMinecraft().thePlayer.dimension;
        for (Waypoint waypoint : waypoints)
        {
            if (waypoint.isEnable() && waypoint.getDimensions().contains(dimension))
            {
                map.put(waypoint.getId(), waypoint);
            }
        }

        LinkedHashMap props = new LinkedHashMap();
        props.put(EntityKey.root, map);

        return props;
    }

    @Override
    public Enum[] getKeys()
    {
        return new Enum[0];
    }

    /**
     * Return length of time in millis data should be kept.
     */
    @Override
    public long getTTL()
    {
        return TTL;
    }

    /**
     * Return false by default. Let cache expired based on TTL.
     */
    @Override
    public boolean dataExpired()
    {
        return false;
    }
}
