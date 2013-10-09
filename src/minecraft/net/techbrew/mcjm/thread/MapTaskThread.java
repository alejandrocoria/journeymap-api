package net.techbrew.mcjm.thread;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.src.ChunkCoordIntPair;
import net.minecraft.src.Minecraft;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.model.ChunkCoord;
import net.techbrew.mcjm.model.ChunkImageCache;
import net.techbrew.mcjm.model.ChunkStub;
import net.techbrew.mcjm.model.RegionImageCache;
import net.techbrew.mcjm.render.ChunkRenderController;
import net.techbrew.mcjm.thread.task.MapTask;

public class MapTaskThread implements Runnable {

	private static volatile AtomicInteger queue = new AtomicInteger(0);
	private static ChunkRenderController renderController;
	private final MapTask task;
	
	private final Logger logger = JourneyMap.getLogger();
	
	private MapTaskThread(MapTask task) {
		this.task = task;
	}
	
	public static MapTaskThread createAndQueue(MapTask task) {
		if(task==null) return null;
		synchronized(queue) {
			if(queue.get()==0) {
				queue.incrementAndGet();
				return new MapTaskThread(task);
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
			
			// Do the real task
			final long start = System.nanoTime();				
			final boolean flushImagesToDisk = task.flushImagesToDisk;
			final Integer chunkY = task.chunkY;	
			final boolean underground = task.underground && chunkY!=null;					
			final Constants.CoordType cType = Constants.CoordType.convert(underground, task.dimension);
			final Map<ChunkCoordIntPair, ChunkStub> chunkStubs = task.chunkStubs;
			final Iterator<ChunkStub> chunkIter = chunkStubs.values().iterator();
			final ChunkImageCache chunkImageCache = new ChunkImageCache();
					
			// Map the chunks
			while(chunkIter.hasNext()) {								
				if(!jm.isMapping()) {
					if(threadLogging) logger.info("JM isn't mapping, aborting"); //$NON-NLS-1$
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
				if(threadLogging) logger.info("JM isn't mapping, aborting.");  //$NON-NLS-1$
				return;
			}
	
			// Push chunk cache to region cache			
			int chunks = chunkImageCache.getEntries().size();
			RegionImageCache.getInstance().putAll(chunkImageCache.getEntries(), flushImagesToDisk);			

			//if(threadLogging) {
				logger.info(task.getClass().getSimpleName() + " mapped " + chunks + " chunks in " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-start) + "ms with flush:" + flushImagesToDisk); //$NON-NLS-1$
			//}

			chunkStubs.clear();
			chunkImageCache.clear();								
				
		} catch (Throwable t) {
			String error = Constants.getMessageJMERR16(t.getMessage());
			JourneyMap.getLogger().log(Level.SEVERE, LogFormatter.toString(t));
			
		} finally {
			synchronized(queue) {
				queue.decrementAndGet();
			}
		}
		
	}
	
}
