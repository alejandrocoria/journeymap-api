package net.techbrew.mcjm.render;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;

import net.minecraft.src.Block;
import net.minecraft.src.EnumSkyBlock;
import net.minecraft.src.MathHelper;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.model.ChunkStub;

public class ChunkStandardRenderer extends BaseRenderer implements IChunkRenderer {

	static final int alphaDepth = 7;
	
	/**
	 * Constructor.
	 * @param mapBlocks
	 */
	ChunkStandardRenderer(MapBlocks mapBlocks) {
		super(mapBlocks);
	}

	/**
	 * Render blocks in the chunk for the standard world.
	 */
	@Override
	public void render(final Graphics2D g2D, final ChunkStub chunkStub, final boolean underground, 
			final int vSlice, final Map<Integer, ChunkStub> neighbors) {
		

		// Render the chunk image
		if(underground) {
			renderUnderground(g2D, chunkStub, vSlice, neighbors);			
		} else {
			renderSurface(g2D, chunkStub, vSlice, neighbors);
		}
		
	}
		
	/**
	 * Render blocks in the chunk for the surface.
	 */
	private void renderSurface(final Graphics2D g2D, final ChunkStub chunkStub, final int vSlice, final Map<Integer, ChunkStub> neighbors) {
		
		float slope, s, sN, sNW, sW, sAvg, shaded, h, hN, hW;
		
		// Initialize ChunkSub slopes if needed
		if(chunkStub.slopes==null) {
			chunkStub.slopes = new float[16][16];
			float minNorm = chunkStub.worldHeight;
			float maxNorm = 0;
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
		
		
		for (int x = 0; x < 16; x++) {
			blockLoop : for (int z = 0; z < 16; z++) {				
				
				int y = chunkStub.getSafeHeightValue(x, z);				
				if (y < 0) y=1; // Weird data error seen on World of Keralis
				
				// Get blockinfo for coords
				BlockInfo blockInfo = mapBlocks.getBlockInfo(chunkStub, x, y, z);				
				if (blockInfo == null) {
					paintBadBlock(x, y, z, g2D);
					continue blockLoop;
				}

				// Get base color for block
				Color color = blockInfo.getColor();
				if(color==null) {
					paintBadBlock(x, y, z, g2D);
					continue blockLoop;
				}
					
				// Paint deeper blocks if alpha used
				boolean useAlpha = blockInfo.alpha < 1F;		
				if (useAlpha) {
					
					// Check for surrounding water
					if(blockInfo.id==9||blockInfo.id==8) {
						BlockInfo bw = getBlock(x, y, z, -1, 0, chunkStub, neighbors, blockInfo);
						BlockInfo be = getBlock(x, y, z, +1, 0, chunkStub, neighbors, blockInfo);
						BlockInfo bn = getBlock(x, y, z, 0, -1, chunkStub, neighbors, blockInfo);
						BlockInfo bs = getBlock(x, y, z, 0, +1, chunkStub, neighbors, blockInfo);
						Set<Color> colors = new HashSet<Color>(5);
						colors.add(blockInfo.getColor());
						if(bw.id==8 || bw.id==9) colors.add(bw.getColor());
						if(be.id==8 || be.id==9) colors.add(be.getColor());
						if(bn.id==8 || bn.id==9) colors.add(bn.getColor());
						if(bs.id==8 || bs.id==9) colors.add(bs.getColor());
						if(colors.size()>1) {
							color = ColorCache.average(colors);
						}
						blockInfo.setColor(color);
						
					}
					paintDepth(chunkStub, blockInfo, x, y, z, g2D);
				}
				
				if(!MapBlocks.noShadows.contains(blockInfo)) {

					// Get slope of block and prepare to shade
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
					
					// Draw daytime map block
					g2D.setComposite(MapBlocks.OPAQUE);						
					g2D.setPaint(color);
					g2D.fillRect(x, z, 1, 1);
				}
				
				// Adjust color for light level
				int lightLevel = chunkStub.getSavedLightValue(EnumSkyBlock.Block, x,y + 1, z);
				if(lightLevel==0) lightLevel = 1;
				if (lightLevel < 15) {
					float diff = Math.min(1F, (lightLevel / 15F) + .1f);
					color = shadeNight(color, diff);
				}

				// Draw nighttime map block
				g2D.setComposite(MapBlocks.OPAQUE);
				g2D.setPaint(color);
				g2D.fillRect(x + 16, z, 1, 1);									
				
			}
		}
		
	}
	
	/**
	 * Render blocks in the chunk for underground.
	 * 
	 */
	public void renderUnderground(final Graphics2D g2D, final ChunkStub chunkStub, final int vSlice, final Map<Integer, ChunkStub> neighbors) {
		
		
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
		
		boolean hasAir;
		int blockId;
		int paintY;
		int lightLevel;
		
		for (int z = 0; z < 16; z++) {
			
			blockLoop: for (int x = 0; x < 16; x++) {			
				try {									
					
					int blockMaxY = mapBlocks.topNonSkyBlock(chunkStub, x, sliceMaxY, z);				

					hasAir = blockMaxY<sliceMaxY+1; // This might not be reliable.
					paintY = blockMaxY;
					lightLevel = -1;
		
					airloop: for (int y = blockMaxY; y >= 0; y--) {
														
						blockId = chunkStub.getBlockID(x, y, z);
		
						if (blockId == 0) {
							hasAir = true;
							continue airloop;
						}
						
						// Treat torches like there is air
						if(blockId == 50 || blockId == 76 || blockId == 76) {
							hasAir = true;
							// Check whether torch is mounted on the block below it
							if(chunkStub.getBlockMetadata(x, y, z)!=5) { // standing on block below=5
								continue airloop;
							}
						}						
						
						// Lava shortcut
						if(blockId==10 || blockId==11) {
							if(!hasAir) {
								paintBlock(x, z, Color.black, g2D);
								continue blockLoop;
							} else {
								lightLevel = 15;
								paintY = y;
								break airloop;
							}
						}
						
						// Arrived at a solid block with air above it
						if (hasAir) {
							paintY = y;
							
							if (caveLighting) {
								lightLevel = chunkStub.getSavedLightValue(EnumSkyBlock.Block, x,paintY+1, z);
								if(lightLevel > 0) {
									break airloop;		
								} else {
									// We've hit an unlit sublayer
									hasAir = false;
								}
							} else {
								break airloop;
							}
							
						} else if(y<=sliceMinY) {
							break airloop;
						}
					}
		
					if (paintY < 0 || !hasAir || (lightLevel<1 && caveLighting)) {
						// No air blocks in column at all
						paintBlock(x, z, Color.black, g2D);
						continue blockLoop;
					}			

					// Get block color
					BlockInfo info = mapBlocks.getBlockInfo(chunkStub, x, paintY, z);
					Color color = info.getColor();
					
					boolean keepflat = MapBlocks.noShadows.contains(info.id);
					
					if(!keepflat) {
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
					}
		
					// Adjust color for light level
					if (caveLighting && lightLevel < 15) {
						float darken = Math.min(1F, (lightLevel / 16F));
						float[] rgb = new float[4];
						rgb = color.getRGBColorComponents(rgb);
						color = new Color(
								MathHelper.clamp_float(rgb[0] * darken, 0f, 1f), 
								MathHelper.clamp_float(rgb[1] * darken, 0f, 1f), 
								MathHelper.clamp_float(rgb[2] * darken, 0f, 1f));
					}								
		
					// Draw block
					paintBlock(x, z, color, g2D);
		
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
		
	private void paintDepth(ChunkStub chunkStub, BlockInfo blockInfo, int x, int y, int z, final Graphics2D g2D) {		
		
		// See how deep the alpha goes
		Stack<BlockInfo> stack = new Stack<BlockInfo>();
		stack.push(blockInfo);
		int down = y;
		while(down>0) {
			down--;
			BlockInfo lowerBlock = mapBlocks.getBlockInfo(chunkStub, x, down, z);
			if(lowerBlock!=null) {
				stack.push(lowerBlock);
				if (lowerBlock.id==0 || lowerBlock.alpha==1f || y-down>alphaDepth) {
					break;
				}	
				
			} else {
				break;
			}
			
		}
		
		boolean thinWaterAdjust = (stack.size()==2 && blockInfo.id==Block.waterStill.blockID);
				
		// If bottom block is same as the top, don't bother with transparency
		if(stack.size()<2 || stack.peek().id==blockInfo.id) {
			g2D.setComposite(MapBlocks.OPAQUE);
			g2D.setPaint(blockInfo.getColor());
			g2D.fillRect(x, z, 1, 1);
		} else {
		
			// Bottom block is always opaque
			if(!stack.isEmpty()) {
				g2D.setComposite(MapBlocks.OPAQUE);
				g2D.setPaint(stack.pop().getColor());
				g2D.fillRect(x, z, 1, 1);
			}
			
			// Overlay blocks above using transparency
			while(!stack.isEmpty()) {
				BlockInfo lowerBlock = stack.pop();
				g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, lowerBlock.alpha));
				g2D.setPaint(lowerBlock.getColor());
				g2D.fillRect(x, z, 1, 1);
			}	
			
			if(thinWaterAdjust) {
				g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .3f));
				g2D.setPaint(blockInfo.getColor());
				g2D.fillRect(x, z, 1, 1);
			}
		}
		
	}
	
	@Override
	public int getHeightInSlice( final ChunkStub chunkStub, final int x, final int z, final int sliceMinY, final int sliceMaxY) {
		
		return mapBlocks.topNonSkyBlock(chunkStub, x, sliceMaxY, z) + 1;
		
//		boolean hasAir = false;
//		int blockId;
//		
//		int y = sliceMaxY;
//		for (; y > 0; y--) {
//			blockId = chunkStub.getBlockID(x, y, z);
//
//			if (blockId == 0) {
//				hasAir = true;
//				continue;
//			}
//			
//			if(blockId == 10 || blockId == 11 || blockId==51  || blockId == 8 || blockId == 9) { // lava or fire or water
//				if(hasAir) {
//					break;
//				} else {
//					return sliceMaxY;
//				}				
//			}
//			
//			if (hasAir) {
//				break;
//			} else if (y <= sliceMinY) {
//				y = sliceMaxY;
//				break;
//			}			
//		}
//		return y;
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
