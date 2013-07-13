package net.techbrew.mcjm.thread;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.src.Minecraft;
import net.minecraft.src.EntityPlayerSP;
import net.minecraft.src.World;
import net.minecraft.src.Chunk;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.Utils;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.io.PropertyManager;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.model.ChunkCoord;
import net.techbrew.mcjm.model.ChunkImageCache;
import net.techbrew.mcjm.model.ChunkStub;
import net.techbrew.mcjm.model.RegionImageCache;
import net.techbrew.mcjm.render.ChunkRenderController;

public class ChunkUpdateThread extends UpdateThreadBase {

	public volatile static ChunkUpdateThread currentThread = null;
	
	// Last player position
	public volatile static int lastChunkX = -1;
	public volatile static int lastChunkY = -1;
	public volatile static int lastChunkZ = -1;
	public volatile static int chunkOffset = 2;
	
	private static class BarrierHolder {
        private static final CyclicBarrier INSTANCE = new CyclicBarrier(2);
    }

    public static CyclicBarrier getBarrier() {
        return BarrierHolder.INSTANCE;
    }
	
	private volatile ConcurrentHashMap<Integer,ChunkStub> chunkStubs = new ConcurrentHashMap<Integer,ChunkStub>();
	private ChunkImageCache chunkImageCache;
	private ChunkRenderController renderController;
	
	public ChunkUpdateThread(JourneyMap journeyMap, World world) {
		super();
		chunkImageCache = new ChunkImageCache();
		chunkOffset = PropertyManager.getInstance().getInteger(PropertyManager.Key.CHUNK_OFFSET);
		renderController = new ChunkRenderController();
	}

