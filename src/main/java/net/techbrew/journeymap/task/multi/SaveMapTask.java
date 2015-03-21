/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.task.multi;

import net.minecraft.client.Minecraft;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.io.MapSaver;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class SaveMapTask implements ITask
{

    private static final Logger logger = JourneyMap.getLogger();

    MapSaver mapSaver;

    private SaveMapTask(MapSaver mapSaver)
    {
        this.mapSaver = mapSaver;
    }

    @Override
    public int getMaxRuntime()
    {
        return 120000;
    }

    @Override
    public void performTask(Minecraft mc, JourneyMap jm, File jmWorldDir, boolean threadLogging)
    {
        mapSaver.saveMap();
    }

    /**
     * ITaskManager for MapPlayerTasks
     *
     * @author mwoodman
     */
    public static class Manager implements ITaskManager
    {

        MapSaver mapSaver;

        @Override
        public Class<? extends ITask> getTaskClass()
        {
            return SaveMapTask.class;
        }

        @Override
        public boolean enableTask(Minecraft minecraft, Object params)
        {
            if (params != null && params instanceof MapSaver)
            {
                mapSaver = (MapSaver) params;
            }
            return isEnabled(minecraft);
        }

        @Override
        public boolean isEnabled(Minecraft minecraft)
        {
            return (mapSaver != null);
        }

        @Override
        public void disableTask(Minecraft minecraft)
        {
            mapSaver = null;
        }

        @Override
        public SaveMapTask getTask(Minecraft minecraft)
        {
            if (mapSaver == null)
            {
                return null;
            }
            return new SaveMapTask(mapSaver);
        }

        @Override
        public void taskAccepted(ITask task, boolean accepted)
        {
            mapSaver = null;
        }

    }
}
