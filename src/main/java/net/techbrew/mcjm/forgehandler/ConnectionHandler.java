package net.techbrew.mcjm.forgehandler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.techbrew.mcjm.JourneyMap;

/**
 * Stops mapping when connectionClosed.
 */
public class ConnectionHandler {

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        JourneyMap.getInstance().stopMapping();
    }

    @SubscribeEvent
    public void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        JourneyMap.getInstance().stopMapping();
    }
}

