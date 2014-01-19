package net.techbrew.mcjm.forgehandler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import net.techbrew.mcjm.JourneyMap;

/**
 * Stops mapping when connectionClosed.
 */
public class ConnectionHandler {


    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        JourneyMap.getInstance().flagForReset();
    }

    @SubscribeEvent
    public void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        JourneyMap.getInstance().flagForReset();
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        JourneyMap.getInstance().flagForReset();
    }

    @SubscribeEvent
    public void onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        JourneyMap.getInstance().flagForReset();
    }

    @SubscribeEvent
    public void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        JourneyMap.getInstance().flagForReset();
    }
}

