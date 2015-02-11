/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.thread;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.io.FileHandler;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.task.ITask;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.concurrent.ExecutorService;

public class RunnableTask implements Runnable
{
    private final ExecutorService taskExecutor;
    private final Runnable innerRunnable;
    private final ITask task;
    private final int timeout;

    public RunnableTask(final ExecutorService taskExecutor, ITask task)
    {
        this.taskExecutor = taskExecutor;
        this.task = task;
        this.timeout = task.getMaxRuntime();
        this.innerRunnable = new Inner();
    }

    @Override
    public void run()
    {
        try
        {
            // TODO ENABLE WHEN NOT DEBUGGING
            taskExecutor.submit(innerRunnable);//.get(timeout, TimeUnit.MILLISECONDS);
        }
        catch (Throwable t)
        {
            JourneyMap.getLogger().warn("Interrupted task that ran too long:" + task);
        }
    }

    class Inner implements Runnable
    {
        final JourneyMap jm = JourneyMap.getInstance();
        final Logger logger = JourneyMap.getLogger();
        final Minecraft mc = FMLClientHandler.instance().getClient();
        final boolean threadLogging = jm.isThreadLogging();

        @Override
        public final void run()
        {
            try
            {
                // Bail if needed
                if (!jm.isMapping())
                {
                    logger.debug("JM not mapping, aborting");
                    return;
                }

                final File jmWorldDir = FileHandler.getJMWorldDir(mc);
                if (jmWorldDir == null)
                {
                    logger.debug("JM world dir not found, aborting");
                    return;
                }

                task.performTask(mc, jm, jmWorldDir, threadLogging);

            }
            catch (Throwable t)
            {
                String error = "Unexpected error during RunnableTask: " + LogFormatter.toString(t);
                logger.error(error);
            }
        }
    }


}
