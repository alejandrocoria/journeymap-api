/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.task;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.StatTimer;
import net.techbrew.journeymap.thread.JMThreadFactory;
import net.techbrew.journeymap.thread.RunnableTask;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TaskController
{
    final static Logger logger = JourneyMap.getLogger();
    final ArrayBlockingQueue<Future> queue = new ArrayBlockingQueue<Future>(1);
    final List<ITaskManager> managers = new LinkedList<ITaskManager>();
    final Minecraft minecraft = FMLClientHandler.instance().getClient();

    // Executor for task threads
    private volatile ScheduledExecutorService taskExecutor;

    public TaskController()
    {
        managers.add(new LegacyMigrationTask.Manager());
        managers.add(new MapRegionTask.Manager());
        managers.add(new SaveMapTask.Manager());
        managers.add(new MapPlayerTask.Manager());
    }

    private void ensureExecutor()
    {
        if (taskExecutor == null || taskExecutor.isShutdown())
        {
            taskExecutor = Executors.newScheduledThreadPool(2, new JMThreadFactory("task")); //$NON-NLS-1$
            queue.clear();
        }
    }

    public Boolean isMapping()
    {
        return taskExecutor != null && !taskExecutor.isShutdown();
    }

    public void enableTasks()
    {
        queue.clear();
        ensureExecutor();

        List<ITaskManager> list = new LinkedList<ITaskManager>(managers);
        for (ITaskManager manager : managers)
        {
            boolean enabled = manager.enableTask(minecraft, null);
            if (!enabled)
            {
                logger.fine("Task not initially enabled: " + manager.getTaskClass().getSimpleName());
            }
            else
            {
                logger.fine("Task ready: " + manager.getTaskClass().getSimpleName());
            }
        }

    }

    public void clear()
    {
        managers.clear();
        queue.clear();

        if (taskExecutor != null && !taskExecutor.isShutdown())
        {
            taskExecutor.shutdown();
            taskExecutor = null;
        }
    }

    private ITaskManager getManager(Class<? extends ITaskManager> managerClass)
    {
        ITaskManager taskManager = null;
        for (ITaskManager manager : managers)
        {
            if (manager.getClass() == managerClass)
            {
                taskManager = manager;
                break;
            }
        }
        return taskManager;
    }

    public boolean isTaskManagerEnabled(Class<? extends ITaskManager> managerClass)
    {
        ITaskManager taskManager = getManager(managerClass);
        if (taskManager != null)
        {
            return taskManager.isEnabled(FMLClientHandler.instance().getClient());
        }
        else
        {
            logger.warning("Couldn't toggle task; manager not in controller: " + managerClass.getClass().getName());
            return false;
        }
    }

    public void toggleTask(Class<? extends ITaskManager> managerClass, boolean enable, Object params)
    {

        ITaskManager taskManager = null;
        for (ITaskManager manager : managers)
        {
            if (manager.getClass() == managerClass)
            {
                taskManager = manager;
                break;
            }
        }
        if (taskManager != null)
        {
            toggleTask(taskManager, enable, params);
        }
        else
        {
            logger.warning("Couldn't toggle task; manager not in controller: " + managerClass.getClass().getName());
        }
    }

    private void toggleTask(ITaskManager manager, boolean enable, Object params)
    {
        Minecraft minecraft = FMLClientHandler.instance().getClient();
        if (manager.isEnabled(minecraft))
        {
            if (!enable)
            {
                logger.fine("Disabling task: " + manager.getTaskClass().getSimpleName());
                manager.disableTask(minecraft);
            }
            else
            {
                logger.fine("Task already enabled: " + manager.getTaskClass().getSimpleName());
            }
        }
        else
        {
            if (enable)
            {
                logger.fine("Enabling task: " + manager.getTaskClass().getSimpleName());
                manager.enableTask(minecraft, params);
            }
            else
            {
                logger.fine("Task already disabled: " + manager.getTaskClass().getSimpleName());
            }
        }
    }

    public void disableTasks()
    {
        for (ITaskManager manager : managers)
        {
            if (manager.isEnabled(minecraft))
            {
                manager.disableTask(minecraft);
                logger.fine("Task disabled: " + manager.getTaskClass().getSimpleName());
            }
        }
    }

    public void performTasks()
    {
        synchronized (queue)
        {
            if (!queue.isEmpty())
            {
                if (queue.peek().isDone())
                {
                    try
                    {
                        queue.take();
                    }
                    catch (InterruptedException e)
                    {
                        logger.warning(e.getMessage());
                    }
                }
            }

            if (queue.isEmpty())
            {
                ITask task = null;
                ITaskManager manager = getNextManager(minecraft);
                if (manager == null)
                {
                    logger.warning("No task managers enabled!");
                    return;
                }
                boolean accepted = false;

                StatTimer timer = StatTimer.get(manager.getTaskClass().getSimpleName() + ".Manager.getTask").start();
                task = manager.getTask(minecraft);

                if (task == null)
                {
                    timer.cancel();
                }
                else
                {
                    timer.stop();

                    ensureExecutor();

                    if (taskExecutor != null && !taskExecutor.isShutdown())
                    {
                        // Create the runnable wrapper
                        final RunnableTask runnableTask = new RunnableTask(taskExecutor, task);

                        // Start the task
                        Future future = taskExecutor.submit(runnableTask);
                        queue.add(future);
                        accepted = true;

                        if (logger.isLoggable(Level.FINE))
                        {
                            logger.fine("Scheduled " + manager.getTaskClass().getSimpleName());
                        }
                    }
                    else
                    {
                        logger.warning("TaskExecutor isn't running");
                    }

                    manager.taskAccepted(task, accepted);
                }
            }
        }
    }

    private ITaskManager getNextManager(final Minecraft minecraft)
    {

        for (ITaskManager manager : managers)
        {
            if (manager.isEnabled(minecraft))
            {
                return manager;
            }
        }
        return null;

    }
}
