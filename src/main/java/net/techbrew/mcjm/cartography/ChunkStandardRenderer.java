package net.techbrew.mcjm.cartography;

import net.minecraft.init.Blocks;
import net.minecraft.world.EnumSkyBlock;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.log.StatTimer;
import net.techbrew.mcjm.model.BlockMD;
import net.techbrew.mcjm.model.BlockUtils;
import net.techbrew.mcjm.model.ChunkMD;
import net.techbrew.mcjm.model.RGB;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;

public class ChunkStandardRenderer extends BaseRenderer implements IChunkRenderer {

	static final int alphaDepth = 7;

	/**
	 * Render blocks in the chunk for the standard world.
	 */
	@Override
	public boolean render(final Graphics2D g2D, final ChunkMD chunkMd, final boolean underground, 
			final Integer vSlice, final ChunkMD.Set neighbors) {

        // Initialize ChunkSub slopes if needed
        if(chunkMd.surfaceSlopes==null) {
            initSurfaceSlopes(chunkMd, neighbors);
        }

		// Render the chunk image
		if(underground) {
			boolean ok = renderSurface(g2D, chunkMd, vSlice, neighbors, true);
            if(!ok) {
                JourneyMap.getLogger().fine("The surface chunk didn't paint: " + chunkMd.toString());
            }
			ok = renderUnderground(g2D, chunkMd, vSlice, neighbors);
            if(!ok) {
                JourneyMap.getLogger().fine("The underground chunk didn't paint: " + chunkMd.toString());
            }
            return ok;
		} else {
			return renderSurface(g2D, chunkMd, vSlice, neighbors, false);
		}
		
	}

    /**
     * Initialize surface slopes in chunk if needed.
     * @param chunkMd
     * @param neighbors
     */
    private void initSurfaceSlopes(final ChunkMD chunkMd, final ChunkMD.Set neighbors) {
        float slope, h, hN, hW;
        chunkMd.surfaceSlopes = new float[16][16];
        for(int y=0; y<16; y++)
        {
            for(int x=0; x<16; x++)
            {
                h = chunkMd.getSlopeHeightValue(x, y);
                hN = (y==0)  ? getBlockHeight(x, y, 0, -1, chunkMd, neighbors, h) : chunkMd.getSlopeHeightValue(x, y - 1);
                hW = (x==0)  ? getBlockHeight(x, y, -1, 0, chunkMd, neighbors, h) : chunkMd.getSlopeHeightValue(x - 1, y);
                slope = ((h/hN)+(h/hW))/2f;
                chunkMd.surfaceSlopes[x][y] = slope;
            }
        }
    }
		
	/**
	 * Render blocks in the chunk for the surface.
	 */
	private boolean renderSurface(final Graphics2D g2D, final ChunkMD chunkMd, final Integer vSlice, final ChunkMD.Set neighbors, final boolean forUndergroundLayer) {

        StatTimer timer = StatTimer.get("ChunkStandardRenderer.renderSurface");
        timer.start();

        g2D.setComposite(BlockUtils.OPAQUE);

		boolean chunkOk = false;
		for (int x = 0; x < 16; x++) {
			blockLoop : for (int z = 0; z < 16; z++) {

                int y = Math.max(1, chunkMd.stub.getHeightValue(x, z));

				// Get blockinfo for coords
				BlockMD blockMD;
                do {
                    blockMD = BlockMD.getBlockMD(chunkMd, x, y, z);

                    // Null check
                    if (blockMD == null) {
                        paintBadBlock(x, y, z, g2D);
                        continue blockLoop;
                    }

                    if(blockMD.isAir()) {
                        y--;
                    } else {
                        break;
                    }
                } while(y>=0);

				// Get base color for block
				RGB color = blockMD.getColor(chunkMd, x, y, z);
				if(color==null) {
					paintBadBlock(x, y, z, g2D);
					continue blockLoop;
				}

				// Paint deeper blocks if alpha used, but not if just for underground layer
				boolean useAlpha = blockMD.getAlpha() < 1F;
				if (!forUndergroundLayer && useAlpha) {

					color = renderSurfaceAlpha(g2D, chunkMd, blockMD, neighbors, x, y, z);

				} else {

                    // Bevel color according to slope
                    if(!blockMD.hasFlag(BlockUtils.Flag.NoShadow)) {
                        surfaceSlopeColor(color, chunkMd, blockMD, neighbors, x, y, z);
					}

                    // Grey out if used to show outside in underground layer
                    if(forUndergroundLayer) {
                        color.ghostSurface();
                    }

                    // Draw daytime map block
                    g2D.setPaint(color.toColor());
                    g2D.fillRect(x, z, 1, 1);
                    chunkOk = true;
                }

                // Night color
				if(!forUndergroundLayer) {
					int lightLevel = Math.max(1, chunkMd.getSavedLightValue(EnumSkyBlock.Block, x,(y + 1), z));
					if (lightLevel < 15) {
						float diff = Math.min(1F, (lightLevel / 15F) + .1f);
						if(diff!=1.0) {
							color.moonlight(diff);
						}
					}

					// Draw nighttime map block
					g2D.setPaint(color.toColor());
					g2D.fillRect(x + 16, z, 1, 1);

                    chunkOk = true;
                }
			}
		}
        timer.stop();
		return chunkOk;
	}

