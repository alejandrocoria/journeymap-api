package net.techbrew.mcjm.cartography;


import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.EnumSkyBlock;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.model.BlockMD;
import net.techbrew.mcjm.model.BlockUtils;
import net.techbrew.mcjm.model.ChunkMD;

import java.awt.*;
import java.util.logging.Level;

/**
 * Render a chunk in the Nether.
 * @author mwoodman
 *
 */
public class ChunkNetherRenderer extends BaseRenderer implements IChunkRenderer {

	/**
	 * Render blocks in the chunk for the Nether world.
	 */
	@Override
	public boolean render(final Graphics2D g2D, final ChunkMD chunkMd, final boolean underground, 
			final Integer vSlice, final ChunkMD.Set neighbors) {
		
		int sliceMinY = Math.max((vSlice << 4) - 1, 0);
		int sliceMaxY = Math.min(((vSlice + 1) << 4) - 1, chunkMd.worldHeight);
		if (sliceMinY == sliceMaxY) {
			sliceMaxY += 2;
		}
		
		// Initialize ChunkSub slopes if needed
		if(chunkMd.sliceSlopes==null) {
			chunkMd.sliceSlopes = new float[16][16];
			float minNorm = Math.min(((vSlice + 1) << 4) - 1, chunkMd.worldHeight);
			float maxNorm = 0;
			float slope, h, hN, hW;
			
			for(int z=0; z<16; z++)
			{
				for(int x=0; x<16; x++)
				{									
					h = getHeightInSlice(chunkMd, x, z, sliceMinY, sliceMaxY);
					hN = (z==0)  ? getBlockHeight(x, z, 0, -1, chunkMd, neighbors, h, sliceMinY, sliceMaxY) : getHeightInSlice(chunkMd, x, z-1, sliceMinY, sliceMaxY);							
					hW = (x==0)  ? getBlockHeight(x, z, -1, 0, chunkMd, neighbors, h, sliceMinY, sliceMaxY) : getHeightInSlice(chunkMd, x-1, z, sliceMinY, sliceMaxY);
					slope = ((h/hN)+(h/hW))/2f;
					chunkMd.sliceSlopes[x][z] = slope;						
				}
			}
		}
		
		boolean chunkOk = false;
		int lightLevel;
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {		
				try {
					String metaId = null;
					boolean hasAir = false;

					int y = getHeightInSlice(chunkMd, x, z, sliceMinY, sliceMaxY);
                    BlockMD blockMD = BlockMD.getBlockMD(chunkMd, x, y, z);
					boolean isLava = (blockMD.getBlock() == Blocks.lava || blockMD.getBlock() == Blocks.flowing_lava);

					Color color = blockMD.getColor(chunkMd, x, y, z);
					
					// Get light level
					if(isLava) {
						lightLevel = 14;
					} else {
						lightLevel = chunkMd.getSavedLightValue(EnumSkyBlock.Block, x, y + 1, z);
						if(y==sliceMaxY) {
							paintBlock(x, z, Color.BLACK, g2D);							
							continue;
						} else if (lightLevel < 3) {
							lightLevel = 3;
						}
					}			
					
					if(true) {
						// Contour shading
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
							s = Math.max(slope * .9f, .2f);
							color = shade(color, s);
		
						} else if(slope>1) {
							
							if(sAvg>1) {
								if(slope>=sAvg) {
									slope = slope*1.2f;
								}
							}
							s = slope * 1.2f;
							s = Math.min(s, 1.2f);
							color = shade(color, s);
						}
					}
		
					// Darken based on light level
					if (lightLevel < 14) {
						float darken = Math.min(1F, (lightLevel / 15F));
						float[] rgb = new float[4];
						rgb = color.getRGBColorComponents(rgb);
						color = new Color(rgb[0] * darken, rgb[1] * darken, rgb[2]
								* darken);
					}
		
					// Draw lighted block
					g2D.setComposite(BlockUtils.OPAQUE);
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

	public int getHeightInSlice( final ChunkMD chunkMd, final int x, final int z, final int sliceMinY, final int sliceMaxY) {
		boolean hasAir = false;
		Block block;
		
		int y = sliceMaxY;
		for (; y > 0; y--) {
			block = chunkMd.getBlock(x, y, z);

			if (BlockUtils.hasFlag(block, BlockUtils.Flag.HasAir)) {
				hasAir = true;
				continue;
			}
			
			if(block==Blocks.fire) {
				y--;
				break;
			}
			
			if (hasAir) {
				break;
			}
			
			if (y <= sliceMinY) {
				y = sliceMaxY;
				break;
			}			
		}	
		return y;
	}
	
	/**
	 * Get the height of the block at the coordinates + offsets.  Uses ChunkMD.sliceSlopes.
	 * @param x
	 * @param z
	 * @param offsetX
	 * @param offsetz
	 * @param currentChunk
	 * @param neighbors
	 * @param defaultVal
	 * @return
	 */
	public Float getBlockHeight(int x, int z, int offsetX, int offsetz, ChunkMD currentChunk, ChunkMD.Set neighbors, float defaultVal, final int sliceMinY, final int sliceMaxY) {
		int newX = x+offsetX;
		int newZ = z+offsetz;
		
		int chunkX = currentChunk.stub.xPosition;
		int chunkZ = currentChunk.stub.zPosition;
		boolean search = false;
		
		if(newX==-1) {
			chunkX--;
			newX = 15;
			search = true;
		} else if(newX==16) {
			chunkX++;
			newX = 0;
			search = true;
		}
		if(newZ==-1) {
			chunkZ--;
			newZ = 15;
			search = true;
		} else if(newZ==16) {
			chunkZ++;
			newZ = 0;
			search = true;
		}
		
		ChunkMD chunk = getChunk(x, z, offsetX, offsetz, currentChunk, neighbors);
		
		if(chunk!=null) {
			return (float) getHeightInSlice(chunk, newX, newZ, sliceMinY, sliceMaxY);
		} else {
			return defaultVal;
		}
	}


}
