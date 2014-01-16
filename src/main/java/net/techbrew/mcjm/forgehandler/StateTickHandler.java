package net.techbrew.mcjm.forgehandler;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.techbrew.mcjm.JourneyMap;

/**
 * Tick handler for JourneyMap state
 */
public class StateTickHandler {

    Minecraft mc = FMLClientHandler.instance().getClient();

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {

        if(event.phase!= TickEvent.Phase.END) {
            return;
        }

        if (mc.theWorld!=null && mc.thePlayer!=null && !mc.thePlayer.isDead) {
            JourneyMap.getInstance().updateState();
        }
    }
}
