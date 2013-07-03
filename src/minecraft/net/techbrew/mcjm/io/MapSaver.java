package net.techbrew.mcjm.io;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

import net.minecraft.src.Minecraft;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
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
	 * @param chunkY
	 * @param cType
	 * @return
	 * @throws IOException
	 * @throws java.lang.OutOfMemoryError
	 */
	public static synchronized File lightWeightSaveMap(final File worldDir, final Constants.MapType mapType, final Integer chunkY, final Constants.CoordType cType) {
		
		File mapFile = null;
		
		try {
		
		// Ensure latest regions are flushed to disk
		RegionImageCache.getInstance().flushToDisk();
		
		long start = 0, stop = 0;
		start = System.currentTimeMillis();
		
		Integer x1=null;
		Integer x2=null;
		Integer z1=null;
		Integer z2=null;
		
		// Find all region files
		FilenameFilter ff = new RegionFileHandler.RegionFileFilter(cType);
		File[] foundFiles = worldDir.listFiles(ff);
		//System.out.println("Found region files: " + foundFiles.length); //$NON-NLS-1$

		for(File file : foundFiles) {
			String segment = file.getName().split("_")[0]; //$NON-NLS-1$
			String[] xz = segment.split(","); //$NON-NLS-1$
			Integer x = Integer.parseInt(xz[0]);
			Integer z = Integer.parseInt(xz[1]);
			int rx1 = RegionCoord.getMinChunkX(x);
			int rx2 = RegionCoord.getMaxChunkX(x);
			int rz1 = RegionCoord.getMinChunkZ(z);
			int rz2 = RegionCoord.getMaxChunkZ(z);
			if(x1==null || rx1<x1) x1 = rx1;			
			if(x2==null || rx2>x2) x2 = rx2;
			if(z1==null || rz1<z1) z1 = rz1;
			if(z2==null || rz2>z2) z2 = rz2;
		}
		
		//System.out.println("Final chunk coordinates: " + x1 + "," + z1 + " to " + x2 + "," + z2);  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		
		if(x1==null || x2==null || z1==null ||z2==null ) {
			JourneyMap.getLogger().warning("No region files to save.");
			return null;
		}
		
		final File saveDir = FileHandler.getJourneyMapDir();
		
		JourneyMap.announce(Constants.getString("MapOverlay.saving_map_to_file", cType + " " + mapType)); //$NON-NLS-1$
		
		mapFile = createMapFile(FileHandler.getSafeName(Minecraft.getMinecraft()) + "_" + cType + "_" + mapType);
				
		RegionFileHandler.getMergedChunksFile(worldDir, x1, z1, x2, z2, mapType, chunkY, cType, mapFile);
		
		stop = System.currentTimeMillis();
		
		JourneyMap.getLogger().info("Map saved in: " + (stop-start) + "ms: " + mapFile); //$NON-NLS-1$ //$NON-NLS-2$
		JourneyMap.announce(Constants.getString("MapSaver.map_saved", mapFile)); //$NON-NLS-1$
		
		
		} catch (java.lang.OutOfMemoryError e) {
			String error = Constants.getMessageJMERR18("Out Of Memory: Increase Java Heap Size for Minecraft to save large maps.");
			JourneyMap.getLogger().severe(error);
			JourneyMap.announce(error);
		} catch (Throwable t) {	
			String error = Constants.getMessageJMERR18(t.getMessage());
			JourneyMap.getLogger().severe(error);
			JourneyMap.getLogger().log(Level.SEVERE, LogFormatter.toString(t));
			JourneyMap.announce(error);
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
