package net.techbrew.mcjm.forgehandler;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.techbrew.mcjm.JourneyMap;

import java.util.EnumSet;

/**
 * Tick handler for JourneyMap state
 */
public class StateTickHandler implements EventHandlerManager.EventHandler {

    @Override
    public EnumSet<EventHandlerManager.BusType> getBus() {
        return EnumSet.of(EventHandlerManager.BusType.FMLCommonHandlerBus);
    }

    Minecraft mc = FMLClientHandler.instance().getClient();
    int counter = 0;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {

        if(event.phase!= TickEvent.Phase.END) {
            return;
        }

        if(counter==20) {
            JourneyMap.getInstance().updateState();
            counter = 0;
        } else if(counter==10) {
            if(JourneyMap.getInstance().isMapping() && mc.theWorld!=null) {
                JourneyMap.getInstance().performTasks();
            }
            counter++;
        }  else {
            counter++;
        }
    }
}
