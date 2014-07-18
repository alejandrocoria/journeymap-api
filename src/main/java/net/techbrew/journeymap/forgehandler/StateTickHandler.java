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

    @Override
    public EnumSet<EventHandlerManager.BusType> getBus()
    {
        return EnumSet.of(EventHandlerManager.BusType.FMLCommonHandlerBus);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {

        if (event.phase != TickEvent.Phase.END)
        {
            return;
        }

        if (counter == 20)
        {
            JourneyMap.getInstance().updateState();
            counter = 0;
        }
        else if (counter == 10)
        {
            if (JourneyMap.getInstance().isMapping() && mc.theWorld != null)
            {
                JourneyMap.getInstance().performTasks();
            }
            counter++;
        }
        else
        {
            counter++;
        }
    }
}
