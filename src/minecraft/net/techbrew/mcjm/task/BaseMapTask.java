package net.techbrew.mcjm.task;

import java.util.Map;

import net.minecraft.src.ChunkCoordIntPair;
import net.minecraft.src.World;
import net.techbrew.mcjm.model.ChunkStub;

public abstract class BaseMapTask implements IMapTask {
	
	final World world;
	final int dimension;
	final boolean underground;
	final Integer vSlice;
	final Map<ChunkCoordIntPair, ChunkStub> chunkStubs;
	final boolean flushCacheWhenDone;
	
	public BaseMapTask(World world, int dimension, boolean underground, Integer vSlice, Map<ChunkCoordIntPair, ChunkStub> chunkStubs, boolean flushCacheWhenDone) {
		this.world = world;
		this.dimension = dimension;
		this.underground = underground;
		this.vSlice = vSlice;
		if((vSlice==null || vSlice==-1) && underground) {
			throw new IllegalStateException("vSlice can't be null (-1) and task be underground");
		}
		this.chunkStubs = chunkStubs;
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
	public Map<ChunkCoordIntPair, ChunkStub> getChunkStubs() {
		return chunkStubs;
	}

	@Override
	public boolean flushCacheWhenDone() {
		return flushCacheWhenDone;
	}

}
