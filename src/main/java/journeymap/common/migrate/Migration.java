/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.common.migrate;

import com.google.common.reflect.ClassPath;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Runs migration tasks found in a given package.
 */
public class Migration
{
    private final String targetPackage;

    public Migration(String targetPackage)
    {
        this.targetPackage = targetPackage;
    }

    public boolean performTasks()
    {
        boolean success = true;

        // Find any tasks in the target package
        List<MigrationTask> tasks = new ArrayList<MigrationTask>();
        try
        {
            Set<ClassPath.ClassInfo> classInfoSet = ClassPath.from(Journeymap.class.getClassLoader()).getTopLevelClassesRecursive(targetPackage);
            for (ClassPath.ClassInfo classInfo : classInfoSet)
            {
                Class<?> clazz = classInfo.load();
                if (MigrationTask.class.isAssignableFrom(clazz))
                {
                    try
                    {
                        MigrationTask task = (MigrationTask) clazz.newInstance();
                        if (task.isActive(Journeymap.JM_VERSION))
                        {
                            tasks.add(task);
                        }
                    }
                    catch (Throwable t)
                    {
                        Journeymap.getLogger().error("Couldn't instantiate MigrationTask " + clazz, LogFormatter.toPartialString(t));
                        success = false;
                    }
                }
            }
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error("Couldn't find MigrationTasks: " + t, LogFormatter.toPartialString(t));
            success = false;
        }

        // Run the tasks
        for (MigrationTask task : tasks)
        {
            try
            {
                if (!task.call())
                {
                    success = false;
                }
            }
            catch (Throwable t)
            {
                Journeymap.getLogger().fatal(LogFormatter.toString(t));
                success = false;
            }
        }

        if (!success)
        {
            Journeymap.getLogger().fatal("Some or all of JourneyMap migration failed! You may experience significant errors.");
        }

        return success;
    }

}