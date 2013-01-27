package net.techbrew.mcjm.data;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.minecraft.client.Minecraft;
import net.minecraft.src.WorldInfo;

/**
 * Provides game-related properties in a Map.
 * 
 * @author mwoodman
 *
 */
public class TimeData implements IDataProvider {
	
	private static long TTL = TimeUnit.SECONDS.toMillis(1);
	
	public static enum Key {
		worldCurrentTime,
		worldTotalTime
	}

	/**
	 * Constructor.
	 */
	public TimeData() {
	}
	
	/**
	 * Provides all possible keys.
	 */
	public Enum[] getKeys() {
		return Key.values();
	}
	
	/**
	 * Return map of world-related properties.
	 */
	public Map getMap() {		
		
		Minecraft mc = Minecraft.getMinecraft();
		WorldInfo worldInfo = mc.theWorld.getWorldInfo();

		LinkedHashMap props = new LinkedHashMap();
		props.put(Key.worldCurrentTime, mc.theWorld.getWorldTime() % 24000L);
		props.put(Key.worldTotalTime, mc.theWorld.getTotalWorldTime());

		return props;
	}
	
	/**
	 * Return length of time in millis data should be kept.
	 */
	public long getTTL() {
		return TTL;
	}
	
}
