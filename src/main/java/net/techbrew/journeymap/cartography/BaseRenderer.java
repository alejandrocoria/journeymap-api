package net.techbrew.journeymap.cartography;


import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.EnumSkyBlock;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.model.BlockMD;
import net.techbrew.journeymap.model.BlockUtils;
import net.techbrew.journeymap.model.ChunkMD;

import java.awt.*;
import java.util.logging.Level;

/**
 * Base class for methods reusable across renderers.
 * @author mwoodman
 *
 */
public abstract class BaseRenderer {

	boolean caveLighting = JourneyMap.getInstance().configProperties.isCaveLighting();
	final boolean fineLogging = JourneyMap.getLogger().isLoggable(Level.FINE);
	
	boolean debug = false;
	long badBlockCount = 0;

    /**
     * Paint the block with magenta to indicate it's a problem.
     * @param x
     * @param y
     * @param z
     * @param g2D
     */
	public void paintBadBlock(final int x, final int y, final int z, final Graphics2D g2D) {
		g2D.setComposite(BlockUtils.OPAQUE);
		g2D.setPaint(Color.magenta);
		g2D.fillRect(x, z, 1, 1);
		badBlockCount++;
		if (badBlockCount % 10 == 0) {
			JourneyMap.getLogger().warning(
					"Bad block at " + x + "," + y + "," + z //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							+ ". Total bad blocks painted: " + badBlockCount); //$NON-NLS-1$
		}
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
    protected Float getBlockHeight(int x, int z, int offsetX, int offsetz, ChunkMD currentChunk, ChunkMD.Set neighbors, float defaultVal) {
		int newX = x+offsetX;
		int newZ = z+offsetz;

        // TODO: I'm sure there's a bitwise way to do this.

        switch(newX) {
            case -1:
                newX = 15;
                break;
            case 16:
                newX = 0;
                break;
        }

        switch(newZ) {
            case -1:
                newZ = 15;
                break;
            case 16:
                newZ = 0;
                break;
        }
		
		ChunkMD chunk = getChunk(x, z, offsetX, offsetz, currentChunk, neighbors);
		
		if(chunk!=null) {
            return (float) chunk.getSlopeHeightValue(newX, newZ);
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
	


}
