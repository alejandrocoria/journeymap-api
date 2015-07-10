/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package net.techbrew.journeymap.forge.event;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.forge.helper.ForgeHelper;
import net.techbrew.journeymap.render.ingame.RenderWaypointBeacon;

import java.util.EnumSet;

/**
 * Event handler for rendering waypoints in-game.
 */
public class WaypointBeaconHandler implements EventHandlerManager.EventHandler
{
    final Minecraft mc = ForgeHelper.INSTANCE.getClient();

    public WaypointBeaconHandler()
    {
    }

    @Override
    public EnumSet<EventHandlerManager.BusType> getBus()
    {
        return EnumSet.of(EventHandlerManager.BusType.MinecraftForgeBus);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onRenderWorldLastEvent(RenderWorldLastEvent event)
    {
        if (mc.thePlayer != null && JourneyMap.getWaypointProperties().beaconEnabled.get())
        {
            if (!this.mc.gameSettings.hideGUI)
            {
                mc.mcProfiler.startSection("journeymap");
                mc.mcProfiler.startSection("beacons");
                RenderWaypointBeacon.renderAll();
                mc.mcProfiler.endSection();
                mc.mcProfiler.endSection();
            }
        }
    }
}
