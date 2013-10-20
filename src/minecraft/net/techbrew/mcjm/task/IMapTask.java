package net.techbrew.mcjm.task;

import java.util.Map;

import net.minecraft.src.ChunkCoordIntPair;
import net.minecraft.src.World;
import net.techbrew.mcjm.model.ChunkStub;

public interface IMapTask extends ITask {
	
	public World getWorld();

	public int getDimension();

	public boolean isUnderground();

	public Integer getVSlice();

	public Map<ChunkCoordIntPair, ChunkStub> getChunkStubs();

	public boolean flushCacheWhenDone();
	
}
