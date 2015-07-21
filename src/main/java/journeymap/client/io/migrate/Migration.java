/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.io.migrate;

import journeymap.client.JourneymapClient;
import journeymap.client.Version;
import journeymap.common.log.LogFormatter;

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
            JourneymapClient.getLogger().fatal(LogFormatter.toString(t));
            success = false;
        }

        if (!success)
        {
            JourneymapClient.getLogger().fatal("Migration failed! JourneyMap is likely to experience significant errors.");
        }

        return success;
    }

    public interface Task extends Callable<Boolean>
    {
        public Version getRequiredVersion();
    }
}
