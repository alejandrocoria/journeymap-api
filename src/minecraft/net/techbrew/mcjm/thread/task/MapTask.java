package net.techbrew.mcjm.thread.task;

import java.util.Map;

import net.minecraft.src.ChunkCoordIntPair;
import net.minecraft.src.World;
import net.techbrew.mcjm.model.ChunkStub;

public abstract class MapTask {
	
	public final World world;
	public final int dimension;
	public final boolean underground;
	public final Integer chunkY;
	public final Map<ChunkCoordIntPair, ChunkStub> chunkStubs;
	public final boolean flushImagesToDisk;
	
	public MapTask(World world, boolean underground, Integer chunkY, Map<ChunkCoordIntPair, ChunkStub> chunkStubs, boolean flushImagesToDisk) {
		this.world = world;
		this.dimension = world.provider.dimensionId;
		this.underground = underground;
		this.chunkY = chunkY;
		this.chunkStubs = chunkStubs;
		this.flushImagesToDisk = flushImagesToDisk;
	}

}
