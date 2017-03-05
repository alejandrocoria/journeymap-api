/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.task.multi;


import net.minecraft.client.Minecraft;

/**
 * The interface Task manager.
 */
public interface ITaskManager
{

    /**
     * Gets task class.
     *
     * @return the task class
     */
    public Class<? extends ITask> getTaskClass();

    /**
     * Enable task boolean.
     *
     * @param minecraft the minecraft
     * @param params    the params
     * @return the boolean
     */
    public boolean enableTask(Minecraft minecraft, Object params);

    /**
     * Is enabled boolean.
     *
     * @param minecraft the minecraft
     * @return the boolean
     */
    public boolean isEnabled(Minecraft minecraft);

    /**
     * Gets task.
     *
     * @param minecraft the minecraft
     * @return the task
     */
    public ITask getTask(Minecraft minecraft);

    /**
     * Task accepted.
     *
     * @param task     the task
     * @param accepted the accepted
     */
    public void taskAccepted(ITask task, boolean accepted);

    /**
     * Disable task.
     *
     * @param minecraft the minecraft
     */
    public void disableTask(Minecraft minecraft);

}
