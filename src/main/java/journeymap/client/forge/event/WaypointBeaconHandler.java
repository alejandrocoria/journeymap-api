/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.forge.event;

import journeymap.client.render.ingame.RenderWaypointBeacon;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Event handler for rendering waypoints in-game.
 */
public class WaypointBeaconHandler implements EventHandlerManager.EventHandler
{
    final Minecraft mc = FMLClientHandler.instance().getClient();

    public WaypointBeaconHandler()
    {
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onRenderWorldLastEvent(RenderWorldLastEvent event)
    {
        if (mc.player != null && Journeymap.getClient().getWaypointProperties().beaconEnabled.get())
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