    private RGB renderSurfaceAlpha(final Graphics2D g2D, final ChunkMD chunkMd, final BlockMD blockMD, final ChunkMD.Set neighbors, int x, int y, int z) {

        RGB color = blockMD.getColor(chunkMd, x, y, z);

        // Check for surrounding water
        if(blockMD.isWater()) {
            BlockMD bw = getBlock(x, y, z, -1, 0, chunkMd, neighbors, blockMD);
            BlockMD be = getBlock(x, y, z, +1, 0, chunkMd, neighbors, blockMD);
            BlockMD bn = getBlock(x, y, z, 0, -1, chunkMd, neighbors, blockMD);
            BlockMD bs = getBlock(x, y, z, 0, +1, chunkMd, neighbors, blockMD);
            Set<RGB> colors = new HashSet<RGB>(5);
            colors.add(color);
            if(bw.isWater()) colors.add(bw.getColor(chunkMd, x, y, z));
            if(be.isWater()) colors.add(be.getColor(chunkMd, x, y, z));
            if(bn.isWater()) colors.add(bn.getColor(chunkMd, x, y, z));
            if(bs.isWater()) colors.add(bs.getColor(chunkMd, x, y, z));
            if(!colors.isEmpty()) {
                color = RGB.average(colors);
            }
        }

        // Paint depth layers
        paintDepth(chunkMd, blockMD, x, y, z, g2D, false);

        return color;
    }

