package net.techbrew.mcjm.render;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.src.EnumSkyBlock;
import net.minecraft.src.WorldProvider;
import net.minecraft.src.WorldProviderHell;
import net.minecraft.src.WorldProviderEnd;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.PropertyManager;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.model.ChunkStub;

/**
 * Render a chunk in the End.
 * @author mwoodman
 *
 */
public class ChunkEndRenderer extends BaseRenderer implements IChunkRenderer {
	
	/**
	 * Constructor.
	 * @param mapBlocks
	 */
	ChunkEndRenderer(MapBlocks mapBlocks) {
		super(mapBlocks);
	}

	/**
	 * Render blocks in the chunk for the End world.
	 */
	@Override
	public void render(final Graphics2D g2D, final ChunkStub chunkStub, final boolean underground, 
			final int vSlice, final Map<Integer, ChunkStub> neighbors) {
		
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {

				try {
					int sliceMinY = Math.max((vSlice << 4) - 1, 0);
					int sliceMaxY = Math.min(((vSlice + 1) << 4) - 1, 128);
					if (sliceMinY == sliceMaxY) {
						sliceMaxY += 2;
					}
		
					String metaId = null;
					boolean hasAir = false;
					int blockId = -1;
					int paintY = -1;
					int lightLevel = -1;
		
					int y = sliceMaxY;
					for (; y > 0; y--) {
						blockId = chunkStub.getBlockID(x, y, z);
		
						if (!hasAir && y <= sliceMinY) {
							break;
						}
		
						if (blockId == 0) {
							hasAir = true;
						} else if (hasAir && paintY == -1) {
							paintY = y;
							lightLevel = chunkStub.getSavedLightValue(EnumSkyBlock.Block, x,paintY + 1, z);
							break;
						}
					}
		
					// Block isn't viable to paint
					if (paintY == -1) {
						paintY = 0;
					}
		
					blockId = chunkStub.getBlockID(x, paintY, z);
		
					lightLevel = chunkStub.getSavedLightValue(EnumSkyBlock.Block, x,paintY + 1, z);
		
					if (lightLevel < 10) {
						lightLevel += 2;
					}
					
					// Ender Crystal
					if(blockId==51 || blockId==200 || blockId==7) {
						lightLevel = 15;
					}		
		
					// Get block color
					BlockInfo block = mapBlocks.getBlockInfo(chunkStub, x, paintY, z);
					Color color = block.color;
		
					// Contour shading
					if(blockId==121) {
			
						// Get light level
						if (lightLevel < 15) {
							float darken = Math.min(1F, (lightLevel / 15F));
							float[] rgb = new float[4];
							rgb = color.getRGBColorComponents(rgb);
							color = new Color(rgb[0] * darken, rgb[1] * darken, rgb[2]
									* darken);
						}
					}
		
					// Draw 
					g2D.setComposite(MapBlocks.OPAQUE);
					g2D.setPaint(color);
					g2D.fillRect(x, z, 1, 1);
					
		
				} catch (Throwable t) {
					paintBadBlock(x, vSlice, z, g2D);
					String error = Constants.getMessageJMERR07("x,vSlice,z = " + x + "," //$NON-NLS-1$ //$NON-NLS-2$
							+ vSlice + "," + z + " : " + t.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
					JourneyMap.getLogger().severe(error);
					JourneyMap.getLogger().log(Level.SEVERE, LogFormatter.toString(t));
				}
			}
		}

	}

}
