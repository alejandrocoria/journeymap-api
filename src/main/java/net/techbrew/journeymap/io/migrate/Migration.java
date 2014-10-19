package net.techbrew.journeymap.io.migrate;

import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.Version;
import net.techbrew.journeymap.log.LogFormatter;

import java.util.concurrent.Callable;

/**
 * Run migration tasks based on current version
 */
public class Migration
{
    Task[] tasks = new Task[]{new Migrate5_0_0()};

    public boolean performTasks()
    {
        boolean success = true;
        try
        {
            for (Task task : tasks)
            {
                success = task.call() && success;
            }
        }
        catch (Throwable t)
        {
            JourneyMap.getLogger().fatal(LogFormatter.toString(t));
            success = false;
        }

        if (!success)
        {
            JourneyMap.getLogger().fatal("Migration failed! JourneyMap is likely to experience significant errors.");
        }

        return success;
    }

    public interface Task extends Callable<Boolean>
    {
        public Version getRequiredVersion();
    }
}
