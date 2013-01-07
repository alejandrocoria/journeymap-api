package net.techbrew.mcjm.thread;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Chunk;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerSP;
import net.minecraft.src.ModLoader;
import net.minecraft.src.World;
import net.techbrew.mcjm.ChunkStub;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.PropertyManager;
import net.techbrew.mcjm.Utils;
import net.techbrew.mcjm.io.ChunkCoord;
import net.techbrew.mcjm.io.ChunkFileHandler;
import net.techbrew.mcjm.io.ChunkImageCache;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.io.RegionCoord;
import net.techbrew.mcjm.io.RegionFileHandler;
import net.techbrew.mcjm.io.RegionImageCache;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.render.ChunkRenderer;

public class ChunkUpdateThread extends UpdateThreadBase {

	public volatile static CyclicBarrier barrier = new CyclicBarrier(2);
	public volatile static ChunkUpdateThread currentThread = null;
	
	// Last player position
	public volatile static int lastChunkX = -1;
	public volatile static int lastChunkY = -1;
	public volatile static int lastChunkZ = -1;
	
	public static CyclicBarrier getBarrier() {
		return barrier;
	}
	
	private volatile ConcurrentHashMap<Integer,ChunkStub> chunkStubs = new ConcurrentHashMap<Integer,ChunkStub>();
	private ChunkImageCache chunkImageCache;
	
	public ChunkUpdateThread(JourneyMap journeyMap, World world) {
		super(journeyMap, world);
		chunkImageCache = new ChunkImageCache();
	}

	/**
	 * Map the chunks around the player.
	 */
	protected void doTask() {
		try {
				Boolean flush = true;
				currentThread = this;
				
				// Wait for main thread to make ChunkStubs available
				try {					
					JourneyMap.getLogger().finer("Waiting... barrier: " + barrier.getNumberWaiting()); //$NON-NLS-1$

					barrier.await();		
				} catch(BrokenBarrierException e) {
					barrier.reset();
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					JourneyMap.getLogger().warning(LogFormatter.toString(e));
					barrier.reset();
					return;
				}
				
				JourneyMap.getLogger().finer("Continuing..."); //$NON-NLS-1$
				
				// If there aren't ChunkStubs, we're done.
				if(chunkStubs.isEmpty()) {
					if(JourneyMap.getLogger().isLoggable(Level.FINER)) {
						JourneyMap.getLogger().finer("No ChunkStubs to process"); //$NON-NLS-1$
					}
					return;
				}
				
				long start = System.currentTimeMillis();
				
				// Clear cache (shouldn't be necessary)
				chunkImageCache.clear();
				
				Iterator<ChunkStub> iter = chunkStubs.values().iterator();
				while(iter.hasNext()) {
					ChunkStub chunkStub = iter.next();
					if(chunkStub.doMap) {
						mapChunk(chunkStub, underground, playerChunkY);
					}
				}
				
				if(!JourneyMap.isRunning()) {
					JourneyMap.getLogger().warning("Interupting ChunkUpdateThread.");			 //$NON-NLS-1$
					return;
				}
		
				// Push chunk cache to region cache
				if(JourneyMap.getLogger().isLoggable(Level.FINE)) {
					JourneyMap.getLogger().fine("Chunks updated: " + chunkImageCache.getEntries().size()); //$NON-NLS-1$
				}
				RegionImageCache.getInstance().putAll(chunkImageCache.getEntries());
		
				// Flush regions to disk
				if(flush) {
					RegionImageCache.getInstance().flushToDisk();
				}
				if(JourneyMap.getLogger().isLoggable(Level.FINER)) {		
					JourneyMap.getLogger().finer("Map regions updated: " + (System.currentTimeMillis()-start) + "ms elapsed"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			} finally {
				chunkStubs.clear();
				chunkImageCache.clear();
				currentThread = null;
				if(JourneyMap.getLogger().isLoggable(Level.FINEST)) {
					long total = Runtime.getRuntime().totalMemory()/1024/1024;
					long free = Runtime.getRuntime().freeMemory()/1024/1024;
					long used = total-free;
					JourneyMap.getLogger().finest("Memory: total/free/used= " + total + " / " + free + " / " + used + " MB"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				}
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
		
		// First Pass				
		if (playerChunkX != lastChunkX || playerChunkZ!=lastChunkZ || playerChunkY != lastChunkY) {
			
			// Player has moved
			lastChunkX = playerChunkX;
			lastChunkY = playerChunkY;
			lastChunkZ = playerChunkZ;

			// Stub surrounding chunks
			int offset = journeyMap.getChunkOffset();
			if(offset>2) {
				if(underground) {
					offset--;
				}
			}

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
								unloadedChunks += ensureNeighbors(stub);
								continue;
							} 
						}
						unloadedChunks++;						
					}
				}
			}
			
		} else {
			// Just stub the current chunk
			if(playerChunk!=null) {
				ChunkStub stub = new ChunkStub(JourneyMap.getLastPlayerChunk());
				tempChunkStubs.put(stub.hashCode(), stub);
				unloadedChunks += ensureNeighbors(stub);
			} else {
				JourneyMap.getLogger().warning("Unexpected state: Null playerChunk.");
			}
		}
		
		chunkStubs.putAll(tempChunkStubs);

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
							ChunkStub neighborStub = new ChunkStub(chunk, false, minecraft.theWorld, journeyMap.lastHash); // do not map
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
		
		if(!JourneyMap.isRunning()) {
			JourneyMap.getLogger().warning("Interupting ChunkUpdateThread.mapChunk()"); //$NON-NLS-1$
			return;
		}
		
		Minecraft minecraft = Minecraft.getMinecraft();
		BufferedImage chunkImage = ChunkRenderer.getChunkImage(chunkStub, underground, chunkY, chunkStubs);
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
