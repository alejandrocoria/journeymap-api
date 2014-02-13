package net.techbrew.journeymap.forgehandler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.event.world.WorldEvent;
import net.techbrew.journeymap.JourneyMap;

import java.util.EnumSet;

/**
 * Created by mwoodman on 1/29/14.
 */
@SideOnly(Side.CLIENT)
public class WorldEventHandler implements EventHandlerManager.EventHandler {

    @Override
    public EnumSet<EventHandlerManager.BusType> getBus() {
        return EnumSet.of(EventHandlerManager.BusType.MinecraftForgeBus);
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        JourneyMap.getInstance().stopMapping();
    }

}
