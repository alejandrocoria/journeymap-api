package net.techbrew.mcjm.io;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.model.RegionCoord;
import net.techbrew.mcjm.model.RegionImageCache;
import net.techbrew.mcjm.ui.ZoomLevel;

/**
 * Merges all region files into a single image
 * @author Mark
 *
 */
public class MapSaver {

	public void saveMapToFile(File worldDir, Constants.MapType mapType, int depth, int worldProviderType, File mapFile)
			throws IOException {

		final Constants.CoordType cType = Constants.CoordType.convert(mapType, worldProviderType);
		BufferedImage mergedImg = saveMap(worldDir, mapType, depth, cType);
		ImageIO.write(mergedImg, "png", mapFile); //$NON-NLS-1$
		
		JourneyMap.getLogger().info("Map saved: "  + mapFile); //$NON-NLS-1$
		JourneyMap.announce(Constants.getString("MapSaver.map_saved", mapFile.getCanonicalPath())); //$NON-NLS-1$
	}
	
	public static synchronized BufferedImage saveMap(final File worldDir, final Constants.MapType mapType, final Integer chunkY, final Constants.CoordType cType)
			throws IOException {
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
		
		if(x1==null || x2==null || z1==null ||z2==null ) return new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);
		
		boolean isUnderground = mapType.equals(Constants.MapType.underground);
		
		// Ensure latest regions are flushed to disk
		RegionImageCache.getInstance().flushToDisk();
		
		// Get region images without using cache
		BufferedImage mergedImg = RegionFileHandler.getMergedChunks(worldDir, x1, z1, x2, z2, mapType, chunkY, cType, false, 
				new ZoomLevel(1, 1, false, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR));
			
		stop = System.currentTimeMillis();
		JourneyMap.getLogger().info("World map saved in: " + (stop-start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
		return mergedImg;
		
	}
}
