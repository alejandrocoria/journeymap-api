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
import net.minecraft.client.gui.GuiGameOver;
import net.techbrew.journeymap.JourneyMap;

import java.util.EnumSet;

/**
 * Tick handler for JourneyMap state
 */
@SideOnly(Side.CLIENT)
public class StateTickHandler implements EventHandlerManager.EventHandler
{

    Minecraft mc = FMLClientHandler.instance().getClient();
    int counter = 0;
    private boolean handleDeathpoint;

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

        final boolean isDead = mc.currentScreen != null && mc.currentScreen instanceof GuiGameOver;
        if(isDead)
        {
            if(!handleDeathpoint)
            {
                handleDeathpoint = true;
                PlayerDeathEvent.onDeath(mc.thePlayer);
            }
        }
        else
        {
            handleDeathpoint = false;
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
}
