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
import net.techbrew.journeymap.task.MapPlayerTask;

import java.util.EnumSet;
import java.util.logging.Level;

/**
 * Listen for events which are likely to need the map to be updated.
 */
public class ChunkUpdateHandler implements EventHandlerManager.EventHandler {

    public ChunkUpdateHandler()
    {

    }

    Level logLevel = Level.FINER;
    boolean debug = JourneyMap.getLogger().isLoggable(logLevel);

    @Override
    public EnumSet<EventHandlerManager.BusType> getBus() {
        return EnumSet.of(EventHandlerManager.BusType.MinecraftForgeBus);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onChunkLoadEvent(ChunkEvent.Load event)
    {
        queueChunk(event, event.getChunk().getChunkCoordIntPair());
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onChunkUnloadEvent(ChunkEvent.Unload event)
    {
        MapPlayerTask.dequeueChunk(event.getChunk().getChunkCoordIntPair());
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
        if(event instanceof BlockEvent.HarvestDropsEvent) return;
        ChunkCoordIntPair coord = new ChunkCoordIntPair(event.x >> 4, event.z >> 4);
        queueChunk(event, coord);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onBlockPlaceEvent(PlayerInteractEvent event)
    {
        if(event.getResult()==event.useBlock)
        {
            System.out.println(event.getClass().toString());
            ChunkCoordIntPair coord = new ChunkCoordIntPair(event.x >> 4, event.z >> 4);
            queueChunk(event, coord);
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onSoundEvent(PlaySoundEvent17 event)
    {
        if(JourneyMap.getInstance().isMapping())
        {
            ChunkCoordIntPair coord;
            if (event.name.contains("explode"))
            {
                final int x = (int) event.sound.getXPosF() >> 4;
                final int z = (int) event.sound.getZPosF() >> 4;
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
                final int x = (int) event.sound.getXPosF() >> 4;
                final int z = (int) event.sound.getZPosF() >> 4;
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
     * @param event
     * @param coord
     */
    private void queueChunk(Event event, ChunkCoordIntPair coord)
    {
        if(MapPlayerTask.queueChunk(coord))
        {
            if (debug) JourneyMap.getLogger().log(logLevel, String.format("Queuing chunk via %s: %s", event.getClass().getName(), coord));
        }
        else
        {
            //if (debug) JourneyMap.getLogger().log(logLevel, String.format("ReQueuing chunk via %s: %s", event.getClass().getName(), coord));
        }
    }

}
