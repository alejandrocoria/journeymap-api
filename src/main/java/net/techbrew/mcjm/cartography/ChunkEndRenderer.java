package net.techbrew.mcjm.cartography;


import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.EnumSkyBlock;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.model.ChunkMD;

import java.awt.*;
import java.util.logging.Level;

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
	public boolean render(final Graphics2D g2D, final ChunkMD chunkMd, final boolean underground, 
			final Integer vSlice, final ChunkMD.Set neighbors) {
		
		// Initialize ChunkSub slopes if needed
		if(chunkMd.sliceSlopes==null) {
			chunkMd.sliceSlopes = new float[16][16];
			float minNorm = chunkMd.worldHeight;
			float maxNorm = 0;
			float slope, h, hN, hW;
			for(int y=0; y<16; y++)
			{
				for(int x=0; x<16; x++)
				{				
					h = chunkMd.getSafeHeightValue(x, y);
					hN = (y==0)  ? getBlockHeight(x, y, 0, -1, chunkMd, neighbors, h) : chunkMd.getSafeHeightValue(x, y-1);							
					hW = (x==0)  ? getBlockHeight(x, y, -1, 0, chunkMd, neighbors, h) : chunkMd.getSafeHeightValue(x-1, y);
					slope = ((h/hN)+(h/hW))/2f;
					chunkMd.sliceSlopes[x][y] = slope;						
				}
			}
		}
		
		boolean chunkOk = false;
		int maxY = chunkMd.worldHeight;
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {

				try {
					int sliceMinY = Math.max((vSlice << 4) - 1, 0);
					int sliceMaxY = maxY;
					if (sliceMinY == sliceMaxY) {
						sliceMaxY += 2;
					}
		
					String metaId = null;
					boolean hasAir = false;
                    BlockInfo blockInfo;
					int paintY = -1;
					int lightLevel = -1;
		
					int y = sliceMaxY;
					for (; y > 0; y--) {
                        blockInfo = mapBlocks.getBlockInfo(chunkMd, x, y, z);
		
						if (!hasAir && y <= sliceMinY) {
							break;
						}
		
						if (blockInfo.isAir()) {
							hasAir = true;
						} else if (hasAir && paintY == -1) {
							paintY = y;
							lightLevel = chunkMd.getSavedLightValue(EnumSkyBlock.Block, x,paintY + 1, z);
							break;
						}
					}
		
					// Block isn't viable to paint
					if (paintY == -1) {
						paintY = 0;
					}
                    blockInfo = mapBlocks.getBlockInfo(chunkMd, x, paintY, z);
		
					lightLevel = chunkMd.getSavedLightValue(EnumSkyBlock.Block, x,paintY + 1, z);
		
					if (lightLevel < 10) {
						lightLevel += 3;
					}
					
					// Ender Crystal
                    // TODO:  Map the entity for the crystal
                    Block block = blockInfo.getBlock();
					if(block== Blocks.bedrock || block==Blocks.obsidian) {
						lightLevel = 15;
					}		
		
					// Get block color
					Color color = blockInfo.getColor(chunkMd, x, paintY, z);
					
					// Get slope of block and prepare to shade
					float slope, s, sN, sNW, sW, sAvg, shaded;
					slope = chunkMd.sliceSlopes[x][z];
					
					sN = getBlockSlope(x, z, 0, -1, chunkMd, neighbors, slope, true);
					sNW = getBlockSlope(x, z, -1, -1, chunkMd, neighbors, slope, true);
					sW = getBlockSlope(x, z, -1, 0, chunkMd, neighbors, slope, true);
					sAvg = (sN+sNW+sW)/3f;
					
					if(slope<1) {
						
						if(slope<=sAvg) {
							slope = slope*.6f;
						} else if(slope>sAvg) {
							slope = (slope+sAvg)/2f;
						}
						s = Math.max(slope * .8f, .1f);
						color = shade(color, s);
	
					} else if(slope>1) {
						
						if(sAvg>1) {
							if(slope>=sAvg) {
								slope = slope*1.2f;
							}
						}
						s = slope * 1.2f;
						s = Math.min(s, 1.4f);
						color = shade(color, s);
					}
		
					// Contour shading
					if(block==Blocks.end_stone) {
			
						// Get light level
						if (lightLevel < 15) {
							float darken = Math.min(1F, lightLevel*1f/14);
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
					chunkOk = true;
					
		
				} catch (Throwable t) {
					paintBadBlock(x, vSlice, z, g2D);
					String error = Constants.getMessageJMERR07("x,vSlice,z = " + x + "," //$NON-NLS-1$ //$NON-NLS-2$
							+ vSlice + "," + z + " : " + t.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
					JourneyMap.getLogger().severe(error);
					JourneyMap.getLogger().log(Level.SEVERE, LogFormatter.toString(t));
				}
			}
		}
		return chunkOk;
	}

}
