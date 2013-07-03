package net.techbrew.mcjm.data;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.minecraft.src.Minecraft;
import net.minecraft.src.ModLoader;
import net.minecraft.src.ServerData;
import net.minecraft.src.WorldInfo;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.log.LogFormatter;

/**
 * Provides game-related properties in a Map.
 * 
 * @author mwoodman
 *
 */
public class WorldData implements IDataProvider {
	
	private static long TTL = TimeUnit.SECONDS.toMillis(1);
	
	public static enum Key {
		dirName, // TODO: Remove?
		name,
		dimension,
		time,
//		totalTime,
		hardcore,
		singlePlayer,
//		worldType,
//		gameType
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
		// props.put(Key.dirName, getWorldDirName(mc));
		props.put(Key.name, getWorldName(mc)); 
		props.put(Key.dimension, mc.theWorld.provider.dimensionId); 
		props.put(Key.hardcore,  worldInfo.isHardcoreModeEnabled());
		props.put(Key.singlePlayer, mc.isSingleplayer()); 
		props.put(Key.time, mc.theWorld.getWorldTime() % 24000L);
//		props.put(Key.totalTime, mc.theWorld.getTotalWorldTime());
//		props.put(Key.gameType, worldInfo.getGameType().toString());
//		props.put(Key.worldType, worldInfo.getTerrainType().getWorldTypeName());

		return props;		
	}
	
//	/**
//	 * Get the current world data directory name.
//	 * @param mc
//	 * @return
//	 */
//	private String getWorldDirName(Minecraft mc) {
//		String worldDirName = null;
//		try {
//			worldDirName = FileHandler.getSafeName(mc);
//		} catch (UnsupportedEncodingException e) {
//			throw new RuntimeException(e);
//		}
//		return worldDirName;
//	}
	
	/**
	 * Get the current world name.
	 * @param mc
	 * @return
	 */
	public static String getWorldName(Minecraft mc) {
		
		// Get the name
		String worldName = null;
		if(mc.isSingleplayer()) {
			worldName = mc.getIntegratedServer().getWorldName();
		} else {
			Object serverData;
			try {
				serverData = ModLoader.getPrivateValue(Minecraft.class, mc, "serverData");
				worldName = ((ServerData) serverData).serverName;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				JourneyMap.getLogger().severe(LogFormatter.toString(e));
				e.printStackTrace();
			} 
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
	
	/**
	 * Return false by default. Let cache expired based on TTL.
	 */
	public boolean dataExpired() {
		return false;
	}

}
