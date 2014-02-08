package net.techbrew.journeymap.data;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.NetworkManager;
import net.minecraft.world.storage.WorldInfo;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.feature.FeatureManager;
import net.techbrew.journeymap.log.LogFormatter;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

;

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
		features,
//		worldType,
//		gameType
	}

	/**
	 * Constructor.
	 */
	public WorldData() {
	}
	
	@Override
	public Enum[] getKeys() {
		return Key.values();
	}
	
	public static boolean isHardcoreAndMultiplayer() {
		boolean hardcore = (Boolean) DataCache.instance().get(WorldData.class, null).get(Key.hardcore);
		boolean multiplayer = (Boolean) DataCache.instance().get(WorldData.class, null).get(Key.singlePlayer)==false;
		return hardcore && multiplayer;
	}
	
	/**
	 * Return map of world-related properties.
	 */
	@Override
	public Map getMap(Map optionalParams) {		
		
		Minecraft mc = Minecraft.getMinecraft();
		WorldInfo worldInfo = mc.theWorld.getWorldInfo();

		LinkedHashMap props = new LinkedHashMap();

		props.put(Key.name, getWorldName(mc)); 
		props.put(Key.dimension, mc.theWorld.provider.dimensionId); 
		props.put(Key.hardcore,  worldInfo.isHardcoreModeEnabled());
		props.put(Key.singlePlayer, mc.isSingleplayer()); 
		props.put(Key.time, mc.theWorld.getWorldTime() % 24000L);
		props.put(Key.features, FeatureManager.getAllowedFeatures());

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
	
	private static String getServerHash() {
		String serverName = getServerName();
		try
	    {
	      MessageDigest md5 = MessageDigest.getInstance("MD5");	      
	      if (md5 != null)
	      {	    	 
	          byte[] bServerName = serverName.getBytes("UTF-8");
	          byte[] hashed = md5.digest(bServerName);
	          BigInteger bigInt = new BigInteger(1, hashed);
	          String md5Hash = bigInt.toString(16);
	          while (md5Hash.length() < 32) {
	            md5Hash = "0" + md5Hash;
	          }
	          return md5Hash;
	      } 
	    }
	    catch (Exception ex)
	    {
	    }
		return serverName;
	}
	
	private static String getServerName() {
		try
	    {
            NetworkManager netManager = FMLClientHandler.instance().getClientToServerNetworkManager();
            if(netManager!=null) {
                SocketAddress socketAddress = netManager.getSocketAddress();
                if ((socketAddress !=null && socketAddress instanceof InetSocketAddress))
                {
                    InetSocketAddress inetAddr = (InetSocketAddress)socketAddress;
                    return inetAddr.getHostName();
                }
            }
	    }
	    catch (Throwable t)
	    {
	    	JourneyMap.getLogger().severe("Couldn't get server name: " + LogFormatter.toString(t));
	    }
	    return "server";
	}
	
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
			if(mc.theWorld==null) return "offline";
			worldName = mc.theWorld.getWorldInfo().getWorldName();
			if(!"MpServer".equals(worldName)) {
				worldName = getServerName() + "_" + worldName;
			} else {
				worldName = getServerName();
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