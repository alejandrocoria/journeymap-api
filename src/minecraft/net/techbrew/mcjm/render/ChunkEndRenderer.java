package net.techbrew.mcjm.render;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Map;
import java.util.logging.Level;

import net.minecraft.src.EnumSkyBlock;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
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
		
		// Initialize ChunkSub slopes if needed
		if(chunkStub.slopes==null) {
			chunkStub.slopes = new float[16][16];
			float minNorm = chunkStub.worldHeight;
			float maxNorm = 0;
			float slope, h, hN, hW;
			for(int y=0; y<16; y++)
			{
				for(int x=0; x<16; x++)
				{				
					h = chunkStub.getSafeHeightValue(x, y);
					hN = (y==0)  ? getBlockHeight(x, y, 0, -1, chunkStub, neighbors, h) : chunkStub.getSafeHeightValue(x, y-1);							
					hW = (x==0)  ? getBlockHeight(x, y, -1, 0, chunkStub, neighbors, h) : chunkStub.getSafeHeightValue(x-1, y);
					slope = ((h/hN)+(h/hW))/2f;
					chunkStub.slopes[x][y] = slope;						
				}
			}
		}
		
		int maxY = chunkStub.worldHeight;
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
						lightLevel += 3;
					}
					
					// Ender Crystal
					if(blockId==51 || blockId==200 || blockId==7) {
						lightLevel = 15;
					}		
		
					// Get block color
					BlockInfo block = mapBlocks.getBlockInfo(chunkStub, x, paintY, z);
					Color color = block.getColor();
					
					// Get slope of block and prepare to shade
					float slope, s, sN, sNW, sW, sAvg, shaded;
					slope = chunkStub.slopes[x][z];
					
					sN = getBlockSlope(x, z, 0, -1, chunkStub, neighbors, slope);
					sNW = getBlockSlope(x, z, -1, -1, chunkStub, neighbors, slope);
					sW = getBlockSlope(x, z, -1, 0, chunkStub, neighbors, slope);
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
						s = (float) slope * 1.2f;
						s = Math.min(s, 1.4f);
						color = shade(color, s);
					}
		
					// Contour shading
					if(blockId==121) {
			
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
