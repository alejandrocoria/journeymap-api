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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.src.EnumSkyBlock;
import net.minecraft.src.MathHelper;
import net.minecraft.src.WorldProvider;
import net.minecraft.src.WorldProviderHell;
import net.minecraft.src.WorldProviderEnd;
import net.techbrew.mcjm.ChunkStub;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.PropertyManager;
import net.techbrew.mcjm.io.ChunkFileHandler;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.log.LogFormatter;

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
		
		float slope, s, sN, sNW, sW, sAvg, shaded;
		
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				
				int y = chunkStub.getSafeHeightValue(x, z);				
				if (y < 0) y=1; // Weird data error seen on World of Keralis
				
				// Get blockinfo for coords
				BlockInfo blockInfo = mapBlocks.getBlockInfo(chunkStub, x, y, z);				
				if (blockInfo == null) {
					paintBadBlock(x, y, z, g2D);
					continue;
				}			

				// Get base color for block
				Color color = blockInfo.color;
				if(color==null) {
					paintBadBlock(x, y, z, g2D);
					return;
				}
					
				// Paint deeper blocks if alpha used
				boolean useAlpha = blockInfo.alpha < 1F;		
				float alpha = 1F;
				if (useAlpha) {
					
					// Check for surrounding water
					if(blockInfo.id==9||blockInfo.id==8) {
						BlockInfo bw = getBlock(x, y, z, -1, 0, chunkStub, neighbors, blockInfo);
						BlockInfo be = getBlock(x, y, z, +1, 0, chunkStub, neighbors, blockInfo);
						BlockInfo bn = getBlock(x, y, z, 0, -1, chunkStub, neighbors, blockInfo);
						BlockInfo bs = getBlock(x, y, z, 0, +1, chunkStub, neighbors, blockInfo);
						Set<Color> colors = new HashSet<Color>(5);
						colors.add(blockInfo.color);
						if(bw.id==8 || bw.id==9) colors.add(bw.color);
						if(be.id==8 || be.id==9) colors.add(be.color);
						if(bn.id==8 || bn.id==9) colors.add(bn.color);
						if(bs.id==8 || bs.id==9) colors.add(bs.color);
						if(colors.size()>1) {
							Iterator<Color> iter = colors.iterator();
							color = iter.next();
							while(iter.hasNext()) {
								//color = Color.white;
								color = mapBlocks.average(color, iter.next());
							}
						}
						blockInfo.color = color;
						
					}
					
					paintDepth(chunkStub, blockInfo, x, y, z, g2D);
				} else {

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
					
					if(x==0 || z==0) {
						color = Color.white;
					}
					
					// TODO: Remove
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

				// Draw lighted block on the night side of the image
				g2D.setComposite(MapBlocks.OPAQUE);
				g2D.setPaint(color);
				g2D.fillRect(x + 16, z, 1, 1);									
				
			}
		}
		
	}
	
	/**
	 * Render blocks in the chunk for underground.
	 * 
	 * TODO remove printlns here and in BaseRenderer
	 */
	public void renderUnderground(final Graphics2D g2D, final ChunkStub chunkStub, final int vSlice, final Map<Integer, ChunkStub> neighbors) {
		
		int sliceMinY = Math.max((vSlice << 4) - 1, 0);
		int defaultSliceMaxY = ((vSlice + 1) << 4) - 1;
		
		boolean hasAir;
		int blockId;
		int paintY;
		int lightLevel;
		boolean usefulPaint = false;
		
		for (int z = 0; z < 16; z++) {
			blockLoop: for (int x = 0; x < 16; x++) {			
				try {
					int sliceMaxY = mapBlocks.topNonSkyBlock(chunkStub, x, defaultSliceMaxY, z) + 1;

					hasAir = sliceMaxY<defaultSliceMaxY+1; // TODO: This might not be reliable.
					paintY = sliceMaxY;
					lightLevel = -1;
		
					airloop: for (int y = sliceMaxY; y >= 0; y--) {
														
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
						
						// If lava with no air, do nothing
						if(!hasAir && (blockId==10 || blockId==11)) {
							paintClearBlock(x, vSlice, z, g2D);
							continue blockLoop;
						}
						
						// Treat water with depth
						if(blockId==8 || blockId==9) {							
							if(hasAir) {
								lightLevel = chunkStub.getSavedLightValue(EnumSkyBlock.Block, x,paintY + 1, z);
								if (caveLighting && lightLevel < 1) {
									// No lit blocks in column
									paintClearBlock(x, vSlice, z, g2D);
									continue blockLoop;
								}
								
								BlockInfo blockInfo = mapBlocks.getBlockInfo(chunkStub, x, y, z);
								paintDepth(chunkStub, blockInfo, x, y, z, g2D);
							}
							continue blockLoop;
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
							
						}
					}
		
					if (paintY < 0) {
						// No air blocks in column at all
						paintClearBlock(x, vSlice, z, g2D);
						continue blockLoop;
					}					
		
					boolean cheatLight = lightLevel<1;
					if(cheatLight) {
						lightLevel=1;
					}
					
					if (caveLighting && lightLevel < 1) {
						// No lit blocks in column
						paintClearBlock(x, vSlice, z, g2D);
						continue blockLoop;
					}
		
					// Get block color
					BlockInfo block = mapBlocks.getBlockInfo(chunkStub, x, paintY, z);
					Color color = block.color;
		
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
					paintBlock(x, vSlice, z, color, g2D);
					//if(!usefulPaint && !cheatLight) usefulPaint=true;
					if(!usefulPaint) usefulPaint=true;
		
				} catch (Throwable t) {
					paintBadBlock(x, vSlice, z, g2D);
					String error = Constants.getMessageJMERR07("x,vSlice,z = " + x + "," //$NON-NLS-1$ //$NON-NLS-2$
							+ vSlice + "," + z + " : " + t.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
					JourneyMap.getLogger().severe(error);
					JourneyMap.getLogger().log(Level.SEVERE, LogFormatter.toString(t));
				}
				
			}
		}
		
		if(!usefulPaint) {
			g2D.setComposite(MapBlocks.OPAQUE);
			g2D.setBackground(Color.DARK_GRAY);
			g2D.clearRect(0,0,16,16);
			//System.out.println("Cleared chunk: " + chunkStub.xPosition + "," + chunkStub.zPosition);
		}

	}
		
	private void paintDepth(ChunkStub chunkStub, BlockInfo blockInfo, int x, int y, int z, final Graphics2D g2D) {
		
		// See how deep the alpha goes
		Stack<BlockInfo> stack = new Stack<BlockInfo>();
		stack.push(blockInfo);
		int down = y;
		while(down>=0) {
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
		
		boolean thinWaterAdjust = stack.size()==2;
				
		// If bottom block is water, don't bother with transparency
		if(stack.size()<2 || stack.peek().id==8 || stack.peek().id==9) {
			g2D.setComposite(MapBlocks.OPAQUE);
			g2D.setPaint(blockInfo.color);
			g2D.fillRect(x, z, 1, 1);
		} else {
		
			// Bottom block is always opaque
			if(!stack.isEmpty()) {
				g2D.setComposite(MapBlocks.OPAQUE);
				g2D.setPaint(stack.pop().color);
				g2D.fillRect(x, z, 1, 1);
			}
			
			// Overlay blocks above using transparency
			while(!stack.isEmpty()) {
				BlockInfo lowerBlock = stack.pop();
				g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, lowerBlock.alpha));
				g2D.setPaint(lowerBlock.color);
				g2D.fillRect(x, z, 1, 1);
			}	
			
			if(thinWaterAdjust) {
				g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f));
				g2D.setPaint(blockInfo.color);
				g2D.fillRect(x, z, 1, 1);
			}
		}
		
	}

	/**
	 * Darken a color by a factor, add a blue tint.
	 * @param original
	 * @param factor
	 * @return
	 */
	public Color shade(Color original, float factor) {
		
		if(factor<0) {
			throw new IllegalArgumentException("factor can't be negative");
		}
		
		float bluer = (factor>=1) ? 1f : .8f;
		
		float[] rgb = new float[4];
		rgb = original.getRGBColorComponents(rgb);
		return new Color(
				mapBlocks.safeColor(rgb[0] * bluer * factor),
				mapBlocks.safeColor(rgb[1] * bluer * factor),
				mapBlocks.safeColor(rgb[2] * factor));
		
	}
	
	/**
	 * Darken a color by a factor, add a blue tint.
	 * @param original
	 * @param factor
	 * @return
	 */
	public Color shadeNight(Color original, float factor) {
		
		if(factor<0) {
			throw new IllegalArgumentException("factor can't be negative");
		}
		
		float[] rgb = new float[4];
		rgb = original.getRGBColorComponents(rgb);
		return new Color(
				mapBlocks.safeColor(rgb[0] * factor),
				mapBlocks.safeColor(rgb[1] * factor),
				mapBlocks.safeColor(rgb[2] * (factor+.1f)));
		
	}


}
