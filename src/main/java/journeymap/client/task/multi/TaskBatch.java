/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.task.multi;

import journeymap.client.JourneymapClient;
import journeymap.client.log.LogFormatter;
import journeymap.client.model.ChunkMD;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.util.List;

/**
 * Created by Mark on 7/16/2014.
 */
public class TaskBatch implements ITask
{
    final List<ITask> taskList;
    final int timeout;
    protected long startNs;
    protected long elapsedNs;

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
    public void performTask(final Minecraft mc, final JourneymapClient jm, final File jmWorldDir, final boolean threadLogging) throws InterruptedException
    {
        if (startNs == 0)
        {
            startNs = System.nanoTime();
        }

        if (threadLogging)
        {
            JourneymapClient.getLogger().debug("START batching tasks");
        }

        while (!taskList.isEmpty())
        {
            if (Thread.interrupted())
            {
                JourneymapClient.getLogger().warn("TaskBatch thread interrupted: " + this);
                throw new InterruptedException();
            }

            ITask task = taskList.remove(0);
            try
            {
                if (threadLogging)
                {
                    JourneymapClient.getLogger().debug("Batching task: " + task);
                }
                task.performTask(mc, jm, jmWorldDir, threadLogging);
            }
            catch (ChunkMD.ChunkMissingException e)
            {
                JourneymapClient.getLogger().warn(e.getMessage());
            }
            catch (Throwable t)
            {
                JourneymapClient.getLogger().error(String.format("Unexpected error during task batch: %s", LogFormatter.toString(t)));
            }
        }

        if (threadLogging)
        {
            JourneymapClient.getLogger().debug("DONE batching tasks");
        }

        elapsedNs = System.nanoTime() - startNs;
    }
}
