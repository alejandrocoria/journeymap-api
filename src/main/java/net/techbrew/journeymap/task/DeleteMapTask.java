/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.task;

import net.minecraft.client.Minecraft;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.data.DataCache;
import net.techbrew.journeymap.log.ChatLog;
import net.techbrew.journeymap.ui.fullscreen.Fullscreen;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class DeleteMapTask implements ITask
{
    private static final int MAX_RUNTIME = 30000;
    private static final Logger logger = JourneyMap.getLogger();
    boolean allDims;

    public DeleteMapTask(boolean allDims)
    {
        this.allDims = allDims;
    }

    @Override
    public final void performTask(Minecraft mc, JourneyMap jm, File jmWorldDir, boolean threadLogging) throws InterruptedException
    {
        try
        {
            boolean ok = DataCache.instance().getRegionImageCache().deleteMap(Fullscreen.state(), allDims);
            if (ok)
            {
                ChatLog.announceI18N("jm.common.deletemap_status_done");
            }
            else
            {
                ChatLog.announceI18N("jm.common.deletemap_status_error");
            }
            MapPlayerTask.forceNearbyRemap();
            Fullscreen.reset();
        }
        finally
        {
            jm.toggleTask(DeleteMapTask.Manager.class, false, false);
        }
    }

    @Override
    public int getMaxRuntime()
    {
        return MAX_RUNTIME;
    }

    /**
     * Stateful ITaskManager for MapRegionTasks
     *
     * @author mwoodman
     */
    public static class Manager implements ITaskManager
    {
        boolean enabled;
        boolean allDims;

        @Override
        public Class<? extends ITask> getTaskClass()
        {
            return DeleteMapTask.class;
        }

        @Override
        public boolean enableTask(Minecraft minecraft, Object params)
        {
            if (params instanceof Boolean)
            {
                allDims = (Boolean) params;
                enabled = true;
            }
            else
            {
                enabled = false;
            }
            return this.enabled;
        }

        @Override
        public boolean isEnabled(Minecraft minecraft)
        {
            return this.enabled;
        }

        @Override
        public void disableTask(Minecraft minecraft)
        {
            enabled = false;
        }

        @Override
        public DeleteMapTask getTask(Minecraft minecraft)
        {
            if (!enabled)
            {
                return null;
            }

            return new DeleteMapTask(allDims);
        }

        @Override
        public void taskAccepted(ITask task, boolean accepted)
        {
            if (!accepted)
            {
                ChatLog.announceI18N("jm.common.deletemap_status_error");
            }
        }
    }
}
