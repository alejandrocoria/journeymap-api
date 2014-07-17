package net.techbrew.journeymap.task;


import net.minecraft.client.Minecraft;

public interface ITaskManager {

	public Class<? extends ITask> getTaskClass();
	
	public boolean enableTask(Minecraft minecraft, Object params);
	
	public boolean isEnabled(Minecraft minecraft);
	
	public ITask getTask(Minecraft minecraft);
	
	public void taskAccepted(ITask task, boolean accepted);
	
	public void disableTask(Minecraft minecraft);
	
}
