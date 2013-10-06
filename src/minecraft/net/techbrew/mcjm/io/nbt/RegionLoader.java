package net.techbrew.mcjm.io.nbt;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.src.Minecraft;
import net.minecraft.src.WorldClient;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.Utils;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.io.RegionFileHandler;
import net.techbrew.mcjm.model.ChunkStub;
import net.techbrew.mcjm.model.RegionCoord;

public class RegionLoader {
	
	private static final Pattern anvilPattern = Pattern.compile("r\\.([^\\.]+)\\.([^\\.]+)\\.mca");
	
	final Logger logger = JourneyMap.getLogger();	
	final WorldClient worldClient;
	final long worldHash;
	final ArrayList<RegionCoord> regions;

	final File worldDir;
		
	public RegionLoader(Minecraft minecraft, int dimension) throws IOException {
		super();
		this.worldClient = minecraft.theWorld;
		this.worldHash = Utils.getWorldHash(minecraft);
		this.worldDir = getWorldDirectory(minecraft);	
		File regionDir = getRegionDirectory(worldDir, dimension);
		regions = findRegions(regionDir);
	}
	
	public static File getWorldDirectory(Minecraft minecraft) {
		File dir = new File(minecraft.mcDataDir, "saves" + File.separator + minecraft.getIntegratedServer().getFolderName());
		if(dir.exists()) {
			return dir;
		} else {
			return null;
		}
	}
	
	public File getRegionDirectory(File worldDirectory, int dimension) {
		return dimension == 0 ? new File(worldDirectory, "region") : new File(worldDirectory, "DIM"+dimension); //$NON-NLS-1$
	}
	
	public Iterator<ChunkStub> chunkIterator() {	
		return new ChunkLoader(worldClient, worldHash, worldDir, regions);		
	}
	
	public Iterator<ChunkStub> chunkIterator(RegionCoord... rCoords) {	
		return new ChunkLoader(worldClient, worldHash, worldDir, Arrays.asList(rCoords));		
	}
	
	public Iterator<RegionCoord> regionIterator() {
		return regions.iterator();
	}
	
	public List<RegionCoord> getRegions() {
		return regions;
	}
	
	ArrayList<RegionCoord> findRegions(File regionDirectory) {
		
	    if (!regionDirectory.exists()) {
	    	return null;
	    }	        
	    
	    File jmImageWorldDir = FileHandler.getWorldDir(Minecraft.getMinecraft(), worldHash);
	    
	    Constants.CoordType ctype = Constants.CoordType.convert(worldClient.provider.dimensionId);
		
		RegionFileHandler rfh = RegionFileHandler.getInstance();

	    File[] anvilFiles = regionDirectory.listFiles();
	    ArrayList<RegionCoord> regions = new ArrayList<RegionCoord>(anvilFiles.length);
	    
		for (File anvilFile : anvilFiles) {
			Matcher matcher = anvilPattern.matcher(anvilFile.getName());
			if (!anvilFile.isDirectory() && matcher.matches()) {
				String x = matcher.group(1);
				String z = matcher.group(2);
				if (x != null && z != null) {
					RegionCoord rc = new RegionCoord(jmImageWorldDir, Integer.parseInt(x), null, Integer.parseInt(z), ctype);
					if(!rfh.getRegionFile(rc).exists()) {
						regions.add(rc);
					}
				}
			}
		}
		
		regions.trimToSize();
		Collections.sort(regions);
		return regions;
	}
	
}
