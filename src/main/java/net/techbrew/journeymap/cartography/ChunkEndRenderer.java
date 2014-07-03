package net.techbrew.journeymap.cartography;


import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.EnumSkyBlock;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.model.BlockMD;
import net.techbrew.journeymap.model.BlockUtils;
import net.techbrew.journeymap.model.ChunkMD;

import java.awt.*;

/**
 * Render a chunk in the End.
 * @author mwoodman
 *
 */
public class ChunkEndRenderer extends BaseRenderer implements IChunkRenderer {

	/**
	 * Render blocks in the chunk for the End world.
	 */
	@Override
	public boolean render(final Graphics2D g2D, final ChunkMD chunkMd, final boolean underground, 
			final Integer vSlice, final ChunkMD.Set neighbors) {

		// Initialize ChunkSub slopes if needed
		if(chunkMd.sliceSlopes==null) {
			chunkMd.sliceSlopes = new float[16][16];
//			float minNorm = chunkMd.worldHeight;
//			float maxNorm = 0;
			float slope, h, hN, hW;
			for(int z=0; z<16; z++)
			{
				for(int x=0; x<16; x++)
				{
					h = chunkMd.getSlopeHeightValue(x, z, false);
					hN = (z==0)  ? getBlockHeight(x, z, 0, -1, chunkMd, neighbors, h, false) : chunkMd.getSlopeHeightValue(x, z - 1, false);
					hW = (x==0)  ? getBlockHeight(x, z, -1, 0, chunkMd, neighbors, h, false) : chunkMd.getSlopeHeightValue(x - 1, z, false);
					slope = ((h/hN)+(h/hW))/2f;
					chunkMd.sliceSlopes[x][z] = slope;
				}
			}
		}

		boolean chunkOk = false;
		int maxY = chunkMd.worldHeight;
		for (int x = 0; x < 16; x++) {
			blockloop: for (int z = 0; z < 16; z++) {

				try {
					int sliceMinY = Math.max((vSlice << 4) - 1, 0);
					int sliceMaxY = maxY;
					if (sliceMinY == sliceMaxY) {
						sliceMaxY += 2;
					}
		
					String metaId = null;
					boolean hasAir = false;
                    BlockMD blockMD;
					int paintY = -1;
					int lightLevel = -1;
		
					int y = sliceMaxY;
					for (; y > 0; y--) {
                        blockMD = BlockMD.getBlockMD(chunkMd, x, y, z);
		
						if (!hasAir && y <= sliceMinY) {
							break;
						}
		
						if (blockMD.isAir()) {
							hasAir = true;
						} else if (hasAir && paintY == -1) {
							paintY = y;
							break;
						}
					}
		
					// Block isn't viable to paint
					if (paintY <=0)
                    {
                        paintBlock(x, z, 0, g2D);
                        continue blockloop;
					}
                    blockMD = BlockMD.getBlockMD(chunkMd, x, paintY, z);
		
					lightLevel = chunkMd.getSavedLightValue(EnumSkyBlock.Block, x,paintY + 1, z);
		
					if (lightLevel < 10) {
						lightLevel += 3;
					}
					
					// Ender Crystal
                    // TODO:  Map the entity for the crystal
                    Block block = blockMD.getBlock();
					if(block== Blocks.bedrock || block==Blocks.obsidian) {
						lightLevel = 15;
					}		
		
					// Get block color
					int color = blockMD.getColor(chunkMd, x, paintY, z);
					
					// Get slope of block and prepare to bevelSlope
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
						color = RGB.bevelSlope(color, s);
	
					} else if(slope>1) {
						
						if(sAvg>1) {
							if(slope>=sAvg) {
								slope = slope*1.2f;
							}
						}
						s = slope * 1.2f;
						s = Math.min(s, 1.4f);
                        color = RGB.bevelSlope(color, s);
					}
		
					// Contour shading
					if(block==Blocks.end_stone) {
			
						// Get light level
						if (lightLevel < 15) {
                            color = RGB.darken(color, Math.min(1F, lightLevel*1f/14));
						}
					}
		
					// Draw 
					g2D.setComposite(BlockUtils.OPAQUE);
					g2D.setPaint(RGB.paintOf(color));
					g2D.fillRect(x, z, 1, 1);
					chunkOk = true;
					
		
				} catch (Throwable t) {
					paintBadBlock(x, vSlice, z, g2D);
					String error = Constants.getMessageJMERR07("x,vSlice,z = " + x + "," //$NON-NLS-1$ //$NON-NLS-2$
							+ vSlice + "," + z + " : " + LogFormatter.toString(t)); //$NON-NLS-1$ //$NON-NLS-2$
					JourneyMap.getLogger().severe(error);
				}
			}
		}
		return chunkOk;
	}

}
