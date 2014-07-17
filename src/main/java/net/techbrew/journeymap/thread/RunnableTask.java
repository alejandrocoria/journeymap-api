package net.techbrew.journeymap.thread;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.io.FileHandler;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.task.ITask;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class RunnableTask implements Runnable
{
    private final ScheduledExecutorService taskExecutor;
    private final Runnable innerRunnable;
    private final ITask task;
    private final int timeout;

    public RunnableTask(final ScheduledExecutorService taskExecutor, ITask task)
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
            taskExecutor.submit(innerRunnable).get(timeout, TimeUnit.MILLISECONDS);
        }
        catch (Throwable t)
        {
            JourneyMap.getLogger().warning("Interrupted task that ran too long:" + task);
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
                    logger.fine("JM not mapping, aborting");
                    return;
                }

                final File jmWorldDir = FileHandler.getJMWorldDir(mc);
                if (jmWorldDir == null)
                {
                    logger.fine("JM world dir not found, aborting");
                    return;
                }

                task.performTask(mc, jm, jmWorldDir, threadLogging);

            }
            catch (Throwable t)
            {
                String error = Constants.getMessageJMERR16(LogFormatter.toString(t));
                logger.severe(error);
            }
        }
    }


}
