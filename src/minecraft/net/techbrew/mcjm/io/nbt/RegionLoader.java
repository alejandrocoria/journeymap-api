package net.techbrew.mcjm.io.nbt;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.src.ChunkCoordIntPair;
import net.minecraft.src.Minecraft;
import net.techbrew.mcjm.Constants.MapType;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.Utils;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.io.RegionImageHandler;
import net.techbrew.mcjm.model.RegionCoord;
import net.techbrew.mcjm.model.RegionImageCache;

public class RegionLoader {
	
	private static final Pattern anvilPattern = Pattern.compile("r\\.([^\\.]+)\\.([^\\.]+)\\.mca");
	
	final Logger logger = JourneyMap.getLogger();	

	final MapType mapType;
	final Integer vSlice;
	final Stack<RegionCoord> regions;
	final int regionsFound;

	public RegionLoader(final Minecraft minecraft, final int dimension, final MapType mapType, final Integer vSlice) throws IOException {
		this.mapType = mapType;		
		this.vSlice = vSlice;
		if(mapType==MapType.underground && (vSlice==null || vSlice==-1)) {
			throw new IllegalArgumentException("Underground map requires vSlice");
		}
		this.regions = findRegions(minecraft, vSlice, dimension);
		this.regionsFound = regions.size();
	}	
	
	public Iterator<RegionCoord> regionIterator() {
		return regions.iterator();
	}
	
	public Stack<RegionCoord> getRegions() {
		return regions;
	}
	
	public int getRegionsFound() {
		return regionsFound;
	}
	
	public boolean isUnderground() {
		return mapType==MapType.underground;
	}
	
	public Integer getVSlice() {
		return vSlice;
	}
	
	Stack<RegionCoord> findRegions(final Minecraft mc, final Integer vSlice, final int dimension) {
		
	    final File mcWorldDir = FileHandler.getMCWorldDir(mc, dimension);	
		final File regionDir = new File(mcWorldDir, "region");
		if (!regionDir.exists() || regionDir.isFile()) {
	    	logger.warning("MC world region directory doesn't exist: " + regionDir);
	    	return null;
	    }	        
	    
	    final File jmImageWorldDir = FileHandler.getJMWorldDir(mc, Utils.getWorldHash(mc));
		final RegionImageHandler rfh = RegionImageHandler.getInstance();
	    final Stack<RegionCoord> stack = new Stack<RegionCoord>();
	    	   
	    RegionImageCache.getInstance().clear();
	    
	    int validFileCount = 0;
	    int existingImageCount = 0;
	    final File[] anvilFiles = regionDir.listFiles();
		for (File anvilFile : anvilFiles) {
			Matcher matcher = anvilPattern.matcher(anvilFile.getName());
			if (!anvilFile.isDirectory() && matcher.matches()) {
				validFileCount++;
				String x = matcher.group(1);
				String z = matcher.group(2);
				if (x != null && z != null) {
					RegionCoord rc = new RegionCoord(jmImageWorldDir, Integer.parseInt(x), vSlice, Integer.parseInt(z), dimension);					
					if(!rfh.getRegionImageFile(rc,mapType,false).exists()) {	
						List<ChunkCoordIntPair> chunkCoords = rc.getChunkCoordsInRegion();
						for(ChunkCoordIntPair coord : chunkCoords) {
							if(ChunkLoader.getChunkFromDisk(coord.chunkXPos, coord.chunkZPos, mcWorldDir, mc.theWorld)!=null) {
								stack.add(rc);
								break;
							}
						}
					} else {
						existingImageCount++;
					}
				}
			}
		}
		if (stack.isEmpty() && (validFileCount!=existingImageCount)) {
	    	logger.warning("Anvil region files in " + regionDir + ": " + validFileCount + ", matching image files: " + existingImageCount + ", but found nothing to do for mapType " + mapType);
	    }
		
		Collections.sort(stack);
		return stack;
	}
	
}
