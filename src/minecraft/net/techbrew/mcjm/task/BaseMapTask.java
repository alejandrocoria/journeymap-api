package net.techbrew.mcjm.task;

import net.minecraft.src.World;
import net.techbrew.mcjm.model.ChunkMD;

public abstract class BaseMapTask implements IMapTask {
	
	final World world;
	final int dimension;
	final boolean underground;
	final Integer vSlice;
	final ChunkMD.Set chunkMdSet;
	final boolean flushCacheWhenDone;
	
	public BaseMapTask(World world, int dimension, boolean underground, Integer vSlice, ChunkMD.Set chunkMdSet, boolean flushCacheWhenDone) {
		this.world = world;
		this.dimension = dimension;
		this.underground = underground;
		this.vSlice = vSlice;
		if((vSlice==null || vSlice==-1) && underground) {
			throw new IllegalStateException("vSlice can't be null (-1) and task be underground");
		}
		this.chunkMdSet = chunkMdSet;
		this.flushCacheWhenDone = flushCacheWhenDone;
	}

	@Override
	public World getWorld() {
		return world;
	}

	@Override
	public int getDimension() {
		return dimension;
	}

	@Override
	public boolean isUnderground() {
		return underground;
	}

	@Override
	public Integer getVSlice() {
		return vSlice;
	}

	@Override
	public ChunkMD.Set getChunkStubs() {
		return chunkMdSet;
	}

	@Override
	public boolean flushCacheWhenDone() {
		return flushCacheWhenDone;
	}

}
