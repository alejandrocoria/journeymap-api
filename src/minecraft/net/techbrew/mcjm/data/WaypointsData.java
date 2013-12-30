package net.techbrew.mcjm.data;

import net.techbrew.mcjm.model.Waypoint;
import net.techbrew.mcjm.model.WaypointHelper;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Provides waypoint data
 * 
 * @author mwoodman
 *
 */
public class WaypointsData implements IDataProvider {
	
	private static long TTL = TimeUnit.SECONDS.toMillis(5);


	/**
	 * Provides all possible keys.
	 */
	@Override
	public Enum[] getKeys() {
		return Waypoint.Key.values();
	}
	
	/**
	 * Return map of waypoints data.
	 */
	@Override
	public Map getMap(Map optionalParams) {		
				
		List<Waypoint> waypoints = WaypointHelper.getWaypoints();
		Map<String, Map> map = new HashMap<String, Map>(waypoints.size());
		for(Waypoint waypoint : waypoints) {
			if(waypoint.getEnable()) {
				map.put(waypoint.getId(),waypoint);
			}
		}		
		
		LinkedHashMap props = new LinkedHashMap();
		props.put(EntityKey.root, map);
		
		return props;		
	}	
		
	
	/**
	 * Return length of time in millis data should be kept.
	 */
	@Override
	public long getTTL() {
		return TTL;
	}
	
	/**
	 * Return false by default. Let cache expired based on TTL.
	 */
	@Override
	public boolean dataExpired() {
		return false;
	}
}
