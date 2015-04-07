/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.forge.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraftforge.event.world.ChunkEvent;
import net.techbrew.journeymap.data.DataCache;

import java.util.EnumSet;

/**
 * Listen for events which are likely to need the map to be updated.
 */
public class ChunkUpdateHandler implements EventHandlerManager.EventHandler
{
    public ChunkUpdateHandler()
    {
    }

    @Override
    public EnumSet<EventHandlerManager.BusType> getBus()
    {
        return EnumSet.of(EventHandlerManager.BusType.MinecraftForgeBus);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onChunkEvent(ChunkEvent.Unload event)
    {
        ChunkCoordIntPair coord = event.getChunk().getChunkCoordIntPair();
        DataCache.instance().invalidateChunkMD(coord);
    }
}
