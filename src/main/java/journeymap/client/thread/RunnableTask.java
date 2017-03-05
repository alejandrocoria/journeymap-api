/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.thread;

import journeymap.client.JourneymapClient;
import journeymap.client.io.FileHandler;
import journeymap.client.task.multi.ITask;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.concurrent.ExecutorService;

/**
 * The type Runnable task.
 */
public class RunnableTask implements Runnable
{
    /**
     * The Jm.
     */
    static final JourneymapClient jm = Journeymap.getClient();
    /**
     * The Logger.
     */
    static final Logger logger = Journeymap.getLogger();
    /**
     * Minecraft client
     */
    static final Minecraft mc = FMLClientHandler.instance().getClient();
    /**
     * The Thread logging.
     */
    static final boolean threadLogging = jm.isThreadLogging();

    private final ExecutorService taskExecutor;
    private final Runnable innerRunnable;
    private final ITask task;
    private final int timeout;


    /**
     * Instantiates a new Runnable task.
     *
     * @param taskExecutor the task executor
     * @param task         the task
     */
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
            Journeymap.getLogger().warn("Interrupted task that ran too long:" + task);
        }
    }

    /**
     * The type Inner.
     */
    class Inner implements Runnable
    {


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
            catch (InterruptedException e)
            {
                logger.debug("Task interrupted: " + LogFormatter.toPartialString(e));
            }
            catch (Throwable t)
            {
                String error = "Unexpected error during RunnableTask: " + LogFormatter.toString(t);
                logger.error(error);
            }
        }
    }


}
