package net.techbrew.journeymap.model;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChunkCoordinates;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.LogFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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
    public static boolean isNativeLoaded() {
        return !isReiLoaded() && !isVoxelMapLoaded();
    }

    /**
     * Get waypoints from whatever sources are supported.
     * @return
     */
    public static List<Waypoint> getWaypoints() {

        ArrayList<Waypoint> list = new ArrayList<Waypoint>(0);

        if(isReiLoaded()) list.addAll(getReiWaypoints());
        if(isVoxelMapLoaded()) list.addAll(getVoxelMapWaypoints());
        if(isNativeLoaded()) list.addAll(getNativeWaypoints());

        return list;
    }

    /**
     * Get waypoints from Rei's Minimap
     * @return
     */
    static List<Waypoint> getReiWaypoints() {

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

            ArrayList<Waypoint> converted = new ArrayList<Waypoint>(wayPts.size());
            for(reifnsk.minimap.Waypoint wp : wayPts) {
                converted.add(new Waypoint(wp.name, wp.x, wp.y, wp.z, wp.enable,
                        (int)(wp.red * 255.0F) & 255,
                        (int)(wp.green * 255.0F) & 255,
                        (int)(wp.blue * 255.0F) & 255,
                        wp.type,
                        "rei",
                        wp.toString()));
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
    static List<Waypoint> getVoxelMapWaypoints() {

        if(!isVoxelMapLoaded()) {
            return Collections.EMPTY_LIST;
        }

        try {
            com.thevoxelbox.voxelmap.VoxelMap voxelMap = com.thevoxelbox.voxelmap.VoxelMap.getInstance();
            List wayPts = new ArrayList(0);
            wayPts.addAll(voxelMap.waypointManager.wayPts);
            if(wayPts.isEmpty()) {
                return wayPts;
            }
            final int dimension = Minecraft.getMinecraft().thePlayer.dimension;
            final int netherOffset = (dimension==-1) ? 8 : 1;
            ArrayList<Waypoint> converted = new ArrayList<Waypoint>(wayPts.size());
            for(Object wpObj : wayPts) {
                com.thevoxelbox.voxelmap.util.Waypoint wp = (com.thevoxelbox.voxelmap.util.Waypoint) wpObj;
                if(!wp.isActive()) continue;
                Waypoint convertWp = new Waypoint(wp.name,
                        wp.x / netherOffset,
                        wp.y,
                        wp.z / netherOffset,
                        wp.enabled,
                        (int)(wp.red * 255.0F) & 255,
                        (int)(wp.green * 255.0F) & 255,
                        (int)(wp.blue * 255.0F) & 255,
                        "skull".equals(wp.imageSuffix) ? 1 : 0,
                        "voxelmap",
                        wp.name);

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

    /**
     * Get native waypoints from JourneyMap
     * @return
     */
    static List<Waypoint> getNativeWaypoints() {

        List<Waypoint> list = new ArrayList<Waypoint>();

		Minecraft mc = Minecraft.getMinecraft();

		ChunkCoordinates spawn = mc.theWorld.getSpawnPoint();

		Waypoint wpSpawn = new Waypoint("Spawn", spawn.posX, spawn.posY, spawn.posZ, true, 0, 255, 0, Waypoint.TYPE_NORMAL, "journeymap", "Spawn");
		list.add(wpSpawn);

        EntityPlayer player = mc.thePlayer;
        Random random = new Random();
        Waypoint wpPlayer = new Waypoint("Footprints", (int) player.posX, (int)player.posY, (int)player.posZ, true, random.nextInt(255), random.nextInt(255), random.nextInt(255), Waypoint.TYPE_NORMAL, "journeymap", "Spawn");
        list.add(wpPlayer);

        list.add(new Waypoint("0,0", 0,0,0, true, random.nextInt(255), random.nextInt(255), random.nextInt(255), Waypoint.TYPE_NORMAL, "journeymap", "0,0"));
        list.add(new Waypoint("-64,-64", -64,0,-64, true, random.nextInt(255), random.nextInt(255), random.nextInt(255), Waypoint.TYPE_NORMAL, "journeymap", "-64,-64"));
        list.add(new Waypoint("64,64", 64,0,64, true, random.nextInt(255), random.nextInt(255), random.nextInt(255), Waypoint.TYPE_NORMAL, "journeymap", "64,64"));
        list.add(new Waypoint("-64,64", -64,0,64, true, random.nextInt(255), random.nextInt(255), random.nextInt(255), Waypoint.TYPE_NORMAL, "journeymap", "-64,64"));
        list.add(new Waypoint("64,-64", 64,0,-64, true, random.nextInt(255), random.nextInt(255), random.nextInt(255), Waypoint.TYPE_NORMAL, "journeymap", "64,-64"));

        list.add(new Waypoint("-512,-512", -512,0,-512, true, random.nextInt(255), random.nextInt(255), random.nextInt(255), Waypoint.TYPE_NORMAL, "journeymap", "-512,-512"));
        list.add(new Waypoint("512,512", 512,0,512, true, random.nextInt(255), random.nextInt(255), random.nextInt(255), Waypoint.TYPE_NORMAL, "journeymap", "512,512"));
        list.add(new Waypoint("-512,512", -512,0,512, true, random.nextInt(255), random.nextInt(255), random.nextInt(255), Waypoint.TYPE_NORMAL, "journeymap", "-512,512"));
        list.add(new Waypoint("512,-512", 512,0,-512, true, random.nextInt(255), random.nextInt(255), random.nextInt(255), Waypoint.TYPE_NORMAL, "journeymap", "512,-512"));

        return list;
    }

}
