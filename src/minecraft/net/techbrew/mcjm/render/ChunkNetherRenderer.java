package net.techbrew.mcjm.render;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Map;
import java.util.logging.Level;

import net.minecraft.world.EnumSkyBlock;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
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
		
		int sliceMinY = Math.max((vSlice << 4) - 1, 0);
		int sliceMaxY = Math.min(((vSlice + 1) << 4) - 1, chunkStub.worldHeight);
		if (sliceMinY == sliceMaxY) {
			sliceMaxY += 2;
		}
		
		// Initialize ChunkSub slopes if needed
		if(chunkStub.slopes==null) {
			chunkStub.slopes = new float[16][16];
			float minNorm = Math.min(((vSlice + 1) << 4) - 1, chunkStub.worldHeight);
			float maxNorm = 0;
			float slope, h, hN, hW;
			
			for(int z=0; z<16; z++)
			{
				for(int x=0; x<16; x++)
				{									
					h = getHeightInSlice(chunkStub, x, z, sliceMinY, sliceMaxY);
					hN = (z==0)  ? getBlockHeight(x, z, 0, -1, chunkStub, neighbors, h, sliceMinY, sliceMaxY) : getHeightInSlice(chunkStub, x, z-1, sliceMinY, sliceMaxY);							
					hW = (x==0)  ? getBlockHeight(x, z, -1, 0, chunkStub, neighbors, h, sliceMinY, sliceMaxY) : getHeightInSlice(chunkStub, x-1, z, sliceMinY, sliceMaxY);
					slope = ((h/hN)+(h/hW))/2f;
					chunkStub.slopes[x][z] = slope;						
				}
			}
		}
		
		int lightLevel;
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {		
				try {
					String metaId = null;
					boolean hasAir = false;
					int blockId = -1;
					int y = getHeightInSlice(chunkStub, x, z, sliceMinY, sliceMaxY);
					blockId = chunkStub.getBlockID(x, y, z);
					boolean isLava = (blockId == 10 || blockId == 11);
					
					BlockInfo block = mapBlocks.getBlockInfo(chunkStub, x, y, z);		
					blockId = block.id;
					Color color = block.getColor();
					
					// Get light level
					if(isLava) {
						lightLevel = 14;
					} else {
						lightLevel = chunkStub.getSavedLightValue(EnumSkyBlock.Block, x, y + 1, z);
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
							s = Math.max(slope * .9f, .2f);
							color = shade(color, s);
		
						} else if(slope>1) {
							
							if(sAvg>1) {
								if(slope>=sAvg) {
									slope = slope*1.2f;
								}
							}
							s = (float) slope * 1.2f;
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

	@Override
	public int getHeightInSlice( final ChunkStub chunkStub, final int x, final int z, final int sliceMinY, final int sliceMaxY) {
		boolean hasAir = false;
		int blockId = 0;
		
		int y = sliceMaxY;
		for (; y > 0; y--) {
			blockId = chunkStub.getBlockID(x, y, z);

			if (blockId == 0) {
				hasAir = true;
				continue;
			}
			
			if(blockId==51) { // fire
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
	 * Get the height of the block at the coordinates + offsets.  Uses ChunkStub.slopes.
	 * @param x
	 * @param z
	 * @param offsetX
	 * @param offsetz
	 * @param currentChunk
	 * @param neighbors
	 * @param defaultVal
	 * @return
	 */
	public Float getBlockHeight(int x, int z, int offsetX, int offsetz, ChunkStub currentChunk, Map<Integer, ChunkStub> neighbors, float defaultVal, final int sliceMinY, final int sliceMaxY) {
		int newX = x+offsetX;
		int newZ = z+offsetz;
		
		int chunkX = currentChunk.xPosition;
		int chunkZ = currentChunk.zPosition;
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
		
		ChunkStub chunk = getChunk(x, z, offsetX, offsetz, currentChunk, neighbors);
		
		if(chunk!=null) {
			return (float) getHeightInSlice(chunk, newX, newZ, sliceMinY, sliceMaxY);
		} else {
			return defaultVal;
		}
	}


}
