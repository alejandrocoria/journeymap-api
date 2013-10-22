package net.techbrew.mcjm.io;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import net.minecraft.src.Minecraft;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.data.WorldData;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.model.RegionCoord;
import net.techbrew.mcjm.model.RegionImageCache;

/**
 * Merges all region files into a single image
 * @author Mark
 *
 */
public class MapSaver {

	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
	
	/**
	 * Use pngj to assemble region files.
	 * 
	 * TODO: Draw grid on saved map
	 * 
	 * @param worldDir
	 * @param mapType
	 * @param vSlice
	 * @param cType
	 * @return
	 * @throws IOException
	 * @throws java.lang.OutOfMemoryError
	 */
	public static synchronized File lightWeightSaveMap(final File worldDir, final Constants.MapType mapType, final Integer vSlice, final int dimension) {
		
		File mapFile = null;
		
		try {
		
		// Ensure latest regions are flushed to disk
		RegionImageCache.getInstance().flushToDisk();
		
		long start = 0, stop = 0;
		start = System.currentTimeMillis();
		
		Integer minX=0, minZ=0, maxX=0, maxZ=0;
		
		RegionCoord rc = new RegionCoord(worldDir, 0, vSlice, 0, dimension);
		File imageDir = RegionImageHandler.getImageDir(rc, mapType);
		List<File> foundFiles = Arrays.asList(imageDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".png");
			}}));
		
		for(File file : foundFiles) {
			String segment = file.getName().split("\\.")[0]; //$NON-NLS-1$
			String[] xz = segment.split(","); //$NON-NLS-1$
			Integer x = Integer.parseInt(xz[0]);
			Integer z = Integer.parseInt(xz[1]);
			if(minX==null || x<minX) minX = x;
			if(minZ==null || z<minZ) minZ = z;
			if(maxX==null || x>maxX) maxX = x;
			if(maxZ==null || z>maxZ) maxZ = z;			
		}

		if(minX==null || maxX==null || minZ==null ||maxZ==null ) {
			JourneyMap.getLogger().warning("No region files to save.");
			return null;
		}
		
		final File saveDir = FileHandler.getJourneyMapDir();
		
		JourneyMap.getInstance().announce(Constants.getString("MapOverlay.saving_map_to_file", Constants.CoordType.convert(dimension) + " " + mapType)); //$NON-NLS-1$
		
		mapFile = createMapFile(WorldData.getWorldName(Minecraft.getMinecraft()) + "_" + dimension + "_" + mapType);
			
		boolean isUnderground = mapType.equals(Constants.MapType.underground);

		// Merge chunk images
		RegionImageHandler rfh = RegionImageHandler.getInstance();
		
		// Get region files
		File rfile;
		ArrayList<File> files = new ArrayList<File>();
		
		for(int rz=minZ;rz<=maxZ;rz++) {
			for(int rx=minX;rx<=maxX;rx++) {			
				rc = new RegionCoord(worldDir, rx, vSlice, rz, dimension);
				rfile = rfh.getRegionImageFile(rc, mapType, true);
				if(!rfile.exists()) {
					BufferedImage image;
					image = rfh.createBlankImage(512,512);				
					try {
						ImageIO.write(image, "png", rfile);
					} catch(IOException e) {
						JourneyMap.getInstance().announce(Constants.getMessageJMERR22(rfile, LogFormatter.toString(e)), Level.SEVERE);
					}
				} else {
					files.add(rfile);
				}
			}
		}
		
		// TODO: if new blank files are created, then the whole thing needs to be started over  
		
		
		File[] fileArray = files.toArray(new File[files.size()]);
		
		PngjHelper.mergeFiles(fileArray, mapFile, (maxX-minX)+1, 512);
				
		if(JourneyMap.getLogger().isLoggable(Level.FINE)) {
			stop = System.currentTimeMillis();
			JourneyMap.getLogger().fine("getMergedChunks time: "  + (stop-start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		stop = System.currentTimeMillis();
		
		JourneyMap.getLogger().info("Map saved in: " + (stop-start) + "ms: " + mapFile); //$NON-NLS-1$ //$NON-NLS-2$
		JourneyMap.getInstance().announce(Constants.getString("MapSaver.map_saved", mapFile)); //$NON-NLS-1$
		
		
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
		
		return mapFile;
		
	}
	
	/**
	 * Create a file handle in the screenshots folder, using the same
	 * dateFormat that MC's ScreenshotHelper uses.
	 * 
	 * @param suffix
	 * @return
	 */
	private static File createMapFile(String suffix)
    {
		File screenshots = new File(Minecraft.getMinecraft().mcDataDir, "screenshots");
		if(!screenshots.exists()) {
			screenshots.mkdir();
		}
		
        String date = dateFormat.format(new Date());
        return new File(screenshots, date + "_" + suffix + ".png");

    }
	
}
