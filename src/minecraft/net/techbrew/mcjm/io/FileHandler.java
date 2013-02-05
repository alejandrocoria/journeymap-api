package net.techbrew.mcjm.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;

import net.minecraft.client.Minecraft;
import net.minecraft.src.MapStorage;
import net.minecraft.src.ModLoader;
import net.minecraft.src.World;
import net.minecraft.src.WorldInfo;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.ReflectionHelper;
import net.techbrew.mcjm.Utils;
import net.techbrew.mcjm.log.LogFormatter;


public class FileHandler {

	public static volatile long lastWorldHash;
	public static volatile File lastWorldDir;
	
	public static File getJourneyMapDir() {
		return new File(Minecraft.getMinecraftDir(), Constants.JOURNEYMAP_DIR);
	}
		
	
	public static File getWorldDir(Minecraft minecraft) {
		if(!minecraft.isSingleplayer()) {
			return getWorldDir(minecraft, Utils.getWorldHash(minecraft));
		} else {
			return getWorldDir(minecraft, -1L);
		}
		
	}
	
	public static File getWorldDir(Minecraft minecraft, long hash) {
		
		File mcDir = Minecraft.getMinecraftDir();
		
		if(lastWorldHash!=hash || lastWorldDir==null) {
			File worldDir = null;
			
			try {				
				if(!minecraft.isSingleplayer()) {
					worldDir = new File(mcDir, Constants.MP_DATA_DIR + getSafeName(minecraft) + "_" + hash); //$NON-NLS-1$
				} else {
					worldDir = new File(mcDir, Constants.SP_DATA_DIR + getSafeName(minecraft));
				}
				
			} catch (UnsupportedEncodingException e) {
				JourneyMap.getLogger().log(Level.SEVERE, LogFormatter.toString(e));
				throw new RuntimeException(e);
			}			
			if(minecraft.isSingleplayer() || minecraft.getServerData()!=null) {
				worldDir.mkdirs();			
			} 
			lastWorldHash = hash;
			lastWorldDir = worldDir;			
		}
		return lastWorldDir;
	}
	
	public static String getSafeName(Minecraft minecraft) throws UnsupportedEncodingException {
		if(!minecraft.isSingleplayer()) {
			String worldName = minecraft.theWorld.getWorldInfo().getWorldName();
			if("MpServer".equals(worldName)) worldName = "";			
			return URLEncoder.encode(minecraft.getServerData().serverName + "_" + worldName, "UTF-8"); //$NON-NLS-1$
		} else {
			return URLEncoder.encode(minecraft.getIntegratedServer().getWorldName(), "UTF-8"); //$NON-NLS-1$
		}		
	}
	
	public static void writeToFile(File file, String contents) {
		try {
			FileWriter out = new FileWriter(file, false);
			out.write(contents);
			out.flush();
			out.close();
		} catch (IOException e) {
			JourneyMap.getLogger().severe(Constants.getMessageJMERR04(e.getMessage()));
			JourneyMap.announce(Constants.getMessageJMERR04(file.getAbsolutePath())); //$NON-NLS-1$
			JourneyMap.getLogger().log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	
}
