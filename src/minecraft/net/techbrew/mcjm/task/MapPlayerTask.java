package net.techbrew.mcjm.task;

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
import net.techbrew.mcjm.io.PropertyManager;
import net.techbrew.mcjm.io.nbt.ChunkLoader;
import net.techbrew.mcjm.model.ChunkMD;

public class MapPlayerTask extends BaseMapTask {
	
	private static final Logger logger = JourneyMap.getLogger();

	private static ChunkMD.Set lastChunkStubs = new ChunkMD.Set(512);
	private static ChunkCoordinates lastPlayerPos;
	private static Boolean lastUnderground;
	static Integer chunkOffset;
	
	private MapPlayerTask(World world, int dimension, boolean underground, Integer chunkY, ChunkMD.Set chunkStubs) {
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
		final boolean underground = (Boolean) playerData.get(EntityKey.underground) && PropertyManager.getInstance().getBoolean(PropertyManager.Key.PREF_SHOW_CAVES);
		final int dimension = (Integer) playerData.get(EntityKey.dimension);
		
		if(lastUnderground==null) lastUnderground = underground;
		if(lastPlayerPos==null) lastPlayerPos = playerPos;
		
		boolean skipUnchanged = (lastUnderground==underground);
		if(skipUnchanged && underground) {
			skipUnchanged = (playerPos.posY==lastPlayerPos.posY);
		}
		
		if(lastPlayerPos.equals(playerPos)) {
			if(offset>=2) {
				offset = offset/2;
			}
		}
		
		lastPlayerPos = playerPos;
		lastUnderground = underground;
		
		final int side = offset + offset + 1;
		final int capacity = (side*side) + (side*side)/4; // alleviates map growth
		final ChunkMD.Set chunks = new ChunkMD.Set(capacity);
		final World world = player.worldObj;
		
		final Integer chunkY = underground ? lastPlayerPos.posY : null;

		final ChunkCoordIntPair min = new ChunkCoordIntPair(lastPlayerPos.posX - offset, lastPlayerPos.posZ - offset);
		final ChunkCoordIntPair max = new ChunkCoordIntPair(lastPlayerPos.posX + offset, lastPlayerPos.posZ + offset);
		
		// Get chunkstubs to map
		for(int x=min.chunkXPos;x<=max.chunkXPos;x++) {
			for(int z=min.chunkZPos;z<=max.chunkZPos;z++) {
				ChunkMD stub = ChunkLoader.getChunkStubFromMemory(x, z, world);
				if(stub!=null) {
					stub.render = true;
					chunks.add(stub);
				} else {
					missing++;
				}
			}
		}			
		
		int initialSize = chunks.size();
		
		// Remove unchanged chunkstubs from last task
		if(!lastChunkStubs.isEmpty()) {
			ChunkMD.Set removed = new ChunkMD.Set(64);
			for(ChunkMD oldChunk : lastChunkStubs) {	
				
				if(!chunks.containsKey(oldChunk.coord)) {					
					if(oldChunk.discard(1)>4) {
						if(logger.isLoggable(Level.FINE)) {
							logger.fine("Discarding out-of-range chunk: " + oldChunk);
						}
						removed.add(oldChunk);
					}
					continue;
				} else {
					oldChunk.discard(-1);
				}
				
				ChunkMD newChunk = chunks.get(oldChunk.coord);
				
				if(skipUnchanged) {
					if(newChunk.coord.chunkXPos!=lastPlayerPos.posX || newChunk.coord.chunkZPos!=lastPlayerPos.posZ) {						
						if(newChunk.isUnchanged(oldChunk)) {						
							newChunk.render=false;
						}
					}		
				}
				
				if(newChunk.render==true) {
					if(logger.isLoggable(Level.FINE)) {
						logger.fine("Mapping chunk: " + newChunk);
					}
				}
			}
			
			lastChunkStubs.keySet().removeAll(removed.keySet());
		}
		
		if(logger.isLoggable(Level.FINE)) {
			logger.fine("Chunks in set: " + chunks.size() + ".  lastChunkStubs=" + lastChunkStubs.size());
		}

		return new MapPlayerTask(world, dimension, underground, chunkY, chunks);
	
	}
	
	public static void clearCache() {
		lastChunkStubs.clear();
		lastPlayerPos = null;
		lastUnderground = null;
		chunkOffset = null;
	}

	@Override
	public void taskComplete() {
		lastChunkStubs.putAll(chunkMdSet);
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
		public boolean enableTask(Minecraft minecraft, Object params) {
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
				
			// Ensure player chunk is loaded
			if(minecraft.thePlayer.addedToChunk) {
				BaseMapTask baseMapTask = MapPlayerTask.create(minecraft.thePlayer, worldHash);
				return baseMapTask;
			} else {
				return null;
			}
		}
		
		@Override
		public void taskAccepted(boolean accepted) {
			// nothing to do
		}
		
	}
}
