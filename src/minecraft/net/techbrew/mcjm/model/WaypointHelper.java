package net.techbrew.mcjm.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.src.Minecraft;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.io.PropertyManager;
import net.techbrew.mcjm.log.LogFormatter;

/**
 * Get waypoints
 * 
 * @author mwoodman
 *
 */
public class WaypointHelper {
	
	private static Boolean reiLoaded;
	private static Boolean voxelMapLoaded;
	private static Boolean nativeLoaded;
	
	/**
	 * Is any waypoint system enabled.
	 * @return
	 */
	public static boolean waypointsEnabled() {
		return PropertyManager.getBooleanProp(PropertyManager.Key.PREF_SHOW_WAYPOINTS) && (isReiLoaded() || isVoxelMapLoaded() || isNativeLoaded());
	}
	
	/**
	 * Check for Rei Minimap
	 * @return
	 */
	private static boolean isReiLoaded() {
		if(reiLoaded==null) {
			try {
				Class.forName("reifnsk.minimap.ReiMinimap");
				Class.forName("reifnsk.minimap.Waypoint");
				reiLoaded = Class.forName("reifnsk.minimap.ReiMinimap").getDeclaredField("instance")!=null;
			} catch(Throwable e) {
				reiLoaded = false;
			}
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
	private static boolean isVoxelMapLoaded() {
		if(voxelMapLoaded==null) {
			try {
				Class.forName("com.thevoxelbox.voxelmap.VoxelMap");
				Class.forName("com.thevoxelbox.voxelmap.util.Waypoint");
				voxelMapLoaded = com.thevoxelbox.voxelmap.VoxelMap.getInstance() != null;
			} catch(Throwable e) {
				voxelMapLoaded = false;
			}
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
	private static boolean isNativeLoaded() {
		return false;
//		if(nativeLoaded==null) {
//			nativeLoaded = !isReiLoaded() && !isVoxelMapLoaded();
//		}
//		return nativeLoaded;
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
	
	/**
	 * Get native waypoints from JourneyMap
	 * @return
	 */
	static List<Waypoint> getNativeWaypoints() {
		
		if(!isNativeLoaded()) {
			return Collections.EMPTY_LIST;
		}
		
		ArrayList<Waypoint> list = new ArrayList<Waypoint>();
		
//		Minecraft mc = Minecraft.getMinecraft();
//		
//		ChunkCoordinates spawn = mc.theWorld.getSpawnPoint();
//		EntityClientPlayerMP player = mc.thePlayer;
//
//		Waypoint wpSpawn = new Waypoint("Spawn", spawn.posX, spawn.posY, spawn.posZ, true, 0, 255, 0, Waypoint.TYPE_NORMAL, "journeymap", "Spawn");
//		list.add(wpSpawn);
//			
//		ChunkCoordinates bed = player.getBedLocation();
//		if(bed!=null && !bed.equals(spawn)) {
//			Waypoint wpBed = new Waypoint("Bed", new Double(Math.floor(bed.posX)).intValue(), new Double(Math.floor(bed.posY)).intValue(), new Double(Math.floor(bed.posZ)).intValue(), true, 0, 0, 255, Waypoint.TYPE_NORMAL, "journeymap", "Bed");
//			list.add(wpBed);
//		}
		
		return list;
	}

}
