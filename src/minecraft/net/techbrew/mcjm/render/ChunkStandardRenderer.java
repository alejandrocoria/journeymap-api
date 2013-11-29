package net.techbrew.mcjm.render;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;

import net.minecraft.src.Block;
import net.minecraft.src.EnumSkyBlock;
import net.minecraft.src.MathHelper;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.model.ChunkMD;

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
	public boolean render(final Graphics2D g2D, final ChunkMD chunkMd, final boolean underground, 
			final Integer vSlice, final ChunkMD.Set neighbors) {
		
		// Render the chunk image
		if(underground) {
			boolean ok = renderSurface(g2D, chunkMd, vSlice, neighbors, true);
			return renderUnderground(g2D, chunkMd, vSlice, neighbors) || ok;			
		} else {
			return renderSurface(g2D, chunkMd, vSlice, neighbors, false);
		}
		
	}
		
	/**
	 * Render blocks in the chunk for the surface.
	 */
	private boolean renderSurface(final Graphics2D g2D, final ChunkMD chunkMd, final Integer vSlice, final ChunkMD.Set neighbors, final boolean prepUnderground) {
		
		float slope, s, sN, sNW, sW, sS, sE, sAvg, shaded, h, hN, hW;
		
		// Initialize ChunkSub slopes if needed
		if(chunkMd.surfaceSlopes==null) {
			chunkMd.surfaceSlopes = new float[16][16];
			float minNorm = chunkMd.worldHeight;
			float maxNorm = 0;
			for(int y=0; y<16; y++)
			{
				for(int x=0; x<16; x++)
				{				
					h = chunkMd.getSafeHeightValue(x, y);
					hN = (y==0)  ? getBlockHeight(x, y, 0, -1, chunkMd, neighbors, h) : chunkMd.getSafeHeightValue(x, y-1);							
					hW = (x==0)  ? getBlockHeight(x, y, -1, 0, chunkMd, neighbors, h) : chunkMd.getSafeHeightValue(x-1, y);
					slope = ((h/hN)+(h/hW))/2f;
					chunkMd.surfaceSlopes[x][y] = slope;						
				}
			}
		}
		
		boolean chunkOk = false;
		for (int x = 0; x < 16; x++) {
			blockLoop : for (int z = 0; z < 16; z++) {				
				
				int y = chunkMd.getSafeHeightValue(x, z);				
				if (y < 0) y=1; // Weird data error seen on World of Keralis
				
				// Get blockinfo for coords
				BlockInfo blockInfo = mapBlocks.getBlockInfo(chunkMd, x, y, z);				
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
				if (!prepUnderground && useAlpha) {
					
					// Check for surrounding water
					if(blockInfo.id==9||blockInfo.id==8) {
						BlockInfo bw = getBlock(x, y, z, -1, 0, chunkMd, neighbors, blockInfo);
						BlockInfo be = getBlock(x, y, z, +1, 0, chunkMd, neighbors, blockInfo);
						BlockInfo bn = getBlock(x, y, z, 0, -1, chunkMd, neighbors, blockInfo);
						BlockInfo bs = getBlock(x, y, z, 0, +1, chunkMd, neighbors, blockInfo);
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
					paintDepth(chunkMd, blockInfo, x, y, z, g2D, false, prepUnderground);
					chunkOk = true;
					
				} else {
				
					if(!MapBlocks.noShadows.contains(blockInfo)) {
	
						// Get slope of block and prepare to shade
						slope = chunkMd.surfaceSlopes[x][z];
						
						if(blockInfo.id!=18) {
							sN = getBlockSlope(x, z, 0, -1, chunkMd, neighbors, slope, false);
							sNW = getBlockSlope(x, z, -1, -1, chunkMd, neighbors, slope, false);
							sW = getBlockSlope(x, z, -1, 0, chunkMd, neighbors, slope, false);
							sAvg = (sN+sNW+sW)/3f;
						} else {
							sN = getBlockSlope(x, z, 0, -1, chunkMd, neighbors, slope, false);	
							sS = getBlockSlope(x, z, 0, 1, chunkMd, neighbors, slope, false);
							sW = getBlockSlope(x, z, -1, 0, chunkMd, neighbors, slope, false);
							sE = getBlockSlope(x, z, 1, 0, chunkMd, neighbors, slope, false);
							sAvg = (sN+sS+sW+sE)/4f;
						}
						
						if(slope<1) {
							
							if(slope<=sAvg) {
								slope = slope*.6f;
							} else if(slope>sAvg) {
								if(blockInfo.id!=18){
									slope = (slope+sAvg)/2f;
								}
							}
							s = Math.max(slope * .8f, .1f);
							color = shade(color, s);
		
						} else if(slope>1) {
							
							if(sAvg>1) {
								if(slope>=sAvg) {
									if(blockInfo.id!=18){
										slope = slope*1.2f;
									}
								}
							}
							s = slope * 1.2f;
							s = Math.min(s, 1.4f);
							color = shade(color, s);
						}
					}
					
					if(prepUnderground) {
						color = ghostSurface(color);
					}
						
					// Draw daytime map block
					g2D.setComposite(MapBlocks.OPAQUE);						
					g2D.setPaint(color);
					g2D.fillRect(x, z, 1, 1);
					chunkOk = true;
				}				
				
				if(!prepUnderground) {
					// Adjust color for light level at night		
					int lightLevel = chunkMd.stub.getSavedLightValue(EnumSkyBlock.Block, x,y + 1, z);
					if(lightLevel==0) lightLevel = 1;
					if (lightLevel < 15) {
						float diff = Math.min(1F, (lightLevel / 15F) + .1f);
						if(diff!=1.0) {
							color = shadeNight(color, diff);
						}
					}
					
					// Draw nighttime map block
					g2D.setComposite(MapBlocks.OPAQUE);
					g2D.setPaint(color);
					g2D.fillRect(x + 16, z, 1, 1);	
				}
				chunkOk = true;
				
			}
		}
		return chunkOk;
	}
	
	/**
	 * Render blocks in the chunk for underground.
	 * 
	 */
	public boolean renderUnderground(final Graphics2D g2D, final ChunkMD chunkMd, final int vSlice, final ChunkMD.Set neighbors) {
				
		final int sliceMinY = Math.max((vSlice << 4) - 1, 0);
		final int hardSliceMaxY = ((vSlice + 1) << 4) - 1;
		int sliceMaxY = Math.min(hardSliceMaxY, chunkMd.worldObj.getActualHeight());
		if (sliceMinY == sliceMaxY) {
			sliceMaxY += 2;
		}
		
		// Initialize ChunkSub slopes if needed
		if(chunkMd.sliceSlopes==null) {
			chunkMd.sliceSlopes = new float[16][16];
			float minNorm = Math.min(((vSlice + 1) << 4) - 1, chunkMd.worldObj.getActualHeight());
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
		
		boolean hasAir;
		boolean hasWater;
		int blockId;
		int paintY;
		int lightLevel;
		
		boolean chunkOk = false;
		for (int z = 0; z < 16; z++) {
			
			blockLoop: for (int x = 0; x < 16; x++) {		
				
				// reset vars
				hasAir = false;
				hasWater = false;
				blockId = 0;
				paintY = -1;
				lightLevel =0;
	
				try {				
					
					int blockMaxY = mapBlocks.ceiling(chunkMd, x, sliceMaxY, z);
					if(blockMaxY==-1) {						
						continue blockLoop;
					}

					// Skip blocks open to the sky
					int checkY = Math.min(sliceMaxY-1, blockMaxY+1);
					if(MapBlocks.skyAbove(chunkMd, x, checkY, z)) {
						continue blockLoop;
					}					
				
					// Check for air at the top of column
					blockId = chunkMd.stub.getBlockID(x, blockMaxY+1, z);
					hasAir = blockId==0;
					hasWater = blockId==8 || blockId==9;
					paintY = blockMaxY;

					// Step downward to find air
					airloop: for (int y = blockMaxY; y >= 0; y--) {
														
						blockId = chunkMd.stub.getBlockID(x, y, z);
						
						// Water handling
						if((blockId == 8 || blockId == 9)) {		
							
							paintDepth(chunkMd, mapBlocks.getBlockInfo(chunkMd, x, y, z), x, y, z, g2D, true, false);
							continue blockLoop;
							
						} 
						
						// Found air
						if (blockId == 0) {
							hasAir = true;
							continue airloop;
						}
																	
						// Treat torches like there is air
						if(blockId == 50 || blockId == 76 || blockId == 76) {
							hasAir = true;
							// Check whether torch is mounted on the block below it
							if(chunkMd.stub.getBlockMetadata(x, y, z)!=5) { // standing on block below=5
								continue airloop;
							}
						}						
						else
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
								lightLevel = chunkMd.stub.getSavedLightValue(EnumSkyBlock.Block, x,paintY+1, z);
								//lightLevel = getMixedBrightnessForBlock(chunkMd, x,y,z);
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
					} // end airloop
					
					// No air blocks in column at all
					if (paintY < 0 || (!hasAir && !hasWater) || (lightLevel<1 && caveLighting)) {							
						// I see an unlit block; I want to paint it black.
						paintBlock(x, z, Color.black, g2D);
						continue blockLoop;
					} 					

					// Get block color
					BlockInfo info = mapBlocks.getBlockInfo(chunkMd, x, paintY, z);
					Color color = info.getColor();

					boolean keepflat = MapBlocks.noShadows.contains(info.id);					
					if(!keepflat) {
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
		
	private void paintDepth(ChunkMD chunkMd, BlockInfo blockInfo, int x, int y, int z, final Graphics2D g2D, final boolean useLighting, final boolean prepUnderground) {		
		
		// See how deep the alpha goes
		
		Stack<BlockInfo> stack = new Stack<BlockInfo>();
		stack.push(blockInfo);
		int maxDepth = alphaDepth;
		int down = y;
		while(down>0) {
			down--;
			BlockInfo lowerBlock = mapBlocks.getBlockInfo(chunkMd, x, down, z);
			if(lowerBlock!=null) {
				stack.push(lowerBlock);
				
				switch(lowerBlock.id) {
					case 79 : { // ice
						maxDepth = 2;
						break;
					}
					case 8 : { // water
						maxDepth = 4;
						break;
					}
					case 9 : { // water
						maxDepth = 4;
						break;
					}
					case 0 : { // air
						maxDepth = 256;
						break;
					}
				}
				
				if (lowerBlock.alpha==1f || y-down>maxDepth) {
					break;
				}	
				
			} else {
				break;
			}
			
		}
		
		float depth = stack.size();
		boolean thinWaterAdjust = (depth==2 && blockInfo.id==Block.waterStill.blockID);
		boolean isWater = (blockInfo.id==Block.waterStill.blockID || blockInfo.id==Block.waterMoving.blockID);		
		
		Color color;

		// Get color for bottom of stack
		color = stack.peek().getColor();
		
		if(useLighting) {
			int lightLevel = chunkMd.stub.getSavedLightValue(EnumSkyBlock.Block, x,down+1, z);
			if (lightLevel < 15) {
				float diff = Math.min(1F, (lightLevel / 15F) + .05f);
				if(diff!=1.0) {
					color = shadeNight(color, diff);
				}
			}
		} else if(isWater) {
			float darken = .68f;
			float[] rgb = new float[4];
			rgb = color.getRGBColorComponents(rgb);
			color = new Color(
					MathHelper.clamp_float(rgb[0] * darken, 0f, 1f), 
					MathHelper.clamp_float(rgb[1] * darken, 0f, 1f), 
					MathHelper.clamp_float(rgb[2] * darken, 0f, 1f));
		}
		
		g2D.setComposite(MapBlocks.OPAQUE);
		g2D.setPaint(color);
		g2D.fillRect(x, z, 1, 1);	
				
		// If bottom block is same as the top, don't bother with transparency
		if(stack.peek().id!=blockInfo.id) {
			stack.pop(); // already used it
			while(!stack.isEmpty()) {
				BlockInfo lowerBlock = stack.pop();
				g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, lowerBlock.alpha));
				color = lowerBlock.getColor();
				
				if(useLighting) {
					int lightLevel = chunkMd.stub.getSavedLightValue(EnumSkyBlock.Block, x,++down, z);
					if (lightLevel < 15) {
						float diff = Math.min(1F, (lightLevel / 15F) + .05f);
						if(diff!=1.0) {
							color = shadeNight(color, diff);
						}
					}
				} else if(isWater) {
					float darken = .7f;
					float[] rgb = new float[4];
					rgb = color.getRGBColorComponents(rgb);
					color = new Color(
							MathHelper.clamp_float(rgb[0] * darken, 0f, 1f), 
							MathHelper.clamp_float(rgb[1] * darken, 0f, 1f), 
							MathHelper.clamp_float(rgb[2] * darken, 0f, 1f));
				}
				
				if(prepUnderground) {
					color = ghostSurface(color);
				}
				
				g2D.setPaint(color);
				g2D.fillRect(x, z, 1, 1);
			}	
			
		} 
	}
	

	public int getHeightInSlice(final ChunkMD chunkMd, final int x, final int z, final int sliceMinY, final int sliceMaxY) {
		
		return mapBlocks.ceiling(chunkMd, x, sliceMaxY, z) + 1;
		
	}
	
	/**
	 * Get the height of the block at the coordinates + offsets.  Uses chunkMd.slopes.
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
