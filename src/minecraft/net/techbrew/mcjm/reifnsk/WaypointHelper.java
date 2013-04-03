package net.techbrew.mcjm.reifnsk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.model.Waypoint;

/**
 * Get waypoints
 * 
 * @author mwoodman
 *
 */
public class WaypointHelper {
	
	private static Boolean reiLoaded;
	
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
		}
		return reiLoaded;
	}
	
	/**
	 * Get waypoints from whatever sources are supported.
	 * @return
	 */
	public static List<Waypoint> getWaypoints() {
		
		ArrayList<Waypoint> list = new ArrayList<Waypoint>(0);
		list.addAll(getReiWaypoints());
		
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
						wp.type, wp.toString()));
			}
			return converted;
			
		} catch(Throwable e) {
			JourneyMap.getLogger().severe("Exception getting Rei waypoints: " + LogFormatter.toString(e));		
			reiLoaded = false;
			return Collections.EMPTY_LIST;
		}
	}

}
