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
import net.techbrew.mcjm.Constants.MapType;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.Utils;
import net.techbrew.mcjm.data.DataCache;
import net.techbrew.mcjm.data.EntityKey;
import net.techbrew.mcjm.data.PlayerData;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.io.RegionImageHandler;
import net.techbrew.mcjm.model.RegionCoord;

public class RegionLoader {
	
	private static final Pattern anvilPattern = Pattern.compile("r\\.([^\\.]+)\\.([^\\.]+)\\.mca");
	
	final Logger logger = JourneyMap.getLogger();	
	final WorldClient worldClient;
	final long worldHash;
	final Stack<RegionCoord> regions;
	final int regionsFound;
	final Integer vSlice;

	File anvilDir;
		
	public RegionLoader(Minecraft minecraft, int dimension) throws IOException {
		super();
		this.worldClient = minecraft.theWorld;
		this.worldHash = Utils.getWorldHash(minecraft);
		final boolean underground = (Boolean) DataCache.instance().get(PlayerData.class).get(EntityKey.underground);
		File worldDir = FileHandler.getMCWorldDir(minecraft);	
		anvilDir = FileHandler.getAnvilRegionDirectory(worldDir, dimension);	
		if(dimension!=0) {
			anvilDir = new File(anvilDir, "region"); //$NON-NLS-1$
		}
		vSlice = underground ? minecraft.thePlayer.chunkCoordY : null;
		regions = findRegions(vSlice, dimension);
		regionsFound = regions.size();
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
		return vSlice!=null && vSlice!=-1;
	}
	
	public Integer getVSlice() {
		return vSlice;
	}
	
	Stack<RegionCoord> findRegions(final Integer vSlice, final int dimension) {
		
	    if (!anvilDir.exists()) {
	    	logger.warning("Anvil directory doesn't exist: " + anvilDir);
	    	return null;
	    }	        
	    
	    final Minecraft mc = Minecraft.getMinecraft();
	    final File jmImageWorldDir = FileHandler.getJMWorldDir(mc, worldHash);		
		final RegionImageHandler rfh = RegionImageHandler.getInstance();

	    final File[] anvilFiles = anvilDir.listFiles();
	    if (anvilFiles.length==0) {
	    	logger.warning("Anvil directory doesn't contain any files: " + anvilDir);
	    	return null;
	    }
	    
	    final Stack<RegionCoord> stack = new Stack<RegionCoord>();
	    MapType mapType = this.isUnderground() ? MapType.underground : MapType.day;
	    
		for (File anvilFile : anvilFiles) {
			Matcher matcher = anvilPattern.matcher(anvilFile.getName());
			if (!anvilFile.isDirectory() && matcher.matches()) {
				String x = matcher.group(1);
				String z = matcher.group(2);
				if (x != null && z != null) {
					RegionCoord rc = new RegionCoord(jmImageWorldDir, Integer.parseInt(x), vSlice, Integer.parseInt(z), dimension);
					if(dimension==0) {
						if(!rfh.getRegionImageFile(rc,mapType,false).exists()) {						
							List<ChunkCoordIntPair> chunkCoords = rc.getChunkCoordsInRegion();
							for(ChunkCoordIntPair coord : chunkCoords) {
								if(ChunkLoader.getChunkFromDisk(coord.chunkXPos, coord.chunkZPos, anvilDir, mc.theWorld)!=null) {
									stack.add(rc);
									break;
								}
							}							
						}
					} else {
						stack.add(rc);
					}
				}
			}
		}
		if (stack.isEmpty()) {
	    	logger.warning("No viable chunk data found among " + anvilFiles.length + " anvil files");
	    }
		Collections.sort(stack);
		return stack;
	}
	
}
