package net.techbrew.mcjm.render;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.model.ChunkStub;

/**
 * Delegates rendering job to one or more renderer.
 * @author mwoodman
 *
 */
public class ChunkRenderController {

	public volatile AtomicInteger updateCounter = new AtomicInteger(0);
	public volatile AtomicLong updateTime = new AtomicLong(0);
	
	private final IChunkRenderer netherRenderer;
	private final IChunkRenderer endRenderer;
	private final IChunkRenderer standardRenderer;
	
	final boolean fineLogging = JourneyMap.getLogger().isLoggable(Level.FINE);
	
	public ChunkRenderController() {
		MapBlocks mapBlocks = new MapBlocks();
		netherRenderer = new ChunkNetherRenderer(mapBlocks);
		endRenderer = new ChunkEndRenderer(mapBlocks);
		standardRenderer = new ChunkStandardRenderer(mapBlocks);
	}
	
	public BufferedImage getChunkImage(ChunkStub chunkStub,
			boolean underground, int vSlice,
			Map<Integer, ChunkStub> neighbors) {
		
		// Initialize image for the chunk
		BufferedImage chunkImage = new BufferedImage(underground ? 16 : 32, 16, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2D = chunkImage.createGraphics();
		g2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		
		int dimension = chunkStub.worldObj.provider.dimensionId;
		
		long start = System.nanoTime();
		try {
			switch(dimension) {
				case -1 : {
					netherRenderer.render(g2D, chunkStub, underground, vSlice, neighbors);
					break;
				}
				case 1 : {
					endRenderer.render(g2D, chunkStub, underground, vSlice, neighbors);
					break;
				}
				default : {
					standardRenderer.render(g2D, chunkStub, underground, vSlice, neighbors);
				}
			}

		} catch (ArrayIndexOutOfBoundsException e) {
			JourneyMap.getLogger().log(Level.WARNING, LogFormatter.toString(e));
			return null; // Can happen when server isn't connected, just wait for next tick
		} catch (Throwable t) {
			t.printStackTrace();
			String error = Constants.getMessageJMERR07(LogFormatter.toString(t));
			JourneyMap.getLogger().severe(error);
			
		}
		
		long stop = System.nanoTime();
		
		updateCounter.incrementAndGet();
		updateTime.addAndGet(stop-start);
		
		double counter = (double) updateCounter.get();
		if(counter>=1000) {
			if(fineLogging) {
				
				double time = (double) TimeUnit.NANOSECONDS.toMillis(updateTime.get());
				double avg = time/counter;

				JourneyMap.getLogger().info("*** Chunks rendered: " + (int) counter + " in avg " + avg + " ms");
				
				updateCounter.set(0);
				updateTime.set(0);
			}
		}
		
		return chunkImage;
					
	}

}
