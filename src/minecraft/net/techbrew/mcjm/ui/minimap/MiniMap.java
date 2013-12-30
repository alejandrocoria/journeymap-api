package net.techbrew.mcjm.ui.minimap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.io.PropertyManager;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.log.StatTimer;
import net.techbrew.mcjm.model.EntityHelper;
import net.techbrew.mcjm.model.MapOverlayState;
import net.techbrew.mcjm.model.WaypointHelper;
import net.techbrew.mcjm.render.draw.DrawEntityStep;
import net.techbrew.mcjm.render.draw.DrawStep;
import net.techbrew.mcjm.render.draw.DrawUtil;
import net.techbrew.mcjm.render.overlay.GridRenderer;
import net.techbrew.mcjm.render.overlay.OverlayRadarRenderer;
import net.techbrew.mcjm.render.overlay.OverlayWaypointRenderer;
import net.techbrew.mcjm.render.texture.TextureCache;
import net.techbrew.mcjm.ui.JmUI;
import net.techbrew.mcjm.ui.map.MapOverlay;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.logging.Logger;

import static org.lwjgl.opengl.GL11.*;

;

/**
 * Displays the map as a minimap overlay in-game.
 * 
 * @author mwoodman
 *
 */
public class MiniMap {

    private final Logger logger = JourneyMap.getLogger();
    private final Minecraft mc = Minecraft.getMinecraft();
    private final MapOverlayState state = MapOverlay.state();
    private final OverlayWaypointRenderer waypointRenderer = new OverlayWaypointRenderer();
    private final OverlayRadarRenderer radarRenderer = new OverlayRadarRenderer();
    private final GridRenderer gridRenderer = new GridRenderer(3);
    private StatTimer drawTimer;
    private final Color playerInfoFgColor = Color.LIGHT_GRAY;
    private final Color playerInfoBgColor = new Color(0x22, 0x22, 0x22);

    private Boolean enabled;
    private Boolean showFps = true;

    private DisplayVars dv;

    private boolean visible = true;

	/**
	 * Default constructor
	 */
	public MiniMap() {
        final PropertyManager pm = PropertyManager.getInstance();

        setEnabled(pm.getBoolean(PropertyManager.Key.PREF_SHOW_MINIMAP));
        setShowFps(pm.getBoolean(PropertyManager.Key.PREF_MINIMAP_SHOWFPS));
        state.fontScale = pm.getDouble(PropertyManager.Key.PREF_MINIMAP_FONTSCALE);

        DisplayVars.Shape shape = DisplayVars.Shape.valueOf(pm.getString(PropertyManager.Key.PREF_MINIMAP_SHAPE));
        DisplayVars.Position position = DisplayVars.Position.valueOf(pm.getString(PropertyManager.Key.PREF_MINIMAP_POSITION));

        updateDisplayVars(shape, position);
	}


