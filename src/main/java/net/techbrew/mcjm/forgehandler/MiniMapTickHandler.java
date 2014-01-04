package net.techbrew.mcjm.forgehandler;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import net.minecraft.client.Minecraft;
import net.techbrew.mcjm.render.overlay.TileCache;
import net.techbrew.mcjm.ui.UIManager;
import net.techbrew.mcjm.ui.map.MapOverlay;

import java.util.EnumSet;

/**
 * Scheduled tick handler for rendering the MiniMap
 */
public class MiniMapTickHandler implements ITickHandler {

    final EnumSet<TickType> tickTypes = EnumSet.of(TickType.RENDER);

    Minecraft mc = Minecraft.getMinecraft();

    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData) {

    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData) {

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

    @Override
    public EnumSet<TickType> ticks() {
        return tickTypes;
    }

    @Override
    public String getLabel() {
        return this.getClass().getName();
    }
}
