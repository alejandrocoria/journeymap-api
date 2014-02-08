package net.techbrew.journeymap.task;

import net.minecraft.client.Minecraft;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.io.PropertyManager;
import net.techbrew.journeymap.log.StatTimer;
import net.techbrew.journeymap.thread.TaskThread;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TaskController {
	
	final static Logger logger = JourneyMap.getLogger();
	final int mapTaskDelay = PropertyManager.getInstance().getInteger(PropertyManager.Key.UPDATETIMER_CHUNKS);
	final List<ITaskManager> managers = new LinkedList<ITaskManager>();

	public TaskController() {
		managers.add(new LegacyMigrationTask.Manager());
		managers.add(new MapRegionTask.Manager());
		managers.add(new SaveMapTask.Manager());
		managers.add(new MapPlayerTask.Manager());
	}
	
	public void enableTasks(final Minecraft minecraft) {
		
		TaskThread.reset();
		
		List<ITaskManager> list = new LinkedList<ITaskManager>(managers);
		for(ITaskManager manager: managers) {
			boolean enabled = manager.enableTask(minecraft, null);
			if(!enabled) {
				logger.fine("Task not initially enabled: " + manager.getTaskClass().getSimpleName());
			} else {
				logger.fine("Task ready: " + manager.getTaskClass().getSimpleName());
			}
		}
		
	}
	
	public void clear() {
		managers.clear();
	}
	
	private ITaskManager getManager(Class<? extends ITaskManager> managerClass) {
		ITaskManager taskManager = null;
		for(ITaskManager manager: managers) {
			if(manager.getClass()==managerClass) {
				taskManager = manager;
				break;
			}
		}
		return taskManager;
	}
	
	public boolean isTaskManagerEnabled(Class<? extends ITaskManager> managerClass) {
		ITaskManager taskManager = getManager(managerClass);
		if(taskManager!=null) {
			return taskManager.isEnabled(Minecraft.getMinecraft());
		} else {
			logger.warning("Couldn't toggle task; manager not in controller: " + managerClass.getClass().getName());
			return false;
		}
	}
	
	public void toggleTask(Class<? extends ITaskManager> managerClass, boolean enable, Object params) {
		
		ITaskManager taskManager = null;
		for(ITaskManager manager: managers) {
			if(manager.getClass()==managerClass) {
				taskManager = manager;
				break;
			}
		}
		if(taskManager!=null) {
			toggleTask(taskManager, enable, params);
		} else {
			logger.warning("Couldn't toggle task; manager not in controller: " + managerClass.getClass().getName());
		}
	}
	
	private void toggleTask(ITaskManager manager, boolean enable, Object params) {
		Minecraft minecraft = Minecraft.getMinecraft();
		if(manager.isEnabled(minecraft)) {
			if(!enable) {
				logger.fine("Disabling task: " + manager.getTaskClass().getSimpleName());
				manager.disableTask(minecraft);
			} else {
				logger.fine("Task already enabled: " + manager.getTaskClass().getSimpleName());
			}
		} else {
			if(enable) {
				logger.fine("Enabling task: " + manager.getTaskClass().getSimpleName());
				manager.enableTask(minecraft, params);
			} else {
				logger.fine("Task already disabled: " + manager.getTaskClass().getSimpleName());
			}
		}
	}
	
	public void disableTasks(final Minecraft minecraft) {
		for(ITaskManager manager: managers) {
			if(manager.isEnabled(minecraft)) {
				manager.disableTask(minecraft);
				logger.fine("Task disabled: " + manager.getTaskClass().getSimpleName());
			}
		}
	}
	
	public void performTasks(final Minecraft minecraft, final ScheduledExecutorService taskExecutor) {
		
		if(!TaskThread.hasQueue()) {
					
			ITask task = null;
			ITaskManager manager = getNextManager(minecraft);
			if(manager==null) {
				logger.warning("No task managers enabled!");
				return;
			}
			boolean accepted = false;

            StatTimer timer = StatTimer.get(manager.getTaskClass().getSimpleName() + ".Manager.getTask").start();
			task = manager.getTask(minecraft);

			if(task!=null) {
                timer.stop();
				TaskThread thread = TaskThread.createAndQueue(task);
				if(thread!=null) {
					if(taskExecutor!=null && !taskExecutor.isShutdown()) {
						taskExecutor.schedule(thread, mapTaskDelay, TimeUnit.MILLISECONDS);
						accepted = true;
						if(logger.isLoggable(Level.FINE)) {
							logger.fine("Scheduled " + manager.getTaskClass().getSimpleName());
						}
					} else {
						logger.warning("TaskExecutor isn't running");
					}
				} else {
					logger.warning("Could not schedule " + manager.getTaskClass().getSimpleName());
				}
			} else {
                timer.cancel();
            }

			manager.taskAccepted(accepted);

		}
		
	}
	
	private ITaskManager getNextManager(final Minecraft minecraft) {
		
		for(ITaskManager manager: managers) {
			if(manager.isEnabled(minecraft)) {
				return manager;
			}
		}		
		return null;
		
	}
}
