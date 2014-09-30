/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.task;

import net.minecraft.client.Minecraft;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.model.ChunkMD;

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
            JourneyMap.getLogger().debug("START batching tasks");
        }

        while (!taskList.isEmpty())
        {
            if (Thread.interrupted())
            {
                JourneyMap.getLogger().warn("BastTask thread interrupted: " + this);
                throw new InterruptedException();
            }

            ITask task = taskList.remove(0);
            try
            {
                if (threadLogging)
                {
                    JourneyMap.getLogger().debug("Batching task: " + task);
                }
                task.performTask(mc, jm, jmWorldDir, threadLogging);
            }
            catch (ChunkMD.ChunkMissingException e)
            {
                JourneyMap.getLogger().warn(e.getMessage());
            }
            catch (Throwable t)
            {
                JourneyMap.getLogger().error(String.format("Unexpected error during task batch: %s", LogFormatter.toString(t)));
            }
        }

        if (threadLogging)
        {
            JourneyMap.getLogger().debug("DONE batching tasks");
        }
    }
}
