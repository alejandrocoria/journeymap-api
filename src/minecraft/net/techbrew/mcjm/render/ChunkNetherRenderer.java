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
 * Render a chunk in the Nether.
 * @author mwoodman
 *
 */
public class ChunkNetherRenderer extends BaseRenderer implements IChunkRenderer {

	static final int alphaDepth = 5;
	
	/**
	 * Constructor.
	 * @param mapBlocks
	 */
	ChunkNetherRenderer(MapBlocks mapBlocks) {
		super(mapBlocks);
	}

	/**
	 * Render blocks in the chunk for the Nether world.
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
						// System.out.println("BlockId " + blockId);
						// z + " (" + y + ")");
		
						if (!hasAir && y <= sliceMinY) {
							break;
						}
		
						if (blockId == 0) {
							hasAir = true;
						} else if (hasAir && paintY == -1) {
							paintY = y;
							lightLevel = chunkStub.getSavedLightValue(EnumSkyBlock.Block, x,paintY + 1, z);
							if (lightLevel > 1) {
								break;
							}
						}
					}
		
					// Block isn't viable to paint
					if (paintY == -1 || lightLevel < 0) {
						paintY = y;
					}
					
					BlockInfo block = mapBlocks.getBlockInfo(chunkStub, x, paintY, z);		
					blockId = block.id;
					
					// Contour shading
					boolean isLava = (blockId == 10 || blockId == 11);
		
					if(isLava) {
						lightLevel = 14;
					} else {
						lightLevel = chunkStub.getSavedLightValue(EnumSkyBlock.Block, x,paintY + 1, z);
						if (lightLevel < 0) {
							//paintClearBlock(x, vSlice, z, g2D);
							//return; // how did we get here?
							lightLevel = 1; // how did we get here?
						}
					}
		
					// Get block color					
					Color color = block.color;
		
					// Get light level
					if (lightLevel < 2)
						lightLevel = 2;
					if (lightLevel < 15) {
						float darken = Math.min(1F, (lightLevel / 15F));
						float[] rgb = new float[4];
						rgb = color.getRGBColorComponents(rgb);
						color = new Color(rgb[0] * darken, rgb[1] * darken, rgb[2]
								* darken);
					}
		
					// Draw lighted block
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
