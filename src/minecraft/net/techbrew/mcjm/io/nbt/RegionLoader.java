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
import net.minecraft.src.WorldClient;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.Utils;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.io.RegionFileHandler;
import net.techbrew.mcjm.model.RegionCoord;

public class RegionLoader {
	
	private static final Pattern anvilPattern = Pattern.compile("r\\.([^\\.]+)\\.([^\\.]+)\\.mca");
	
	final Logger logger = JourneyMap.getLogger();	
	final WorldClient worldClient;
	final long worldHash;
	final Stack<RegionCoord> regions;
	final int regionsFound;

	final File worldDir;
		
	public RegionLoader(Minecraft minecraft, int dimension) throws IOException {
		super();
		this.worldClient = minecraft.theWorld;
		this.worldHash = Utils.getWorldHash(minecraft);
		this.worldDir = FileHandler.getMCWorldDir(minecraft);	
		File regionDir = getRegionDirectory(worldDir, dimension);
		regions = findRegions(regionDir);
		regionsFound = regions.size();
	}	
	
	public File getRegionDirectory(File worldDirectory, int dimension) {
		return dimension == 0 ? new File(worldDirectory, "region") : new File(worldDirectory, "DIM"+dimension); //$NON-NLS-1$
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
	
	Stack<RegionCoord> findRegions(File regionDirectory) {
		
	    if (!regionDirectory.exists()) {
	    	return null;
	    }	        
	    
	    Minecraft mc = Minecraft.getMinecraft();
	    File jmImageWorldDir = FileHandler.getJMWorldDir(mc, worldHash);
	    
	    Constants.CoordType ctype = Constants.CoordType.convert(worldClient.provider.dimensionId);
		
		RegionFileHandler rfh = RegionFileHandler.getInstance();

	    File[] anvilFiles = regionDirectory.listFiles();
	    Stack<RegionCoord> stack = new Stack<RegionCoord>();
	    
		for (File anvilFile : anvilFiles) {
			Matcher matcher = anvilPattern.matcher(anvilFile.getName());
			if (!anvilFile.isDirectory() && matcher.matches()) {
				String x = matcher.group(1);
				String z = matcher.group(2);
				if (x != null && z != null) {
					RegionCoord rc = new RegionCoord(jmImageWorldDir, Integer.parseInt(x), null, Integer.parseInt(z), ctype);
					if(!rfh.getRegionFile(rc).exists()) {						
						List<ChunkCoordIntPair> chunkCoords = rc.getChunkCoordsInRegion();
						for(ChunkCoordIntPair coord : chunkCoords) {
							if(ChunkLoader.getChunkFromDisk(coord.chunkXPos, coord.chunkZPos, worldDir, mc.theWorld)!=null) {
								stack.add(rc);
								break;
							}
						}
						
					}
				}
			}
		}
		
		Collections.sort(stack);
		return stack;
	}
	
}
