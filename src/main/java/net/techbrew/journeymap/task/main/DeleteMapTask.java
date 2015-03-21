/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.task.main;

import net.minecraft.client.Minecraft;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.ChatLog;
import net.techbrew.journeymap.model.RegionImageCache;
import net.techbrew.journeymap.render.map.GridRenderer;
import net.techbrew.journeymap.task.multi.MapPlayerTask;
import net.techbrew.journeymap.task.multi.MapRegionTask;
import net.techbrew.journeymap.ui.fullscreen.Fullscreen;
import org.apache.logging.log4j.Logger;

public class DeleteMapTask implements IMainThreadTask
{
    private static String NAME = "Tick." + MappingMonitorTask.class.getSimpleName();
    private static Logger LOGGER = JourneyMap.getLogger();
    boolean allDims;

    private DeleteMapTask(boolean allDims)
    {
        this.allDims = allDims;
    }

    public static void queue(boolean allDims)
    {
        JourneyMap.getInstance().queueMainThreadTask(new DeleteMapTask(allDims));
    }

    @Override
    public final IMainThreadTask perform(Minecraft mc, JourneyMap jm)
    {
        try
        {
            jm.toggleTask(MapPlayerTask.Manager.class, false, false);
            jm.toggleTask(MapRegionTask.Manager.class, false, false);
            GridRenderer.setEnabled(false);
            boolean ok = RegionImageCache.instance().deleteMap(Fullscreen.state(), allDims);
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
            GridRenderer.setEnabled(true);
            jm.toggleTask(MapPlayerTask.Manager.class, true, true);
        }
        return null;
    }

    @Override
    public String getName()
    {
        return NAME;
    }
}