    private void surfaceSlopeColor(final RGB color, final ChunkMD chunkMd, final BlockMD blockMD, final ChunkMD.Set neighbors, int x, int ignored, int z) {

        float slope, bevel, sN, sNW, sW, sS, sE, sAvg;
        slope = chunkMd.surfaceSlopes[x][z];

        if(!blockMD.isFoliage()) {
            // Trees look more distinct if just beveled on upper left corners
            sN = getBlockSlope(x, z, 0, -1, chunkMd, neighbors, slope, false);
            sNW = getBlockSlope(x, z, -1, -1, chunkMd, neighbors, slope, false);
            sW = getBlockSlope(x, z, -1, 0, chunkMd, neighbors, slope, false);
            sAvg = (sN+sNW+sW)/3f;
        } else {
            // Everything else gets beveled based on average slope across n,s,e,w
            sN = getBlockSlope(x, z, 0, -1, chunkMd, neighbors, slope, false);
            sS = getBlockSlope(x, z, 0, 1, chunkMd, neighbors, slope, false);
            sW = getBlockSlope(x, z, -1, 0, chunkMd, neighbors, slope, false);
            sE = getBlockSlope(x, z, 1, 0, chunkMd, neighbors, slope, false);
            sAvg = (sN+sS+sW+sE)/4f;
        }

        bevel = 1f;
        if(slope<1) {
            if(slope<=sAvg) {
                slope = slope*.6f;
            } else if(slope>sAvg) {
                if(!blockMD.isFoliage()){
                    slope = (slope+sAvg)/2f;
                }
            }
            bevel = Math.max(slope * .8f, .1f);
        } else if(slope>1) {
            if(sAvg>1) {
                if(slope>=sAvg) {
                    if(!blockMD.isFoliage()){
                        slope = slope*1.2f;
                    }
                }
            }
            bevel = Math.min(slope * 1.2f, 1.4f);
        }

        if(bevel!=1f) {
            color.bevelSlope(bevel);
        }
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
        BlockMD info;

		int paintY;
		int lightLevel;

		boolean chunkOk = false;
		for (int z = 0; z < 16; z++) {

			blockLoop: for (int x = 0; x < 16; x++) {

				// reset vars
				hasAir = false;
				hasWater = false;

				paintY = -1;
				lightLevel =0;

				try {

					int blockMaxY = BlockUtils.ceiling(chunkMd, x, sliceMaxY, z);

					// Skip blocks open to the sky
					int checkY = Math.min(sliceMaxY - 1, blockMaxY + 1);
                    if(BlockUtils.skyAbove(chunkMd, x, checkY, z)) {
                        chunkOk = true;
                        continue blockLoop;
                    }

					// Check for air at the top of column
                    info = BlockMD.getBlockMD(chunkMd, x, blockMaxY + 1, z);

					hasAir = info.isAir();
					hasWater = info.isWater();
					paintY = blockMaxY;

					// Step downward to find air
					airloop: for (int y = blockMaxY; y >= 0; y--) {

                        info = BlockMD.getBlockMD(chunkMd, x, y, z);

						// Water handling
						if(info.isWater()) {
                            hasWater = true;
							paintDepth(chunkMd, BlockMD.getBlockMD(chunkMd, x, y, z), x, y, z, g2D, true);
							continue blockLoop;
						}

						// Found air
						if (info.isAir()) {
							hasAir = true;
							continue airloop;
						}

						// Treat torches like there is air
						if(info.isTorch()) {
							hasAir = true;
							// Check whether torch is mounted on the block below it
							if(chunkMd.stub.getBlockMetadata(x, y, z)!=5) { // standing on block below=5
								continue airloop;
							}
						}
						else
						// Lava shortcut
						if(info.isLava()) {
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
								lightLevel = chunkMd.getSavedLightValue(EnumSkyBlock.Block, x,paintY+1, z);
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
                        chunkOk = true;
						continue blockLoop;
					}

					// Get block color
					info = BlockMD.getBlockMD(chunkMd, x, paintY, z);
                    RGB color = info.getColor(chunkMd, x, paintY, z);

					boolean keepflat = info.hasFlag(BlockUtils.Flag.NoShadow);
					if(!keepflat) {
						// Contour shading
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
							color.bevelSlope(s);

						} else if(slope>1) {

							if(sAvg>1) {
								if(slope>=sAvg) {
									slope = slope*1.2f;
								}
							}
							s = slope * 1.2f;
							s = Math.min(s, 1.4f);
                            color.bevelSlope(s);
						}
					}

					// Adjust color for light level
					if (caveLighting && lightLevel < 15) {
						float factor = Math.min(1F, (lightLevel / 16F));
                        color.darken(factor);
					}

					// Draw block
					paintBlock(x, z, color.toColor(), g2D);
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

	private void paintDepth(ChunkMD chunkMd, BlockMD blockMD, int x, int y, int z, final Graphics2D g2D, final boolean useLighting) {

		// See how deep the alpha goes

		Stack<BlockMD> stack = new Stack<BlockMD>();
		stack.push(blockMD);
		int maxDepth = alphaDepth;
		int down = y;
		while(down>0) {
			down--;
			BlockMD lowerBlock = BlockMD.getBlockMD(chunkMd, x, down, z);
			if(lowerBlock!=null) {
				stack.push(lowerBlock);

                if(lowerBlock.isWater() || lowerBlock.getBlock()==Blocks.ice){
                    maxDepth = 4;
                } else if(lowerBlock.isAir()) {
                    maxDepth = 256;
                }

				if (lowerBlock.getAlpha()==1f || y-down>maxDepth) {
					break;
				}

			} else {
				break;
			}

		}

		RGB color;
        boolean isWater = blockMD.isWater();

		// Get color for bottom of stack
		color = stack.peek().getColor(chunkMd, x, down, z);

		if(useLighting) {
			int lightLevel = chunkMd.getSavedLightValue(EnumSkyBlock.Block, x,down+1, z);
			if (lightLevel < 15) {
				float diff = Math.min(1F, (lightLevel / 15F) + .05f);
				if(diff!=1.0) {
					color.moonlight(diff);
				}
			}
		} else if(isWater) {
			float factor = .68f;
            color.darken(factor);
		}

		g2D.setComposite(BlockUtils.OPAQUE);
		g2D.setPaint(color.toColor());
		g2D.fillRect(x, z, 1, 1);

		// If bottom block is same as the top, don't bother with transparency
		if(stack.peek().getBlock()!= blockMD.getBlock()) {
			stack.pop(); // already used it
			while(!stack.isEmpty()) {
				BlockMD lowerBlock = stack.pop();
				g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, lowerBlock.getAlpha()));
				color = lowerBlock.getColor(chunkMd, x, down, z);

				if(useLighting) {
					int lightLevel = chunkMd.getSavedLightValue(EnumSkyBlock.Block, x,++down, z);
					if (lightLevel < 15) {
						float diff = Math.min(1F, (lightLevel / 15F) + .05f);
						if(diff!=1.0) {
							color.moonlight(diff);
						}
					}
				} else if(isWater) {
					float factor = .7f;
					color.darken(factor);
				}
				
				g2D.setPaint(color.toColor());
				g2D.fillRect(x, z, 1, 1);
			}	
			
		} 
	}

	public int getHeightInSlice(final ChunkMD chunkMd, final int x, final int z, final int sliceMinY, final int sliceMaxY) {
		return BlockUtils.ceiling(chunkMd, x, sliceMaxY, z) + 1;
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
