package net.techbrew.mcjm.thread;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.src.ChunkCoordIntPair;
import net.minecraft.src.Minecraft;
import net.minecraft.src.World;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.io.PropertyManager;
import net.techbrew.mcjm.io.nbt.ChunkHelper;
import net.techbrew.mcjm.io.nbt.RegionLoader;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.model.ChunkCoord;
import net.techbrew.mcjm.model.ChunkImageCache;
import net.techbrew.mcjm.model.ChunkStub;
import net.techbrew.mcjm.model.RegionCoord;
import net.techbrew.mcjm.model.RegionImageCache;
import net.techbrew.mcjm.render.ChunkRenderController;

public class ChunkUpdateThread extends UpdateThreadBase {

	public volatile static ChunkUpdateThread currentThread = null;
	public volatile static boolean flushImagesToDisk = false;
	
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
	
	private volatile ConcurrentHashMap<ChunkCoordIntPair,ChunkStub> chunkStubs = new ConcurrentHashMap<ChunkCoordIntPair,ChunkStub>();
	private final ChunkImageCache chunkImageCache;
	private final ChunkRenderController renderController;
	
	private final Logger logger = JourneyMap.getLogger();
	
	public ChunkUpdateThread(JourneyMap journeyMap, World world) {
		super();
		chunkImageCache = new ChunkImageCache();
		chunkOffset = PropertyManager.getInstance().getInteger(PropertyManager.Key.CHUNK_OFFSET);
		renderController = new ChunkRenderController();
	}

	/**
	 * Map the chunks around the player.
	 */
	@Override
	protected void doTask() {
		
		JourneyMap jm = JourneyMap.getInstance();
		Minecraft mc = Minecraft.getMinecraft();
		boolean threadLogging = jm.isThreadLogging();
		CyclicBarrier barrier = getBarrier();
		
		try {
			currentThread = this;
									
			// Wait for main thread to make ChunkStubs available
			try {			
				if(threadLogging) logger.info("Waiting... barrier: " + barrier.getNumberWaiting()); //$NON-NLS-1$
				barrier.await();		
				
			} catch(BrokenBarrierException e) {
				
				if(threadLogging) logger.info("Barrier Broken: " + barrier.getNumberWaiting()); //$NON-NLS-1$					
				//barrier.reset();
				
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
			
			// Clear cache
			if(!chunkImageCache.isEmpty()) {
				logger.warning("chunkImageCache isn't empty.  This is likely a bug.");
				chunkImageCache.clear();
			}			
			
			// Map the chunks
			Iterator<ChunkStub> iter = chunkStubs.values().iterator();
			while(iter.hasNext()) {
				ChunkStub chunkStub = iter.next();
				if(!jm.isMapping()) {
					if(threadLogging) logger.info("JourneyMap isn't mapping, aborting mapChunk()"); //$NON-NLS-1$
					return;
				}
				if(chunkStub.doMap) {
					mapChunk(mc, chunkStub, underground, playerChunkY);
				}
			}
			
			if(!jm.isMapping()) {
				if(threadLogging) logger.info("JM isn't mapping, Interupting ChunkUpdateThread.");			 //$NON-NLS-1$
				return;
			}
	
			// Push chunk cache to region cache			
			int chunks = chunkImageCache.getEntries().size();
			RegionImageCache.getInstance().putAll(chunkImageCache.getEntries(), flushImagesToDisk);
								
			if(threadLogging) {
				logger.info("Mapped " + chunks + " chunks in " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-start) + "ms with flush:" + flushImagesToDisk); //$NON-NLS-1$
			}
			
		} finally {
			chunkStubs.clear();
			chunkImageCache.clear();
			currentThread = null;				
		}
		
	}
	
