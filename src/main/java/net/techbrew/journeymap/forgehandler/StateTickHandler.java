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
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.model.Waypoint;
import net.techbrew.journeymap.properties.WaypointProperties;
import net.techbrew.journeymap.waypoint.WaypointStore;

import java.util.EnumSet;

/**
 * Tick handler for JourneyMap state
 */
@SideOnly(Side.CLIENT)
public class StateTickHandler implements EventHandlerManager.EventHandler
{

    Minecraft mc = FMLClientHandler.instance().getClient();
    int counter = 0;
    private boolean deathpointCreated;

    @Override
    public EnumSet<EventHandlerManager.BusType> getBus()
    {
        return EnumSet.of(EventHandlerManager.BusType.FMLCommonHandlerBus);
    }

    @SubscribeEvent()
    public void onClientTick(TickEvent.ClientTickEvent event)
    {

        if (event.phase == TickEvent.Phase.END)
        {
            return;
        }

        mc.mcProfiler.startSection("journeymap");

        if (mc.thePlayer != null && mc.thePlayer.isDead)
        {
            if (!deathpointCreated)
            {
                deathpointCreated = true;
                createDeathpoint();
            }
        }
        else
        {
            deathpointCreated = false;
        }

        try
        {
            if (counter == 20)
            {
                mc.mcProfiler.startSection("updateState");
                JourneyMap.getInstance().updateState();
                counter = 0;
                mc.mcProfiler.endSection();
            }
            else if (counter == 10)
            {
                mc.mcProfiler.startSection("performTasks");
                if (JourneyMap.getInstance().isMapping() && mc.theWorld != null)
                {
                    JourneyMap.getInstance().performTasks();
                }
                counter++;
                mc.mcProfiler.endSection();
            }
            else
            {
                counter++;
            }

        }
        catch(Exception e)
        {
            JourneyMap.getLogger().warn("Error during onClientTick: " + e);
        }
        finally
        {
            mc.mcProfiler.endSection();
        }
    }

    private void createDeathpoint()
    {
        try
        {
            EntityPlayer player = mc.thePlayer;
            if (player == null)
            {
                JourneyMap.getLogger().error("Lost reference to player before Deathpoint could be created");
                return;
            }

            WaypointProperties waypointProperties = JourneyMap.getWaypointProperties();
            boolean doCreate = waypointProperties.managerEnabled.get() && waypointProperties.createDeathpoints.get();

            if (doCreate)
            {
                ChunkCoordinates cc = new ChunkCoordinates(MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ));
                Waypoint deathpoint = Waypoint.at(cc, Waypoint.Type.Death, player.worldObj.provider.dimensionId);
                WaypointStore.instance().save(deathpoint);
            }

            JourneyMap.getLogger().info(String.format("%s died at x:%s, y:%s, z:%s. Deathpoint created: %s", player.getCommandSenderName(),
                    MathHelper.floor_double(player.posX),
                    MathHelper.floor_double(player.posY),
                    MathHelper.floor_double(player.posZ),
                    doCreate));

        }
        catch (Throwable t)
        {
            JourneyMap.getLogger().error("Unexpected Error in createDeathpoint(): " + LogFormatter.toString(t));
        }
    }
}
