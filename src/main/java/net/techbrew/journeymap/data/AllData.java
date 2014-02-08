package net.techbrew.journeymap.data;

import net.techbrew.journeymap.io.PropertyManager;
import net.techbrew.journeymap.model.WaypointHelper;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Provides game-related properties in a Map.
 * 
 * @author mwoodman
 *
 */
public class AllData implements IDataProvider {
	
	private final long TTL;
	
	public static enum Key {
		animals,
		images,
		mobs,
		player,
		players,
		villagers,
		waypoints,
		world;
	}

	/**
	 * Constructor.
	 */
	public AllData() {
		TTL = PropertyManager.getInstance().getInteger(PropertyManager.Key.BROWSER_POLL);
	}
	
	@Override
	public Enum[] getKeys() {
		return Key.values();
	}
	
	/**
	 * Return map of world-related properties.
	 */
	@Override
	public Map getMap(Map optionalParams) {		
		
		DataCache cache = DataCache.instance();
		LinkedHashMap props = new LinkedHashMap();
		props.put(Key.world, cache.get(WorldData.class));
		props.put(Key.images, cache.get(ImagesData.class, optionalParams));
		props.put(Key.player, cache.get(PlayerData.class));
		
		if(WaypointHelper.waypointsEnabled() && PropertyManager.getBooleanProp(PropertyManager.Key.PREF_SHOW_WAYPOINTS)) {
			props.put(Key.waypoints, cache.get(WaypointsData.class).get(EntityKey.root));
		} else {
			props.put(Key.waypoints, Collections.emptyMap());
		}
		
		if(!WorldData.isHardcoreAndMultiplayer()) {
		
			if(PropertyManager.getBooleanProp(PropertyManager.Key.PREF_SHOW_ANIMALS)) {
				props.put(Key.animals, cache.get(AnimalsData.class).get(EntityKey.root));
			} else {
				props.put(Key.animals, Collections.emptyMap());
			}
		
			if(PropertyManager.getBooleanProp(PropertyManager.Key.PREF_SHOW_MOBS)) {
				props.put(Key.mobs, cache.get(MobsData.class).get(EntityKey.root));
			} else {
				props.put(Key.mobs, Collections.emptyMap());
			}
		
			if(PropertyManager.getBooleanProp(PropertyManager.Key.PREF_SHOW_PLAYERS)) {
				props.put(Key.players, cache.get(PlayersData.class).get(EntityKey.root));
			} else {
				props.put(Key.players, Collections.emptyMap());
			}
			
			if(PropertyManager.getBooleanProp(PropertyManager.Key.PREF_SHOW_VILLAGERS)) {
				props.put(Key.villagers, cache.get(VillagersData.class).get(EntityKey.root));
			} else {
				props.put(Key.villagers, Collections.emptyMap());
			}
		}
				
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