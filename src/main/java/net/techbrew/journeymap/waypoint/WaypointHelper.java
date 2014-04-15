package net.techbrew.journeymap.waypoint;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.data.DataCache;
import net.techbrew.journeymap.data.EntityKey;
import net.techbrew.journeymap.data.WaypointsData;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.model.Waypoint;

import java.util.*;

/**
 * Pull waypoints from one of several waypoint managers.
 *
 * @author mwoodman
 *
 */
public class WaypointHelper {

    private static Boolean reiLoaded;
    private static Boolean voxelMapLoaded;
    private static Boolean renderWaypoints;

    /**
     * Reset state so classes can be checked again. Useful
     * after post-init of all mods.
     */
    public static void reset() {
        reiLoaded = null;
        voxelMapLoaded = null;
        renderWaypoints = null;
    }

    /**
     * Is any waypoint system enabled.
     * @return
     */
    public static boolean waypointsEnabled() {
        return (isReiLoaded() || isVoxelMapLoaded() || isNativeLoaded());
    }

    /**
     * Check for Rei Minimap
     * @return
     */
    public static boolean isReiLoaded() {
        if(reiLoaded==null) {
            reiLoaded = waypointClassesFound("reifnsk.minimap.ReiMinimap", "reifnsk.minimap.Waypoint");
            if(reiLoaded) {
                JourneyMap.getLogger().info("Rei's Minimap detected.  Waypoints enabled.");
            }
        }
        return reiLoaded;
    }

    /**
     * Check for VoxelMap (was Zans) Minimap
     * @return
     */
    public static boolean isVoxelMapLoaded() {
        if(voxelMapLoaded==null) {
            voxelMapLoaded = waypointClassesFound("com.thevoxelbox.voxelmap.VoxelMap", "com.thevoxelbox.voxelmap.util.Waypoint");
            if(voxelMapLoaded) {
                JourneyMap.getLogger().info("VoxelMap detected.  Waypoints enabled.");
            }
        }
        return voxelMapLoaded;
    }

    /**
     * Check for native waypoint support
     * @return
     */
    public static boolean isNativeLoaded()
    {
        Minecraft mc = Minecraft.getMinecraft();
        if(mc.thePlayer != null && mc.theWorld !=null)
        {
            return WaypointStore.instance().hasLoaded();
        }
        return !isReiLoaded() && !isVoxelMapLoaded();
    }

    /**
     * Get waypoints from whatever sources are supported.
     * @return
     */
    public static List<net.techbrew.journeymap.model.Waypoint> getWaypoints() {

        ArrayList<net.techbrew.journeymap.model.Waypoint> list = new ArrayList<net.techbrew.journeymap.model.Waypoint>(0);

        if(isReiLoaded())
        {
            list.addAll(getReiWaypoints());
        }

        if(isVoxelMapLoaded())
        {
            list.addAll(getVoxelMapWaypoints());
        }

        if(isNativeLoaded())
        {
            list.addAll(WaypointStore.instance().getAll());
        }

        return list;
    }

    /**
     * Get cached waypoints to avoid load overhead.
     * @return
     */
    public static Collection<Waypoint> getCachedWaypoints()
    {
        if(isReiLoaded() || isVoxelMapLoaded())
        {
            // TODO: Circular dependency makes me sad.
            return ((HashMap<String, Waypoint>) DataCache.instance().get(WaypointsData.class).get(EntityKey.root)).values();
        }
        else
        {
            return WaypointStore.instance().getAll();
        }
    }

