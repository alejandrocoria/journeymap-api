package net.techbrew.mcjm.io;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import net.minecraft.src.Minecraft;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.Utils;
import net.techbrew.mcjm.data.WorldData;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.model.EntityHelper;


public class FileHandler {

	public static final String WEB_DIR = "/net/techbrew/mcjm/web";
	public static volatile long lastWorldHash;
	public static volatile File lastWorldDir;
	
	public static File getMCWorldDir(Minecraft minecraft) {
		File dir = new File(minecraft.mcDataDir, "saves" + File.separator + minecraft.getIntegratedServer().getFolderName());
		if(dir.exists()) {
			return dir;
		} else {
			return null;
		}
	}
	
	public static File getJourneyMapDir() {
		return new File(Minecraft.getMinecraft().mcDataDir, Constants.JOURNEYMAP_DIR);
	}
		
	
	public static File getJMWorldDir(Minecraft minecraft) {
		if(!minecraft.isSingleplayer()) {
			return getJMWorldDir(minecraft, Utils.getWorldHash(minecraft));
		} else {
			return getJMWorldDir(minecraft, -1L);
		}
		
	}
	
	public static File getJMWorldDir(Minecraft minecraft, long hash) {
		
		File mcDir = Minecraft.getMinecraft().mcDataDir;
		
		if(lastWorldHash!=hash || lastWorldDir==null) {
			File worldDir = null;
			
			try {				
				String worldName = WorldData.getWorldName(minecraft);
				if(!minecraft.isSingleplayer()) {
					worldDir = new File(mcDir, Constants.MP_DATA_DIR + worldName + "_" + hash); //$NON-NLS-1$
				} else {
					worldDir = new File(mcDir, Constants.SP_DATA_DIR + worldName);
				}
				worldDir.mkdirs();
			} catch (Exception e) {
				JourneyMap.getLogger().log(Level.SEVERE, LogFormatter.toString(e));
				throw new RuntimeException(e);
			}			

			lastWorldHash = hash;
			lastWorldDir = worldDir;			
		}
		return lastWorldDir;
	}
	
	
	public static File getAnvilRegionDirectory(File worldDirectory, int dimension) {
		if(dimension==0) {
			return new File(worldDirectory, "region"); //$NON-NLS-1$
		} else {
			return new File(worldDirectory, "DIM"+dimension); //$NON-NLS-1$
		}
	}
	
	
	public static BufferedImage getImage(String fileName) {
		try {
			String png = FileHandler.WEB_DIR + "/img/" + fileName;//$NON-NLS-1$
			InputStream is = EntityHelper.class.getResourceAsStream(png);
			if(is==null) {
				JourneyMap.getLogger().warning("Unable to get image: " + png);
				return null;
			}
			BufferedImage img = ImageIO.read(is);
			is.close();
			return img;
		} catch (IOException e) {
			String error = Constants.getMessageJMERR17(e.getMessage());
			JourneyMap.getLogger().severe(error);
			return null;
		}
	}
	
}
