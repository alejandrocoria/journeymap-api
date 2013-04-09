package net.techbrew.mcjm.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.techbrew.mcjm.JourneyMap;
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
	
	/**
	 * Is any waypoint system enabled.
	 * @return
	 */
	public static boolean waypointsEnabled() {
		return isReiLoaded() || isVoxelMapLoaded();
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
				reiLoaded = reifnsk.minimap.ReiMinimap.instance != null;
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
	 * Get waypoints from whatever sources are supported.
	 * @return
	 */
	public static List<Waypoint> getWaypoints() {
		
		ArrayList<Waypoint> list = new ArrayList<Waypoint>(0);
		
		if(isReiLoaded()) list.addAll(getReiWaypoints());
		if(isVoxelMapLoaded()) list.addAll(getVoxelMapWaypoints());
		
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
			reifnsk.minimap.ReiMinimap reiMinimap = reifnsk.minimap.ReiMinimap.instance;
			List<reifnsk.minimap.Waypoint> wayPts = (List<reifnsk.minimap.Waypoint>) reiMinimap.getWaypoints();
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
			
			ArrayList<Waypoint> converted = new ArrayList<Waypoint>(wayPts.size());
			for(Object wpObj : wayPts) {
				com.thevoxelbox.voxelmap.util.Waypoint wp = (com.thevoxelbox.voxelmap.util.Waypoint) wpObj;
				converted.add(new Waypoint(wp.name, wp.x, wp.y, wp.z, wp.enabled,						
						(int)(wp.red * 255.0F) & 255,
						(int)(wp.green * 255.0F) & 255,
		        		(int)(wp.blue * 255.0F) & 255,
		        		null,
		        		"voxelmap",
		        		wp.name));
			}
			return converted;
			
		} catch(Throwable e) {
			JourneyMap.getLogger().severe("Exception getting VoxelMap waypoints: " + LogFormatter.toString(e));		
			voxelMapLoaded = false;
			return Collections.EMPTY_LIST;
		}
	}

}
