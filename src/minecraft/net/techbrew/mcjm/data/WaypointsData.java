package net.techbrew.mcjm.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.minecraft.client.Minecraft;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayerSP;
import net.minecraft.src.EntityTameable;
import net.minecraft.src.IAnimals;
import net.techbrew.mcjm.model.EntityHelper;
import net.techbrew.mcjm.model.Waypoint;
import net.techbrew.mcjm.reifnsk.WaypointHelper;

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
	public Enum[] getKeys() {
		return Waypoint.Key.values();
	}
	
	/**
	 * Return map of waypoints data.
	 */
	public Map getMap() {		
				
		List<Waypoint> waypoints = WaypointHelper.getWaypoints();
		ArrayList<Map> list = new ArrayList<Map>(waypoints.size());
		for(Waypoint waypoint : waypoints) {
			if(waypoint.getBoolean(Waypoint.Key.enable)) {
				list.add(waypoint.getProperties());
			}
		}		
		
		LinkedHashMap props = new LinkedHashMap();
		props.put(EntityKey.root, list);
		
		return props;		
	}	
		
	
	/**
	 * Return length of time in millis data should be kept.
	 */
	public long getTTL() {
		return TTL;
	}
	
	/**
	 * Return false by default. Let cache expired based on TTL.
	 */
	public boolean dataExpired() {
		return false;
	}
}
