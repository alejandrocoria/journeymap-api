package net.techbrew.journeymap.forgehandler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.event.world.ChunkEvent;

import java.util.EnumSet;

/**
 * Unused
 */
@SideOnly(Side.CLIENT)
public class ChunkUpdateHandler implements EventHandlerManager.EventHandler {

    @Override
    public EnumSet<EventHandlerManager.BusType> getBus() {
        return EnumSet.of(EventHandlerManager.BusType.MinecraftForgeBus);
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        //JourneyMap.getLogger().info(event.getChunk().getChunkCoordIntPair().toString());
    }

}
