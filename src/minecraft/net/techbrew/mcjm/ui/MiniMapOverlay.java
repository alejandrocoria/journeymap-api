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

    private enum Position {TopLeft, TopRight, BottomLeft, BottomRight}
    private enum Shape {SmallSquare, LargeSquare, SmallCircle, LargeCircle}

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
    private TextureImpl minimapTexture;
    private int lastMcWidth = 0;
    private int lastMcHeight = 0;
    private ScaledResolution lastScaledResolution;

    private Position position;
    private Shape shape;

    private boolean visible = true;

	/**
	 * Default constructor
	 */
	public MiniMapOverlay() {
        setPosition(Position.TopRight);
        setShape(Shape.LargeSquare); // TODO: Get as preference
        updateResolution();
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

            // Update the grid
            gridRenderer.setContext(state.getWorldDir(), state.getDimension());
            boolean moved = gridRenderer.center((int) mc.thePlayer.posX, (int) mc.thePlayer.posZ, state.currentZoom);
            if(doStateRefresh || moved) {
                gridRenderer.updateTextures(state.getMapType(), state.getVSlice(), mc.displayWidth, mc.displayHeight, true, 0, 0);
            }

            // Update the state first
            if(doStateRefresh) {
                // Build list of drawSteps
                state.generateDrawSteps(mc, gridRenderer, waypointRenderer, radarRenderer);

                // Reset timers
                state.updateLastRefresh();
            }

            updateResolution();

            // Use 1:1 resolution for minimap regardless of how Minecraft UI is scaled
            JmUI.sizeDisplay(mc.displayWidth, mc.displayHeight);

            // Ensure colors and alpha reset
            GL11.glColor4f(1,1,1,1);

            // Push matrix for translation to corner
            GL11.glPushMatrix();

            int minimapSize=0,textureX=0,textureY=0;
            double minimapOffset=0,translateX=0,translateY=0;
            int scissorMarginX=0,scissorMarginY=0,scissorX=0,scissorY=0,labelX=0,labelY=0,labelYOffset=0;

            switch(shape){
                case SmallSquare: {
                    minimapSize = 256;
                    scissorMarginX=5;
                    scissorMarginY=5;
                    break;
                }
                case SmallCircle: {
                    minimapSize = 256;
                    scissorMarginX=5;
                    scissorMarginY=5;
                    break;
                }
                case LargeSquare: {
                    minimapSize = 512;
                    scissorMarginX=5;
                    scissorMarginY=6;
                    break;
                }
                case LargeCircle: {
                    minimapSize = 512;
                    scissorMarginX=5;
                    scissorMarginY=5;
                    break;
                }
            }

            minimapOffset = minimapSize*0.5;
            labelYOffset = -7;

            switch(position){
                case TopRight : {
                    textureX = mc.displayWidth- minimapTexture.width;
                    textureY = 0;
                    translateX = (mc.displayWidth/2)-minimapOffset;
                    translateY = -(mc.displayHeight/2)+minimapOffset;
                    scissorX = mc.displayWidth-minimapSize-scissorMarginX;
                    scissorY = mc.displayHeight-minimapSize-scissorMarginY;
                    labelX = mc.displayWidth-(minimapSize/2);
                    labelY = minimapSize;
                    break;
                }
                case BottomRight : {
                    textureX = mc.displayWidth- minimapTexture.width;
                    textureY = mc.displayHeight- minimapSize-scissorMarginY-scissorMarginY;
                    translateX = (mc.displayWidth/2)-minimapOffset;
                    translateY = (mc.displayHeight/2)-minimapOffset;
                    scissorX = mc.displayWidth-minimapSize-scissorMarginX;
                    scissorY = scissorMarginY;
                    labelX = mc.displayWidth-(minimapSize/2);
                    labelY = mc.displayHeight-scissorMarginY;
                    break;
                }
                case TopLeft : {
                    textureX = -minimapTexture.width+minimapSize+scissorMarginX+scissorMarginX;
                    textureY = 0;
                    translateX = -(mc.displayWidth/2)+minimapOffset;
                    translateY = -(mc.displayHeight/2)+minimapOffset;
                    scissorX = 0+scissorMarginX;
                    scissorY = mc.displayHeight-minimapSize-scissorMarginY;
                    labelX = minimapSize/2;
                    labelY = minimapSize;
                    break;
                }
                case BottomLeft : {
                    textureX = 0;
                    textureY = mc.displayHeight- minimapTexture.height;
                    translateX = -(mc.displayWidth/2)+minimapOffset;
                    translateY = (mc.displayHeight/2)-minimapOffset;
                    scissorX = 0+scissorMarginX;
                    scissorY = mc.displayHeight-scissorMarginY;
                    labelX = minimapSize/2;
                    labelY = mc.displayHeight-20; // TODO
                    break;
                }
            }

            // Draw texture
            BaseOverlayRenderer.drawImage(minimapTexture, textureX, textureY, false);

            // Move map center
            GL11.glTranslated(translateX, translateY, 0);

            // Scissor area that shouldn't be drawn
            GL11.glScissor(scissorX,scissorY,minimapSize,minimapSize);
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
            final int worldX = ((int) Math.floor(player.posX) % 16) & 15;
            final int worldZ = ((int) Math.floor(player.posZ) % 16) & 15;
            String biomeName = mc.theWorld.getChunkFromChunkCoords(player.chunkCoordX, player.chunkCoordZ).getBiomeGenForWorldCoords(worldX, worldZ, mc.theWorld.getWorldChunkManager()).biomeName;
            state.playerLastPos = Constants.getString("MapOverlay.player_location_abbrev", playerX, playerZ, playerY, mc.thePlayer.chunkCoordY, biomeName);

            BaseOverlayRenderer.drawCenteredLabel(state.playerLastPos, labelX, labelY, 14, labelYOffset, playerInfoBgColor, playerInfoFgColor, 215);

            // Restore GL attrs
            GL11.glEnable(GL11.GL_DEPTH_TEST);

            // Return resolution to how it is normally scaled
            JmUI.sizeDisplay(lastScaledResolution.getScaledWidth_double(), lastScaledResolution.getScaledHeight_double());

        } catch(Throwable t) {
            logger.severe("Minimap error:" + LogFormatter.toString(t));
        } finally {
            timer.pause();
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

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Shape getShape() {
        return shape;
    }

    public void setShape(Shape shape) {
        if(shape!=this.shape) {
            this.shape = shape;
            if(this.minimapTexture!=null){
                this.minimapTexture.deleteTexture(); // TODO: ensure reloading texture works
            }
            switch(shape) {
                case SmallSquare: {
                    this.minimapTexture = TextureCache.instance().getMinimapSmallSquare();
                    break;
                }
                case SmallCircle: {
                    this.minimapTexture = TextureCache.instance().getMinimapSmallCircle();
                    break;
                }
                case LargeSquare: {
                    this.minimapTexture = TextureCache.instance().getMinimapLargeSquare();
                    break;
                }
                case LargeCircle: {
                    this.minimapTexture = TextureCache.instance().getMinimapLargeCircle();
                    break;
                }
            }
            this.drawTimer = StatTimer.get("MiniMapOverlay.drawMap." + shape.name());
            this.drawTimerWithRefresh = StatTimer.get("MiniMapOverlay.drawMap+refreshState." + shape.name());
        }
    }
}

