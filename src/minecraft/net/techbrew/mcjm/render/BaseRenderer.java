package net.techbrew.mcjm.render;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Map;
import java.util.logging.Level;

import net.techbrew.mcjm.ChunkStub;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.PropertyManager;

/**
 * Base class for methods reusable across renderers.
 * @author mwoodman
 *
 */
public abstract class BaseRenderer {

	final MapBlocks mapBlocks;	
	boolean caveLighting = PropertyManager.getInstance().getBoolean(PropertyManager.CAVE_LIGHTING_PROP);
	final boolean fineLogging = JourneyMap.getLogger().isLoggable(Level.FINE);
	
	boolean debug = false;
	long badBlockCount = 0;
	
	public BaseRenderer(MapBlocks mapBlocks) {
		super();
		this.mapBlocks = mapBlocks;
	}

	/**
	 * Find the effective heightmap value within the slice.
	 * @param chunkStub
	 * @param x
	 * @param z
	 * @param sliceMinY
	 * @param sliceMaxY
	 * @return
	 */
	public int getHeightInSlice(ChunkStub chunkStub, int x, int z, int sliceMinY, int sliceMaxY) {
		int y = sliceMinY;

		int blockId;
		while (y <= 1) {
			blockId = chunkStub.getBlockID(x, y, z);
			if (blockId!= 0) {
				y++;
			} else {
				break;
			}
		}

		while (y >= 1) {
			blockId = chunkStub.getBlockID(x, y, z);
			if (blockId == 0) {
				y--;
			} else {
				break;
			}
		}
		return (y<0) ? 0 : y;
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
	public void paintBlock(final int x, final int vSlice, final int z, final Color color,
			final Graphics2D g2D) {
		g2D.setComposite(MapBlocks.OPAQUE);
		g2D.setColor(color);
		g2D.fillRect(x, z, 1, 1);
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
	public Float getBlockHeight(int x, int z, int offsetX, int offsetz, ChunkStub currentChunk, Map<Integer, ChunkStub> neighbors, float defaultVal) {
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
			return (float) chunk.getSafeHeightValue(newX, newZ);
		} else {
			return defaultVal;
		}
	}
	
	/**
	 * Get the slope of the block at the coordinates + offsets.  Uses ChunkStub.slopes.
	 * @param x
	 * @param z
	 * @param offsetX
	 * @param offsetz
	 * @param currentChunk
	 * @param neighbors
	 * @param defaultVal
	 * @return
	 */
	public Float getBlockSlope(int x, int z, int offsetX, int offsetz, ChunkStub currentChunk, Map<Integer, ChunkStub> neighbors, float defaultVal) {
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
	 * Get the block at the coordinates + offsets.  Uses ChunkStub.slopes.
	 * @param x
	 * @param z
	 * @param offsetX
	 * @param offsetz
	 * @param currentChunk
	 * @param neighbors
	 * @param defaultVal
	 * @return
	 */
	public BlockInfo getBlock(int x, int y, int z, int offsetX, int offsetz, ChunkStub currentChunk, Map<Integer, ChunkStub> neighbors, BlockInfo defaultVal) {
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
			return mapBlocks.getBlockInfo(chunk, newX, y, newZ);
		} else {
			return defaultVal;
		}
	}
	
	/**
	 * Gets the chunkStub at the coordinates + offsets.
	 * @param x
	 * @param z
	 * @param offsetX
	 * @param offsetz
	 * @param currentChunk
	 * @param neighbors
	 * @return
	 */
	ChunkStub getChunk(int x, int z, int offsetX, int offsetz, ChunkStub currentChunk, Map<Integer, ChunkStub> neighbors) {
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
		
		ChunkStub chunk = null;
		if(search) {
			Integer stubHash = ChunkStub.toHashCode(chunkX, chunkZ);
			chunk = neighbors.get(stubHash);
		} else {
			chunk = currentChunk;
		}
		
		return chunk;
	}

}
