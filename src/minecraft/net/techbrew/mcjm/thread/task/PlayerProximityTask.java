package net.techbrew.mcjm.thread.task;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.src.ChunkCoordIntPair;
import net.minecraft.src.ChunkCoordinates;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Minecraft;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.data.DataCache;
import net.techbrew.mcjm.data.EntityKey;
import net.techbrew.mcjm.data.PlayerData;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.io.PropertyManager;
import net.techbrew.mcjm.io.nbt.ChunkLoader;
import net.techbrew.mcjm.model.ChunkStub;

public class PlayerProximityTask extends UpdateThreadTask {
	
	private static final Logger logger = JourneyMap.getLogger();

	private static ChunkCoordinates lastPlayerPos;
	
	private PlayerProximityTask(int dimension, boolean underground, int chunkY, Map<ChunkCoordIntPair, ChunkStub> chunkStubs) {
		super(dimension, underground, chunkY, chunkStubs, false);
	}
	
	public static UpdateThreadTask create(Minecraft minecraft, long hash) {
				
		int missing = 0;

		// Stub surrounding chunks
		int offset = PropertyManager.getInstance().getInteger(PropertyManager.Key.CHUNK_OFFSET);
		if(lastPlayerPos==null) {
			lastPlayerPos = getPlayerPos(minecraft);
		} else {
			ChunkCoordinates playerPos = getPlayerPos(minecraft);
			if(playerPos.equals(lastPlayerPos)) {
				offset = 1;
			} else {
				lastPlayerPos = playerPos;
			}
		}
		final int side = offset + offset + 1;
		final int capacity = (side*side) + (side*side)/4; // alleviates map growth
		final Map<ChunkCoordIntPair, ChunkStub> chunks = new HashMap<ChunkCoordIntPair, ChunkStub>(capacity);
		final int dimension = minecraft.theWorld.provider.dimensionId;
		final boolean underground = (Boolean) DataCache.instance().get(PlayerData.class).get(EntityKey.underground);
		final int chunkY = underground ? minecraft.thePlayer.chunkCoordY : -1;

		final ChunkCoordIntPair min = new ChunkCoordIntPair(lastPlayerPos.posX - offset, lastPlayerPos.posZ - offset);
		final ChunkCoordIntPair max = new ChunkCoordIntPair(lastPlayerPos.posX + offset, lastPlayerPos.posZ + offset);
		
		final File worldDir = FileHandler.getMCWorldDir(Minecraft.getMinecraft());
 
		// First pass = chunks to map
		for(int x=min.chunkXPos;x<=max.chunkXPos;x++) {
			for(int z=min.chunkZPos;z<=max.chunkZPos;z++) {
				ChunkStub stub = ChunkLoader.getChunkStubFromMemory(x, z, worldDir, minecraft.theWorld, hash);
				if(stub!=null) {
					chunks.put(stub.getChunkCoordIntPair(), stub);
				} else {
					missing++;
				}
			}
		}				
		
		if(logger.isLoggable(Level.FINE)) {
			logger.fine("Chunks: " + missing + " skipped, " + chunks.size() + " used");
		}
		
		if(chunks.size()>0) {
			return new PlayerProximityTask(dimension, underground, chunkY, chunks);
		} else {
			return null;
		}
	
	}
	
	private static ChunkCoordinates getPlayerPos(Minecraft minecraft) {
		EntityPlayer player = minecraft.thePlayer;
		return new ChunkCoordinates(player.chunkCoordX, player.chunkCoordY, player.chunkCoordZ);
	}
}
