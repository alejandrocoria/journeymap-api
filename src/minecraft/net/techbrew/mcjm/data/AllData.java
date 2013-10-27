package net.techbrew.mcjm.data;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import net.techbrew.mcjm.io.PropertyManager;

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
		if(PropertyManager.getBooleanProp(PropertyManager.Key.PREF_SHOW_ANIMALS)) {
			props.put(Key.animals, cache.get(AnimalsData.class).get(EntityKey.root));
		} else {
			props.put(Key.animals, Collections.emptyMap());
		}
		props.put(Key.images, cache.get(ImagesData.class, optionalParams));
		if(PropertyManager.getBooleanProp(PropertyManager.Key.PREF_SHOW_MOBS)) {
			props.put(Key.mobs, cache.get(MobsData.class).get(EntityKey.root));
		} else {
			props.put(Key.mobs, Collections.emptyMap());
		}
		props.put(Key.player, cache.get(PlayerData.class));
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
		props.put(Key.world, cache.get(WorldData.class));
		if(PropertyManager.getBooleanProp(PropertyManager.Key.PREF_SHOW_WAYPOINTS)) {
			props.put(Key.waypoints, cache.get(WaypointsData.class).get(EntityKey.root));
		} else {
			props.put(Key.waypoints, Collections.emptyMap());
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
