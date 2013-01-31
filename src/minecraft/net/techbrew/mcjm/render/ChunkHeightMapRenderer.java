package net.techbrew.mcjm.render;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import net.minecraft.client.Minecraft;
import net.minecraft.src.EnumSkyBlock;
import net.techbrew.mcjm.ChunkStub;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.PropertyManager;
import net.techbrew.mcjm.log.LogFormatter;

public class ChunkHeightMapRenderer implements IChunkRenderer {

	@Override
	public BufferedImage getChunkImage(ChunkStub chunkStub,
			boolean underground, int vSlice,
			Map<Integer, ChunkStub> neighbors) {

		BufferedImage chunkImage = null;
		try {
			// Get data from chunk, then stop using it directly
			chunkImage = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2D = chunkImage.createGraphics();

			if (underground) {
				// Clear the pixels
				for (int x = 0; x < 16; x++) {
					for (int z = 0; z < 16; z++) {
						paintClearBlock(x, vSlice, z, g2D);
					}
				}

			} else {
				float worldHeight = Minecraft.getMinecraft().theWorld.getHeight();
				
				// Surface map
				for (int x = 0; x < 16; x++) {
					for (int z = 0; z < 16; z++) {
						float y = chunkStub.getSafeHeightValue(x, z);
						if (y < 0) y=0;
						
						float grey = y/worldHeight;
						try {
							// Paint the block
							paintGreyBlock(x,z,new Color(grey,grey,grey), g2D );
						} catch (ArrayIndexOutOfBoundsException e) {
							// Can happen when server isn't connected, just wait
							// for
							// next tick
							JourneyMap.getLogger().log(Level.WARNING,
									LogFormatter.toString(e));
							return null;
						} catch (Throwable t) {
							String error = Constants.getMessageJMERR07(LogFormatter.toString(t));
							JourneyMap.getLogger().severe(error);
							return null;
						}
					}
				}
			}
		} finally {
			if (chunkImage == null) {
				chunkImage = getPlaceholderChunk();
			}
		}

		return chunkImage;
	}
	
	
	/**
	 * Paint the block grey
	 * 
	 * @param x
	 * @param vSlice
	 * @param z
	 */
	void paintGreyBlock(final int x, final int z, Color grey, final Graphics2D g2D) {
		g2D.setComposite(MapBlocks.OPAQUE);
		g2D.setPaint(grey);
		g2D.fillRect(x, z, 1, 1);
	}
	
	/**
	 * Paint the block clear.
	 * 
	 * @param x
	 * @param vSlice
	 * @param z
	 */
	void paintClearBlock(final int x, final int vSlice, final int z,
			final Graphics2D g2D) {
		g2D.setComposite(MapBlocks.OPAQUE);
		g2D.setPaint(MapBlocks.COLOR_TRANSPARENT);
		g2D.fillRect(x, z, 1, 1);
		g2D.clearRect(x, z, 1, 1);
	}
	
	// TODO: Make threadsafe
	private static volatile BufferedImage placeHolderChunk;

	static BufferedImage getPlaceholderChunk() {
		// if(placeHolderChunk==null) {
		placeHolderChunk = new BufferedImage(16, 16,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2D = placeHolderChunk.createGraphics();
		g2D.setComposite(MapBlocks.CLEAR);
		g2D.setPaint(Color.black);
		g2D.fillRect(0, 0, 16, 16);
		// }
		return placeHolderChunk;
	}

}
