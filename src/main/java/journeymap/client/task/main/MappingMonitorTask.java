/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.task.main;

import journeymap.client.JourneymapClient;
import journeymap.client.log.ChatLog;
import journeymap.client.ui.fullscreen.Fullscreen;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import org.apache.logging.log4j.Logger;

/**
 * Checks state to start/stop mapping (code formerly in JourneyMap.java)
 */
public class MappingMonitorTask implements IMainThreadTask
{
    private static String NAME = "Tick." + MappingMonitorTask.class.getSimpleName();
    Logger logger = Journeymap.getLogger();
    private int lastDimension = 0;

    @Override
    public IMainThreadTask perform(Minecraft mc, JourneymapClient jm)
    {
        //long start = System.nanoTime();
        try
        {
            if (!jm.isInitialized())
            {
                return this;
            }

            final boolean isDead = mc.currentScreen != null && mc.currentScreen instanceof GuiGameOver;

            if (mc.world == null)
            {
                if (jm.isMapping())
                {
                    jm.stopMapping();
                }

                GuiScreen guiScreen = mc.currentScreen;
                if (guiScreen instanceof GuiMainMenu ||
                        guiScreen instanceof GuiWorldSelection ||
                        guiScreen instanceof GuiMultiplayer)
                {
                    if (jm.getCurrentWorldId() != null)
                    {
                        logger.info("World ID has been reset.");
                        jm.setCurrentWorldId(null);
                    }
                }

                return this;
            }
            else if (lastDimension != mc.player.dimension)
            {
                lastDimension = mc.player.dimension;
                if (jm.isMapping())
                {
                    jm.stopMapping();
                }
            }
            else
            {
                if (!jm.isMapping() && !isDead && Journeymap.getClient().getCoreProperties().mappingEnabled.get())
                {
                    jm.startMapping();
                }
            }

            final boolean isGamePaused = mc.currentScreen != null && !(mc.currentScreen instanceof Fullscreen);
            if (isGamePaused)
            {
                if (!jm.isMapping())
                {
                    return this;
                }
            }

            // Show announcements
            if (!isGamePaused)
            {
                ChatLog.showChatAnnouncements(mc);
            }

            // Start Mapping
            if (!jm.isMapping() && Journeymap.getClient().getCoreProperties().mappingEnabled.get())
            {
                jm.startMapping();
            }
        }
        catch (Throwable t)
        {
            logger.error("Error in JourneyMap.performMainThreadTasks(): " + LogFormatter.toString(t));
        }
        return this;
    }

    @Override
    public String getName()
    {
        return NAME;
    }
}
