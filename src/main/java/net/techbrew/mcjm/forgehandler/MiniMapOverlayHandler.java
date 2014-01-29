package net.techbrew.mcjm.forgehandler;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.techbrew.mcjm.render.overlay.TileCache;
import net.techbrew.mcjm.ui.UIManager;
import net.techbrew.mcjm.ui.map.MapOverlay;

import java.util.EnumSet;

/**
 * RenderGameOverlayEvent handler for rendering the MiniMap
 */
public class MiniMapOverlayHandler implements EventHandlerManager.EventHandler {

    @Override
    public EnumSet<EventHandlerManager.BusType> getBus() {
        return EnumSet.of(EventHandlerManager.BusType.MinecraftForgeBus);
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {

        final Minecraft mc = FMLClientHandler.instance().getClient();
        final boolean isGamePaused = mc.currentScreen != null && !(mc.currentScreen instanceof MapOverlay);

        // Manage tiles
        if(isGamePaused) {
            TileCache.pause();
        } else {
            TileCache.resume();
        }

        if (event.type == RenderGameOverlayEvent.ElementType.ALL) {
            UIManager.getInstance().drawMiniMap();
        }
    }
}
