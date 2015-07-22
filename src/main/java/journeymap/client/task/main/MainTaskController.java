/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.task.main;

import com.google.common.collect.Queues;
import journeymap.client.JourneymapClient;
import journeymap.client.log.LogFormatter;
import journeymap.client.log.StatTimer;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Controller for tasks that run on main task only
 */
public class MainTaskController
{
    // Main thread tasks
    private final ConcurrentLinkedQueue<IMainThreadTask> currentQueue = Queues.newConcurrentLinkedQueue();
    private final ConcurrentLinkedQueue<IMainThreadTask> deferredQueue = Queues.newConcurrentLinkedQueue();

    private Minecraft minecraft;
    private JourneymapClient journeyMap;
    private Logger logger;

    public MainTaskController(Minecraft mc, JourneymapClient jm)
    {
        this.minecraft = mc;
        this.journeyMap = jm;
        this.logger = Journeymap.getLogger();
    }

    public void addTask(IMainThreadTask task)
    {
        synchronized (currentQueue)
        {
            currentQueue.add(task);
        }
    }

    public void performTasks()
    {
        try
        {
            synchronized (currentQueue)
            {
                if (currentQueue.isEmpty())
                {
                    currentQueue.add(new MappingMonitorTask());
                }

                while (!currentQueue.isEmpty())
                {
                    IMainThreadTask task = currentQueue.poll();
                    if (task != null)
                    {
                        StatTimer timer = StatTimer.get(task.getName());
                        timer.start();
                        IMainThreadTask deferred = task.perform(minecraft, journeyMap);
                        timer.stop();
                        if (deferred != null)
                        {
                            deferredQueue.add(deferred);
                        }
                    }
                }

                currentQueue.addAll(deferredQueue);
                deferredQueue.clear();
            }
        }
        catch (Throwable t)
        {
            String error = "Error in TickTaskController.performMainThreadTasks(): " + t.getMessage();
            logger.error(error);
            logger.error(LogFormatter.toString(t));
        }
    }


}
