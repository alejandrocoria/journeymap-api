package net.techbrew.mcjm.ui;

import net.minecraft.src.EntityClientPlayerMP;
import net.minecraft.src.Minecraft;
import net.minecraft.src.ScaledResolution;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.data.DataCache;
import net.techbrew.mcjm.data.EntityKey;
import net.techbrew.mcjm.data.PlayerData;
import net.techbrew.mcjm.io.PropertyManager;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.model.EntityHelper;
import net.techbrew.mcjm.model.MapOverlayState;
import net.techbrew.mcjm.model.WaypointHelper;
import net.techbrew.mcjm.render.overlay.BaseOverlayRenderer;
import net.techbrew.mcjm.render.overlay.GridRenderer;
import net.techbrew.mcjm.render.overlay.OverlayRadarRenderer;
import net.techbrew.mcjm.render.overlay.OverlayWaypointRenderer;
import net.techbrew.mcjm.render.texture.TextureCache;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.logging.Logger;

/**
 * Displays the map as a minimap overlay in-game.
 * 
 * @author mwoodman
 *
 */
public class MiniMapOverlay {

    static Boolean enabled;

	final MapOverlayState state = MapOverlay.state();
	final OverlayWaypointRenderer waypointRenderer = new OverlayWaypointRenderer();
	final OverlayRadarRenderer radarRenderer = new OverlayRadarRenderer();
    final GridRenderer gridRenderer;
    int lastMcWidth = 0;
    int lastMcHeight = 0;
    ScaledResolution lastScaledResolution;
    Color playerInfoFgColor = Color.GREEN;
    Color playerInfoBgColor = new Color(0x22, 0x22, 0x22);

    private enum Mode{TopRight}

	Logger logger = JourneyMap.getLogger();
    final Minecraft mc;

    private Mode displayMode = Mode.TopRight;

    private boolean visible = true;

	/**
	 * Default constructor
	 */
	public MiniMapOverlay() {
        gridRenderer = new GridRenderer(3);
        mc = Minecraft.getMinecraft();
        updateResolution();
	}


	void drawMap() {

        // Check player status
        EntityClientPlayerMP player = mc.thePlayer;
        if (player==null) {
            return;
        }

        final boolean doStateRefresh = state.shouldRefresh();

        // Update the state first
        if(doStateRefresh) {
            state.refresh(mc, player);
        }

        // Update the grid
        gridRenderer.setContext(state.getWorldDir(), state.getDimension());
        gridRenderer.center((int) mc.thePlayer.posX, (int) mc.thePlayer.posZ, state.currentZoom);
        gridRenderer.updateTextures(state.getMapType(), state.getVSlice(), mc.displayWidth, mc.displayHeight, true, 0, 0);

        // Update the state first
        if(doStateRefresh) {
            // Build list of drawSteps
            state.generateDrawSteps(mc, gridRenderer, waypointRenderer, radarRenderer);

            // Reset timers
            state.updateLastRefresh();
        }

        try {

            updateResolution();
            JmUI.sizeDisplay(mc.displayWidth, mc.displayHeight);

            GL11.glPushMatrix();
            // TODO: encapsulate the offsets for positioning and sizing
            final int minimapSize = 256;
            final double minimapOffset = minimapSize*0.5;
            GL11.glTranslated((mc.displayWidth/2)-minimapOffset, -(mc.displayHeight/2)+minimapOffset, 0);

            GL11.glScissor(mc.displayWidth-minimapSize,mc.displayHeight-minimapSize,minimapSize,minimapSize);
            GL11.glEnable(GL11.GL_SCISSOR_TEST);

            final int xOffset = 0;
            final int yOffset = 0;

            gridRenderer.draw(1f, xOffset, yOffset);
            BaseOverlayRenderer.draw(state.getDrawSteps(), xOffset, yOffset);

            Point playerPixel = gridRenderer.getPixel((int) mc.thePlayer.posX, (int) mc.thePlayer.posZ);
            if(playerPixel!=null) {
                BaseOverlayRenderer.drawPlayer(gridRenderer, mc, xOffset, yOffset, false);
            }

            GL11.glDisable(GL11.GL_SCISSOR_TEST);


            GL11.glPopMatrix();

            final int playerX = (int) player.posX;
            final int playerZ = (int) player.posZ;
            final int playerY = (int) player.posY;
            final int worldX = ((int) Math.floor(player.posX) % 16) & 15;
            final int worldZ = ((int) Math.floor(player.posZ) % 16) & 15;

            String biomeName = mc.theWorld.getChunkFromChunkCoords(player.chunkCoordX, player.chunkCoordZ).getBiomeGenForWorldCoords(worldX, worldZ, mc.theWorld.getWorldChunkManager()).biomeName;
            state.playerLastPos = Constants.getString("MapOverlay.player_location_abbrev", playerX, playerZ, playerY, mc.thePlayer.chunkCoordY, biomeName);
            BaseOverlayRenderer.drawCenteredLabel(state.playerLastPos, mc.displayWidth-(minimapSize/2), minimapSize, 14, -7, playerInfoBgColor, playerInfoFgColor, 215);

            JmUI.sizeDisplay(lastScaledResolution.getScaledWidth_double(), lastScaledResolution.getScaledHeight_double());

        } catch(Throwable t) {
            logger.severe("Minimap error:" + t);
        }
				
	}

    private void updateResolution(){
        if(mc.displayHeight!=lastMcHeight || mc.displayWidth!=lastMcWidth) {
            lastMcWidth = mc.displayWidth;
            lastMcHeight = mc.displayHeight;
            lastScaledResolution = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
        }
    }

	public void reset() {
		state.requireRefresh();
        gridRenderer.clear();
	}

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public static boolean isEnabled() {
        if(enabled==null) {
            enabled = PropertyManager.getInstance().getBoolean(PropertyManager.Key.PREF_SHOW_MINIMAP)
                    && !WaypointHelper.isReiLoaded() && !WaypointHelper.isVoxelMapLoaded();
        }
        return enabled;
    }

    public static void setEnabled(boolean enable){
        enabled = enable;
        PropertyManager.getInstance().setProperty(PropertyManager.Key.PREF_SHOW_MINIMAP, enable);
    }
}

