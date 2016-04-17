/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.task.multi;


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
