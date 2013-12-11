package net.techbrew.mcjm.ui;

import net.minecraft.src.EntityClientPlayerMP;
import net.minecraft.src.Minecraft;
import net.minecraft.src.ScaledResolution;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.model.EntityHelper;
import net.techbrew.mcjm.model.MapOverlayState;
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

	final MapOverlayState state = MapOverlay.state();
	final OverlayWaypointRenderer waypointRenderer = new OverlayWaypointRenderer();
	final OverlayRadarRenderer radarRenderer = new OverlayRadarRenderer();
    final GridRenderer gridRenderer;
    int lastMcWidth = 0;
    int lastMcHeight = 0;
    ScaledResolution lastScaledResolution;

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
        gridRenderer.updateTextures(state.getMapType(), state.getVSlice(), 512, 512, doStateRefresh, 0, 0);

        // Update the state first
        if(doStateRefresh) {
            // Build list of drawSteps
            state.generateDrawSteps(mc, false, gridRenderer, waypointRenderer, radarRenderer);

            // Reset timers
            state.updateLastRefresh();
        }




        try {
            GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glLoadIdentity();
            GL11.glOrtho(0.0D, lastMcWidth, lastMcHeight, 0.0D, 1000.0D, 3000.0D);
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glLoadIdentity();

            GL11.glPushMatrix();

            // TODO: encapsulate the offsets for positioning and sizing
            GL11.glTranslatef(mc.displayWidth - 384, -128, -2000.0F);

            GL11.glScissor(mc.displayWidth-256,mc.displayHeight-256,512,512);
            GL11.glEnable(GL11.GL_SCISSOR_TEST);

            final int xOffset = 0;
            final int yOffset = 0;

            gridRenderer.draw(1f, xOffset, yOffset);
            BaseOverlayRenderer.draw(state.getDrawSteps(), xOffset, yOffset);

            Point playerPixel = gridRenderer.getPixel((int) mc.thePlayer.posX, (int) mc.thePlayer.posZ);
            if(playerPixel!=null) {
                new BaseOverlayRenderer.DrawEntityStep(playerPixel, EntityHelper.getHeading(mc.thePlayer), false, TextureCache.instance().getPlayerLocator(), 8).draw(xOffset, yOffset);
            }

            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            GL11.glPopMatrix();

            updateResolution();
            GL11.glOrtho(0.0D, lastScaledResolution.getScaledWidth_double(), lastScaledResolution.getScaledHeight_double(), 0.0D, 1000.0D, 3000.0D);

        } catch(Throwable t) {
            logger.severe(t.getMessage());
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
}