	public void drawMap() {

        // Check player status
        if (mc.thePlayer==null) {
            return;
        }

        final boolean doStateRefresh = state.shouldRefresh();
        drawTimer.start();

        try {
            final EntityClientPlayerMP player = mc.thePlayer;

            // Update the state first
            if(doStateRefresh) {
                state.refresh(mc, player);
            }

            // Update the grid
            gridRenderer.setContext(state.getWorldDir(), state.getDimension());
            gridRenderer.center(mc.thePlayer.posX, mc.thePlayer.posZ, state.currentZoom);
            gridRenderer.updateTextures(state.getMapType(), state.getVSlice(), mc.displayWidth, mc.displayHeight, doStateRefresh, 0, 0);
            if(doStateRefresh ) {
                state.generateDrawSteps(mc, gridRenderer, waypointRenderer, radarRenderer);
                state.updateLastRefresh();
            }

            updateDisplayVars();

            // Use 1:1 resolution for minimap regardless of how Minecraft UI is scaled
            JmUI.sizeDisplay(mc.displayWidth, mc.displayHeight);

            // Ensure colors and alpha reset
            GL11.glColor4f(1, 1, 1, 1);

            // Push matrix for translation to corner
            GL11.glPushMatrix();

            // Draw mask (if present) using stencil buffer
            if (dv.maskTexture!=null)
            {
                try {
                    glClear(GL_DEPTH_BUFFER_BIT);
                    glEnable(GL_STENCIL_TEST);
                    glColorMask(false, false, false, false);
                    glDepthMask(false);
                    glStencilFunc(GL_NEVER, 1, 0xFF);
                    glStencilOp(GL_REPLACE, GL_KEEP, GL_KEEP);
                    glStencilMask(0xFF);
                    glClear(GL_STENCIL_BUFFER_BIT);
                    DrawUtil.drawQuad(dv.maskTexture, dv.textureX, dv.textureY, dv.maskTexture.width, dv.maskTexture.height, null, 1f, false, true, GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                    glColorMask(true, true, true, true);
                    glDepthMask(true);
                    glStencilMask(0x00);
                    //glStencilFunc(GL_EQUAL, 0, 0xFF);
                    glStencilFunc(GL_EQUAL, 1, 0xFF);
                } catch(Throwable t) {
                    logger.warning("Stencil buffer failing with circle mask:" + LogFormatter.toString(t));
                    if(getShape()==DisplayVars.Shape.LargeCircle){
                        setShape(DisplayVars.Shape.LargeSquare);
                    } else if(getShape()==DisplayVars.Shape.SmallCircle){
                        setShape(DisplayVars.Shape.SmallSquare);
                    }
                    return;
                }
            }

            // Move center to corner
            GL11.glTranslated(dv.translateX, dv.translateY, 0);

            // Scissor area that shouldn't be drawn
            GL11.glScissor(dv.scissorX,dv.scissorY,dv.minimapSize,dv.minimapSize);
            GL11.glEnable(GL11.GL_SCISSOR_TEST);

            // Draw grid
            gridRenderer.draw(1f, 0, 0);

            // Draw entities, etc
            gridRenderer.draw(state.getDrawSteps(), 0, 0);

            // Draw player
            Point2D playerPixel = gridRenderer.getPixel(mc.thePlayer.posX, mc.thePlayer.posZ);
            if(playerPixel!=null) {
                DrawStep drawStep = new DrawEntityStep(mc.thePlayer.posX, mc.thePlayer.posZ, EntityHelper.getHeading(mc.thePlayer), false, TextureCache.instance().getPlayerLocator(), 8);
                gridRenderer.draw(0, 0, drawStep);
            }

            // Return center to mid-screen
            GL11.glTranslated(-dv.translateX, -dv.translateY, 0);

            // If using a mask, turn off the stencil test
            if (dv.maskTexture!=null)
            {
                glDisable(GL_STENCIL_TEST);
            }

            // Finish Scissor
            GL11.glDisable(GL11.GL_SCISSOR_TEST);

            // Determine current biome
            final int playerX = (int) player.posX;
            final int playerZ = (int) player.posZ;
            final int playerY = (int) player.posY;

            // Player info string
            String playerInfo = Constants.getString("MapOverlay.player_location_minimap", playerX, playerZ, playerY, mc.thePlayer.chunkCoordY, state.getPlayerBiome());
            if(dv.fontScale>1 && mc.fontRenderer.getStringWidth(playerInfo)*dv.fontScale>dv.minimapSize){
                // Drop biome if running of space
                playerInfo = Constants.getString("MapOverlay.player_location_minimap_nobiome", playerX, playerZ, playerY, mc.thePlayer.chunkCoordY);
            }

            // Draw position text
            DrawUtil.drawCenteredLabel(playerInfo, dv.labelX, dv.bottomLabelY, playerInfoBgColor, playerInfoFgColor, 200, dv.fontScale);

            // Draw FPS
            if(showFps){
                String fps = mc.debug;
                final int i = fps!=null ? fps.indexOf(',') : -1;
                if(i>0){
                    DrawUtil.drawCenteredLabel(fps.substring(0, i), dv.labelX, dv.topLabelY, playerInfoBgColor, playerInfoFgColor, 200, dv.fontScale);
                }
            }

            // Restore GL attrs assumed by Minecraft to be enabled
            GL11.glEnable(GL11.GL_DEPTH_TEST);

            // Draw border texture
            DrawUtil.drawImage(dv.borderTexture, dv.textureX, dv.textureY, false);

            GL11.glPopMatrix();

            // TODO: Move this somewhere else


            // Return resolution to how it is normally scaled
            JmUI.sizeDisplay(dv.scaledResolution.getScaledWidth_double(), dv.scaledResolution.getScaledHeight_double());

        } catch(Throwable t) {
            logger.severe("Minimap error:" + LogFormatter.toString(t));
        } finally {
            drawTimer.stop();
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

    public boolean isEnabled() {
        if(enabled==null) {
            enabled = PropertyManager.getInstance().getBoolean(PropertyManager.Key.PREF_SHOW_MINIMAP)
                    && !WaypointHelper.isReiLoaded() && !WaypointHelper.isVoxelMapLoaded();
        }
        return enabled;
    }

    public void setEnabled(boolean enable){
        enabled = enable;
        PropertyManager.getInstance().setProperty(PropertyManager.Key.PREF_SHOW_MINIMAP, enable);
    }

    public boolean isShowFps() {
        return showFps;
    }

    public void setShowFps(boolean enable){
        showFps = enable;
        //PropertyManager.getInstance().setProperty(PropertyManager.Key.PREF_SHOW_MINIMAP, enable); // TODO
    }

    public DisplayVars.Position getPosition() {
        return dv.position;
    }

    public void setPosition(DisplayVars.Position position) {
        if(dv!=null) {
            updateDisplayVars(dv.shape, position);
        }
    }

    public DisplayVars.Shape getShape() {
        return dv.shape;
    }

    public void setShape(DisplayVars.Shape shape) {
        if(dv!=null) {
            updateDisplayVars(shape, dv.position);
        }
    }

    public void updateDisplayVars() {
        if(dv!=null) {
            updateDisplayVars(dv.shape, dv.position);
        }
    }
    public void updateDisplayVars(DisplayVars.Shape shape, DisplayVars.Position position) {

        if(dv!=null
                && mc.displayHeight==dv.displayHeight
                && mc.displayWidth==dv.displayWidth
                && this.dv.shape==shape
                && this.dv.position==position
                && this.dv.fontScale==state.fontScale){
            return;
        }

        DisplayVars oldDv = this.dv;
        this.dv = new DisplayVars(mc, shape, position, state.fontScale);

        if(oldDv==null || oldDv.shape!=this.dv.shape){
            this.drawTimer = StatTimer.get("MiniMap.drawMap." + shape.name(), 200);
        }

        if(oldDv!=null && oldDv.shape!=this.dv.shape){
            oldDv.borderTexture.deleteTexture(); // TODO: ensure reloading texture works
        }

        // THIS IS WRONG - scissorY you bastard
        gridRenderer.setViewPort(new Point2D.Double(this.dv.scissorX, this.dv.scissorY+dv.minimapSize), this.dv.minimapSize);
    }

}

