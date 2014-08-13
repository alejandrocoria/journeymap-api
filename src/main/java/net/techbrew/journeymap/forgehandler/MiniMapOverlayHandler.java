/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.forgehandler;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.techbrew.journeymap.render.overlay.TileCache;
import net.techbrew.journeymap.ui.UIManager;
import net.techbrew.journeymap.ui.map.MapOverlay;

import java.util.EnumSet;

/**
 * RenderGameOverlayEvent handler for rendering the MiniMap
 */
@SideOnly(Side.CLIENT)
public class MiniMapOverlayHandler implements EventHandlerManager.EventHandler
{

    final Minecraft mc = FMLClientHandler.instance().getClient();

    @Override
    public EnumSet<EventHandlerManager.BusType> getBus()
    {
        return EnumSet.of(EventHandlerManager.BusType.MinecraftForgeBus);
    }

    @SubscribeEvent()
    public void onRenderOverlay(RenderGameOverlayEvent.Pre event)
    {
        if (event.type == RenderGameOverlayEvent.ElementType.HOTBAR)
        {
            mc.mcProfiler.startSection("journeymap");

            mc.mcProfiler.startSection("tileCache");
            final boolean isGamePaused = mc.currentScreen != null && !(mc.currentScreen instanceof MapOverlay);
            if (isGamePaused)
            {
                TileCache.pause();
            }
            else
            {
                TileCache.resume();
            }
            mc.mcProfiler.endStartSection("minimap");
            UIManager.getInstance().drawMiniMap();
            mc.mcProfiler.endSection();

            mc.mcProfiler.endSection();
        }
    }
}
