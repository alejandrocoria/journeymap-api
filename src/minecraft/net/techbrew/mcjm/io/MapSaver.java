package net.techbrew.mcjm.io;

import net.minecraft.client.Minecraft;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.Constants.MapType;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.data.WorldData;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.model.RegionCoord;
import net.techbrew.mcjm.model.RegionImageCache;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

;

/**
 * Merges all region files into a single image
 * @author Mark
 *
 */
public class MapSaver {

	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
	
	final File worldDir;
	File saveFile;
	final Constants.MapType mapType;
	final Integer vSlice;
	final int dimension;
	long estimatedBytes;
	int outputColumns;
	int outputRows;
	ArrayList<File> files;
	
	public MapSaver(File worldDir, MapType mapType, Integer vSlice, int dimension) {
		super();
		this.worldDir = worldDir;
		this.mapType = mapType;
		this.vSlice = vSlice;
		this.dimension = dimension;
		
		prepareFiles();
	}

	/**
	 * Use pngj to assemble region files.
	 */
	public File saveMap() {
		
		try {					
					
			if(!isValid()) {
				JourneyMap.getLogger().warning("No images found in " + getImageDir());
				return null;
			}					
			
			// Ensure latest regions are flushed to disk
			RegionImageCache.getInstance().flushToDisk();
			
			long start = 0, stop = 0;
			start = System.currentTimeMillis();
			
	        // Merge image files
			File[] fileArray = files.toArray(new File[files.size()]);			
			PngjHelper.mergeFiles(fileArray, saveFile, outputColumns, 512);
			
			stop = System.currentTimeMillis();
			
			JourneyMap.getLogger().info("Map saved in: " + (stop-start) + "ms: " + saveFile + ". Estimated/actual filesize:" + estimatedBytes + " / " + saveFile.length()); //$NON-NLS-1$ //$NON-NLS-2$
			JourneyMap.getInstance().announce(Constants.getString("MapSaver.map_saved", saveFile)); //$NON-NLS-1$		
			FileHandler.open(saveFile);
		
		} catch (java.lang.OutOfMemoryError e) {
			String error = Constants.getMessageJMERR18("Out Of Memory: Increase Java Heap Size for Minecraft to save large maps.");
			JourneyMap.getLogger().severe(error);
			JourneyMap.getInstance().announce(error);
		} catch (Throwable t) {	
			String error = Constants.getMessageJMERR18(t.getMessage());
			JourneyMap.getLogger().severe(error);
			JourneyMap.getLogger().log(Level.SEVERE, LogFormatter.toString(t));
			JourneyMap.getInstance().announce(error);
			return null;
		}
		
		return saveFile;
	}
	
	public String getSaveFileName() {
		return saveFile.getName();
	}
	
	public boolean isValid() {
		return files!=null && files.size()>0;
	}
	
	private File getImageDir() {
		// Fake coord gets us to the image directory
		RegionCoord fakeRc = new RegionCoord(worldDir, 0, vSlice, 0, dimension);
		return RegionImageHandler.getImageDir(fakeRc, mapType);
	}
	
	/**
	 * Prepares files to be merged, returns estimatedBytes of the result.
	 */
	private void prepareFiles() {
		
		try {
			
			// Build save file name
			final Minecraft mc = Minecraft.getMinecraft();
	        final String date = dateFormat.format(new Date());
	        final boolean isUnderground = mapType.equals(Constants.MapType.underground);
	        final StringBuilder sb = new StringBuilder(date).append("_");
	        sb.append(WorldData.getWorldName(mc)).append("_");
	        sb.append(mc.theWorld.provider.getDimensionName()).append("_");
	        if(isUnderground) {
	        	sb.append("slice").append(vSlice);
	        } else {
	        	sb.append(mapType);
	        }
	        sb.append(".png");	        
	        
			// Ensure screenshots directory
			File screenshotsDir = new File(Minecraft.getMinecraft().mcDataDir, "screenshots");
			if(!screenshotsDir.exists()) {
				screenshotsDir.mkdir();
			}
			
			// Create result file
	        saveFile = new File(screenshotsDir, sb.toString());
		
			// Ensure latest regions are flushed to disk
			RegionImageCache.getInstance().flushToDisk();

			// Look for pngs
			File imageDir = getImageDir();
			File[] pngFiles = imageDir.listFiles();
			
			final Pattern tilePattern = Pattern.compile("([^\\.]+)\\,([^\\.]+)\\.png");
			Integer minX=null, minZ=null, maxX=null, maxZ=null;

			for(File file : pngFiles) {
				Matcher matcher = tilePattern.matcher(file.getName());
				if(matcher.matches()) {
					Integer x = Integer.parseInt(matcher.group(1));
					Integer z = Integer.parseInt(matcher.group(2));
					if(minX==null || x<minX) minX = x;
					if(minZ==null || z<minZ) minZ = z;
					if(maxX==null || x>maxX) maxX = x;
					if(maxZ==null || z>maxZ) maxZ = z;
				}
			}
	
			if(minX==null || maxX==null || minZ==null ||maxZ==null ) {
				JourneyMap.getLogger().warning("No region files to save in " + imageDir);
				return;
			}								
			// Create blank
			final long blankSize = RegionImageHandler.getBlank512x512ImageFile().length();

			outputColumns = (maxX-minX)+1;
			outputRows = (maxZ-minZ)+1;
			files = new ArrayList<File>(outputColumns*outputRows);
			File rfile;
			RegionCoord rc;		
			
			// Sum the sizes of the files
			for(int rz=minZ;rz<=maxZ;rz++) {
				for(int rx=minX;rx<=maxX;rx++) {			
					rc = new RegionCoord(worldDir, rx, vSlice, rz, dimension);
					rfile = RegionImageHandler.getRegionImageFile(rc, mapType, true);
					if(rfile.canRead()) {
						files.add(rfile);
					} else {						
						files.add(RegionImageHandler.getBlank512x512ImageFile());
					} 					
				}
			}
			
		} catch (Throwable t) {	
			String error = Constants.getMessageJMERR18(t.getMessage());
			JourneyMap.getLogger().severe(error);
			JourneyMap.getLogger().log(Level.SEVERE, LogFormatter.toString(t));
		}
		
	}
	
}
