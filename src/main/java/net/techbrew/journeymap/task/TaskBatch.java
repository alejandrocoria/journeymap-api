/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.task;

import net.minecraft.client.Minecraft;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.LogFormatter;

import java.io.File;
import java.util.List;

/**
 * Created by Mark on 7/16/2014.
 */
public class TaskBatch implements ITask
{
    final List<ITask> taskList;
    final int timeout;

    public TaskBatch(List<ITask> tasks)
    {
        taskList = tasks;
        int timeout = 0;
        for (ITask task : tasks)
        {
            timeout += task.getMaxRuntime();
        }
        this.timeout = timeout;
    }

    @Override
    public int getMaxRuntime()
    {
        return timeout;
    }

    @Override
    public void performTask(final Minecraft mc, final JourneyMap jm, final File jmWorldDir, final boolean threadLogging) throws InterruptedException
    {
        if (threadLogging)
        {
            JourneyMap.getLogger().fine("START batching tasks");
        }

        while (!taskList.isEmpty())
        {
            if (Thread.interrupted())
            {
                JourneyMap.getLogger().warning("BastTask thread interrupted: " + this);
                throw new InterruptedException();
            }

            ITask task = taskList.remove(0);
            try
            {
                if (threadLogging)
                {
                    JourneyMap.getLogger().fine("Batching task: " + task);
                }
                task.performTask(mc, jm, jmWorldDir, threadLogging);
            }
            catch (Throwable t)
            {
                String error = Constants.getMessageJMERR16(LogFormatter.toString(t));
                JourneyMap.getLogger().severe(error);
            }
        }

        if (threadLogging)
        {
            JourneyMap.getLogger().fine("DONE batching tasks");
        }
    }
}
