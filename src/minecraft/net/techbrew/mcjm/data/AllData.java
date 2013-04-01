package net.techbrew.mcjm.data;

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
	
	private long TTL;
	
	public static enum Key {
		animals,
		mobs,
		player,
		players,
		villagers,
		world;
	}

	/**
	 * Constructor.
	 */
	public AllData() {
		TTL = PropertyManager.getInstance().getInteger(PropertyManager.Key.BROWSER_POLL);
	}
	
	public Enum[] getKeys() {
		return Key.values();
	}
	
	/**
	 * Return map of world-related properties.
	 */
	public Map getMap() {		
		
		DataCache cache = DataCache.instance();
		LinkedHashMap props = new LinkedHashMap();
		props.put(Key.animals, cache.get(AnimalsData.class).get(EntityKey.root));
		props.put(Key.mobs, cache.get(MobsData.class).get(EntityKey.root));
		props.put(Key.player, cache.get(PlayerData.class));
		props.put(Key.players, cache.get(PlayersData.class).get(EntityKey.root));
		props.put(Key.villagers, cache.get(VillagersData.class).get(EntityKey.root));
		props.put(Key.world, cache.get(WorldData.class));
		
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