    /**
     * Get waypoints from Rei's Minimap
     * @return
     */
    static List<net.techbrew.journeymap.model.Waypoint> getReiWaypoints() {

        if(!isReiLoaded()) {
            return Collections.EMPTY_LIST;
        }

        try {
            Class.forName("reifnsk.minimap.ReiMinimap").getDeclaredField("instance");
            reifnsk.minimap.ReiMinimap reiMinimap = reifnsk.minimap.ReiMinimap.instance;
            List<reifnsk.minimap.Waypoint> wayPts = reiMinimap.getWaypoints();
            if(wayPts==null || wayPts.isEmpty()) {
                return Collections.EMPTY_LIST;
            }

            EntityPlayer player = Minecraft.getMinecraft().thePlayer;
            int dimension = player != null ? player.dimension : 1;
            ArrayList<net.techbrew.journeymap.model.Waypoint> converted = new ArrayList<net.techbrew.journeymap.model.Waypoint>(wayPts.size());
            for(reifnsk.minimap.Waypoint wp : wayPts) {
                converted.add(new net.techbrew.journeymap.model.Waypoint(wp.name, wp.x, wp.y, wp.z, wp.enable,
                        (int)(wp.red * 255.0F) & 255,
                        (int)(wp.green * 255.0F) & 255,
                        (int)(wp.blue * 255.0F) & 255,
                        wp.type == 1 ? Waypoint.Type.Death : Waypoint.Type.Normal,
                        "rei",
                        dimension));
            }
            return converted;

        } catch(Throwable e) {
            JourneyMap.getLogger().severe("Exception getting Rei waypoints: " + LogFormatter.toString(e));
            reiLoaded = false;
            return Collections.EMPTY_LIST;
        }
    }

    /**
     * Get waypoints from VoxelMap
     * @return
     */
    static List<net.techbrew.journeymap.model.Waypoint> getVoxelMapWaypoints() {

        if(!isVoxelMapLoaded()) {
            return Collections.EMPTY_LIST;
        }

        try {
            com.thevoxelbox.voxelmap.VoxelMap voxelMap = com.thevoxelbox.voxelmap.VoxelMap.instance;
            List wayPts = new ArrayList(0);
            wayPts.addAll(voxelMap.waypointManager.wayPts);
            if(wayPts.isEmpty()) {
                return wayPts;
            }

            ArrayList<net.techbrew.journeymap.model.Waypoint> converted = new ArrayList<net.techbrew.journeymap.model.Waypoint>(wayPts.size());
            for(Object wpObj : wayPts) {
                com.thevoxelbox.voxelmap.util.Waypoint wp = (com.thevoxelbox.voxelmap.util.Waypoint) wpObj;
                if(!wp.isActive()) continue;
                net.techbrew.journeymap.model.Waypoint convertWp = new net.techbrew.journeymap.model.Waypoint(
                        wp.name,
                        wp.x,
                        wp.y,
                        wp.z,
                        wp.enabled,
                        (int)(wp.red * 255.0F) & 255,
                        (int)(wp.green * 255.0F) & 255,
                        (int)(wp.blue * 255.0F) & 255,
                        "skull".equals(wp.imageSuffix) ? Waypoint.Type.Death : Waypoint.Type.Normal,
                        "voxelmap",
                        (Integer[]) wp.dimensions.toArray());

                converted.add(convertWp);
            }
            return converted;

        } catch(Throwable e) {
            JourneyMap.getLogger().severe("Exception getting VoxelMap waypoints: " + LogFormatter.toString(e));
            voxelMapLoaded = false;
            return Collections.EMPTY_LIST;
        }
    }

    private static boolean waypointClassesFound(String... names) {
        boolean loaded = true;

        for(String name : names) {
            if(!loaded) break;
            try {
                loaded = false;
                Class.forName(name);
                loaded = true;
                JourneyMap.getLogger().fine("Class found: " + name);
            } catch(NoClassDefFoundError e) {
                JourneyMap.getLogger().warning("Class detected, but is obsolete. Can't use waypoints: " + e.getMessage());
            } catch(ClassNotFoundException e) {
                JourneyMap.getLogger().fine("Class not found: " + name);
            } catch(VerifyError v) {
                JourneyMap.getLogger().warning("Class detected, but is obsolete. Can't use waypoints: " + v.getMessage());
            } catch(Throwable t) {
                JourneyMap.getLogger().warning("Class detected, but with errors. Can't use waypoints: " + LogFormatter.toString(t));
            }
        }

        return loaded;
    }

}
