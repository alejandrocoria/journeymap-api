package net.techbrew.journeymap.ui.minimap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.Vec3;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.log.StatTimer;
import net.techbrew.journeymap.model.EntityHelper;
import net.techbrew.journeymap.model.MapOverlayState;
import net.techbrew.journeymap.model.Waypoint;
import net.techbrew.journeymap.properties.FullMapProperties;
import net.techbrew.journeymap.properties.MiniMapProperties;
import net.techbrew.journeymap.properties.WaypointProperties;
import net.techbrew.journeymap.render.draw.DrawStep;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.render.draw.DrawWayPointStep;
import net.techbrew.journeymap.render.overlay.GridRenderer;
import net.techbrew.journeymap.render.overlay.OverlayRadarRenderer;
import net.techbrew.journeymap.render.overlay.OverlayWaypointRenderer;
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.render.texture.TextureImpl;
import net.techbrew.journeymap.ui.JmUI;
import net.techbrew.journeymap.ui.map.MapOverlay;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.logging.Logger;

import static org.lwjgl.opengl.GL11.*;

/**
 * Displays the map as a minimap overlay in-game.
 *
 * @author mwoodman
 */
public class MiniMap
{
    private static final float lightmapS = (float) (15728880 % 65536) / 1f;
    private static final float lightmapT = (float) (15728880 / 65536) / 1f;
    private static final long labelRefreshRate = 1001;

    private final Logger logger = JourneyMap.getLogger();
    private final Minecraft mc = Minecraft.getMinecraft();

    private final Color playerInfoFgColor = Color.LIGHT_GRAY;
    private final Color playerInfoBgColor = new Color(0x22, 0x22, 0x22);
    private final String[] locationFormats = {"MapOverlay.location_xzye", "MapOverlay.location_xzy", "MapOverlay.location_xz"};

    private final MiniMapProperties miniMapProperties = JourneyMap.getInstance().miniMapProperties;
    private final FullMapProperties fullMapProperties = JourneyMap.getInstance().fullMapProperties;
    private final WaypointProperties waypointProperties = JourneyMap.getInstance().waypointProperties;
    private final MapOverlayState state = MapOverlay.state();
    private final OverlayWaypointRenderer waypointRenderer = new OverlayWaypointRenderer();
    private final OverlayRadarRenderer radarRenderer = new OverlayRadarRenderer();
    private final GridRenderer gridRenderer = new GridRenderer(3);
    private final TextureImpl playerLocatorTex;

    private EntityClientPlayerMP player;
    private StatTimer drawTimer;
    private DisplayVars dv;
    private boolean visible = true;

    private long lastLabelRefresh = 0;
    private String fpsLabelText;
    private String locationLabelText;
    private String biomeLabelText;

    /**
     * Default constructor
     */
    public MiniMap()
    {
        player = mc.thePlayer;
        playerLocatorTex = TextureCache.instance().getPlayerLocatorSmall();
        updateDisplayVars(DisplayVars.Shape.getPreferred(), DisplayVars.Position.getPreferred(), true);
    }

