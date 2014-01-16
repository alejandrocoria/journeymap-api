package net.techbrew.mcjm.forgehandler;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.Minecraft;
import net.techbrew.mcjm.render.overlay.TileCache;
import net.techbrew.mcjm.ui.UIManager;
import net.techbrew.mcjm.ui.map.MapOverlay;

/**
 * Scheduled tick handler for rendering the MiniMap
 */
public class MiniMapTickHandler {

    Minecraft mc = FMLClientHandler.instance().getClient();

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {

        if(event.side!= Side.CLIENT || event.phase!= TickEvent.Phase.END) {
            return;
        }

        final boolean isGamePaused = mc.currentScreen != null && !(mc.currentScreen instanceof MapOverlay);

        // Manage tiles
        if(isGamePaused) {
            TileCache.pause();
        } else {
            TileCache.resume();
        }

        // Draw Minimap
        UIManager.getInstance().drawMiniMap();
    }
}