	/**
	 * WARNING: Should be called by main Minecraft thread only to ensure the world's chunkloader
	 * doesn't load/unload while we're trying to read chunks.
	 * 
	 * @param playerChunk
	 * @param theWorld
	 * @param hash
	 * @return
	 */
	public int[] updateChunksAroundPlayer(ChunkStub playerChunk, World theWorld, long hash) {
		
		HashMap<ChunkCoordIntPair,ChunkStub> tempChunkStubs = new HashMap<ChunkCoordIntPair,ChunkStub>();
		int missing = 0;

		// Stub surrounding chunks
		int offset = chunkOffset;
		if (offset>0 && playerChunkX == lastChunkX && playerChunkZ==lastChunkZ) {
			offset=1;
		}
		
		lastChunkX = playerChunkX;
		lastChunkY = playerChunkY;
		lastChunkZ = playerChunkZ;

		ChunkCoordIntPair min = new ChunkCoordIntPair(playerChunkX - offset, playerChunkZ - offset);
		ChunkCoordIntPair max = new ChunkCoordIntPair(playerChunkX + offset, playerChunkZ + offset);
		
		File worldDir = RegionLoader.getWorldDirectory(Minecraft.getMinecraft());
 
		// First pass = chunks to map
		for(int x=min.chunkXPos;x<=max.chunkXPos;x++) {
			for(int z=min.chunkZPos;z<=max.chunkZPos;z++) {
				//Chunk chunk = Utils.getChunkIfAvailable(theWorld, x, z);
				//if(chunk!=null) {
					//ChunkStub stub = new ChunkStub(chunk, true, theWorld, hash); // do Map
					ChunkStub stub = ChunkHelper.getChunkStub(new ChunkCoordIntPair(x,z), worldDir, theWorld, hash);
					if(stub!=null) {
						tempChunkStubs.put(new ChunkCoordIntPair(x,z), stub);
					} else {
						missing++;
					}
			   //}
			}
		}
				
		if(tempChunkStubs.size()>0) {
			chunkStubs.putAll(tempChunkStubs);
			// Second pass = bordering chunks needed for heightmaps
			//addPerimeterStubs(min,max,null,theWorld, hash, false);
		}			
		
		if(logger.isLoggable(Level.FINE)) {
			logger.fine("Chunks: " + missing + " skipped, " + tempChunkStubs.size() + " used");
		}
		
		flushImagesToDisk = false;

		return new int[]{tempChunkStubs.size(), missing};
	}
	
//	public int addRegion(RegionCoord rCoord, IChunkLoader chunkLoader, World theWorld, long hash, boolean flushToDisk) {
//		
//		Collection<ChunkCoord> cCoordList = rCoord.getChunkCoordsInRegion();
//		Iterator<ChunkCoord> iter = cCoordList.iterator();
//		while(iter.hasNext()) {
//			ChunkCoord cCoord = iter.next();
//			try {
//				Chunk chunk = chunkLoader.loadChunk(theWorld, cCoord.chunkX, cCoord.chunkZ);
//				if(chunk==null) {
//					chunk = new Chunk(theWorld, cCoord.chunkX, cCoord.chunkZ);
//				}
//				if(!chunk.isChunkLoaded) {
//					chunk.populateChunk(theWorld.getChunkProvider(), theWorld.getChunkProvider(), cCoord.chunkX, cCoord.chunkZ);
//				}	
//				ChunkStub stub = new ChunkStub(chunk, true, theWorld, hash);
//				chunkStubs.put(stub.hashCode(), stub);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			
//		}
//		if(flushToDisk) {
//			RegionImageCache.getInstance().flushToDisk();
//		}
//		
//		return chunkStubs.size();
//	}
	
	/**
	 * WARNING: Should be called by main Minecraft thread only to ensure the world's chunkloader
	 * doesn't I/O compete with the chunkloader provided.
	 * 
	 * @param chunkStubIter
	 * @param chunkLoader
	 * @param theWorld
	 * @param hash
	 * @return
	 */
	public int updateFromIterator(Iterator<ChunkStub> chunkStubIter, int limit) {
		
		if(!chunkStubs.isEmpty()) {
			logger.warning("ChunkStubs isn't empty. This is likely a bug.  Aborting.");
			return 0;
		}
		
		int nullStubs = 0;
		HashMap<ChunkCoordIntPair,ChunkStub> tempChunkStubs = new HashMap<ChunkCoordIntPair,ChunkStub>(limit);
		
		// First pass

		while(chunkStubIter.hasNext()) {
			if(tempChunkStubs.size()+nullStubs==limit) break;
			ChunkStub stub = chunkStubIter.next();
			if(stub!=null) {
				tempChunkStubs.put(new ChunkCoordIntPair(stub.xPosition, stub.zPosition), stub);
			} else {
				nullStubs++;
			}
		}
		
		chunkStubs.putAll(tempChunkStubs);		
		
		int total = tempChunkStubs.size() + nullStubs;
		logger.info("Got stubs: " + tempChunkStubs.size() + ", nulls: " + nullStubs + ", TOTAL: " + total);
		tempChunkStubs.clear();
		tempChunkStubs = null;
		
		flushImagesToDisk = true;

		return chunkStubs.size();
	}
	
