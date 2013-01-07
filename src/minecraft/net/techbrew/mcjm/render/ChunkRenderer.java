package net.techbrew.mcjm.render;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import net.minecraft.src.EnumSkyBlock;
import net.techbrew.mcjm.ChunkStub;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.PropertyManager;
import net.techbrew.mcjm.log.LogFormatter;

public class ChunkRenderer {

	static int[] SPECIAL_BLOCK_IDS = { 50, // torch
			75, // redstone torch off
			76, // redstone torch on
			78, // snow
			85, // fence
			107, // fence gate
			113 }; // nether fence

	static MapBlocks mapBlocks = new MapBlocks();
	static boolean debug = false;
	static long badBlockCount = 0;
	static boolean caveLighting = PropertyManager.getInstance().getBoolean(
			PropertyManager.CAVE_LIGHTING_PROP);
	static Boolean doOriginalBump = false;

	public static BufferedImage getChunkImage(ChunkStub chunkStub,
			boolean underground, int vSlice,
			Map<Integer, ChunkStub> neighbors) {

		// System.out.println();
		BufferedImage chunkImage = null;
		try {
			// Get data from chunk, then stop using it directly
			chunkImage = new BufferedImage(underground ? 16 : 32, 16,
					BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2D = chunkImage.createGraphics();

			renderloop: if (underground) {
				// Underground / interior map
				for (int x = 0; x < 16; x++) {
					for (int z = 0; z < 16; z++) {
						if (!JourneyMap.isRunning()) {
							if (JourneyMap.getLogger().isLoggable(Level.FINE)) {
								JourneyMap.getLogger().fine(
										"Interupting render of chunk."); //$NON-NLS-1$
							}
							break renderloop;
						}
						try {
							if (chunkStub.hasNoSky) {
								if (chunkStub.worldType == -1) {
									paintNetherBlock(chunkStub, x, vSlice, z,
											g2D, neighbors);
								} else if (chunkStub.worldType == 1) {
									paintEndBlock(chunkStub, x, vSlice, z, g2D,
											neighbors);
								}
							} else {
								paintUndergroundBlock(chunkStub, x, vSlice, z, g2D, neighbors);
							}

						} catch (ArrayIndexOutOfBoundsException e) {
							JourneyMap.getLogger().log(Level.WARNING,
									LogFormatter.toString(e));
							return null; // Can happen when server isn't
											// connected, just wait for next
											// tick
						} catch (Throwable t) {
							String error = Constants.getMessageJMERR07(LogFormatter.toString(t));
							JourneyMap.getLogger().severe(error);
							return null;
						}
					}
				}

			} else {
				// Surface map
				for (int x = 0; x < 16; x++) {
					for (int z = 0; z < 16; z++) {
						int y = chunkStub.getHeightValue(x, z)-1;
						// Weird data error seen on World of Keralis
						if (y < 0) {
							if (JourneyMap.getLogger().isLoggable(Level.FINE)) {
								JourneyMap.getLogger().fine(
										"Unexpected height from heightmap at " //$NON-NLS-1$
												+ x + "," + z + ": " + y); //$NON-NLS-1$ //$NON-NLS-2$
							}
							return null;
						}
						try {
							// Paint the block
							paintNormalBlock(chunkStub, x, y, z, g2D, true, neighbors);
						} catch (ArrayIndexOutOfBoundsException e) {
							// Can happen when server isn't connected, just wait
							// for
							// next tick
							JourneyMap.getLogger().log(Level.WARNING,
									LogFormatter.toString(e));
							return null;
						} catch (Throwable t) {
							String error = Constants.getMessageJMERR07(LogFormatter.toString(t));
							JourneyMap.getLogger().severe(error);
							return null;
						}
					}
				}
			}
		} finally {
			if (chunkImage == null) {
				chunkImage = getPlaceholderChunk();
			}
		}

		return chunkImage;
	}

	@SuppressWarnings("unused")
	static void paintNormalBlock(ChunkStub chunkStub, int x, int y, int z, Graphics2D g2D,
			boolean checkDepth, Map<Integer,ChunkStub> neighbors) {
		
		int[] blockInfo = MapBlocks.getBlockInfo(chunkStub, x, y, z);
		if (blockInfo == null) {
			paintBadBlock(x, y, z, g2D);
			return;
		}

		boolean useAlpha = mapBlocks.getBlockAlpha(blockInfo) < 1F;
		boolean isWater = (blockInfo[0] == 8 || blockInfo[0] == 9 || blockInfo[0] == 79);
		boolean isTree = (blockInfo[0] == 18);
		
		// Check for snow, torches, fence
		if(!useAlpha) {
			int[] upOneBlock = MapBlocks.getBlockInfo(chunkStub, x, y+1, z);
			if(Arrays.binarySearch(SPECIAL_BLOCK_IDS, upOneBlock[0])>-1) { 
				blockInfo = upOneBlock;
			}
		} 
		
		// Get base color for block
		Color color = mapBlocks.getBlockColor(chunkStub, blockInfo, x, y, z);
		if(color==null) {
			paintBadBlock(x, y, z, g2D);
			return;
		}
			
		// Paint deeper blocks if alpha used
		if (useAlpha && checkDepth) {

			// See how deep the alpha goes
			int depth;
			for (depth = 1; depth < 6; depth++) {
				int[] iBlock = MapBlocks.getBlockInfo(chunkStub, x, Math.max(0, y - depth), z);
				if (iBlock[0]==0 || mapBlocks.getBlockAlpha(iBlock)==0) {
					break;
				}					
			}

			// Start with color
			g2D.setComposite(MapBlocks.OPAQUE);
			g2D.setPaint(color);
			g2D.fillRect(x, z, 1, 1);

			// Paint from the bottom up
			for (int i = depth; i > 0; i--) {
				if(y-i<0) break;
				paintNormalBlock(chunkStub, x, y - i, z, g2D, false, neighbors);
			}
		}

		// Get alpha for final block
		float alpha = 1F;
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

		// Contour shading for anything except water
		if (!isWater && checkDepth) {
			g2D.setComposite(MapBlocks.OPAQUE);

			float diff = getSlopeDiff(chunkStub, x, y, z, neighbors);
			if(isTree && diff>1) diff = diff*.5F;
			
			if(diff!=0) {
				float[] rgb = new float[4];
				rgb = color.getRGBColorComponents(rgb);
				color = new Color(mapBlocks.safeColor(rgb[0] * diff), mapBlocks.safeColor(rgb[1]
						* diff), mapBlocks.safeColor(rgb[2] * diff));
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
	
	static float getSlopeDiff(ChunkStub chunkStub, int x, int y, int z, Map<Integer,ChunkStub> neighbors) {
		Integer n = y, w = y, nw = y;

		if (z > 0) {
			//n = chunkStub.heightMap[(z - 1) << 4 | (x)] - 1; // north
			n = chunkStub.getHeightValue(x, z-1) -1;
		} else {
			Integer stubHash = ChunkStub.toHashCode(chunkStub.xPosition, chunkStub.zPosition - 1);
			ChunkStub nchunk = neighbors.get(stubHash);
			if(nchunk!=null && nchunk.doMap) {
				//n = nchunk.heightMap[15 << 4 | (x)] - 1;
				n = nchunk.getHeightValue(x, 15)-1;
			} else {
				return 0; 
			}
		}
		if (x > 0) {
			//w = chunkStub.heightMap[(z) << 4 | (x - 1)] - 1; // west
			w = chunkStub.getHeightValue(x-1, z)-1;
		} else {
			Integer stubHash = ChunkStub.toHashCode(chunkStub.xPosition-1, chunkStub.zPosition);
			ChunkStub wchunk = neighbors.get(stubHash);
			if(wchunk!=null && wchunk.doMap) {
				//w = wchunk.heightMap[z << 4 | 15] - 1;
				w = wchunk.getHeightValue(15, z)-1;
			} else {
				return 0; 
			}
		}
		if(x>0 && z>0) {
			//nw = chunkStub.heightMap[(z-1) << 4 | (x-1)] - 1;
			nw = chunkStub.getHeightValue(x-1, z-1)-1;
		} else if (x == 0 && z == 0) {
			Integer stubHash = ChunkStub.toHashCode(chunkStub.xPosition-1, chunkStub.zPosition-1);
			ChunkStub nwchunk = neighbors.get(stubHash);
			if(nwchunk!=null && nwchunk.doMap) {
				//nw = nwchunk.heightMap[15 << 4 | 15] - 1; // northwest
				nw = nwchunk.getHeightValue(15, 15)-1;
			} else {
				return 0; 
			}
		}
			
		float gradient1 = (nw-y)*.20F;
		float gradient2 = (w-y)*.20F;
		float gradient3 = (n-y)*.20F;
		float diff;
		if(nw!=y) {
			diff = 1F- ((gradient1 + gradient2 + gradient3));
		} else {
			diff = 1F - ((gradient2 + gradient3)); 
		}
		diff = (float) Math.min(2.5, diff);
		return diff;
	}

	static void paintUndergroundBlock(ChunkStub chunkStub, final int x,
			final int vSlice, final int z, final Graphics2D g2D, Map<Integer, ChunkStub> neighbors) {

		try {
			int sliceMinY = Math.max((vSlice << 4) - 1, 0);
			int sliceMaxY = Math.min(((vSlice + 1) << 4) - 1, chunkStub.getHeightValue(x, z)-1);
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
			int[] block = MapBlocks.getBlockInfo(chunkStub, x, paintY, z);
			Color color = mapBlocks.getBlockColor(chunkStub, block, x, y, z);

			// Contour lighting
			float diff = getSlopeInSlice(chunkStub, x, z, sliceMinY, y,
					sliceMaxY, neighbors);
			// System.out.println("y=" + y + ",diff=" + diff);
			if (diff < 0 || diff > 1) {
				float[] rgb = new float[4];
				rgb = color.getRGBColorComponents(rgb);
				color = new Color(mapBlocks.safeColor(rgb[0] * diff),
						mapBlocks.safeColor(rgb[1] * diff),
						mapBlocks.safeColor(rgb[2] * diff));
			}

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

	static Boolean blockOpenToSky(ChunkStub chunk, int x, int blockY, int z) {

		if (mapBlocks.skyAbove(chunk, x, blockY, z)) {
			return true;
		}
		return false;
	}

	static void paintNetherBlock(ChunkStub chunkStub, final int x,
			final int vSlice, final int z, final Graphics2D g2D,
			Map<Integer, ChunkStub> neighbors) {

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
			int[] block = MapBlocks.getBlockInfo(chunkStub, x, paintY, z);
			Color color = mapBlocks.getBlockColor(chunkStub, block, x, y, z);

			// Contour shading
			boolean isLava = (blockId == 10 || blockId == 11);
			if (!isLava) {

				float diff = getSlopeInSlice(chunkStub, x, z, sliceMinY, y, sliceMaxY, neighbors);
				// System.out.println("y=" + y + ",diff=" + diff);
				if (diff < 0 || diff > 1) {
					float[] rgb = new float[4];
					rgb = color.getRGBColorComponents(rgb);
					color = new Color(mapBlocks.safeColor(rgb[0] * diff),
							mapBlocks.safeColor(rgb[1] * diff),
							mapBlocks.safeColor(rgb[2] * diff));
				}
			}

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

	static float getSlopeInSlice(ChunkStub chunkStub, final int x, final int z,
			final int sliceMinY, final int y, final int sliceMaxY,
			Map<Integer, ChunkStub> neighbors) {
		Integer n = y, w = y, nw = y;

		if (z > 0) {
			n = getHeightInSlice(chunkStub, x, z - 1, sliceMinY, sliceMaxY);
		} else {
			Integer stubHash = ChunkStub.toHashCode(chunkStub.xPosition,
					chunkStub.zPosition - 1);
			ChunkStub nchunk = neighbors.get(stubHash);
			if (nchunk != null) {
				n = getHeightInSlice(nchunk, x, 15, sliceMinY, sliceMaxY);
			}
		}
		if (x > 0) {
			w = getHeightInSlice(chunkStub, x - 1, z, sliceMinY, sliceMaxY);
		} else {
			Integer stubHash = ChunkStub.toHashCode(chunkStub.xPosition - 1,
					chunkStub.zPosition);
			ChunkStub wchunk = neighbors.get(stubHash);
			if (wchunk != null) {
				w = getHeightInSlice(wchunk, 15, z, sliceMinY, sliceMaxY);
			}
		}
		if (x > 0 && z > 0) {
			nw = getHeightInSlice(chunkStub, x - 1, z - 1, sliceMinY, sliceMaxY);
		} else if (x == 0 && z == 0) {
			Integer stubHash = ChunkStub.toHashCode(chunkStub.xPosition - 1,
					chunkStub.zPosition - 1);
			ChunkStub nwchunk = neighbors.get(stubHash);
			if (nwchunk != null) {
				nw = getHeightInSlice(nwchunk, 15, 15, sliceMinY, sliceMaxY);
			}
		}

		float gradient1 = (nw - y) * .20F;
		float gradient2 = (w - y) * .20F;
		float gradient3 = (n - y) * .20F;
		float slope;
		if (nw != y) {
			slope = 1F - ((gradient1 + gradient2 + gradient3));
		} else {
			slope = 1F - ((gradient2 + gradient3));
		}
		slope = (float) Math.min(2.5, slope);
		return slope;
	}

	static void paintEndBlock(ChunkStub chunkStub, final int x,
			final int vSlice, final int z, final Graphics2D g2D,
			Map<Integer, ChunkStub> neighbors) {

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
			int[] block = MapBlocks.getBlockInfo(chunkStub, x, paintY, z);
			Color color = mapBlocks.getBlockColor(chunkStub, block, x, y,z);

			// Contour shading
			if(blockId==121) {
				float diff = getSlopeDiff(chunkStub, x, y, z, neighbors);
				
				if (diff != 0) {
					float[] rgb = new float[4];
					rgb = color.getRGBColorComponents(rgb);
					color = new Color(mapBlocks.safeColor(rgb[0] * diff),
							mapBlocks.safeColor(rgb[1] * diff),
							mapBlocks.safeColor(rgb[2] * diff));
				}
	
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

	static int getHeightInSlice(ChunkStub chunkStub, int x, int z,
			int sliceMinY, int sliceMaxY) {
		int y = sliceMinY;

		int[] blockInfo = null;
		while (y <= 1) {
			blockInfo = MapBlocks.getBlockInfo(chunkStub, x, y, z);
			if (blockInfo[0] != 0) {
				y++;
			} else {
				break;
			}
		}

		while (y >= 1) {
			blockInfo = MapBlocks.getBlockInfo(chunkStub, x, y, z);
			if (blockInfo[0] == 0) {
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
	static void paintBadBlock(final int x, final int y, final int z, final Graphics2D g2D) {
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
	static void paintClearBlock(final int x, final int vSlice, final int z,
			final Graphics2D g2D) {
		g2D.setComposite(MapBlocks.OPAQUE);
		g2D.setPaint(MapBlocks.COLOR_TRANSPARENT);
		g2D.fillRect(x, z, 1, 1);
		g2D.clearRect(x, z, 1, 1);
	}

	private static BufferedImage placeHolderChunk;

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
