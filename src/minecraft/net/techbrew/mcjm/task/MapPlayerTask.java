package net.techbrew.mcjm.task;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.src.ChunkCoordIntPair;
import net.minecraft.src.ChunkCoordinates;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Minecraft;
import net.minecraft.src.World;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.data.DataCache;
import net.techbrew.mcjm.data.EntityKey;
import net.techbrew.mcjm.data.PlayerData;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.io.PropertyManager;
import net.techbrew.mcjm.io.nbt.ChunkLoader;
import net.techbrew.mcjm.model.ChunkStub;

public class MapPlayerTask extends BaseMapTask {
	
	private static final Logger logger = JourneyMap.getLogger();

	private static ChunkCoordinates lastPlayerPos;
	static Integer chunkOffset;
	
	private MapPlayerTask(World world, int dimension, boolean underground, Integer chunkY, Map<ChunkCoordIntPair, ChunkStub> chunkStubs) {
		super(world, dimension, underground, chunkY, chunkStubs, false);
	}
	
	public static BaseMapTask create(EntityPlayer player, long hash) {
				
		int missing = 0;

		if(chunkOffset==null) {
			chunkOffset = PropertyManager.getInstance().getInteger(PropertyManager.Key.CHUNK_OFFSET);
		}
		int offset = chunkOffset;
		
		final ChunkCoordinates playerPos = new ChunkCoordinates(player.chunkCoordX,player.chunkCoordY,player.chunkCoordZ);
		final Map playerData = DataCache.instance().get(PlayerData.class);
		final boolean underground = (Boolean) playerData.get(EntityKey.underground);
		final int dimension = (Integer) playerData.get(EntityKey.dimension);
		
		if(playerPos.equals(lastPlayerPos)) {
			offset = 1;
		}
		lastPlayerPos = playerPos;
		
		final int side = offset + offset + 1;
		final int capacity = (side*side) + (side*side)/4; // alleviates map growth
		final Map<ChunkCoordIntPair, ChunkStub> chunks = new HashMap<ChunkCoordIntPair, ChunkStub>(capacity);
		final World world = player.worldObj;
		
		final Integer chunkY = underground ? lastPlayerPos.posY : null;

		final ChunkCoordIntPair min = new ChunkCoordIntPair(lastPlayerPos.posX - offset, lastPlayerPos.posZ - offset);
		final ChunkCoordIntPair max = new ChunkCoordIntPair(lastPlayerPos.posX + offset, lastPlayerPos.posZ + offset);
		
		final File worldDir = FileHandler.getMCWorldDir(Minecraft.getMinecraft(), player.worldObj.provider.dimensionId);
 
		// First pass = chunks to map
		for(int x=min.chunkXPos;x<=max.chunkXPos;x++) {
			for(int z=min.chunkZPos;z<=max.chunkZPos;z++) {
				ChunkStub stub = ChunkLoader.getChunkStubFromMemory(x, z, worldDir, world, hash);
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
			return new MapPlayerTask(world, dimension, underground, chunkY, chunks);
		} else {
			return null;
		}
	
	}
	
	/**
	 * ITaskManager for MapPlayerTasks
	 * 
	 * @author mwoodman
	 *
	 */
	public static class Manager implements ITaskManager {
		
		boolean enabled;
		
		@Override
		public Class<? extends BaseMapTask> getTaskClass() {
			return MapPlayerTask.class;
		}
		
		@Override
		public boolean enableTask(Minecraft minecraft) {
			enabled = true;
			return enabled;
		}
		
		@Override
		public boolean isEnabled(Minecraft minecraft) {
			return enabled;
		}
		
		@Override
		public void disableTask(Minecraft minecraft) {
			enabled = false;
		}
		
		@Override
		public BaseMapTask getTask(Minecraft minecraft, long worldHash) {			
			if(!enabled) return null;
			BaseMapTask baseMapTask = MapPlayerTask.create(minecraft.thePlayer, worldHash);
			return baseMapTask;
		}
		
		@Override
		public void taskAccepted(boolean accepted) {
			// nothing to do
		}
		
	}
}
