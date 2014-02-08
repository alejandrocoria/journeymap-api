package net.techbrew.journeymap.task;


import net.minecraft.world.World;
import net.techbrew.journeymap.model.ChunkMD;

public interface IMapTask extends ITask {
	
	public World getWorld();

	public int getDimension();

	public boolean isUnderground();

	public Integer getVSlice();

	public ChunkMD.Set getChunkStubs();

	public boolean flushCacheWhenDone();
	
	public void taskComplete();
	
}
