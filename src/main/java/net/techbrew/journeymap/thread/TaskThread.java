package net.techbrew.journeymap.thread;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.Constants.MapType;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.cartography.ChunkRenderController;
import net.techbrew.journeymap.io.FileHandler;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.log.StatTimer;
import net.techbrew.journeymap.model.ChunkCoord;
import net.techbrew.journeymap.model.ChunkImageCache;
import net.techbrew.journeymap.model.ChunkMD;
import net.techbrew.journeymap.model.RegionImageCache;
import net.techbrew.journeymap.task.IGenericTask;
import net.techbrew.journeymap.task.IMapTask;
import net.techbrew.journeymap.task.ITask;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

;

public class TaskThread implements Runnable {

	private static volatile AtomicInteger queue = new AtomicInteger(0);
	private static ChunkRenderController renderController;
	private final ITask task;
	
	private final Logger logger = JourneyMap.getLogger();
	
	private TaskThread(ITask task) {
		this.task = task;
	}
	
	public static TaskThread createAndQueue(ITask task) {
		if(task==null) {
            return null;
        }
		synchronized(queue) {
            final int q = queue.get();
            if(q>1) {
                return null;
            } else {
                queue.set(1);
                return new TaskThread(task);
            }
		}
	}
	
	public static void reset() {
		synchronized(queue) {
			queue.set(0);
		}	
		renderController = new ChunkRenderController();
	}
	
	public static boolean hasQueue() {
		synchronized(queue) {
			return queue.get()>0;
		}
	}
	
	@Override
	public final void run() {

		try {
			final JourneyMap jm = JourneyMap.getInstance();
			final Minecraft mc = FMLClientHandler.instance().getClient();
			final boolean threadLogging = jm.isThreadLogging();
			
			// Bail if needed
			if(!jm.isMapping()) {
				jm.getLogger().fine("JM not mapping, aborting");
				return;
			}				
			
			final File jmWorldDir = FileHandler.getJMWorldDir(mc);
			if(jmWorldDir==null) {
				jm.getLogger().fine("JM world dir not found, aborting");
				return;
			}
			
			if(task instanceof IMapTask) {
				runMapTask((IMapTask) task, mc, jm, jmWorldDir, threadLogging);
			} else if (task instanceof IGenericTask) {
				runGenericTask((IGenericTask) task, mc, jm, jmWorldDir, threadLogging);
			} else {
				throw new UnsupportedOperationException("ITask unknown: " + task.getClass());
			}
		} finally {
			synchronized(queue) {
				queue.decrementAndGet();
			}
		}
		
		
	}
	
	private final void runMapTask(IMapTask task, Minecraft mc, JourneyMap jm, File jmWorldDir, boolean threadLogging) {

        StatTimer timer = StatTimer.get(task.getClass().getSimpleName() + ".runMapTask").start();

		try {					
			final long start = System.nanoTime();				
			final boolean flushImagesToDisk = task.flushCacheWhenDone();
			final Integer vSlice = task.getVSlice();	
			final boolean underground = task.isUnderground();					
			final int dimension = task.getDimension();
			final ChunkMD.Set chunkSet = task.getChunkStubs();
			final Iterator<ChunkMD> chunkIter = chunkSet.iterator();
			final ChunkImageCache chunkImageCache = new ChunkImageCache();

            // Check the dimension
            int currentDimension = mc.theWorld.provider.dimensionId;
            if(currentDimension!=dimension) {
                if(threadLogging) logger.fine("Dimension changed, map task obsolete."); //$NON-NLS-1$
                timer.cancel();
                return;
            }

			// Map the chunks
			while(chunkIter.hasNext()) {								
				if(!jm.isMapping()) {
					if(threadLogging) logger.fine("JM isn't mapping, aborting"); //$NON-NLS-1$
                    timer.cancel();
					return;
				}
				ChunkMD chunkMd = chunkIter.next();
				if(chunkMd.render) {
					BufferedImage chunkImage = renderController.getChunkImage(chunkMd, underground, vSlice, chunkSet);
					ChunkCoord cCoord = ChunkCoord.fromChunkMD(jmWorldDir, chunkMd, vSlice, dimension);
					if(underground) {
						chunkImageCache.put(cCoord, MapType.underground, chunkImage);
					} else {
						chunkImageCache.put(cCoord, MapType.day, getSubimage(MapType.day, chunkImage));
						chunkImageCache.put(cCoord, MapType.night, getSubimage(MapType.night, chunkImage));
					}
				}
			}
			
			if(!jm.isMapping()) {
				if(threadLogging) logger.fine("JM isn't mapping, aborting.");  //$NON-NLS-1$
                timer.cancel();
				return;
			}
	
			// Push chunk cache to region cache			
			int chunks = chunkImageCache.size();
			RegionImageCache.getInstance().putAll(chunkImageCache.values(), flushImagesToDisk);			

			if(threadLogging) {
				logger.fine(task.getClass().getSimpleName() + " mapped " + chunks + " chunks in " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-start) + "ms with flush:" + flushImagesToDisk); //$NON-NLS-1$ //$NON-NLS-2$
			}

			task.taskComplete();
			chunkSet.clear();
			chunkImageCache.clear();

            timer.stop();
				
		} catch (Throwable t) {
			String error = Constants.getMessageJMERR16(LogFormatter.toString(t));
			JourneyMap.getLogger().severe(error);
            timer.cancel();
		} 
	}
	
	private final void runGenericTask(IGenericTask task, Minecraft mc, JourneyMap jm, File jmWorldDir, boolean threadLogging) {

        StatTimer timer = StatTimer.get(task.getClass().getSimpleName() + ".performTask").start();

		try {					

			task.performTask();

            timer.stop();
			
			if(threadLogging) {
                timer.report();
			}
		} catch (Throwable t) {
            String error = Constants.getMessageJMERR16(LogFormatter.toString(t));
            JourneyMap.getLogger().severe(error);
            timer.cancel();
		} 
	}
	
	private BufferedImage getSubimage(MapType mapType, BufferedImage image) {
		if(image==null) return null;
		switch(mapType) {
			case night: {
				return image.getSubimage(16, 0, 16, 16);
			}
			default: {
				return image.getSubimage(0, 0, 16, 16);
			}
		}
	}
	
}
