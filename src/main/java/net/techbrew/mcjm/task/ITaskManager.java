package net.techbrew.mcjm.task;


import net.minecraft.client.Minecraft;

public interface ITaskManager {

	public Class<? extends ITask> getTaskClass();
	
	public boolean enableTask(Minecraft minecraft, Object params);
	
	public boolean isEnabled(Minecraft minecraft);
	
	public ITask getTask(Minecraft minecraft);
	
	public void taskAccepted(boolean accepted);
	
	public void disableTask(Minecraft minecraft);
	
}
