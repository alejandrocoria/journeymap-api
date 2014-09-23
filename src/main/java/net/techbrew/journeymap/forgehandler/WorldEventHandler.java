/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.forgehandler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.event.world.WorldEvent;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.feature.FeatureManager;

import java.util.EnumSet;

/**
 * Created by mwoodman on 1/29/14.
 */
@SideOnly(Side.CLIENT)
public class WorldEventHandler implements EventHandlerManager.EventHandler
{

    String playerName;

    @Override
    public EnumSet<EventHandlerManager.BusType> getBus()
    {
        return EnumSet.of(EventHandlerManager.BusType.MinecraftForgeBus);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void invoke(WorldEvent.Unload event)
    {
        JourneyMap.getInstance().stopMapping();
        FeatureManager.instance().reset();
    }
}
