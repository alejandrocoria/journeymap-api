/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.task;


import net.minecraft.client.Minecraft;

public interface ITaskManager
{

    public Class<? extends ITask> getTaskClass();

    public boolean enableTask(Minecraft minecraft, Object params);

    public boolean isEnabled(Minecraft minecraft);

    public ITask getTask(Minecraft minecraft);

    public void taskAccepted(ITask task, boolean accepted);

    public void disableTask(Minecraft minecraft);

}
