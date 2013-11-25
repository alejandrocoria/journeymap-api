package net.techbrew.mcjm.io;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import net.minecraft.src.EnumOS;
import net.minecraft.src.Minecraft;
import net.minecraft.src.Util;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.Utils;
import net.techbrew.mcjm.data.WorldData;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.model.EntityHelper;

import org.lwjgl.Sys;


public class FileHandler {

	public static final String WEB_DIR = "/net/techbrew/mcjm/web";
	public static volatile long lastWorldHash;
	public static volatile File lastJMWorldDir;
	
	public static volatile String lastMCFolderName = "";
	public static volatile File lastMCWorldDir = null;
	
	public static File getMCWorldDir(Minecraft minecraft) {		
		if(minecraft.isIntegratedServerRunning()) {
			if(lastMCWorldDir==null || !lastMCFolderName.equals(minecraft.getIntegratedServer().getFolderName())) {
				lastMCFolderName = minecraft.getIntegratedServer().getFolderName();
				lastMCWorldDir = new File(minecraft.mcDataDir, "saves" + File.separator + lastMCFolderName);
				System.out.println("New world, new dir: " + lastMCWorldDir);
			} 
			return lastMCWorldDir;			
		}
		return null;
	}
	
	public static File getMCWorldDir(Minecraft minecraft, final int dimension) {
		File worldDir = getMCWorldDir(minecraft);
		if(worldDir==null) return null;
		if(dimension==0) {
			return worldDir;
		} else {
			final String dimString = Integer.toString(dimension);
			File dimDir = null;
			
			// Normal dimensions handled this way
			if(dimension==-1 || dimension==1) {			
				dimDir = new File(worldDir, "DIM"+dimString); //$NON-NLS-1$
			}
			
			// Custom dimensions handled this way
			// TODO: Use Forge dimensionhandler to get directory name.  This is a brittle workaround.
			if(dimDir==null || !dimDir.exists()) {
				File[] dims = worldDir.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.startsWith("DIM") && name.endsWith(dimString) && !name.endsWith("-"+dimString); // this last part prevents negative matches, but may nerf a dumb naming scheme
					}					
				});
				
				if(dims.length==0) {
					dimDir = dims[0];
				} else {
					// 7 might match DIM7 and DIM17.  Sort and return shortest filename.
					List<File> list = Arrays.asList(dims);
					Collections.sort(list, new Comparator<File>() {
						@Override
						public int compare(File o1, File o2) {
							return new Integer(o1.getName().length()).compareTo(o2.getName().length());
						}						
					});
					return list.get(0);
				}
			}
			
			return dimDir;
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
		
		if(lastWorldHash!=hash || lastJMWorldDir==null) {
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
			lastJMWorldDir = worldDir;			
		}
		return lastJMWorldDir;
	}
	
	
	public static File getAnvilRegionDirectory(File worldDirectory, int dimension) {
		if(dimension==0) {
			return new File(worldDirectory, "region"); //$NON-NLS-1$
		} else {
			return new File(worldDirectory, "DIM"+dimension); //$NON-NLS-1$
		}
	}
	
	
	public static BufferedImage getWebImage(String fileName) {
		try {
			String png = FileHandler.WEB_DIR + "/img/" + fileName;//$NON-NLS-1$
			InputStream is = EntityHelper.class.getResourceAsStream(png);
			if(is==null) {
				JourneyMap.getLogger().warning("Image not found: " + png);
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
	
	public static File getCustomDir() {
		return new File(Minecraft.getMinecraft().mcDataDir, Constants.CUSTOM_DIR);
	}
	
	public static BufferedImage getCustomImage(String fileName) {
		try {
			File png = new File(getCustomDir(), "img/" + fileName); //$NON-NLS-1$
			if(!png.canRead()) {
				return null;
			}
			return ImageIO.read(png);
		} catch (IOException e) {
			String error = Constants.getMessageJMERR17(e.getMessage());
			JourneyMap.getLogger().severe(error);
			return null;
		}
	}
	
	public static void setCustomImage(String fileName, BufferedImage img) {
		try {			
			File pngFile = new File(getCustomDir(), "img/" + fileName); //$NON-NLS-1$
			File parentDir = pngFile.getParentFile();
			if(!parentDir.exists()) parentDir.mkdirs();			
			ImageIO.write(img, "png", pngFile);
		} catch (Exception e) {
			String error = Constants.getMessageJMERR00("Can't write custom image " + fileName + ": " + e);
			JourneyMap.getLogger().severe(error);
		}
	}
	
	public static BufferedImage getCustomizableImage(String fileName, BufferedImage defaultImg) {
				
		BufferedImage img = null;
		if(getCustomDir().exists()) {
			img = FileHandler.getCustomImage(fileName);
		}
		if(img==null) {
			img = FileHandler.getWebImage(fileName);			
			if(img==null) {				
				img = defaultImg;				
				setCustomImage(fileName, img);
				JourneyMap.getLogger().info("Created placeholder image: " + new File(getCustomDir(), fileName));
			}
		}		
		
		return img;
	}
	
	public static void open(File file) {

        String path = file.getAbsolutePath();

        if (Util.getOSType() == EnumOS.MACOS) {
            try {
                Runtime.getRuntime().exec(new String[] {"/usr/bin/open", path});
                return;
            } catch (IOException e) {
                JourneyMap.getLogger().severe("Could not open path with /usr/bin/open: " + path + " : " + LogFormatter.toString(e));
            }
            
        } else if (Util.getOSType() == EnumOS.WINDOWS) {
        	
            String cmd = String.format("cmd.exe /C start \"Open file\" \"%s\"", new Object[] {path});

            try {
                Runtime.getRuntime().exec(cmd);
                return;
            } catch (IOException e) {
                JourneyMap.getLogger().severe("Could not open path with cmd.exe: " + path + " : " + LogFormatter.toString(e));
            }
        }

        try {
            Class desktopClass = Class.forName("java.awt.Desktop");
            Object method = desktopClass.getMethod("getDesktop", new Class[0]).invoke((Object)null, new Object[0]);
            desktopClass.getMethod("browse", new Class[] {URI.class}).invoke(method, new Object[] {file.toURI()});
        } catch (Throwable e) {
            JourneyMap.getLogger().severe("Could not open path with Desktop: " + path + " : " + LogFormatter.toString(e));
            Sys.openURL("file://" + path);
        }
	}
	
}
