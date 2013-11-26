package net.techbrew.mcjm.render;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.logging.Level;

import net.minecraft.src.ChunkCoordIntPair;
import net.minecraft.src.EnumSkyBlock;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.io.PropertyManager;
import net.techbrew.mcjm.model.ChunkMD;

/**
 * Base class for methods reusable across renderers.
 * @author mwoodman
 *
 */
public abstract class BaseRenderer {

	final MapBlocks mapBlocks;	
	boolean caveLighting = PropertyManager.getInstance().getBoolean(PropertyManager.Key.CAVE_LIGHTING);
	final boolean fineLogging = JourneyMap.getLogger().isLoggable(Level.FINE);
	
	boolean debug = false;
	long badBlockCount = 0;
	
	public BaseRenderer(MapBlocks mapBlocks) {
		super();
		this.mapBlocks = mapBlocks;
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
		g2D.setBackground(MapBlocks.COLOR_TRANSPARENT);
		g2D.clearRect(x, z, 1, 1);
	}
	
	/**
	 * Paint the block.
	 * 
	 * @param x
	 * @param vSlice
	 * @param z
	 * @param color
	 */
	public void paintBlock(final int x, final int z, final Color color,
			final Graphics2D g2D) {
		g2D.setComposite(MapBlocks.OPAQUE);
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
	public Float getBlockSlope(int x, int z, int offsetX, int offsetz, ChunkMD currentChunk, ChunkMD.Set neighbors, float defaultVal) {
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
			if(chunk.slopes==null) {
				return defaultVal;
			} else {
				return chunk.slopes[newX][newZ];
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
			return chunk.stub.getSavedLightValue(EnumSkyBlock.Block, x,y+1, z);
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
	public BlockInfo getBlock(int x, int y, int z, int offsetX, int offsetz, ChunkMD currentChunk, ChunkMD.Set neighbors, BlockInfo defaultVal) {
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
			return mapBlocks.getBlockInfo(chunk, newX, y, newZ);
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
	 * @param factor
	 * @return
	 */
	public Color shadeOutside(Color original, float factor) {

		float[] rgb = original.getRGBColorComponents(null); 
		Color washout= new Color(
				ColorCache.safeColor((rgb[0] + 2f) /3f),
				ColorCache.safeColor((rgb[1] + 2f) /3f),
				ColorCache.safeColor((rgb[2] + 2f) /3f));
		
		float hsb[] = Color.RGBtoHSB(washout.getRed(), washout.getGreen(), washout.getBlue(), null);
		int result = Color.HSBtoRGB(hsb[0], 0, hsb[2]*factor);
		return new Color(result);
		
		
		
	}

}