    /**
     * Called in the render loop.
     */
    public void drawMap()
    {
        // Check player status
        player = mc.thePlayer;
        if (player == null)
        {
            return;
        }

        final boolean doStateRefresh = state.shouldRefresh(mc);
        drawTimer.start();

        try
        {
            // Update the state first
            if (doStateRefresh)
            {
                state.refresh(mc, player, miniMapProperties);
            }
            boolean showCaves = fullMapProperties.showCaves.get();

            // Update the grid
            gridRenderer.setContext(state.getWorldDir(), state.getDimension());
            gridRenderer.center(mc.thePlayer.posX, mc.thePlayer.posZ, state.currentZoom);
            gridRenderer.updateTextures(state.getMapType(showCaves), state.getVSlice(), mc.displayWidth, mc.displayHeight, doStateRefresh, 0, 0, miniMapProperties);
            if (doStateRefresh)
            {
                //boolean unicodeForced = DrawUtil.startUnicode(mc.fontRenderer, state.mapForceUnicode);
                state.generateDrawSteps(mc, gridRenderer, waypointRenderer, radarRenderer, miniMapProperties, dv.drawScale);
                //if(unicodeForced) DrawUtil.stopUnicode(mc.fontRenderer);
                state.updateLastRefresh();
            }

            // Update display vars if needed
            updateDisplayVars(false);

            // Update labels if needed
            if (System.currentTimeMillis() - lastLabelRefresh > labelRefreshRate)
            {
                updateLabels();
            }

            // Separate waypoint drawsteps
            ArrayList<DrawStep> allDrawSteps = new ArrayList<DrawStep>(state.getDrawSteps());
            ArrayList<DrawStep> offscreenWpDrawSteps = new ArrayList<DrawStep>();

            if(miniMapProperties.showWaypoints.get())
            {
                int dimension = mc.thePlayer.dimension;
                int maxDistance = waypointProperties.maxDistance.get();
                boolean checkDist = maxDistance>0;

                Vec3 playerVec = checkDist ? mc.thePlayer.getPosition(1) : null;
                Vec3 waypointVec;
                DrawWayPointStep drawWayPointStep;
                Waypoint waypoint;

                for (DrawStep drawStep : state.getDrawSteps())
                {
                    if (drawStep instanceof DrawWayPointStep)
                    {
                        drawWayPointStep = (DrawWayPointStep) drawStep;

                        if(checkDist)
                        {
                            // Get waypoint coords for dimension
                            waypoint = drawWayPointStep.waypoint;
                            waypointVec = mc.theWorld.getWorldVec3Pool().getVecFromPool(waypoint.getX(dimension), waypoint.getY(dimension), waypoint.getZ(dimension));

                            // Get view distance from waypoint
                            final double actualDistance = playerVec.distanceTo(waypointVec);
                            if(actualDistance>maxDistance)
                            {
                                allDrawSteps.remove(drawStep);
                                continue;
                            }
                        }

                        if (!drawWayPointStep.isOnScreen(0, 0, gridRenderer))
                        {
                            offscreenWpDrawSteps.add(drawStep);
                            allDrawSteps.remove(drawStep);
                        }
                    }
                }
            }

            // Use 1:1 resolution for minimap regardless of how Minecraft UI is scaled
            JmUI.sizeDisplay(mc.displayWidth, mc.displayHeight);

            // Experimental fix for overly-dark screens with some graphics cards
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightmapS, lightmapT);

            // Ensure colors and alpha reset
            GL11.glColor4f(1, 1, 1, 1);

            // Push matrix for translation to corner
            GL11.glPushMatrix();

            // Draw mask (if present) using stencil buffer
            if (dv.maskTexture != null)
            {
                try
                {
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
                }
                catch (Throwable t)
                {
                    logger.severe("Stencil buffer failing with circle mask:" + LogFormatter.toString(t));
                    return;
                }
            }

            // Move center to corner
            GL11.glTranslated(dv.translateX, dv.translateY, 0);

            // Scissor area that shouldn't be drawn
            GL11.glScissor((int) dv.scissorX + 1, (int) dv.scissorY + 1, (int) dv.minimapSize - 2, (int) dv.minimapSize - 2);
            GL11.glEnable(GL11.GL_SCISSOR_TEST);

            // Draw grid
            gridRenderer.draw(1f, 0, 0);

            // Draw entities, etc
            boolean unicodeForced = DrawUtil.startUnicode(mc.fontRenderer, miniMapProperties.forceUnicode.get());
            gridRenderer.draw(allDrawSteps, 0, 0, dv.drawScale, getMapFontScale());
            if (unicodeForced)
            {
                DrawUtil.stopUnicode(mc.fontRenderer);
            }

            // Draw player
            if (miniMapProperties.showSelf.get())
            {
                Point2D playerPixel = gridRenderer.getPixel(mc.thePlayer.posX, mc.thePlayer.posZ);
                if (playerPixel != null)
                {
                    DrawUtil.drawEntity(playerPixel.getX(), playerPixel.getY(), EntityHelper.getHeading(mc.thePlayer), false, playerLocatorTex, 8, dv.drawScale);
                }
            }

            // Return center to mid-screen
            GL11.glTranslated(-dv.translateX, -dv.translateY, 0);

            // Draw labels if scissored
            if (dv.labelFps.scissor && dv.showFps)
            {
                dv.labelFps.draw(fpsLabelText, playerInfoBgColor, 200, playerInfoFgColor, 255);
            }

            if (dv.labelLocation.scissor)
            {
                dv.labelLocation.draw(locationLabelText, playerInfoBgColor, 200, playerInfoFgColor, 255);
            }

            if (dv.labelBiome.scissor)
            {
                dv.labelBiome.draw(biomeLabelText, playerInfoBgColor, 200, playerInfoFgColor, 255);
            }

            // If using a mask, turn off the stencil test
            if (dv.maskTexture != null)
            {
                glDisable(GL_STENCIL_TEST);
            }

            // Finish Scissor
            GL11.glDisable(GL11.GL_SCISSOR_TEST);

            // Draw labels if not scissored
            if (!dv.labelFps.scissor && dv.showFps)
            {
                dv.labelFps.draw(fpsLabelText, playerInfoBgColor, 200, playerInfoFgColor, 255);
            }

            if (!dv.labelLocation.scissor)
            {
                dv.labelLocation.draw(locationLabelText, playerInfoBgColor, 200, playerInfoFgColor, 255);
            }

            if (!dv.labelBiome.scissor)
            {
                dv.labelBiome.draw(biomeLabelText, playerInfoBgColor, 200, playerInfoFgColor, 255);
            }

            // Restore GL attrs assumed by Minecraft to be managerEnabled
            GL11.glEnable(GL11.GL_DEPTH_TEST);

            // Draw border texture
            DrawUtil.drawImage(dv.borderTexture, dv.textureX, dv.textureY, false, 1f);

            // Draw off-screen waypoints on top of border texture
            if(!offscreenWpDrawSteps.isEmpty())
            {
                // Move center back to corner
                GL11.glTranslated(dv.translateX, dv.translateY, 0);
                gridRenderer.draw(offscreenWpDrawSteps, 0, 0, dv.drawScale, getMapFontScale());
            }
            // Pop matrix changes
            GL11.glPopMatrix();

            // Return resolution to how it is normally scaled
            JmUI.sizeDisplay(dv.scaledResolution.getScaledWidth_double(), dv.scaledResolution.getScaledHeight_double());

        }
        catch (Throwable t)
        {
            logger.severe("Minimap error:" + LogFormatter.toString(t));
        }
        finally
        {
            drawTimer.stop();
        }

    }

    public void reset()
    {
        state.requireRefresh();
        gridRenderer.clear();
    }

    public double getMapFontScale()
    {
        return (miniMapProperties.fontSmall.get() ? 1 : 2) * (miniMapProperties.forceUnicode.get() ? 2 : 1);
    }

    public void nextPosition()
    {
        int nextIndex = dv.position.ordinal() + 1;
        if (nextIndex == DisplayVars.Position.values().length)
        {
            nextIndex = 0;
        }
        setPosition(DisplayVars.Position.values()[nextIndex]);
    }

    public DisplayVars.Position getPosition()
    {
        return dv.position;
    }

    public void setPosition(DisplayVars.Position position)
    {
        miniMapProperties.position.set(position);
        miniMapProperties.save();
        if (dv != null)
        {
            updateDisplayVars(dv.shape, position, false);
        }
    }

    public DisplayVars.Shape getShape()
    {
        return dv.shape;
    }

    public void setShape(DisplayVars.Shape shape)
    {
        if (dv != null)
        {
            updateDisplayVars(shape, dv.position, false);
        }
    }

    public void updateDisplayVars(boolean force)
    {
        if (dv != null)
        {
            updateDisplayVars(dv.shape, dv.position, force);
        }
    }

    public void updateDisplayVars(DisplayVars.Shape shape, DisplayVars.Position position, boolean force)
    {
        if (dv != null
                && !force
                && mc.displayHeight == dv.displayHeight
                && mc.displayWidth == dv.displayWidth
                && this.dv.shape == shape
                && this.dv.position == position
                && this.dv.forceUnicode == miniMapProperties.forceUnicode.get()
                && this.dv.fontScale == (miniMapProperties.fontSmall.get() ? 1 : 2))
        {
            return;
        }

        miniMapProperties.shape.set(shape);
        miniMapProperties.position.set(position);
        miniMapProperties.save();

        DisplayVars oldDv = this.dv;
        this.dv = new DisplayVars(mc, shape, position, miniMapProperties.fontSmall.get() ? 1 : 2);

        if (oldDv == null || oldDv.shape != this.dv.shape)
        {
            this.drawTimer = StatTimer.get("MiniMap.drawMap." + shape.name(), 200);
        }

        // Update labels
        updateLabels();

        // Set viewport
        double xpad = this.dv.viewPortPadX;
        double ypad = this.dv.viewPortPadY;
        Rectangle2D.Double viewPort = new Rectangle2D.Double(this.dv.textureX + xpad, this.dv.textureY + ypad, this.dv.minimapSize - (2 * xpad), this.dv.minimapSize - (2 * ypad));
        gridRenderer.setViewPort(viewPort);
    }

    private void updateLabels()
    {
        // FPS label
        if (dv.showFps)
        {
            String fps = mc.debug;
            final int idx = fps != null ? fps.indexOf(',') : -1;
            if (idx > 0)
            {
                fpsLabelText = fps.substring(0, idx);
            }
            else
            {
                fpsLabelText = "";
            }
        }

        // Location label
        String playerInfo = "";
        final int playerX = (int) player.posX;
        final int playerZ = (int) player.posZ;
        final int playerY = (int) player.posY;

        for (String format : locationFormats)
        {
            playerInfo = Constants.getString(format, playerX, playerZ, playerY, mc.thePlayer.chunkCoordY);
            double infoWidth = mc.fontRenderer.getStringWidth(playerInfo) * dv.fontScale;
            if (infoWidth <= dv.minimapSize - (dv.viewPortPadX * 2))
            {
                break;
            }
        }

        locationLabelText = playerInfo;

        // Biome label
        biomeLabelText = state.getPlayerBiome();

        // Update timestamp
        lastLabelRefresh = System.currentTimeMillis();
    }

}

