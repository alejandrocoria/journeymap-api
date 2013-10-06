package net.techbrew.mcjm.thread;

import java.io.File;
import java.util.logging.Logger;

import net.minecraft.src.Minecraft;
import net.minecraft.src.World;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.model.ChunkStub;
import net.techbrew.mcjm.model.RegionCoord;

public class ChunkUpdateProvider {

	final Logger logger = JourneyMap.getLogger();
	final boolean threadLogging = JourneyMap.getInstance().isThreadLogging();
	
	public boolean isReady() {
		
		// Check for broken barrier
		if(ChunkUpdateThread.getBarrier().isBroken()) {
			if(threadLogging) logger.warning("Resetting broken Barrier");
			ChunkUpdateThread.getBarrier().reset();
		}
		
		// Populate ChunkStubs on ChunkUpdateThread if it is waiting
		if(ChunkUpdateThread.getBarrier().getNumberWaiting()==1) {
			
			//if(threadLogging) logger.info("ChunkUpdateThread is waiting for fillChunkStubs");
				
			if(ChunkUpdateThread.currentThread!=null) {
				return true;
			} else {
				if(threadLogging) logger.warning("ChunkUpdateThread.currentThread==null"); //$NON-NLS-1$ 
			}				
			
		} else {
			if(threadLogging) logger.info("ChunkUpdateThread.getBarrier().getNumberWaiting()==" + ChunkUpdateThread.getBarrier().getNumberWaiting()); //$NON-NLS-1$ 
		}
		return false;
	}
	
	public void updateAroundPlayer(Minecraft minecraft, ChunkStub lastPlayerChunk) {		
		synchronized(ChunkUpdateThread.currentThread) {
			long start = System.currentTimeMillis();
			int[] result = ChunkUpdateThread.currentThread.updateChunksAroundPlayer(lastPlayerChunk, minecraft.theWorld, FileHandler.lastWorldHash);
			long stop = System.currentTimeMillis();
			if(threadLogging) logger.info("Stubbed/skipped: " + result[0] + "," + result[1] + " in " + (stop-start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
		cleanupBarrier();
	}
	
	public void updateRegion(RegionCoord rCoord, File worldDir, World world, long worldHash) {		
		synchronized(ChunkUpdateThread.currentThread) {
			long start = System.currentTimeMillis();
			int result = ChunkUpdateThread.currentThread.updateRegion(rCoord, worldDir, world, worldHash);
			long stop = System.currentTimeMillis();
			//if(threadLogging) logger.info("Filled: " + result + " in " + (stop-start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
		cleanupBarrier();
	}
	
//	public void updateRegion(RegionCoord rCoord, IChunkLoader chunkLoader, World theWorld, long hash, boolean flushToDisk) {		
//		synchronized(ChunkUpdateThread.currentThread) {
//			long start = System.currentTimeMillis();
//			int result = ChunkUpdateThread.currentThread.addRegion(rCoord, chunkLoader, theWorld, hash, true);
//			long stop = System.currentTimeMillis();
//			if(threadLogging) logger.info("Filled: " + result + " in " + (stop-start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
//		}
//		cleanupBarrier();
//	}
	
	private void cleanupBarrier() {
		if(ChunkUpdateThread.getBarrier().getNumberWaiting()==1) {
			if(threadLogging) logger.info("Resetting barrier so ChunkUpdateThread can continue");
			ChunkUpdateThread.getBarrier().reset(); // Let the chunkthread continue
		}
	}

}
