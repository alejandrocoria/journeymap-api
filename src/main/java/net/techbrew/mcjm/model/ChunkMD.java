package net.techbrew.mcjm.model;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.techbrew.mcjm.cartography.MapBlocks;

import java.util.HashMap;
import java.util.Iterator;

/**
 * ChunkStub MetaData wrapper for the various bits
 * of metadata that need to accompany a ChunkStub.
 * 
 * @author mwoodman
 *
 */
public class ChunkMD {

	public volatile float[][] surfaceSlopes;
	public volatile float[][] sliceSlopes;
	public final World worldObj;
	public final int worldHeight;
	public final Boolean hasNoSky;
	public final ChunkStub stub;
	public final ChunkCoordIntPair coord;
	public Boolean render;
	private int discards;
	
	public ChunkMD(Chunk chunk, Boolean render, World worldObj) {
		this(chunk,render,worldObj,false);
	}
	
	public ChunkMD(Chunk chunk, Boolean render, World worldObj, boolean doErrorChecks) {		
		this(new ChunkStub(chunk), render, worldObj);
		if(chunk.isEmpty() || !chunk.isChunkLoaded) {
			render = false;
		}
	}
	
	public ChunkMD(ChunkStub stub, Boolean render, World worldObj) {
		this.stub = stub;
		this.render = render;
		this.worldObj = worldObj;
		this.worldHeight = worldObj.getActualHeight();		
		this.hasNoSky = worldObj.provider.hasNoSky;
		this.coord = new ChunkCoordIntPair(stub.xPosition, stub.zPosition);
	}
	
	public int discard(int i) {
		discards = Math.max(0, discards+i);
		return discards;
	}

    public Block getBlock(int x, int y, int z) {
        return stub.func_150810_a(x, y, z);
    }
	
	/**
	 * Added to do a safety check on the world height value
	 * @param par1EnumSkyBlock
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public int getSavedLightValue(EnumSkyBlock par1EnumSkyBlock, int x, int y, int z) {
		return stub.getSavedLightValue(par1EnumSkyBlock, x, Math.min(y, worldHeight-1), z);
	}
	
	/**
     * Added because getHeightValue() sometimes returns an air block.
     * Returns the value in the height map at this x, z coordinate in the chunk
     */
    public int getSafeHeightValue(int x, int z)
    {
    	try {
	    	int y = 0;
	    	Block block = null;
	    	y = stub.heightMap[z << 4 | x];
	    	if(y<1) return 0;
	    	while(block == Blocks.air || block.func_149688_o() == Material.field_151579_a) {
	    		block = stub.func_150810_a(x, y, z);
                if(MapBlocks.hasFlag(block, MapBlocks.Flag.NotTopBlock)) {
	    			y=y-1;
	    		}
	    		if(y==0) {
	    			break;
	    		}
	    	}
	    	return y;       
    	} catch(Exception e) {
    		return stub.heightMap[z << 4 | x];
    	}
    }

	@Override
	public int hashCode() {
		return coord.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChunkMD other = (ChunkMD) obj;
		if (stub.xPosition != other.stub.xPosition)
			return false;
		if (stub.zPosition != other.stub.zPosition)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "ChunkStubMD [" + stub.xPosition + ", " + stub.zPosition + "]"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public static class Set extends HashMap<ChunkCoordIntPair, ChunkMD> implements Iterable<ChunkMD> {

		public Set(int i) {
			super(i);
		}
		
		public void put(ChunkMD chunkMd) {
			super.put(chunkMd.coord, chunkMd);
		}
		
		public void add(ChunkMD chunkMd) {
			super.put(chunkMd.coord, chunkMd);
		}
		
		public ChunkMD remove(ChunkMD chunkMd) {
			return super.remove(chunkMd.coord);
		}

		@Override
		public Iterator<ChunkMD> iterator() {
			return this.values().iterator();
		}
		
	}
}
