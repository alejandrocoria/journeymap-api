package net.techbrew.mcjm.task;

import java.util.Map;

import net.minecraft.src.ChunkCoordIntPair;
import net.minecraft.src.World;
import net.techbrew.mcjm.model.ChunkStub;

public abstract class BaseTask {
	
	public final World world;
	public final int dimension;
	public final boolean underground;
	public final Integer vSlice;
	public final Map<ChunkCoordIntPair, ChunkStub> chunkStubs;
	public final boolean flushImagesToDisk;
	
	public BaseTask(World world, boolean underground, Integer vSlice, Map<ChunkCoordIntPair, ChunkStub> chunkStubs, boolean flushImagesToDisk) {
		this.world = world;
		this.dimension = world.provider.dimensionId;
		this.underground = underground;
		this.vSlice = vSlice;
		if((vSlice==null || vSlice==-1) && underground) {
			throw new IllegalStateException("vSlice can't be null (-1) and task be underground");
		}
		this.chunkStubs = chunkStubs;
		this.flushImagesToDisk = flushImagesToDisk;
	}

}