	/**
	 * Map the chunks around the player.
	 */
	protected void doTask() {
		
		JourneyMap jm = JourneyMap.getInstance();
		boolean threadLogging = jm.isThreadLogging();
		Logger logger = JourneyMap.getLogger();
		CyclicBarrier barrier = getBarrier();
		
		try {
			Boolean flush = false;
			currentThread = this;
									
			// Wait for main thread to make ChunkStubs available
			try {			
				if(threadLogging) logger.info("Waiting... barrier: " + barrier.getNumberWaiting()); //$NON-NLS-1$
				barrier.await();		
				
			} catch(BrokenBarrierException e) {
				
				if(threadLogging) logger.info("Barrier Broken: " + barrier.getNumberWaiting()); //$NON-NLS-1$					
				barrier.reset();
				
			} catch (Throwable e) {
				
				logger.warning("Aborting: " + LogFormatter.toString(e));
				barrier.reset();
				return;
			}
			
			if(threadLogging) logger.info("Barrier done waiting: " + barrier.getNumberWaiting()); //$NON-NLS-1$
			
			// If there aren't ChunkStubs, we're done.
			if(chunkStubs.isEmpty()) {
				if(threadLogging) logger.info("No ChunkStubs to process"); //$NON-NLS-1$
				return;
			}
			
			long start = System.nanoTime();
			
			// Clear cache (precaution only)
			chunkImageCache.clear();
			
			Iterator<ChunkStub> iter = chunkStubs.values().iterator();
			while(iter.hasNext()) {
				ChunkStub chunkStub = iter.next();
				if(chunkStub.doMap) {
					mapChunk(chunkStub, underground, playerChunkY);
				}
			}
			
			if(!JourneyMap.getInstance().isMapping()) {
				if(threadLogging) logger.info("JM isn't mapping, Interupting ChunkUpdateThread.");			 //$NON-NLS-1$
				return;
			}
	
			// Push chunk cache to region cache
			if(threadLogging) {
				logger.info("Chunks updated: " + chunkImageCache.getEntries().size()); //$NON-NLS-1$
			}
			
			RegionImageCache.getInstance().putAll(chunkImageCache.getEntries());
	
			// Flush regions to disk
			if(flush) {
				logger.info("Force-flushing RegionImageCache");
				RegionImageCache.getInstance().flushToDisk();
			}
			if(threadLogging) {		
				logger.info("Map regions updated: " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-start) + "ms elapsed"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			
		} finally {
			chunkStubs.clear();
			currentThread = null;				
		}
		
	}
	
	/**
	 * Should be called by main Minecraft thread only
	 * @param hash 
	 * @param theWorld 
	 * @param thePlayer 
	 */
	public int[] fillChunkStubs(EntityPlayerSP thePlayer, ChunkStub playerChunk, World theWorld, long hash) {
		
		HashMap<Integer,ChunkStub> tempChunkStubs = new HashMap<Integer,ChunkStub>();
		int unloadedChunks = 0;

		// Stub surrounding chunks
		int offset = chunkOffset;
		if (offset>0 && playerChunkX == lastChunkX && playerChunkZ==lastChunkZ) {
			offset=1;
		}
		
		lastChunkX = playerChunkX;
		lastChunkY = playerChunkY;
		lastChunkZ = playerChunkZ;

		int startX = playerChunkX - offset;
		int endX = playerChunkX + offset;
		int startZ = playerChunkZ - offset;
		int endZ = playerChunkZ + offset;
 
		// First pass = chunks to map
		for(int x = startX;x<=endX;x++) {
			for(int z=startZ;z<=endZ;z++) {
				if(theWorld.getChunkProvider().chunkExists(x,z)) {
					Chunk chunk = Utils.getChunkIfAvailable(theWorld, x, z);
					if(chunk!=null) {
						ChunkStub stub = new ChunkStub(chunk, true, theWorld, hash); // doMap
						tempChunkStubs.put(stub.hashCode(), stub);
						continue;
					} 
				}
				unloadedChunks++;
			}
		}
		
		// Second pass = bordering chunks needed for heightmaps
		for(int x = startX;x<=endX;x++) {
			for(int z=startZ;z<=endZ;z++) {
				if(x==startX || x==endX || z==startZ || z==endZ) {
					if(theWorld.getChunkProvider().chunkExists(x,z)) {
						Chunk chunk = Utils.getChunkIfAvailable(theWorld, x, z);
						if(chunk!=null) {					
							ChunkStub stub = new ChunkStub(chunk, false, theWorld, hash); // do not Map
							//unloadedChunks += ensureNeighbors(stub);
							continue;
						} 
					}
					unloadedChunks++;						
				}
			}
		}
		
		chunkStubs.putAll(tempChunkStubs);
		
		if(JourneyMap.getLogger().isLoggable(Level.FINE)) {
			JourneyMap.getLogger().fine("Chunks: " + unloadedChunks + " skipped, " + tempChunkStubs.size() + " used");
		}

		return new int[]{tempChunkStubs.size(), unloadedChunks};
	}
	

	private int ensureNeighbors(ChunkStub stub) {
		int unloadedChunks = 0;
		int offset = 1;
		int startX = stub.xPosition - offset;
		int endX = stub.xPosition + offset;
		int startZ = stub.zPosition - offset;
		int endZ = stub.zPosition + offset;
 
		Minecraft minecraft = Minecraft.getMinecraft();
		for(int x = startX;x<=endX;x++) {
			for(int z=startZ;z<=endZ;z++) {
				if(minecraft.theWorld.getChunkProvider().chunkExists(x, z)) {
					Chunk chunk = Utils.getChunkIfAvailable(minecraft.theWorld, x, z);
					if(chunk!=null) {										
						if(!chunkStubs.containsKey(ChunkStub.toHashCode(x, z))) {
							ChunkStub neighborStub = new ChunkStub(chunk, false, minecraft.theWorld, FileHandler.lastWorldHash); // do not map
							chunkStubs.put(neighborStub.hashCode(), neighborStub);
							continue;
						}
					} 
				}
				unloadedChunks++;
				stub.doMap = false;
			}
		}
		return unloadedChunks;
	}
	
	

	/**
	 * Map chunk to image cache
	 * @param world			The world
	 * @param chunkStub			The chunk
	 * @param underground	Do cave map image
	 * @param chunkY		ChunkY (ignored if not underground)
	 */
	private void mapChunk(ChunkStub chunkStub, boolean underground, int chunkY) {
		
		if(!JourneyMap.getInstance().isMapping()) {
			JourneyMap.getLogger().warning("JM isn't mapping, aborting"); //$NON-NLS-1$
			return;
		}
		
		Minecraft minecraft = Minecraft.getMinecraft();
		BufferedImage chunkImage = renderController.getChunkImage(chunkStub, underground, chunkY, chunkStubs);

		
		if(chunkImage!=null  && minecraft.theWorld!=null) {
			File worldDir = FileHandler.getWorldDir(minecraft);
			Constants.CoordType cType = Constants.CoordType.convert(underground, minecraft.theWorld.provider.dimensionId);
			ChunkCoord cCoord = ChunkCoord.fromChunkStub(worldDir, chunkStub, chunkY, cType);
			chunkImageCache.put(cCoord, chunkImage);
			
		} else {
			if(JourneyMap.getLogger().isLoggable(Level.FINE)) {
				JourneyMap.getLogger().fine("Could not render chunk image:" + chunkStub.xPosition + "," + chunkStub.zPosition + " at " + chunkY + " and underground = " + underground); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}
		}
	}

}
