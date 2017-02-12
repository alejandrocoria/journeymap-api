/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.forge.event;

import journeymap.client.Constants;
import journeymap.client.JourneymapClient;
import journeymap.client.log.JMLogger;
import journeymap.client.log.StatTimer;
import journeymap.client.task.multi.MapPlayerTask;
import journeymap.client.ui.UIManager;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.List;

/**
 * RenderGameOverlayEvent handler for rendering the MiniMap1
 */
@SideOnly(Side.CLIENT)
public class MiniMapOverlayHandler implements EventHandlerManager.EventHandler
{
    private static final String DEBUG_PREFIX = TextFormatting.AQUA + "[JM] " + TextFormatting.RESET;
    private static final String DEBUG_SUFFIX = "";
    private static RenderGameOverlayEvent.ElementType EVENT_TYPE = RenderGameOverlayEvent.ElementType.ALL;
    private static boolean EVENT_PRE = true;
    private final Minecraft mc = FMLClientHandler.instance().getClient();
    private JourneymapClient jm;
    private long statTimerCheck;
    private List<String> statTimerReport = Collections.EMPTY_LIST;

    public static void checkEventConfig()
    {
        EVENT_TYPE = Journeymap.getClient().getCoreProperties().renderOverlayEventTypeName.get();
        EVENT_PRE = Journeymap.getClient().getCoreProperties().renderOverlayPreEvent.get();
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onRenderOverlayDebug(RenderGameOverlayEvent.Text event)
    {
        try
        {
            if (mc.gameSettings.showDebugInfo)
            {
                event.getLeft().add(null);
                if (Journeymap.getClient().getCoreProperties().mappingEnabled.get())
                {
                    for (String line : MapPlayerTask.getDebugStats())
                    {
                        event.getLeft().add(DEBUG_PREFIX + line + DEBUG_SUFFIX);
                    }
                }
                else
                {
                    event.getLeft().add(Constants.getString("jm.common.enable_mapping_false_text") + DEBUG_SUFFIX);
                }

                if (mc.gameSettings.showDebugProfilerChart)
                {
                    if (System.currentTimeMillis() - statTimerCheck > 3000)
                    {
                        statTimerReport = StatTimer.getReportByTotalTime(DEBUG_PREFIX, DEBUG_SUFFIX);
                        statTimerCheck = System.currentTimeMillis();
                    }

                    event.getLeft().add(null);

                    for (String line : statTimerReport)
                    {
                        event.getLeft().add(line);
                    }
                }
            }
        }
        catch (Throwable t)
        {
            JMLogger.logOnce("Unexpected error during onRenderOverlayEarly: " + t, t);
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onRenderOverlay(RenderGameOverlayEvent event)
    {
        try
        {
            if (event.getType() == EVENT_TYPE && (event.isCancelable() == EVENT_PRE))
            {
                UIManager.INSTANCE.drawMiniMap();
            }
        }
        catch (Throwable t)
        {
            JMLogger.logOnce("Unexpected error during onRenderOverlayEarly: " + t, t);
        }
    }

}
