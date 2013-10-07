package net.techbrew.mcjm.thread.task;

import java.util.Map;

import net.minecraft.src.ChunkCoordIntPair;
import net.techbrew.mcjm.model.ChunkStub;

public class UpdateThreadTask {
	
	public final int dimension;
	public final boolean underground;
	public final int chunkY;
	public final Map<ChunkCoordIntPair, ChunkStub> chunkStubs;
	public final boolean flushImagesToDisk;
	
	public UpdateThreadTask(int dimension, boolean underground, int chunkY, Map<ChunkCoordIntPair, ChunkStub> chunkStubs, boolean flushImagesToDisk) {
		this.dimension = dimension;
		this.underground = underground;
		this.chunkY = chunkY;
		this.chunkStubs = chunkStubs;
		this.flushImagesToDisk = flushImagesToDisk;
	}

}