	/**
	 * WARNING: Should be called by main Minecraft thread only to ensure the world's chunkloader
	 * doesn't I/O compete with the chunkloader provided.
	 * 
	 * @param chunkStubIter
	 * @param chunkLoader
	 * @param theWorld
	 * @param hash
	 * @return
	 */
	public int updateRegion(RegionCoord rCoord, File worldDir, World world, long worldHash) {
		
		if(!chunkStubs.isEmpty()) {
			logger.warning("ChunkStubs isn't empty. This is likely a bug.  Aborting.");
			return 0;
		}
		
		int nullStubs = 0;
		HashMap<ChunkCoordIntPair,ChunkStub> tempChunkStubs = new HashMap<ChunkCoordIntPair,ChunkStub>(1024);
		
		List<ChunkCoordIntPair> coords = rCoord.getChunkCoordsInRegion();
		while(!coords.isEmpty()) {
			ChunkCoordIntPair coord = coords.remove(0);
			ChunkStub stub = ChunkHelper.getChunkStub(coord, worldDir, world, worldHash);
			if(stub==null) {
				nullStubs++;
			} else {
				tempChunkStubs.put(coord, stub);
			}
		}		
		chunkStubs.putAll(tempChunkStubs);		
		
		int total = tempChunkStubs.size() + nullStubs;
		if(logger.isLoggable(Level.FINE)) {
			logger.fine("Got stubs: " + tempChunkStubs.size() + ", nulls: " + nullStubs + ", TOTAL: " + total);
		}
		tempChunkStubs.clear();
		tempChunkStubs = null;
		
		flushImagesToDisk = true;

		return chunkStubs.size();
	}
	
//	/**
//	 * 
//	 * @param min
//	 * @param max
//	 * @param chunkSource
//	 * @param theWorld
//	 * @param hash
//	 * @param doMap
//	 * @return
//	 */
//	public int addPerimeterStubs(ChunkCoordinates min, ChunkCoordinates max, IChunkLoader chunkLoader, World theWorld, long hash, boolean doMap) {
//		
//		boolean threadLogging = true; // JourneyMap.getInstance().isThreadLogging(); // TODO
//		
//		IChunkProvider chunkProvider = theWorld.getChunkProvider();
//		
//		HashMap<ChunkCoordIntPair,ChunkStub> tempChunkStubs = new HashMap<ChunkCoordIntPair,ChunkStub>();		
//		int innerX = min.posX-1;
//		int outerX = max.posX+1;
//		int innerZ = min.posZ-1;
//		int outerZ = max.posZ+1;
//		
//		int forceLoaded = 0;
//				
//		outer : for(int x = innerX;x<=outerX;x++) {
//			for(int z=innerZ;z<=outerZ;z++) {
//				if(x>innerX && x<outerX && z>innerZ && z<outerZ) {
//					z = outerZ; // skip empty middle
//				}
//				if((x==innerX || x==outerX || z==innerZ || z==outerZ)) {
//					Chunk chunk = null;
//					try {
//						if(chunkLoader!=null) {
//							chunk = chunkLoader.loadChunk(theWorld,x,z);
//						} 
//						if(chunk==null) {
//							if(chunkProvider.chunkExists(x, z)) {
//								chunk = chunkProvider.loadChunk(x,z);
//								if(chunkLoader!=null && chunk!=null) {
//									forceLoaded++;
//								}
//							}
//						}
//						if(chunk!=null) {					
//							ChunkStub stub = new ChunkStub(chunk, false, theWorld, hash); // do not Map								
//							tempChunkStubs.put(stub.hashCode(), stub);
//							if(threadLogging) logger.fine("Peripheral Chunk added: " + x + "," + z);
//						} else {
//							if(threadLogging) logger.info("Peripheral Chunk not loaded: " + x + "," + z);							
//						}
//					} catch (IOException e) {
//						logger.warning("Error loading peripheral chunk: " + x + "," + z);
//						break outer;
//					}
//				}
//			}
//		}
//		
//		int expectedChunks = Math.abs(((outerX-innerX+1)*(outerZ-innerZ+1)) - ((max.posX-min.posX+1)*(max.posZ-min.posZ+1)));
//		int missing = expectedChunks-tempChunkStubs.size();
//		chunkStubs.putAll(tempChunkStubs);
//		if(threadLogging) logger.info("Expected " + expectedChunks +" periphery chunks, got: " + tempChunkStubs.size() + ", forceLoaded: " + forceLoaded);
//
//		return missing;
//	}

	/**
	 * Map chunk to image cache
	 * @param world			The world
	 * @param chunkStub			The chunk
	 * @param underground	Do cave map image
	 * @param chunkY		ChunkY (ignored if not underground)
	 */
	private void mapChunk(Minecraft minecraft, ChunkStub chunkStub, boolean underground, int chunkY) {
				
		BufferedImage chunkImage = renderController.getChunkImage(chunkStub, underground, chunkY, chunkStubs);		
		if(chunkImage!=null && minecraft.theWorld!=null) {
			File worldDir = FileHandler.getWorldDir(minecraft);
			Constants.CoordType cType = Constants.CoordType.convert(underground, minecraft.theWorld.provider.dimensionId);
			ChunkCoord cCoord = ChunkCoord.fromChunkStub(worldDir, chunkStub, chunkY, cType);
			chunkImageCache.put(cCoord, chunkImage);			
		} else {
			if(logger.isLoggable(Level.FINE)) {
				logger.fine("Could not render chunk image:" + chunkStub.xPosition + "," + chunkStub.zPosition + " at " + chunkY + " and underground = " + underground); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}
		}
		
	}

}
