/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.forgehandler;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraftforge.client.event.sound.PlaySoundEvent17;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.UseHoeEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.data.DataCache;
import net.techbrew.journeymap.task.MapPlayerTask;

import java.util.EnumSet;
import org.apache.logging.log4j.Level;

/**
 * Listen for events which are likely to need the map to be updated.
 */
public class ChunkUpdateHandler implements EventHandlerManager.EventHandler
{

    Level logLevel = Level.TRACE;
    boolean debug = JourneyMap.getLogger().isEnabled(logLevel);
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
    public void onChunkEvent(ChunkEvent event)
    {
        ChunkCoordIntPair coord = event.getChunk().getChunkCoordIntPair();
        if(event instanceof ChunkEvent.Unload)
        {
            MapPlayerTask.dequeueChunk(coord);
            DataCache.instance().invalidateChunkMD(coord);
        }
        else
        {
            queueChunk(event, coord);
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onChunkPopulateEvent(PopulateChunkEvent.Post event)
    {
        ChunkCoordIntPair coord = new ChunkCoordIntPair(event.chunkX, event.chunkZ);
        queueChunk(event, coord);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onBlockBreakEvent(BlockEvent event)
    {
        if (event instanceof BlockEvent.HarvestDropsEvent)
        {
            return;
        }
        ChunkCoordIntPair coord = new ChunkCoordIntPair(event.x >> 4, event.z >> 4);
        queueChunk(event, coord);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onBlockPlaceEvent(PlayerInteractEvent event)
    {
        if (event.getResult() == event.useBlock)
        {
            ChunkCoordIntPair coord = new ChunkCoordIntPair(event.x >> 4, event.z >> 4);
            queueChunk(event, coord);
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onSoundEvent(PlaySoundEvent17 event)
    {
        if (JourneyMap.getInstance().isMapping() && event != null && event.sound != null)
        {
            final int x = (int) event.sound.getXPosF() >> 4;
            final int z = (int) event.sound.getZPosF() >> 4;
            ChunkCoordIntPair coord;
            if (event.name != null)
            {
                if (event.name.contains("explode"))
                {
                    for (int ox = x - 1; ox <= x + 1; ox++)
                    {
                        for (int oz = z - 1; oz <= z + 1; oz++)
                        {
                            coord = new ChunkCoordIntPair(ox, oz);
                            queueChunk(event, coord);
                        }
                    }
                }
                else if (event.name.contains("dig"))
                {
                    coord = new ChunkCoordIntPair(x, z);
                    queueChunk(event, coord);
                }
            }
            else
            {
                coord = new ChunkCoordIntPair(x, z);
                queueChunk(event, coord);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onHoeEvent(UseHoeEvent event)
    {
        ChunkCoordIntPair coord = new ChunkCoordIntPair(event.x >> 4, event.z >> 4);
        queueChunk(event, coord);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onBonemealEvent(BonemealEvent event)
    {
        ChunkCoordIntPair coord = new ChunkCoordIntPair(event.x >> 4, event.z >> 4);
        queueChunk(event, coord);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onChunkDataEvent(ChunkDataEvent event)
    {
        queueChunk(event, event.getChunk().getChunkCoordIntPair());
    }

    /**
     * Queue a chunk with the MapPlayerTask
     *
     * @param event
     * @param coord
     */
    private void queueChunk(Event event, ChunkCoordIntPair coord)
    {
        if (MapPlayerTask.queueChunk(coord))
        {
            if (debug)
            {
                JourneyMap.getLogger().log(logLevel, String.format("Queuing chunk via %s: %s", event.getClass().getName(), coord));
            }
        }
        else
        {
            //if (debug) JourneyMap.getLogger().log(logLevel, String.format("ReQueuing chunk via %s: %s", event.getClass().getName(), coord));
        }
    }

}
