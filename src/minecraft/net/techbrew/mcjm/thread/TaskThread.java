package net.techbrew.mcjm.thread;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.src.Minecraft;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.Constants.MapType;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.model.ChunkCoord;
import net.techbrew.mcjm.model.ChunkImageCache;
import net.techbrew.mcjm.model.ChunkMD;
import net.techbrew.mcjm.model.RegionImageCache;
import net.techbrew.mcjm.render.ChunkRenderController;
import net.techbrew.mcjm.task.IGenericTask;
import net.techbrew.mcjm.task.IMapTask;
import net.techbrew.mcjm.task.ITask;

public class TaskThread implements Runnable {

	private static volatile AtomicInteger queue = new AtomicInteger(0);
	private static ChunkRenderController renderController;
	private final ITask task;
	
	private final Logger logger = JourneyMap.getLogger();
	
	private TaskThread(ITask task) {
		this.task = task;
	}
	
	public static TaskThread createAndQueue(ITask task) {
		if(task==null) return null;
		synchronized(queue) {
			if(queue.get()==0) {
				queue.incrementAndGet();
				return new TaskThread(task);
			} else {
				return null;
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
			final Minecraft mc = Minecraft.getMinecraft();
			final boolean threadLogging = jm.isThreadLogging();
			
			// Bail if needed
			if(!jm.isMapping()) {
				jm.getLogger().warning("JM not mapping, aborting");
				return;
			}				
			
			final File jmWorldDir = FileHandler.getJMWorldDir(mc);
			if(jmWorldDir==null) {
				jm.getLogger().warning("JM world dir not found, aborting");
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
		
		try {					
			final long start = System.nanoTime();				
			final boolean flushImagesToDisk = task.flushCacheWhenDone();
			final Integer vSlice = task.getVSlice();	
			final boolean underground = task.isUnderground();					
			final int dimension = task.getDimension();
			final ChunkMD.Set chunkSet = task.getChunkStubs();
			final Iterator<ChunkMD> chunkIter = chunkSet.iterator();
			final ChunkImageCache chunkImageCache = new ChunkImageCache();
					
			// Map the chunks
			while(chunkIter.hasNext()) {								
				if(!jm.isMapping()) {
					if(threadLogging) logger.info("JM isn't mapping, aborting"); //$NON-NLS-1$
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
					chunkMd.render = false;
				}
			}
			
			if(!jm.isMapping()) {
				if(threadLogging) logger.info("JM isn't mapping, aborting.");  //$NON-NLS-1$
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
				
		} catch (Throwable t) {
			String error = Constants.getMessageJMERR16(t.getMessage());
			JourneyMap.getLogger().log(Level.SEVERE, LogFormatter.toString(t));			
		} 
	}
	
	private final void runGenericTask(IGenericTask task, Minecraft mc, JourneyMap jm, File jmWorldDir, boolean threadLogging) {
		try {					
			final long start = System.nanoTime();	
			
			task.performTask();
			
			if(threadLogging) {
				logger.fine(task.getClass().getSimpleName() + " completed in " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		} catch (Throwable t) {
			String error = Constants.getMessageJMERR16(t.getMessage());
			JourneyMap.getLogger().log(Level.SEVERE, LogFormatter.toString(t));			
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
