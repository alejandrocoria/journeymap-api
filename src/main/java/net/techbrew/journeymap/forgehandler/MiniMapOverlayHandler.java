package net.techbrew.journeymap.forgehandler;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.ForgeSubscribe;
import net.techbrew.journeymap.render.overlay.TileCache;
import net.techbrew.journeymap.ui.UIManager;
import net.techbrew.journeymap.ui.map.MapOverlay;

import java.util.EnumSet;

/**
 * RenderGameOverlayEvent handler for rendering the MiniMap
 */
@SideOnly(Side.CLIENT)
public class MiniMapOverlayHandler implements EventHandlerManager.EventHandler {

    @Override
    public EnumSet<EventHandlerManager.BusType> getBus() {
        return EnumSet.of(EventHandlerManager.BusType.MinecraftForgeBus);
    }

    @ForgeSubscribe
    public void onRenderOverlay(RenderGameOverlayEvent.Pre event) {

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
