/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.ui.minimap;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.MathHelper;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.log.StatTimer;
import net.techbrew.journeymap.model.MapOverlayState;
import net.techbrew.journeymap.properties.FullMapProperties;
import net.techbrew.journeymap.properties.MiniMapProperties;
import net.techbrew.journeymap.properties.WaypointProperties;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.render.draw.DrawWayPointStep;
import net.techbrew.journeymap.render.overlay.GridRenderer;
import net.techbrew.journeymap.render.overlay.OverlayRadarRenderer;
import net.techbrew.journeymap.render.overlay.OverlayWaypointRenderer;
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.render.texture.TextureImpl;
import net.techbrew.journeymap.ui.JmUI;
import net.techbrew.journeymap.ui.map.MapOverlay;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

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
    private static final long labelRefreshRate = 400;

    private final Logger logger = JourneyMap.getLogger();
    private final Minecraft mc = FMLClientHandler.instance().getClient();

    private final Color playerInfoFgColor = Color.LIGHT_GRAY;
    private final Color playerInfoBgColor = new Color(0x22, 0x22, 0x22);
    private final String[] locationFormats = {"jm.common.location_xzye", "jm.common.location_xzy", "jm.common.location_xz"};
    private final GridRenderer gridRenderer = new GridRenderer(3, JourneyMap.getMiniMapProperties());
    private final MapOverlayState state = MapOverlay.state();
    private final OverlayWaypointRenderer waypointRenderer = new OverlayWaypointRenderer();
    private final OverlayRadarRenderer radarRenderer = new OverlayRadarRenderer();
    private final TextureImpl playerLocatorTex;
    private MiniMapProperties miniMapProperties = JourneyMap.getMiniMapProperties();
    private FullMapProperties fullMapProperties = JourneyMap.getFullMapProperties();
    private WaypointProperties waypointProperties = JourneyMap.getWaypointProperties();
    private EntityClientPlayerMP player;
    private StatTimer drawTimer;
    private StatTimer refreshStateTimer;
    private DisplayVars dv;

    private long lastLabelRefresh = 0;
    private String fpsLabelText;
    private String locationLabelText;
    private String biomeLabelText;

    // Separate waypoint drawsteps
    private ArrayList<DrawWayPointStep> allWaypointSteps = new ArrayList<DrawWayPointStep>();
    private ArrayList<DrawWayPointStep> offscreenWpDrawSteps = new ArrayList<DrawWayPointStep>();

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
        StatTimer timer = drawTimer;

        try
        {
            // Check player status
            player = mc.thePlayer;
            if (player == null)
            {
                return;
            }

            // Check state
            final boolean doStateRefresh = state.shouldRefresh(mc, miniMapProperties) || gridRenderer.hasUnloadedTile();

            // Update the state first
            if (doStateRefresh)
            {
                timer = refreshStateTimer.start();
                miniMapProperties = JourneyMap.getMiniMapProperties();
                gridRenderer.setMapProperties(JourneyMap.getMiniMapProperties());
                fullMapProperties = JourneyMap.getFullMapProperties();
                waypointProperties = JourneyMap.getWaypointProperties();
                state.refresh(mc, player, miniMapProperties);
            }
            else
            {
                timer.start();
            }

            // Update the grid
            DrawUtil.Default_glTexParameteri = GL11.GL_LINEAR;
            gridRenderer.setContext(state.getWorldDir(), state.getDimension());
            boolean moved = gridRenderer.center(mc.thePlayer.posX, mc.thePlayer.posZ, miniMapProperties.zoomLevel.get());
            if (moved || doStateRefresh)
            {
                boolean showCaves = player.worldObj.provider.hasNoSky || fullMapProperties.showCaves.get();
                gridRenderer.updateTextures(state.getMapType(showCaves), state.getVSlice(), mc.displayWidth, mc.displayHeight, doStateRefresh, 0, 0);
            }

            if (doStateRefresh)
            {
                boolean checkWaypointDistance = waypointProperties.maxDistance.get() > 0;
                state.generateDrawSteps(mc, gridRenderer, waypointRenderer, radarRenderer, miniMapProperties, dv.drawScale, checkWaypointDistance);
                state.updateLastRefresh();

                allWaypointSteps.clear();
                allWaypointSteps.addAll(state.getDrawWaypointSteps());

//                offscreenWpDrawSteps.clear();
//                if(miniMapProperties.showWaypoints.get())
//                {
//                    for (DrawWayPointStep drawWayPointStep : allWaypointSteps)
//                    {
//                        if (!drawWayPointStep.isOnScreen(0, 0, gridRenderer))
//                        {
//                            offscreenWpDrawSteps.add(drawWayPointStep);
//                            allWaypointSteps.remove(drawWayPointStep);
//                        }
//                        else
//                        {
//                            offscreenWpDrawSteps.remove(drawWayPointStep);
//                        }
//                    }
//                    allWaypointSteps.removeAll(offscreenWpDrawSteps);
//                }
            }

            // Update display vars if needed
            updateDisplayVars(false);

            // Update labels if needed
            if (System.currentTimeMillis() - lastLabelRefresh > labelRefreshRate)
            {
                updateLabels();
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
                    DrawUtil.drawQuad(dv.maskTexture, dv.textureX, dv.textureY, dv.maskTexture.width, dv.maskTexture.height, 9, null, 1f, false, true, GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                    glColorMask(true, true, true, true);
                    glDepthMask(true);
                    glStencilMask(0x00);
                    //glStencilFunc(GL_EQUAL, 0, 0xFF);
                    glStencilFunc(GL_EQUAL, 1, 0xFF);
                }
                catch (Throwable t)
                {
                    logger.error("Stencil buffer failing with circle mask:" + LogFormatter.toString(t));
                    //return;
                }
            }

            // Rotatate around player heading
            double rotation = 0;
            switch (dv.orientation)
            {
                case North:
                {
                    rotation = 0;
                    break;
                }
                case OldNorth:
                {
                    rotation = -90;
                    break;
                }
                case PlayerHeading:
                {
                    rotation = (180 - mc.thePlayer.rotationYawHead);
                    break;
                }
            }

            if (rotation != 0)
            {
                double width = dv.displayWidth / 2 + (dv.translateX);
                double height = dv.displayHeight / 2 + (dv.translateY);
                GL11.glPushMatrix();
                GL11.glTranslated(width, height, 0);
                GL11.glRotated(rotation, 0, 0, 1.0f);
                GL11.glTranslated(-width, -height, 0);
                //rotation+=180;
            }

            // Move center to corner
            GL11.glTranslated(dv.translateX, dv.translateY, 0);

            // Scissor area that shouldn't be drawn
            GL11.glScissor((int) dv.scissorX + 1, (int) dv.scissorY + 1, (int) dv.minimapSize - 2, (int) dv.minimapSize - 2);
            //GL11.glEnable(GL11.GL_SCISSOR_TEST);

            // Draw grid
            gridRenderer.draw(1f, 0, 0);

            // Draw entities, etc
            final double fontScale = getMapFontScale();
            boolean unicodeForced = DrawUtil.startUnicode(mc.fontRenderer, miniMapProperties.forceUnicode.get());
            gridRenderer.draw(state.getDrawSteps(), 0, 0, dv.drawScale, fontScale, rotation);
            if (!allWaypointSteps.isEmpty())
            {
                gridRenderer.draw(allWaypointSteps, 0, 0, dv.drawScale, fontScale, rotation);
            }
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
                    DrawUtil.drawEntity(playerPixel.getX(), playerPixel.getY(), mc.thePlayer.rotationYawHead, false, playerLocatorTex, dv.drawScale, rotation);
                }
            }

            // Return center to mid-screen
            GL11.glTranslated(-dv.translateX, -dv.translateY, 0);

            // If using a mask, turn off the stencil test
            if (dv.maskTexture != null)
            {
                glDisable(GL_STENCIL_TEST);
            }

            // Finish Scissor
            GL11.glDisable(GL11.GL_SCISSOR_TEST);

            // Restore GL attrs assumed by Minecraft to be managerEnabled
            GL11.glEnable(GL11.GL_DEPTH_TEST);

            // TODO: These need to be checked for straying outside the scissor
            // Draw off-screen waypoints on top of border texture
            if (!allWaypointSteps.isEmpty())
            {
                // Move center back to corner
                GL11.glTranslated(dv.translateX, dv.translateY, 0);
                for (DrawWayPointStep drawWayPointStep : allWaypointSteps)
                {
                    if (!drawWayPointStep.isOnScreen(0, 0, gridRenderer))
                    {
                        drawWayPointStep.draw(0, 0, gridRenderer, dv.drawScale, fontScale, rotation);
                    }
                }
            }

            if (rotation != 0)
            {
                GL11.glPopMatrix();
            }

            // Draw border texture
            if (dv.borderTexture != null)
            {
                GL11.glPushMatrix();
                //GL11.glTranslated(512-dv.minimapSize, 0, 0);
                // TODO: Draw Frame scaled
                DrawUtil.drawImage(dv.borderTexture, dv.textureX, dv.textureY, false, (float) (dv.minimapSize / 512.0), 0);
                GL11.glPopMatrix();
            }

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

            // Pop matrix changes
            GL11.glPopMatrix();

            // Return resolution to how it is normally scaled
            JmUI.sizeDisplay(dv.scaledResolution.getScaledWidth_double(), dv.scaledResolution.getScaledHeight_double());

        }
        catch (Throwable t)
        {
            logger.error("Minimap error:" + LogFormatter.toString(t));
        }
        finally
        {
            timer.stop();
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
            this.drawTimer = StatTimer.get("MiniMap." + shape.name(), 500);
            this.drawTimer.reset();
            this.refreshStateTimer = StatTimer.get("MiniMap." + shape.name() + "+refreshState", 5);
            this.refreshStateTimer.reset();
        }

        // Update labels
        updateLabels();

        // Set viewport
        double xpad = this.dv.viewPortPadX;
        double ypad = this.dv.viewPortPadY;
        Rectangle2D.Double viewPort = new Rectangle2D.Double(this.dv.textureX + xpad, this.dv.textureY + ypad, this.dv.minimapSize - (2 * xpad), this.dv.minimapSize - (2 * ypad));
        gridRenderer.setViewPort(viewPort);
    }

    public void forceRefreshState()
    {
        state.requireRefresh();
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
        final int playerX = MathHelper.floor_double(player.posX);
        final int playerY = MathHelper.floor_double(player.boundingBox.minY);
        final int playerZ = MathHelper.floor_double(player.posZ);

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

