package net.techbrew.mcjm.task;

import net.minecraft.src.Minecraft;

public interface ITaskManager {

	public Class<? extends ITask> getTaskClass();
	
	public boolean enableTask(Minecraft minecraft);
	
	public boolean isEnabled(Minecraft minecraft);
	
	public ITask getTask(Minecraft minecraft, long worldHash);
	
	public void taskAccepted(boolean accepted);
	
	public void disableTask(Minecraft minecraft);
	
}
