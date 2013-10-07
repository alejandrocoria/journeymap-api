package net.techbrew.mcjm.thread;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.src.ChunkCoordIntPair;
import net.minecraft.src.EntityClientPlayerMP;
import net.minecraft.src.Minecraft;
import net.minecraft.src.World;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.model.ChunkCoord;
import net.techbrew.mcjm.model.ChunkImageCache;
import net.techbrew.mcjm.model.ChunkStub;
import net.techbrew.mcjm.model.RegionImageCache;
import net.techbrew.mcjm.render.ChunkRenderController;
import net.techbrew.mcjm.thread.task.UpdateThreadTask;

public class ChunkUpdateThread implements Runnable {

	public volatile static ChunkUpdateThread currentThread = null;
	
	private static class BarrierHolder {
        private static final CyclicBarrier INSTANCE = new CyclicBarrier(2);
    }

    public static CyclicBarrier getBarrier() {
        return BarrierHolder.INSTANCE;
    }
	
	private volatile UpdateThreadTask task;
	private final ChunkImageCache chunkImageCache;
	private final ChunkRenderController renderController;
	
	private final Logger logger = JourneyMap.getLogger();
	
	public ChunkUpdateThread(JourneyMap journeyMap, World world) {
		super();
		chunkImageCache = new ChunkImageCache();
		
		renderController = new ChunkRenderController();
	}
	
	public void setTask(UpdateThreadTask newTask) {
		synchronized (this) {
			if(task==null) {
				task = newTask;
			} else {
				throw new IllegalStateException("Task already set");
			}
		}
	}
	
	@Override
	public final void run() {
		
		currentThread = this;
		
		final JourneyMap jm = JourneyMap.getInstance();
		final Minecraft mc = Minecraft.getMinecraft();
		final boolean threadLogging = jm.isThreadLogging();
		final CyclicBarrier barrier = getBarrier();

		try {
			
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
			
			// If there isn't a task, we're done.
			if(task==null) {
				if(threadLogging) logger.info("No task to process"); //$NON-NLS-1$
				return;
			}
			
			// Bail if needed
			if(!jm.isMapping()) {
				jm.getLogger().fine("JM not mapping, aborting");
				return;
			}
						
			// Check player status
			final EntityClientPlayerMP player = mc.thePlayer;
			if (player==null || player.isDead) {
				jm.getLogger().fine("Player dead, aborting");
				return;
			}					
			
			// Do the real task
			runTask(jm, mc, threadLogging);

		} catch (Throwable t) {
			String error = Constants.getMessageJMERR16(t.getMessage());
			JourneyMap.getLogger().log(Level.SEVERE, LogFormatter.toString(t));		
			
		} finally {
			task = null;
			currentThread = null;		
		}
		
	}

	/**
	 * Map the chunks around the player.
	 */
	protected void runTask(JourneyMap jm, Minecraft mc, boolean threadLogging) {
		
		try {					
			synchronized(this) {

				final long start = System.nanoTime();				
				final boolean flushImagesToDisk = task.flushImagesToDisk;
				final boolean underground = task.underground;
				final int chunkY = task.chunkY;
				final File jmWorldDir = FileHandler.getJMWorldDir(mc);
				final Constants.CoordType cType = Constants.CoordType.convert(underground, task.dimension);
				final Map<ChunkCoordIntPair, ChunkStub> chunkStubs = task.chunkStubs;
				final Iterator<ChunkStub> chunkIter = chunkStubs.values().iterator();
						
				// Map the chunks
				while(chunkIter.hasNext()) {								
					if(!jm.isMapping()) {
						if(threadLogging) logger.info("JourneyMap isn't mapping, aborting mapChunk()"); //$NON-NLS-1$
						return;
					}
					ChunkStub chunkStub = chunkIter.next();
					if(chunkStub.doMap) {
						BufferedImage chunkImage = renderController.getChunkImage(chunkStub, underground, chunkY, chunkStubs);		
						if(chunkImage!=null) {			
							ChunkCoord cCoord = ChunkCoord.fromChunkStub(jmWorldDir, chunkStub, chunkY, cType);
							chunkImageCache.put(cCoord, chunkImage);			
						} else {
							if(logger.isLoggable(Level.FINE)) {
								logger.fine("Could not render chunk image:" + chunkStub.xPosition + "," + chunkStub.zPosition + " at " + chunkY + " and underground = " + underground); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
							}
						}
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
			}
			
		} finally {
			chunkImageCache.clear();		
		}
		
	}

}
