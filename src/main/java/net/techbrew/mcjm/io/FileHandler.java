package net.techbrew.mcjm.io;


import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.Utils;
import net.techbrew.mcjm.data.WorldData;
import net.techbrew.mcjm.log.LogFormatter;
import org.lwjgl.Sys;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.logging.Level;

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
			InputStream is = JourneyMap.class.getResourceAsStream(png);
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

    public static Properties getLangFile(String fileName) {
        try {
            InputStream is = JourneyMap.class.getResourceAsStream("/assets/JourneyMap/lang/" + fileName);
            if(is==null) {
                JourneyMap.getLogger().warning("Language file not found: " + fileName);
                return null;
            }
            Properties properties = new Properties();
            properties.load(is);
            is.close();
            return properties;
        } catch (IOException e) {
            String error = Constants.getMessageJMERR17(e.getMessage());
            JourneyMap.getLogger().severe(error);
            return null;
        }
    }
	
	public static File getCacheDir() {
		return new File(Minecraft.getMinecraft().mcDataDir, Constants.CACHE_DIR);
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

        if (Util.getOSType() == Util.EnumOS.MACOS) {
            try {
                Runtime.getRuntime().exec(new String[] {"/usr/bin/open", path});
                return;
            } catch (IOException e) {
                JourneyMap.getLogger().severe("Could not open path with /usr/bin/open: " + path + " : " + LogFormatter.toString(e));
            }
            
        } else if (Util.getOSType() == Util.EnumOS.WINDOWS) {
        	
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
	
	public static boolean serializeCache(String name, Serializable cache) {
		try {
			File cacheDir = getCacheDir();
			if(!cacheDir.exists()) cacheDir.mkdirs();		
			
			File cacheFile = new File(cacheDir, name);
			
			FileOutputStream fileOut = new FileOutputStream(cacheFile);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(cache);
			out.close();
			fileOut.close();
			return true;
		} catch(IOException e) {
			JourneyMap.getLogger().severe("Could not serialize cache: " + name + " : " + LogFormatter.toString(e));
			return false;
	    }
	}

    public static boolean writeDebugFile(String name, String contents) {
        try {
            File debugFile = new File(getJourneyMapDir(), "DEBUG-" + name);
            FileWriter writer = new FileWriter(debugFile, false);
            writer.write(contents);
            writer.flush();
            writer.close();
            return true;
        } catch(IOException e) {
            JourneyMap.getLogger().severe("Could not write debug file: " + name + " : " + LogFormatter.toString(e));
            return false;
        }
    }
	
	public static <C extends Serializable> C deserializeCache(String name, Class<C> cacheClass) {

        File cacheFile = new File(getCacheDir(), name);
        if(!cacheFile.exists()) return null;
		try {
			FileInputStream fileIn = new FileInputStream(cacheFile);
	        ObjectInputStream in = new ObjectInputStream(fileIn);
	        C cache = (C) in.readObject();
	        in.close();
	        fileIn.close();
            if(cache.getClass()!=cacheClass) throw new ClassCastException(cache.getClass() + " can't be cast to " + cacheClass);
			return cache;
		} catch(Exception e) {
			JourneyMap.getLogger().warning("Could not deserialize cache: " + name + " : " + e);
            if(cacheFile.exists()) cacheFile.delete();
			return null;
	    } 
	}
	
}
