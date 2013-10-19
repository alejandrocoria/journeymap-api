package net.techbrew.mcjm.render;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

import net.minecraft.src.ChunkCoordIntPair;
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

	private static AtomicInteger updateCounter = new AtomicInteger(0);
	private static AtomicLong updateTime = new AtomicLong(0);
	
	private final IChunkRenderer netherRenderer;
	private final IChunkRenderer endRenderer;
	private final IChunkRenderer standardRenderer;
	
	final boolean fineLogging = JourneyMap.getLogger().isLoggable(Level.FINE);
	
	private BufferedImage blankChunkImage = null;
	private BufferedImage blankChunkImageUnderground = null;
	
	public ChunkRenderController() {
		MapBlocks mapBlocks = new MapBlocks();
		netherRenderer = new ChunkNetherRenderer(mapBlocks);
		endRenderer = new ChunkEndRenderer(mapBlocks);
		standardRenderer = new ChunkStandardRenderer(mapBlocks);
	}
	
	public BufferedImage getChunkImage(ChunkStub chunkStub,
			boolean underground, Integer vSlice,
			Map<ChunkCoordIntPair, ChunkStub> neighbors) {
		
		// Initialize image for the chunk
		BufferedImage chunkImage = new BufferedImage(underground ? 16 : 32, 16, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2D = chunkImage.createGraphics();
		g2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		
		int dimension = chunkStub.worldObj.provider.dimensionId;
		boolean renderOkay = false;
		
		long start = System.nanoTime();
		try {			
			switch(dimension) {
				case -1 : {
					renderOkay = netherRenderer.render(g2D, chunkStub, underground, vSlice, neighbors);
					break;
				}
				case 1 : {
					renderOkay = endRenderer.render(g2D, chunkStub, underground, vSlice, neighbors);
					break;
				}
				default : {
					renderOkay = standardRenderer.render(g2D, chunkStub, underground, vSlice, neighbors);
				}
			}

		} catch (ArrayIndexOutOfBoundsException e) {
			JourneyMap.getLogger().log(Level.WARNING, LogFormatter.toString(e));
			return null; // Can happen when server isn't connected, just wait for next tick
		} catch (Throwable t) {
			t.printStackTrace();
			String error = Constants.getMessageJMERR07(LogFormatter.toString(t));
			JourneyMap.getLogger().severe(error);
			
		} finally {
			g2D.dispose();
		}
		
		long stop = System.nanoTime();
		
		if(fineLogging) {
			updateCounter.incrementAndGet();
			updateTime.addAndGet(stop-start);		
		}
		
		if(!renderOkay) {
			if(fineLogging) {
				JourneyMap.getLogger().log(Level.WARNING, "Chunk didn't render: " + chunkStub.xPosition + "," + chunkStub.zPosition);
			}
			// Use blank
			chunkImage = underground ? getBlankChunkImageUnderground() : getBlankChunkImage();
		}
		
		if(fineLogging) {
			double counter = updateCounter.get();
			if(counter>=1000) {				
				double time = TimeUnit.NANOSECONDS.toMillis(updateTime.get());
				double avg = time/counter;

				JourneyMap.getLogger().info("*** Chunks rendered: " + (int) counter + " in avg " + avg + " ms");
				
				updateCounter.set(0);
				updateTime.set(0);
			}
		}
		
		
		return chunkImage;
					
	}
	
	private BufferedImage getBlankChunkImage() {
		if(blankChunkImage==null) {
			blankChunkImage = new BufferedImage(32, 16, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2D = blankChunkImage.createGraphics();
			g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .1F));
			g2D.setColor(Color.black);
			g2D.fillRect(0, 0, 1, 1);
			g2D.dispose();
		}
		return blankChunkImage;
	}
	
	private BufferedImage getBlankChunkImageUnderground() {
		if(blankChunkImageUnderground==null) {
			blankChunkImageUnderground = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2D = blankChunkImageUnderground.createGraphics();
			g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .1F));
			g2D.setColor(Color.black);
			g2D.fillRect(0, 0, 1, 1);
			g2D.dispose();
		}
		return blankChunkImageUnderground;
	}

}
