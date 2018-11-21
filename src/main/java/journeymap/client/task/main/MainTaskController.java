/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.task.main;

import com.google.common.collect.Queues;
import journeymap.client.JourneymapClient;
import journeymap.client.log.StatTimer;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Controller for tasks that run on main task only
 */
public class MainTaskController
{
    // Main thread tasks
    private final ConcurrentLinkedQueue<IMainThreadTask> currentQueue = Queues.newConcurrentLinkedQueue();
    private final ConcurrentLinkedQueue<IMainThreadTask> deferredQueue = Queues.newConcurrentLinkedQueue();

    /**
     * Instantiates a new Main task controller.
     */
    public MainTaskController()
    {
    }

    /**
     * Add task.
     *
     * @param task the task
     */
    public void addTask(IMainThreadTask task)
    {
        synchronized (currentQueue)
        {
            currentQueue.add(task);
        }
    }

    public boolean isActive()
    {
        if (currentQueue.isEmpty())
        {
            return false;
        }
        if (currentQueue.size() == 1 && currentQueue.peek() instanceof MappingMonitorTask)
        {
            return false;
        }
        return true;
    }

    /**
     * Perform tasks.
     */
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

                Minecraft minecraft = FMLClientHandler.instance().getClient();
                JourneymapClient journeymapClient = Journeymap.getClient();

                while (!currentQueue.isEmpty())
                {
                    IMainThreadTask task = currentQueue.poll();
                    if (task != null)
                    {
                        StatTimer timer = StatTimer.get(task.getName());
                        timer.start();
                        IMainThreadTask deferred = task.perform(minecraft, journeymapClient);
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
            Journeymap.getLogger().error(error);
            Journeymap.getLogger().error(LogFormatter.toString(t));
        }
    }


}
