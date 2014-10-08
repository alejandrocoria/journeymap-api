/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.forgehandler;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.JMLogger;
import net.techbrew.journeymap.render.map.TileCache;
import net.techbrew.journeymap.ui.UIManager;
import net.techbrew.journeymap.ui.fullscreen.Fullscreen;

import java.util.EnumSet;

/**
 * RenderGameOverlayEvent handler for rendering the MiniMap1
 */
@SideOnly(Side.CLIENT)
public class MiniMapOverlayHandler implements EventHandlerManager.EventHandler
{
    private static RenderGameOverlayEvent.ElementType EVENT_TYPE = RenderGameOverlayEvent.ElementType.ALL;
    private static boolean EVENT_PRE = true;

    private final Minecraft mc = FMLClientHandler.instance().getClient();

    public static void checkEventConfig()
    {
        EVENT_TYPE = JourneyMap.getMiniMapProperties().getRenderOverlayEventType();
        EVENT_PRE = JourneyMap.getMiniMapProperties().renderOverlayPreEvent.get();
    }

    @Override
    public EnumSet<EventHandlerManager.BusType> getBus()
    {
        return EnumSet.of(EventHandlerManager.BusType.MinecraftForgeBus);
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onRenderOverlay(RenderGameOverlayEvent event)
    {
        try
        {
            if (event.type == EVENT_TYPE && (event.isCancelable() == EVENT_PRE))
            {
                mc.mcProfiler.startSection("journeymap");

                mc.mcProfiler.startSection("tileCache");
                final boolean isGamePaused = mc.currentScreen != null && !(mc.currentScreen instanceof Fullscreen);
                if (isGamePaused)
                {
                    TileCache.pause();
                }
                else
                {
                    TileCache.resume();
                }
                mc.mcProfiler.endStartSection("minimap"); // tileCache

                UIManager.getInstance().drawMiniMap();

                mc.mcProfiler.endSection(); // minimap

                mc.mcProfiler.endSection(); // journeymap
            }
        }
        catch (Throwable t)
        {
            JMLogger.logOnce("Unexpected error during onRenderOverlayEarly: " + t, t);
        }
    }

}
