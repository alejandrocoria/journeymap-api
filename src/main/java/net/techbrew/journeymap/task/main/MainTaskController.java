package net.techbrew.journeymap.task.main;

import com.google.common.collect.Queues;
import net.minecraft.client.Minecraft;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.log.StatTimer;
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
    private JourneyMap journeyMap;
    private Logger logger;

    public MainTaskController(Minecraft mc, JourneyMap jm)
    {
        this.minecraft = mc;
        this.journeyMap = jm;
        this.logger = JourneyMap.getLogger();
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
