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
import net.techbrew.mcjm.ChunkStub;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.PropertyManager;
import net.techbrew.mcjm.io.ChunkFileHandler;
import net.techbrew.mcjm.io.FileHandler;
import net.techbrew.mcjm.log.LogFormatter;

public class ChunkFlatRenderer implements IChunkRenderer {

	static int[] SPECIAL_BLOCK_IDS = {
		    31, // shrub or grass
		    32, // shrub or grass
		    50, // torch
			75, // redstone torch off
			76, // redstone torch on
			78, // snow
			85, // fence
			107, // fence gate
			113 }; // nether fence

	MapBlocks mapBlocks = new MapBlocks();
	boolean debug = false;
	long badBlockCount = 0;
	boolean caveLighting = PropertyManager.getInstance().getBoolean(PropertyManager.CAVE_LIGHTING_PROP);
	

	public BufferedImage getChunkImage(ChunkStub chunkStub,
			boolean underground, int vSlice,
			Map<Integer, ChunkStub> neighbors) {

		boolean fineLogging = JourneyMap.getLogger().isLoggable(Level.FINE);
		BufferedImage chunkImage = null;
		try {
			// Get data from chunk, then stop using it directly
			chunkImage = new BufferedImage(underground ? 16 : 32, 16, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2D = chunkImage.createGraphics();
			g2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

			try {
				
				if (chunkStub.worldType == -1) {
					paintNetherBlocks(chunkStub, vSlice, g2D, neighbors);
				} else if (chunkStub.worldType == 1) {
					paintEndBlocks(chunkStub, vSlice, g2D, neighbors);
				} else if (underground || chunkStub.hasNoSky) {
					paintUndergroundBlocks(chunkStub, vSlice, g2D, neighbors);
				} else {
					paintNormalBlocks(chunkStub, g2D, neighbors);
				}

			} catch (ArrayIndexOutOfBoundsException e) {
				JourneyMap.getLogger().log(Level.WARNING, LogFormatter.toString(e));
				return null; // Can happen when server isn't connected, just wait for next tick
			} catch (Throwable t) {
				String error = Constants.getMessageJMERR07(LogFormatter.toString(t));
				JourneyMap.getLogger().severe(error);
				return null;
			}
			
		} finally {
			if (chunkImage == null) {
				chunkImage = getPlaceholderChunk();
			}
		}

		return chunkImage;
	}

	@SuppressWarnings("unused")
	public void paintNormalBlocks(ChunkStub chunkStub, Graphics2D g2D, Map<Integer,ChunkStub> neighbors) {
		
		boolean fineLogging = JourneyMap.getLogger().isLoggable(Level.FINE);
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				
				int y = chunkStub.getSafeHeightValue(x, z);
				// Weird data error seen on World of Keralis
				if (y < 0) {
					if (fineLogging) {
						JourneyMap.getLogger().fine(
								"Unexpected height from heightmap at " //$NON-NLS-1$
										+ x + "," + z + ": " + y); //$NON-NLS-1$ //$NON-NLS-2$
					}
					return;
				}
				
				///////////////////////////////////////////////////////
				
				BlockInfo blockInfo = MapBlocks.getBlockInfo(chunkStub, x, y, z);
				
				if (blockInfo == null) {
					paintBadBlock(x, y, z, g2D);
					return;
				}			

				boolean useAlpha = mapBlocks.getBlockAlpha(blockInfo) < 1F;
				boolean isWater = (blockInfo.id == 8 || blockInfo.id == 9 || blockInfo.id == 79);		
				
				// Check for snow, torches, fence
//				if(!useAlpha) {
//					
//					BlockInfo upOneBlock = MapBlocks.getBlockInfo(chunkStub, x, y+1, z);
//					if(Arrays.binarySearch(SPECIAL_BLOCK_IDS, upOneBlock.id)>-1) { 
//						blockInfo = upOneBlock;
//					}
//
//				} 
				
				// Get base color for block
				Color color = mapBlocks.getBlockColor(chunkStub, blockInfo, x, y, z);
				if(color==null) {
					paintBadBlock(x, y, z, g2D);
					return;
				}
					
				// Paint deeper blocks if alpha used
				float alpha = 1F;
				if (useAlpha) {

					// See how deep the alpha goes
					BlockInfo[] stack = new BlockInfo[5];
					int depth;
					for (depth = 1; depth < 5; depth++) {
						BlockInfo iBlock = MapBlocks.getBlockInfo(chunkStub, x, Math.max(0, y - depth), z);
						stack[depth-1] = iBlock;
						if (iBlock.id==0 || mapBlocks.getBlockAlpha(iBlock)==0) {
							break;
						}					
					}

					// TODO: Ensure bottom color is opaque
					// Paint from the bottom up
					for (int i = stack.length-1; i >= 0; i--) {
						if(stack[i]==null) continue;
						alpha = mapBlocks.getBlockAlpha(stack[i]);
						if(alpha==0F) {
							g2D.setComposite(MapBlocks.CLEAR);
						} else if(alpha==1F) {
							g2D.setComposite(MapBlocks.OPAQUE);
						} else {
							g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
						}						
						g2D.setPaint(mapBlocks.getBlockColor(chunkStub, stack[i], x, y, z));
						g2D.fillRect(x, z, 1, 1);
					}
				}

				// Get alpha for final block
				
				if (useAlpha) {			
					alpha = mapBlocks.getBlockAlpha(blockInfo);
					if(alpha==0F) {
						g2D.setComposite(MapBlocks.CLEAR);
					} else if(alpha==1F) {
						g2D.setComposite(MapBlocks.OPAQUE);
					} else {
						g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
					}
					
				} 

				// One last safety check
				if(color==null) {
					paintBadBlock(x, y, z, g2D);
					return;
				}

				// Draw block
				g2D.setPaint(color);
				g2D.fillRect(x, z, 1, 1);

				// Get light level
				int lightLevel = chunkStub.getSavedLightValue(EnumSkyBlock.Block, x,y + 1, z);
				if (lightLevel < 15) {
					float diff = Math.min(1F, (lightLevel / 15F) + 0.07F);
					float[] rgb = new float[4];
					rgb = color.getRGBColorComponents(rgb);
					color = new Color(mapBlocks.safeColor(rgb[0] * diff),
							mapBlocks.safeColor(rgb[1] * diff), mapBlocks.safeColor(rgb[2] * diff));
				}

				// Draw lighted block
				g2D.setComposite(MapBlocks.OPAQUE);
				g2D.setPaint(color);
				g2D.fillRect(x + 16, z, 1, 1);
				
				
			}
		}
		
	}
	
	public void paintUndergroundBlocks(ChunkStub chunkStub, final int vSlice, final Graphics2D g2D,
			Map<Integer, ChunkStub> neighbors) {

		boolean fineLogging = JourneyMap.getLogger().isLoggable(Level.FINE);
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {

				
				try {
					int sliceMinY = Math.max((vSlice << 4) - 1, 0);
					int sliceMaxY = Math.min(((vSlice + 1) << 4) - 1, chunkStub.getSafeHeightValue(x, z)-1);
					if (sliceMinY == sliceMaxY) {
						sliceMaxY += 2;
					}
					if (blockOpenToSky(chunkStub, x, sliceMinY, z)) {
						paintClearBlock(x, vSlice, z, g2D);
						return;
					}
		
					boolean hasAir = false;
					int blockId = -1;
					int paintY = -1;
					int lightLevel = -1;
		
					int y = sliceMaxY;
					while (blockOpenToSky(chunkStub, x, y, z)) {
						if (y <= sliceMinY) {
							paintClearBlock(x, vSlice, z, g2D);
							return;
						}
						y--;
					}
		
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
							if (lightLevel > 1)
								break;
						}
					}
		
					// Block isn't viable to paint
					if (paintY == -1) {
						paintClearBlock(x, vSlice, z, g2D);
						return;
					}
		
					// Too dark
					if (caveLighting && lightLevel < 2) {
						paintClearBlock(x, vSlice, z, g2D);
						return;
					}
		
					// Get block color
					BlockInfo block = MapBlocks.getBlockInfo(chunkStub, x, paintY, z);
					Color color = mapBlocks.getBlockColor(chunkStub, block, x, y, z);
		
					// Get light level
					if (caveLighting && lightLevel < 15) {
						float darken = Math.min(1F, (lightLevel / 16F));
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

	public Boolean blockOpenToSky(ChunkStub chunk, int x, int blockY, int z) {

		if (mapBlocks.skyAbove(chunk, x, blockY, z)) {
			return true;
		}
		return false;
	}

	public void paintNetherBlocks(ChunkStub chunkStub, final int vSlice, final Graphics2D g2D,
			Map<Integer, ChunkStub> neighbors) {

		boolean fineLogging = JourneyMap.getLogger().isLoggable(Level.FINE);
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
							if (lightLevel > 1)
								break;
						}
					}
		
					// Block isn't viable to paint
					if (paintY == -1 || lightLevel < 0) {
						paintClearBlock(x, vSlice, z, g2D);
						return;
					}
		
					blockId = chunkStub.getBlockID(x, paintY, z);
		
					lightLevel = chunkStub.getSavedLightValue(EnumSkyBlock.Block, x,paintY + 1, z);
					if (lightLevel < 0) {
						paintClearBlock(x, vSlice, z, g2D);
						return; // how did we get here?
					}
		
					// Get block color
					BlockInfo block = MapBlocks.getBlockInfo(chunkStub, x, paintY, z);
					Color color = mapBlocks.getBlockColor(chunkStub, block, x, y, z);
		
					// Contour shading
					boolean isLava = (blockId == 10 || blockId == 11);
		
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
	

	public void paintEndBlocks(ChunkStub chunkStub, final int vSlice, final Graphics2D g2D,
			Map<Integer, ChunkStub> neighbors) {
		
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
							break;
						}
					}
		
					// Block isn't viable to paint
					if (paintY == -1) {
						paintClearBlock(x, vSlice, z, g2D);
						return;
					}
		
					blockId = chunkStub.getBlockID(x, paintY, z);
		
					lightLevel = chunkStub.getSavedLightValue(EnumSkyBlock.Block, x,paintY + 1, z);
		
					if (lightLevel < 10) {
						lightLevel += 2;
					}
					
					if(blockId==51 || blockId==200 || blockId==7) {
						lightLevel = 15;
					}		
		
					// Get block color
					BlockInfo block = MapBlocks.getBlockInfo(chunkStub, x, paintY, z);
					Color color = mapBlocks.getBlockColor(chunkStub, block, x, y,z);
		
					// Contour shading
					if(blockId==121) {
			
						// Get light level
						if (lightLevel < 15) {
							float darken = Math.min(1F, (lightLevel / 15F));
							float[] rgb = new float[4];
							rgb = color.getRGBColorComponents(rgb);
							color = new Color(rgb[0] * darken, rgb[1] * darken, rgb[2]
									* darken);
						}
					}
		
					// Draw lighted block
					g2D.setComposite(MapBlocks.OPAQUE);
					g2D.setPaint(color);
					g2D.fillRect(x, z, 1, 1);
					
					block = null;
		
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

	public int getHeightInSlice(ChunkStub chunkStub, int x, int z,
			int sliceMinY, int sliceMaxY) {
		int y = sliceMinY;

		BlockInfo blockInfo = null;
		while (y <= 1) {
			blockInfo = MapBlocks.getBlockInfo(chunkStub, x, y, z);
			if (blockInfo.id != 0) {
				y++;
			} else {
				break;
			}
		}

		while (y >= 1) {
			blockInfo = MapBlocks.getBlockInfo(chunkStub, x, y, z);
			if (blockInfo.id == 0) {
				y--;
			} else {
				break;
			}
		}
		return y;
	}

	/**
	 * Paint the block with half-clear red to indicate it's a problem.
	 * 
	 * @param x
	 * @param vSlice
	 * @param z
	 */
	public void paintBadBlock(final int x, final int y, final int z, final Graphics2D g2D) {
		g2D.setComposite(MapBlocks.SEMICLEAR);
		g2D.setPaint(Color.red);
		g2D.fillRect(x, z, 1, 1);
		badBlockCount++;
		if (badBlockCount % 10 == 0) {
			JourneyMap.getLogger().warning(
					"Bad block at " + x + "," + y + "," + z //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							+ ". Total bad blocks painted: " + badBlockCount); //$NON-NLS-1$
		}
	}

	/**
	 * Paint the block clear.
	 * 
	 * @param x
	 * @param vSlice
	 * @param z
	 */
	public void paintClearBlock(final int x, final int vSlice, final int z,
			final Graphics2D g2D) {
		g2D.setComposite(MapBlocks.OPAQUE);
		g2D.setPaint(MapBlocks.COLOR_TRANSPARENT);
		g2D.fillRect(x, z, 1, 1);
		g2D.clearRect(x, z, 1, 1);
	}

	// TODO: Make threadsafe
	private static volatile BufferedImage placeHolderChunk;

	static BufferedImage getPlaceholderChunk() {
		// if(placeHolderChunk==null) {
		placeHolderChunk = new BufferedImage(16, 16,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2D = placeHolderChunk.createGraphics();
		g2D.setComposite(MapBlocks.CLEAR);
		g2D.setPaint(Color.black);
		g2D.fillRect(0, 0, 16, 16);
		// }
		return placeHolderChunk;
	}

}
