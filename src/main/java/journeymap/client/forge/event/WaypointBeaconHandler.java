/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.forge.event;

import journeymap.client.JourneymapClient;
import journeymap.client.render.ingame.RenderWaypointBeacon;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
        if (mc.thePlayer != null && JourneymapClient.getWaypointProperties().beaconEnabled.get())
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
