package net.techbrew.mcjm.data;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.minecraft.client.Minecraft;
import net.minecraft.src.WorldInfo;
import net.techbrew.mcjm.PropertyManager;
import net.techbrew.mcjm.io.FileHandler;

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
		TTL = PropertyManager.getInstance().getInteger(PropertyManager.BROWSER_POLL_PROP);
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
		props.put(Key.animals, cache.get(AnimalsData.class).get(AnimalsData.Key.root));
		props.put(Key.mobs, cache.get(MobsData.class).get(MobsData.Key.root));
		props.put(Key.player, cache.get(PlayerData.class));
		props.put(Key.players, cache.get(PlayersData.class).get(PlayersData.Key.root));
		props.put(Key.villagers, cache.get(VillagersData.class).get(VillagersData.Key.root));
		props.put(Key.world, cache.get(WorldData.class));
		
		return props;		
	}
	

	
	/**
	 * Return length of time in millis data should be kept.
	 */
	public long getTTL() {
		return TTL;
	}

}
