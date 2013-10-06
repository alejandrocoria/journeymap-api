package net.techbrew.mcjm.io.nbt;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;

import net.minecraft.src.AnvilChunkLoader;
import net.minecraft.src.ChunkCoordIntPair;
import net.minecraft.src.RegionFile;
import net.minecraft.src.WorldClient;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.model.ChunkStub;
import net.techbrew.mcjm.model.RegionCoord;

/**
 * Provides an iterator of ChunkStubs populated by
 * chunks read from region files via AnvilChunkLoader.
 * 
 * Not threadsafe.
 * 
 * @author mwoodman
 *
 */
public class ChunkLoader implements Iterator<ChunkStub> {
	
	final private Logger logger = JourneyMap.getLogger();
	final private WorldClient worldClient;
	final private File worldDir;
	final private long worldHash;
	final private Iterator<RegionCoord> regionIter;
	final private AnvilChunkLoader chunkLoader;
	private Iterator<ChunkCoordIntPair> coordIter;
		
	/**
	 * Construct the iterator for regions matching the regionCoords.
	 * @param worldClient
	 * @param worldHash
	 * @param worldDir
	 * @param regionCoords
	 */
	ChunkLoader(WorldClient worldClient, long worldHash, File worldDir, final Collection<RegionCoord> regionCoords) {
		this.worldClient = worldClient;
		this.worldHash = worldHash;
		this.worldDir = worldDir;
		this.regionIter = new ArrayList<RegionCoord>(regionCoords).iterator();
		this.chunkLoader = new AnvilChunkLoader(worldDir);
	}

	@Override
	public synchronized boolean hasNext() {
		
		if(coordIter!=null && coordIter.hasNext()) {
			return true;
		} else if(regionIter.hasNext()) {
			RegionCoord rCoord = regionIter.next();			
			coordIter = rCoord.getChunkCoordsInRegion().iterator();
			return true;
		} else {
			return false;
		}
		
	}

	@Override
	public ChunkStub next() {
		if(!hasNext()) {
			return null;
		} else {
			ChunkCoordIntPair coord = coordIter.next();
			ChunkStub stub = ChunkHelper.getChunkStub(coord, worldDir, worldClient, worldHash);
			return stub;
		} 
	}

	@Override
	public void remove() {
	}	
	
	public File getRegionFile(RegionCoord rCoord, File worldDir) throws FileNotFoundException 
    {
		File regionDir = new File(worldDir, "region");
        return new File(regionDir, "r." + rCoord.regionX + "." + rCoord.regionZ + ".mca");
    }
		
	
	public RegionFile getRegion(RegionCoord rCoord, File worldDir) 
    {
		File regionDir = new File(worldDir, "region");
        File file = new File(regionDir, "r." + rCoord.regionX + "." + rCoord.regionZ + ".mca");
        return new RegionFile(file);
    }
			
}
