package net.techbrew.journeymap.data;

import net.minecraft.client.Minecraft;
import net.techbrew.journeymap.model.Waypoint;
import net.techbrew.journeymap.waypoint.WaypointHelper;

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
	 * Return map of waypoints data.
	 */
	@Override
	public Map getMap(Map optionalParams) {		
				
		List<Waypoint> waypoints = WaypointHelper.getWaypoints();
		Map<String, Waypoint> map = new HashMap<String, Waypoint>(waypoints.size());
        final int dimension = Minecraft.getMinecraft().thePlayer.dimension;
		for(Waypoint waypoint : waypoints) {
			if(waypoint.isEnable() && waypoint.getDimensions().contains(dimension)) {
				map.put(waypoint.getId(),waypoint);
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
