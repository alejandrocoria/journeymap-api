package net.techbrew.mcjm.thread.task;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.src.ChunkCoordIntPair;
import net.minecraft.src.ChunkCoordinates;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Minecraft;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.io.nbt.ChunkLoader;
import net.techbrew.mcjm.model.ChunkStub;
import net.techbrew.mcjm.model.RegionCoord;

public class RegionTask extends UpdateThreadTask {
	
	private static final Logger logger = JourneyMap.getLogger();
	private static boolean underground = false;
	private static int dimension = 0;
	
	private RegionTask(int dimension, boolean underground, int chunkY, Map<ChunkCoordIntPair, ChunkStub> chunkStubs) {
		super(dimension, underground, chunkY, chunkStubs, true);
	}
	
	public static void setProperties(boolean underground, int dimension) {
		RegionTask.underground = underground;
		RegionTask.dimension = dimension;
	}
	
	public static UpdateThreadTask create(RegionCoord rCoord, Minecraft minecraft, long worldHash) {
		
		int missing = 0;
		int chunkY = underground ? minecraft.thePlayer.chunkCoordY : -1;
		File worldDir = FileHandler.getMCWorldDir(minecraft);		
		Map<ChunkCoordIntPair, ChunkStub> chunks = new HashMap<ChunkCoordIntPair, ChunkStub>(1280); // 1024 * 1.25 alleviates map growth
		
		List<ChunkCoordIntPair> coords = rCoord.getChunkCoordsInRegion();
		while(!coords.isEmpty()) {
			ChunkCoordIntPair coord = coords.remove(0);
			ChunkStub stub = ChunkLoader.getChunkStubFromDisk(coord.chunkXPos, coord.chunkZPos, worldDir, minecraft.theWorld, worldHash);
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
			return new RegionTask(dimension, underground, chunkY, chunks);
		} else {
			return null;
		}
	
	}
	
	private static ChunkCoordinates getPlayerPos(Minecraft minecraft) {
		EntityPlayer player = minecraft.thePlayer;
		return new ChunkCoordinates(player.chunkCoordX, player.chunkCoordY, player.chunkCoordZ);
	}
}
