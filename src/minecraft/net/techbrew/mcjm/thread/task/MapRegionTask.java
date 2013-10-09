package net.techbrew.mcjm.thread.task;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.src.ChunkCoordIntPair;
import net.minecraft.src.Minecraft;
import net.minecraft.src.World;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.io.nbt.ChunkLoader;
import net.techbrew.mcjm.model.ChunkStub;
import net.techbrew.mcjm.model.RegionCoord;

public class MapRegionTask extends MapTask {
	
	private static final Logger logger = JourneyMap.getLogger();
	private static boolean underground = false;
	private static int dimension = 0;
	
	private MapRegionTask(World world, boolean underground, Integer chunkY, Map<ChunkCoordIntPair, ChunkStub> chunkStubs) {
		super(world, underground, chunkY, chunkStubs, true);
	}
	
	public static void setProperties(boolean underground, int dimension) {
		MapRegionTask.underground = underground;
		MapRegionTask.dimension = dimension;
	}
	
	public static MapTask create(RegionCoord rCoord, Minecraft minecraft, long worldHash) {
		
		int missing = 0;
		final int chunkY = underground ? minecraft.thePlayer.chunkCoordY : -1;
		final World world = minecraft.theWorld;
		final File worldDir = FileHandler.getMCWorldDir(minecraft);		
		final Map<ChunkCoordIntPair, ChunkStub> chunks = new HashMap<ChunkCoordIntPair, ChunkStub>(1280); // 1024 * 1.25 alleviates map growth		
		final List<ChunkCoordIntPair> coords = rCoord.getChunkCoordsInRegion();
		
		while(!coords.isEmpty()) {
			ChunkCoordIntPair coord = coords.remove(0);
			ChunkStub stub = ChunkLoader.getChunkStubFromDisk(coord.chunkXPos, coord.chunkZPos, worldDir, world, worldHash);
			if(stub==null) {
				missing++;
			} else {
				chunks.put(coord, stub);
			}
		}		

		if(logger.isLoggable(Level.FINE)) {
			logger.fine("Chunks: " + missing + " skipped, " + chunks.size() + " used");
		}
		
		if(chunks.size()>0) {
			return new MapRegionTask(world, underground, chunkY, chunks);
		} else {
			return null;
		}
	
	}
}
