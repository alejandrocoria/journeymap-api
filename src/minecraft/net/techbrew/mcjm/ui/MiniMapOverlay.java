package net.techbrew.mcjm.ui;

import net.minecraft.src.EntityClientPlayerMP;
import net.minecraft.src.Minecraft;
import net.minecraft.src.ScaledResolution;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.io.PropertyManager;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.log.StatTimer;
import net.techbrew.mcjm.model.EntityHelper;
import net.techbrew.mcjm.model.MapOverlayState;
import net.techbrew.mcjm.model.WaypointHelper;
import net.techbrew.mcjm.render.overlay.BaseOverlayRenderer;
import net.techbrew.mcjm.render.overlay.GridRenderer;
import net.techbrew.mcjm.render.overlay.OverlayRadarRenderer;
import net.techbrew.mcjm.render.overlay.OverlayWaypointRenderer;
import net.techbrew.mcjm.render.texture.TextureCache;
import net.techbrew.mcjm.render.texture.TextureImpl;
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

    private final Logger logger = JourneyMap.getLogger();
    private final Minecraft mc = Minecraft.getMinecraft();
    private final MapOverlayState state = MapOverlay.state();
    private final OverlayWaypointRenderer waypointRenderer = new OverlayWaypointRenderer();
    private final OverlayRadarRenderer radarRenderer = new OverlayRadarRenderer();
    private final GridRenderer gridRenderer = new GridRenderer(3);
    private StatTimer drawTimer;
    private StatTimer drawTimerWithRefresh;
    private final Color playerInfoFgColor = Color.GREEN;
    private final Color playerInfoBgColor = new Color(0x22, 0x22, 0x22);

    private Boolean enabled;

    private DisplayVars dv;

    private boolean visible = true;

	/**
	 * Default constructor
	 */
	public MiniMapOverlay() {
        try {
            updateDisplayVars(DisplayVars.Shape.SmallSquare, DisplayVars.Position.TopRight); // TODO: Get from preferences
        } catch(Throwable t) {
            t.printStackTrace();
        }
	}


	void drawMap() {

        // Check player status
        if (mc.thePlayer==null) {
            return;
        }

        final boolean doStateRefresh = state.shouldRefresh();
        final StatTimer timer = doStateRefresh ? drawTimerWithRefresh : drawTimer;
        timer.start();

        try {
            final EntityClientPlayerMP player = mc.thePlayer;

            // Update the state first
            if(doStateRefresh) {
                state.refresh(mc, player);
            }

            // Update the grid // TODO:  Do this in another thread
            gridRenderer.setContext(state.getWorldDir(), state.getDimension());
            gridRenderer.center((int) mc.thePlayer.posX, (int) mc.thePlayer.posZ, state.currentZoom);
            gridRenderer.updateTextures(state.getMapType(), state.getVSlice(), mc.displayWidth, mc.displayHeight, doStateRefresh, 0, 0);
            if(doStateRefresh ) {

                // Build list of drawSteps
                state.generateDrawSteps(mc, gridRenderer, waypointRenderer, radarRenderer);

                // Reset timers
                state.updateLastRefresh();
            }

            updateDisplayVars();

            // Use 1:1 resolution for minimap regardless of how Minecraft UI is scaled
            JmUI.sizeDisplay(mc.displayWidth, mc.displayHeight);

            // Ensure colors and alpha reset
            GL11.glColor4f(1,1,1,1);

            // Push matrix for translation to corner
            GL11.glPushMatrix();

            // Move map center
            GL11.glTranslated(dv.translateX, dv.translateY, 0);

            // Scissor area that shouldn't be drawn
            GL11.glScissor(dv.scissorX,dv.scissorY,dv.minimapSize,dv.minimapSize);
            GL11.glEnable(GL11.GL_SCISSOR_TEST);

            // Draw grid
            gridRenderer.draw(1f, 0, 0);

            // Draw entities, etc
            gridRenderer.draw(state.getDrawSteps(), 0, 0);

            // Draw player
            Point playerPixel = gridRenderer.getPixel((int) mc.thePlayer.posX, (int) mc.thePlayer.posZ);
            if(playerPixel!=null) {
                BaseOverlayRenderer.DrawStep drawStep = new BaseOverlayRenderer.DrawEntityStep((int) mc.thePlayer.posX, (int) mc.thePlayer.posZ, EntityHelper.getHeading(mc.thePlayer), false, TextureCache.instance().getPlayerLocator(), 8);
                gridRenderer.draw(0, 0, drawStep);
            }

            // Finish Scissor
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            GL11.glPopMatrix();

            // Determine current biome
            final int playerX = (int) player.posX;
            final int playerZ = (int) player.posZ;
            final int playerY = (int) player.posY;
            final String playerInfo = Constants.getString("MapOverlay.player_location_abbrev",
                    playerX, playerZ, playerY, mc.thePlayer.chunkCoordY, state.getPlayerBiome());

            // Draw position text
            BaseOverlayRenderer.drawCenteredLabel(playerInfo, dv.labelX, dv.labelY, 14, dv.labelYOffset, playerInfoBgColor, playerInfoFgColor, 215, state.fontScale);

            // Draw minimap texture
            BaseOverlayRenderer.drawImage(dv.minimapTexture, dv.textureX, dv.textureY, false);

            // Restore GL attrs
            GL11.glEnable(GL11.GL_DEPTH_TEST);

            // Return resolution to how it is normally scaled
            JmUI.sizeDisplay(dv.scaledResolution.getScaledWidth_double(), dv.scaledResolution.getScaledHeight_double());

        } catch(Throwable t) {
            logger.severe("Minimap error:" + LogFormatter.toString(t));
        } finally {
            timer.stop();
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

    private void updateDisplayVars() {
        if(dv!=null) {
            updateDisplayVars(dv.shape, dv.position);
        }
    }
    public void updateDisplayVars(DisplayVars.Shape shape, DisplayVars.Position position) {

        if(dv!=null && mc.displayHeight==dv.displayHeight && mc.displayWidth==dv.displayWidth
                && this.dv.shape==shape && this.dv.position==position){
            return;
        }

        DisplayVars oldDv = this.dv;
        this.dv = new DisplayVars(mc, shape, position);

        if(oldDv==null || oldDv.shape!=this.dv.shape){
            this.drawTimer = StatTimer.get("MiniMapOverlay.drawMap." + shape.name(), 200);
            this.drawTimerWithRefresh = StatTimer.get("MiniMapOverlay.drawMap+refreshState." + shape.name());
        }

        if(oldDv!=null && oldDv.shape!=this.dv.shape){
            oldDv.minimapTexture.deleteTexture(); // TODO: ensure reloading texture works
        }
    }

    static class DisplayVars {

        enum Position {TopLeft, TopRight, BottomLeft, BottomRight}
        enum Shape {SmallSquare, LargeSquare, SmallCircle, LargeCircle}

        final Position position;
        final Shape shape;
        final TextureImpl minimapTexture;
        final int displayWidth;
        final int displayHeight;
        final ScaledResolution scaledResolution;
        final int minimapSize,textureX,textureY;
        final double minimapOffset,translateX,translateY;
        final int marginX,marginY,scissorX,scissorY,labelX,labelY,labelYOffset;

        DisplayVars(Minecraft mc, Shape shape, Position position){
            this.shape = shape;
            this.position = position;
            displayWidth = mc.displayWidth;
            displayHeight = mc.displayHeight;
            scaledResolution = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);

            switch(shape){
                case LargeCircle: {
                    minimapTexture = TextureCache.instance().getMinimapLargeCircle();
                    minimapSize = 512;
                    marginX=5;
                    marginY=5;
                    break;
                }
                case SmallCircle: {
                    minimapTexture = TextureCache.instance().getMinimapSmallCircle();
                    minimapSize = 256;
                    marginX=5;
                    marginY=5;
                    break;
                }
                case LargeSquare: {
                    minimapTexture = TextureCache.instance().getMinimapLargeSquare();
                    minimapSize = 512;
                    marginX=5;
                    marginY=5;
                    break;
                }
                case SmallSquare:
                default: {
                    minimapTexture = TextureCache.instance().getMinimapSmallSquare();
                    minimapSize = 256;
                    marginX=2;
                    marginY=4;
                    break;
                }
            }

            minimapOffset = minimapSize*0.5;
            labelYOffset = -7;

            switch(position){
                case BottomRight : {
                    textureX = mc.displayWidth- minimapTexture.width;
                    textureY = mc.displayHeight- minimapSize-marginY-marginY;
                    translateX = (mc.displayWidth/2)-minimapOffset;
                    translateY = (mc.displayHeight/2)-minimapOffset;
                    scissorX = mc.displayWidth-minimapSize-marginX;
                    scissorY = marginY;
                    labelX = mc.displayWidth-(minimapSize/2);
                    labelY = mc.displayHeight-marginY;
                    break;
                }
                case TopLeft : {
                    textureX = -minimapTexture.width+minimapSize+marginX+marginX;
                    textureY = 0;
                    translateX = -(mc.displayWidth/2)+minimapOffset;
                    translateY = -(mc.displayHeight/2)+minimapOffset;
                    scissorX = 0+marginX;
                    scissorY = mc.displayHeight-minimapSize-marginY;
                    labelX = minimapSize/2;
                    labelY = minimapSize;
                    break;
                }
                case BottomLeft : {
                    textureX = 0;
                    textureY = mc.displayHeight- minimapTexture.height;
                    translateX = -(mc.displayWidth/2)+minimapOffset;
                    translateY = (mc.displayHeight/2)-minimapOffset;
                    scissorX = 0+marginX;
                    scissorY = mc.displayHeight-marginY;
                    labelX = minimapSize/2;
                    labelY = mc.displayHeight-20; // TODO
                    break;
                }
                case TopRight :
                default : {
                    textureX = mc.displayWidth- (minimapTexture.width) + ((minimapTexture.width-minimapSize)/2) - marginX;
                    textureY = -(minimapTexture.height-minimapSize)/2 + marginY;
                    translateX = (mc.displayWidth/2)-minimapOffset;
                    translateY = -(mc.displayHeight/2)+minimapOffset;
                    scissorX = mc.displayWidth-minimapSize-marginX;
                    scissorY = mc.displayHeight-minimapSize-marginY;
                    labelX = mc.displayWidth-(minimapSize/2);
                    labelY = minimapSize;
                    break;
                }
            }

            System.out.println("New DisplayVars: " + shape + " " + position + " : " + displayWidth + "x" + displayHeight);
        }
    }
}

