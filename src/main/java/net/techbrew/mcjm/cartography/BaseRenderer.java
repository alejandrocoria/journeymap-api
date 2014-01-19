package net.techbrew.mcjm.cartography;


import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.EnumSkyBlock;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.io.PropertyManager;
import net.techbrew.mcjm.model.BlockMD;
import net.techbrew.mcjm.model.BlockUtils;
import net.techbrew.mcjm.model.ChunkMD;

import java.awt.*;
import java.util.logging.Level;

/**
 * Base class for methods reusable across renderers.
 * @author mwoodman
 *
 */
public abstract class BaseRenderer {

	boolean caveLighting = PropertyManager.getInstance().getBoolean(PropertyManager.Key.CAVE_LIGHTING);
	final boolean fineLogging = JourneyMap.getLogger().isLoggable(Level.FINE);
	
	boolean debug = false;
	long badBlockCount = 0;

    /**
     * Paint the block with half-clear red to indicate it's a problem.
     * @param x
     * @param y
     * @param z
     * @param g2D
     */
	public void paintBadBlock(final int x, final int y, final int z, final Graphics2D g2D) {
		g2D.setComposite(BlockUtils.SEMICLEAR);
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
     * @param x
     * @param vSlice
     * @param z
     * @param g2D
     */
	public void paintClearBlock(final int x, final int vSlice, final int z,
			final Graphics2D g2D) {
		g2D.setComposite(BlockUtils.OPAQUE);
		g2D.setBackground(BlockUtils.COLOR_TRANSPARENT);
		g2D.clearRect(x, z, 1, 1);
	}

    /**
     * Paint the block.
     * @param x
     * @param z
     * @param color
     * @param g2D
     */
	public void paintBlock(final int x, final int z, final Color color,
			final Graphics2D g2D) {
		g2D.setComposite(BlockUtils.OPAQUE);
		g2D.setColor(color);
		g2D.fillRect(x, z, 1, 1);
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
	public Float getBlockHeight(int x, int z, int offsetX, int offsetz, ChunkMD currentChunk, ChunkMD.Set neighbors, float defaultVal) {
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
			return (float) chunk.getSafeHeightValue(newX, newZ);
		} else {
			return defaultVal;
		}
	}
	
	/**
	 * Get the slope of the block at the coordinates + offsets.  Uses chunkMd.slopes.
	 * @param x
	 * @param z
	 * @param offsetX
	 * @param offsetz
	 * @param currentChunk
	 * @param neighbors
	 * @param defaultVal
	 * @return
	 */
	public Float getBlockSlope(int x, int z, int offsetX, int offsetz, ChunkMD currentChunk, ChunkMD.Set neighbors, float defaultVal, boolean underground) {
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
		
		ChunkMD chunkMd = getChunk(x, z, offsetX, offsetz, currentChunk, neighbors);		
		if(chunkMd!=null) {
			float[][] slopes = underground ? chunkMd.sliceSlopes : chunkMd.surfaceSlopes;
			if(slopes==null) {
				return defaultVal;
			} else {
				return slopes[newX][newZ];
			}
		} else {
			return defaultVal;
		}
	}
	
	/**
	 * Get the slope of the block at the coordinates + offsets.  Uses chunkMd.slopes.
	 * @param x
	 * @param z
	 * @param offsetX
	 * @param offsetz
	 * @param currentChunk
	 * @param neighbors
	 * @param defaultVal
	 * @return
	 */
	public int getBlockLight(int x, int y, int z, int offsetX, int offsetz, ChunkMD currentChunk, ChunkMD.Set neighbors, int defaultVal) {
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
			return chunk.getSavedLightValue(EnumSkyBlock.Block, x,y+1, z);
		} else {
			return defaultVal;
		}
	}
	
	/**
	 * Get the block at the coordinates + offsets.  Uses chunkMd.slopes.
	 * @param x
	 * @param z
	 * @param offsetX
	 * @param offsetz
	 * @param currentChunk
	 * @param neighbors
	 * @param defaultVal
	 * @return
	 */
	public BlockMD getBlock(int x, int y, int z, int offsetX, int offsetz, ChunkMD currentChunk, ChunkMD.Set neighbors, BlockMD defaultVal) {
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
			return BlockMD.getBlockMD(chunk, newX, y, newZ);
		} else {
			return defaultVal;
		}
	}
	
	/**
	 * Gets the chunkMd at the coordinates + offsets.
	 * @param x
	 * @param z
	 * @param offsetX
	 * @param offsetz
	 * @param currentChunk
	 * @param neighbors
	 * @return
	 */
	ChunkMD getChunk(int x, int z, int offsetX, int offsetz, ChunkMD currentChunk, ChunkMD.Set neighbors) {
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
		
		ChunkMD chunk = null;
		if(search) {
			ChunkCoordIntPair coord = new ChunkCoordIntPair(chunkX, chunkZ);
			chunk = neighbors.get(coord);
		} else {
			chunk = currentChunk;
		}
		
		return chunk;
	}
	
	/**
	 * Darken a color by a factor, add a blue tint.
	 * @param original
	 * @param factor
	 * @return
	 */
	public final Color shade(Color original, float factor) {
		
		if(factor<0) {
			throw new IllegalArgumentException("factor can't be negative");
		}
		
		float bluer = (factor>=1) ? 1f : .8f;
		
		float[] rgb = new float[4];
		rgb = original.getRGBColorComponents(rgb);
		return new Color(
				ColorCache.safeColor(rgb[0] * bluer * factor),
				ColorCache.safeColor(rgb[1] * bluer * factor),
				ColorCache.safeColor(rgb[2] * factor));
		
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
				ColorCache.safeColor(rgb[0] * factor),
				ColorCache.safeColor(rgb[1] * factor),
				ColorCache.safeColor(rgb[2] * (factor+.1f)));
		
	}
	
	/**
	 * Adjust color to indicate it's outside (for underground rendering)
	 * @param original
	 * @return
	 */
	public Color ghostSurface(Color original) {
		final float factor = .3f;
		float hsb[] = Color.RGBtoHSB(original.getRed(), original.getGreen(), original.getBlue(), null);
		Color grey = new Color(Color.HSBtoRGB(hsb[0], 0, hsb[2]));
		
		float[] rgb = grey.getRGBColorComponents(null); 
		Color washout= new Color(
				ColorCache.safeColor((rgb[0]+.5f)/2*factor),
				ColorCache.safeColor((rgb[1]+.5f)/2*factor),
				ColorCache.safeColor((rgb[2]+.6f)/2*factor));
				
		return washout;
		
	}

}
