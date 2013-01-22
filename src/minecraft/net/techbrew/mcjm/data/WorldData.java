package net.techbrew.mcjm.data;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.minecraft.client.Minecraft;
import net.minecraft.src.WorldInfo;
import net.techbrew.mcjm.io.FileHandler;

/**
 * Provides game-related properties in a Map.
 * 
 * @author mwoodman
 *
 */
public class WorldData implements IDataProvider {
	
	private static long TTL = TimeUnit.SECONDS.toMillis(10);
	
	public static enum Key {
		dirName,
		name,
		dimension,
		time,
		hardcore,
		worldType,
		gameType
	}

	/**
	 * Constructor.
	 */
	public WorldData() {
	}
	
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
		props.put(Key.dirName, getWorldDirName(mc));
		props.put(Key.name, getWorldName(mc)); 
		props.put(Key.dimension, worldInfo.getDimension()); 
		props.put(Key.hardcore,  worldInfo.isHardcoreModeEnabled());
		props.put(Key.gameType, worldInfo.getGameType().toString());
		props.put(Key.worldType, worldInfo.getTerrainType().getWorldTypeName());

		return Collections.unmodifiableMap(props);		
	}
	
	/**
	 * Get the current world data directory name.
	 * @param mc
	 * @return
	 */
	private String getWorldDirName(Minecraft mc) {
		String worldDirName = null;
		try {
			worldDirName = FileHandler.getSafeName(mc);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		return worldDirName;
	}
	
	/**
	 * Get the current world name.
	 * @param mc
	 * @return
	 */
	private String getWorldName(Minecraft mc) {
		
		// Get the name
		String worldName = null;
		if(mc.isSingleplayer()) {
			worldName = mc.getIntegratedServer().getWorldName();
		} else if(mc.getServerData()!=null) {
			worldName = mc.getServerData().serverName; 
		} 
		
		// Clean it up for display
		try {
			worldName = URLEncoder.encode(worldName, "UTF-8").replaceAll("\\+", " "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} catch (UnsupportedEncodingException e) {
			// Shouldn't happen
			worldName = "Minecraft";	//$NON-NLS-1$
		}
		return worldName;
	}
	
	/**
	 * Return length of time in millis data should be kept.
	 */
	public long getTTL() {
		return TTL;
	}

}
