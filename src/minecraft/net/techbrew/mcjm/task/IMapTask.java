package net.techbrew.mcjm.task;


import net.minecraft.world.World;
import net.techbrew.mcjm.model.ChunkMD;

public interface IMapTask extends ITask {
	
	public World getWorld();

	public int getDimension();

	public boolean isUnderground();

	public Integer getVSlice();

	public ChunkMD.Set getChunkStubs();

	public boolean flushCacheWhenDone();
	
	public void taskComplete();
	
}
