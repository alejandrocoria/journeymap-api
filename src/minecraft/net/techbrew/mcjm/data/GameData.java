package net.techbrew.mcjm.data;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.VersionCheck;
import net.techbrew.mcjm.io.PropertyManager;
import net.techbrew.mcjm.model.WaypointHelper;

import org.lwjgl.opengl.Display;

/**
 * Provides game-related properties in a Map.
 * 
 * @author mwoodman
 *
 */
public class GameData implements IDataProvider {
	
	private static long TTL = TimeUnit.HOURS.toMillis(1);
	
	public static enum Key {
		jm_version,
		latest_journeymap_version,
		mc_version,
		mod_name,
		browser_poll,
		waypoints_enabled
	}

	/**
	 * Constructor.
	 */
	public GameData() {
	}
	
	@Override
	public Enum[] getKeys() {
		return Key.values();
	}
	
	/**
	 * Return a map of game data.
	 */
	@Override
	public Map getMap(Map optionalParams) {	
		
		PropertyManager pm = PropertyManager.getInstance();		
		LinkedHashMap props = new LinkedHashMap();		
		
		props.put(Key.mod_name, JourneyMap.MOD_NAME);
		props.put(Key.jm_version,JourneyMap.JM_VERSION);
		props.put(Key.latest_journeymap_version, VersionCheck.getVersionAvailable()); 
		props.put(Key.mc_version, Display.getTitle().split("\\s(?=\\d)")[1]); //$NON-NLS-1$ 		
		props.put(Key.browser_poll, pm.getInteger(PropertyManager.Key.BROWSER_POLL));
		props.put(Key.waypoints_enabled, WaypointHelper.waypointsEnabled());

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
