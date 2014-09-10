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
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.render.ingame.RenderWaypointBeacon;

import java.util.EnumSet;

/**
 * Event handler for rendering waypoints in-game.
 */
public class WaypointBeaconHandler implements EventHandlerManager.EventHandler
{
    final Minecraft mc = FMLClientHandler.instance().getClient();

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
            mc.mcProfiler.startSection("journeymap");
            mc.mcProfiler.startSection("beacons");
            RenderWaypointBeacon.renderAll();
            mc.mcProfiler.endSection();
            mc.mcProfiler.endSection();
        }
    }
}
