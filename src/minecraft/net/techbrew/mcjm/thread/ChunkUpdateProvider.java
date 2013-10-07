package net.techbrew.mcjm.thread;

import java.util.logging.Logger;

import net.minecraft.src.Minecraft;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.model.RegionCoord;
import net.techbrew.mcjm.thread.task.PlayerProximityTask;
import net.techbrew.mcjm.thread.task.RegionTask;

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
			
			if(ChunkUpdateThread.currentThread!=null) {
				return true;
			} else {
				logger.warning("ChunkUpdateThread.currentThread==null"); //$NON-NLS-1$ 
				return false;
			}				
			
		} else {
			if(threadLogging) logger.info("ChunkUpdateThread.getBarrier().getNumberWaiting()==" + ChunkUpdateThread.getBarrier().getNumberWaiting()); //$NON-NLS-1$ 
		}
		return false;
	}
	
	public void updateAroundPlayer(Minecraft minecraft, long worldHash) {		
		ChunkUpdateThread.currentThread.setTask(PlayerProximityTask.create(minecraft, worldHash));
		cleanupBarrier();
	}
	
	public void updateRegion(RegionCoord rCoord, Minecraft minecraft, long worldHash) {		
		ChunkUpdateThread.currentThread.setTask(RegionTask.create(rCoord, minecraft, worldHash));
		cleanupBarrier();
	}
	
	private void cleanupBarrier() {
		if(ChunkUpdateThread.getBarrier().getNumberWaiting()==1) {
			if(threadLogging) logger.info("Resetting barrier so ChunkUpdateThread can continue");
			ChunkUpdateThread.getBarrier().reset(); // Let the chunkthread continue
		}
	}

}
