package net.techbrew.mcjm.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import net.minecraft.src.Chunk;
import net.minecraft.src.ChunkCoordIntPair;
import net.minecraft.src.EnumSkyBlock;
import net.minecraft.src.World;
import net.techbrew.mcjm.cartography.MapBlocks;

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
		this(new ChunkStub(chunk, doErrorChecks), render, worldObj);
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
	    	int id = 0;
	    	y = stub.heightMap[z << 4 | x];
	    	if(y<1) return 0;
	    	while(id==0) {    		
	    		id = stub.getBlockID(x,y,z); 
	    		if(MapBlocks.excludeHeight.contains(id)) {
	    			y=y-1;
	    		}
	    		if(y==0) {
	    			break;
	    		}
	    	}
	    	return y;       
    	} catch(ArrayIndexOutOfBoundsException e) {
    		return stub.heightMap[z << 4 | x];
    	}
    }
    
    /**
	 * Compare storage arrays of another chunkStub at the same position.
	 * @param other
	 * @return
	 */
	public boolean isUnchanged(ChunkMD other) {
		if(stub.lastSaveTime!=other.stub.lastSaveTime) {
			return false;
		}
		if(stub.storageArrays.length!=other.stub.storageArrays.length) {
			return false;
		}
		if(!Arrays.equals(stub.heightMap,other.stub.heightMap)) {
			return false;
		}
		for(int i=0;i<stub.storageArrays.length;i++) {
			ExtendedBlockStorageStub ebs = stub.storageArrays[i];
			ExtendedBlockStorageStub otherEbs = other.stub.storageArrays[i];
			if(ebs==null && otherEbs!=null) return false;
			if(ebs!=null && otherEbs==null) return false;
			if(ebs!=null && !ebs.equals(otherEbs)) return false;
		}
		return true;
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
