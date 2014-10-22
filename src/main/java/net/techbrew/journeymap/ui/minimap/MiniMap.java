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
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.feature.Feature;
import net.techbrew.journeymap.feature.FeatureManager;
import net.techbrew.journeymap.forgehandler.MiniMapOverlayHandler;
import net.techbrew.journeymap.log.JMLogger;
import net.techbrew.journeymap.log.StatTimer;
import net.techbrew.journeymap.model.MapState;
import net.techbrew.journeymap.properties.MiniMapProperties;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.render.draw.DrawWayPointStep;
import net.techbrew.journeymap.render.draw.RadarDrawStepFactory;
import net.techbrew.journeymap.render.draw.WaypointDrawStepFactory;
import net.techbrew.journeymap.render.map.GridRenderer;
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.render.texture.TextureImpl;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Displays the map as a minimap overlay in-game.
 *
 * @author mwoodman
 */
public class MiniMap
{
    private static final MapState state = new MapState();
    private static final float lightmapS = (float) (15728880 % 65536) / 1f;
    private static final float lightmapT = (float) (15728880 / 65536) / 1f;
    private static final long labelRefreshRate = 400;
    private final static GridRenderer gridRenderer = new GridRenderer(3);
    private final Logger logger = JourneyMap.getLogger();
    private final Minecraft mc = FMLClientHandler.instance().getClient();
    private final WaypointDrawStepFactory waypointRenderer = new WaypointDrawStepFactory();
    private final RadarDrawStepFactory radarRenderer = new RadarDrawStepFactory();
    private final TextureImpl playerLocatorTex;
    private MiniMapProperties miniMapProperties;
    private EntityClientPlayerMP player;
    private StatTimer drawTimer;
    private StatTimer refreshStateTimer;
    private DisplayVars dv;
    private long lastLabelRefresh = 0;
    private String fpsLabelText;
    private String locationLabelText;
    private String biomeLabelText;

    private Point2D.Double centerPoint;
    private Rectangle2D.Double centerRect;

    private long initTime;

    /**
     * Default constructor
     */
    public MiniMap(MiniMapProperties miniMapProperties)
    {
        initTime = System.currentTimeMillis();
        player = mc.thePlayer;
        playerLocatorTex = TextureCache.instance().getPlayerLocatorSmall();
        setMiniMapProperties(miniMapProperties);
    }

    public static synchronized MapState state()
    {
        return state;
    }

    private void initGridRenderer()
    {
        state.requireRefresh();
        if (player == null || player.isDead)
        {
            return;
        }

        state.refresh(mc, player, miniMapProperties);
        int gridSize = miniMapProperties.getSize() <= 768 ? 3 : 5;
        gridRenderer.setGridSize(gridSize);
        gridRenderer.setContext(state.getWorldDir(), state.getDimension());
        gridRenderer.center(mc.thePlayer.posX, mc.thePlayer.posZ, miniMapProperties.zoomLevel.get());
        boolean showCaves = FeatureManager.isAllowed(Feature.MapCaves) && (player.worldObj.provider.hasNoSky || miniMapProperties.showCaves.get());
        gridRenderer.updateTextures(state.getMapType(showCaves), state.getVSlice(), mc.displayWidth, mc.displayHeight, true, 0, 0);

    }

    public void resetInitTime()
    {
        initTime = System.currentTimeMillis();
    }

    public void setMiniMapProperties(MiniMapProperties miniMapProperties)
    {
        this.miniMapProperties = miniMapProperties;
        reset();
    }

    public MiniMapProperties getCurrentMinimapProperties()
    {
        return miniMapProperties;
    }

    /**
     * Called in the render loop.
     */
    public void drawMap()
    {
        drawMap(false);
    }

    /**
     * Called in the render loop.
     */
    public void drawMap(boolean preview)
    {
        StatTimer timer = drawTimer;

        boolean unicodeForced = false;

        try
        {
            // Check player status
            player = mc.thePlayer;
            if (player == null || player.isDead)
            {
                return;
            }

            // Check state
            final boolean doStateRefresh = gridRenderer.hasUnloadedTile() || state.shouldRefresh(mc, miniMapProperties);

            // Update the state first
            if (doStateRefresh)
            {
                timer = refreshStateTimer.start();
                gridRenderer.setContext(state.getWorldDir(), state.getDimension());
                if (!preview)
                {
                    state.refresh(mc, player, miniMapProperties);
                }
            }
            else
            {
                timer.start();
            }

            // Update the grid
            boolean moved = gridRenderer.center(mc.thePlayer.posX, mc.thePlayer.posZ, miniMapProperties.zoomLevel.get());
            if (moved || doStateRefresh)
            {
                boolean showCaves = FeatureManager.isAllowed(Feature.MapCaves) && (player.worldObj.provider.hasNoSky || miniMapProperties.showCaves.get());
                gridRenderer.updateTextures(state.getMapType(showCaves), state.getVSlice(), mc.displayWidth, mc.displayHeight, doStateRefresh || preview, 0, 0);
            }

            if (doStateRefresh)
            {
                boolean checkWaypointDistance = JourneyMap.getWaypointProperties().maxDistance.get() > 0;
                state.generateDrawSteps(mc, gridRenderer, waypointRenderer, radarRenderer, miniMapProperties, dv.drawScale, checkWaypointDistance);
                state.updateLastRefresh();
            }

            // Update display vars if needed
            updateDisplayVars(false);

            // Update labels if needed
            long now = System.currentTimeMillis();
            if (now - lastLabelRefresh > labelRefreshRate)
            {
                updateLabels();
            }

            // Use 1:1 resolution for minimap regardless of how Minecraft UI is scaled
            DrawUtil.sizeDisplay(mc.displayWidth, mc.displayHeight);

            // Experimental fix for overly-dark screens with some graphics cards
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightmapS, lightmapT);

            // Ensure colors and alpha reset
            GL11.glColor4f(1, 1, 1, 1);

            // Mask the stencil
            beginStencil();

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
                    rotation = 90;
                    break;
                }
                case PlayerHeading:
                {
                    if (dv.shape == Shape.Circle)
                    {
                        rotation = (180 - mc.thePlayer.rotationYawHead);
                    }
                    break;
                }
            }

            unicodeForced = DrawUtil.startUnicode(mc.fontRenderer, dv.forceUnicode);

            /***** BEGIN MATRIX: ROTATION *****/
            startMapRotation(rotation);

            try
            {
                // Move origin to top-left corner
                GL11.glTranslated(dv.translateX, dv.translateY, 0);

                // Draw grid
                gridRenderer.draw(dv.terrainAlpha, 0, 0);

                // Draw entities, etc

                gridRenderer.draw(state.getDrawSteps(), 0, 0, dv.drawScale, dv.fontScale, rotation);

                // Get center of minimap and rect of minimap
                centerPoint = gridRenderer.getPixel(mc.thePlayer.posX, mc.thePlayer.posZ);
                centerRect = new Rectangle2D.Double(centerPoint.x - dv.minimapWidth / 2, centerPoint.y - dv.minimapHeight / 2, dv.minimapWidth, dv.minimapHeight);

                // Draw waypoints
                drawOnMapWaypoints(rotation);

                // Draw Minimap Preset Id
                if (now - initTime <= 1000)
                {
                    if (!unicodeForced)
                    {
                        DrawUtil.startUnicode(mc.fontRenderer, true);
                    }
                    int alpha = (int) Math.min(255, Math.max(0, 1100 - (now - initTime)));
                    DrawUtil.drawLabel(Integer.toString(miniMapProperties.getId()), centerPoint.getX(), centerPoint.getY(), DrawUtil.HAlign.Center, DrawUtil.VAlign.Middle, Color.black, Math.max(0, alpha - 100), Color.white, alpha, 8, false, rotation);
                    if (!unicodeForced)
                    {
                        DrawUtil.stopUnicode(mc.fontRenderer);
                    }
                }

                // Draw player
                if (miniMapProperties.showSelf.get())
                {
                    if (centerPoint != null)
                    {
                        DrawUtil.drawEntity(centerPoint.getX(), centerPoint.getY(), mc.thePlayer.rotationYawHead, false, playerLocatorTex, dv.drawScale, rotation);
                    }
                }

                // Return centerPoint to mid-screen
                GL11.glTranslated(-dv.translateX, -dv.translateY, 0);

                // Draw Reticle
                ReticleOrientation reticleOrientation = null;
                if (dv.showReticle)
                {
                    reticleOrientation = dv.minimapFrame.getReticleOrientation();
                    if (reticleOrientation == ReticleOrientation.Compass)
                    {
                        dv.minimapFrame.drawReticle();
                    }
                    else
                    {
                        /***** BEGIN MATRIX: ROTATION *****/
                        startMapRotation(player.rotationYawHead);
                        dv.minimapFrame.drawReticle();
                        /***** END MATRIX: ROTATION *****/
                        stopMapRotation(player.rotationYawHead);
                    }
                }

                // Finish stencil
                endStencil();

                // Draw Frame
                if (dv.shape == Shape.Circle || rotation == 0)
                {
                    dv.minimapFrame.drawFrame();
                }
                else
                {
                    /***** END MATRIX: ROTATION *****/
                    stopMapRotation(rotation);
                    try
                    {
                        // Draw Minimap Frame
                        dv.minimapFrame.drawFrame();
                    }
                    finally
                    {
                        /***** BEGIN MATRIX: ROTATION *****/
                        startMapRotation(rotation);
                    }
                }

                // Draw cardinal compass points
                if (dv.showCompass)
                {
                    dv.minimapCompassPoints.drawPoints(rotation);
                }

                // Move origin to top-left corner
                GL11.glTranslated(dv.translateX, dv.translateY, 0);

                // Draw off-screen waypoints on top of frame
                drawOffMapWaypoints(rotation);

                // Draw cardinal compass point labels
                if (dv.showCompass)
                {
                    // Return centerPoint to mid-screen
                    GL11.glTranslated(-dv.translateX, -dv.translateY, 0);
                    dv.minimapCompassPoints.drawLabels(rotation);
                }

            }
            finally
            {
                /***** END MATRIX: ROTATION *****/
                GL11.glPopMatrix();
            }

            // Draw minimap labels
            if (dv.showFps)
            {
                dv.labelFps.draw(fpsLabelText);
            }
            if (dv.showLocation)
            {
                dv.labelLocation.draw(locationLabelText);
            }
            if (dv.showBiome)
            {
                dv.labelBiome.draw(biomeLabelText);
            }

            // Return resolution to how it is normally scaled
            DrawUtil.sizeDisplay(dv.scaledResolution.getScaledWidth_double(), dv.scaledResolution.getScaledHeight_double());
        }
        catch (Throwable t)
        {
            JMLogger.logOnce("Error during MiniMap1.drawMap(): " + t.getMessage(), t);
        }
        finally
        {
            if (unicodeForced)
            {
                DrawUtil.stopUnicode(mc.fontRenderer);
            }
            cleanup();
            timer.stop();
        }
    }

    private void drawOnMapWaypoints(double rotation)
    {
        boolean showLabel = miniMapProperties.showWaypointLabels.get();
        for (DrawWayPointStep drawWayPointStep : state.getDrawWaypointSteps())
        {
            Point2D.Double waypointPos = drawWayPointStep.getPosition(0, 0, gridRenderer, true);
            boolean onScreen = isOnScreen(waypointPos, centerPoint, centerRect);
            drawWayPointStep.setOnScreen(onScreen);
            if (onScreen)
            {
                drawWayPointStep.setShowLabel(showLabel);
                drawWayPointStep.draw(0, 0, gridRenderer, dv.drawScale, dv.fontScale, rotation);
            }
        }
    }

    private void drawOffMapWaypoints(double rotation)
    {
        for (DrawWayPointStep drawWayPointStep : state.getDrawWaypointSteps())
        {
            if (!drawWayPointStep.isOnScreen())
            {
                Point2D.Double point = getPointOnFrame(
                        drawWayPointStep.getPosition(0, 0, gridRenderer, false),
                        centerPoint,
                        dv.minimapSpec.waypointOffset);

                //point = drawWayPointStep.getPosition(0, 0, gridRenderer, false);
                drawWayPointStep.drawOffscreen(point, rotation);
            }
        }
    }

    private void startMapRotation(double rotation)
    {
        GL11.glPushMatrix();
        if (rotation % 360 != 0)
        {
            double width = dv.displayWidth / 2 + (dv.translateX);
            double height = dv.displayHeight / 2 + (dv.translateY);

            GL11.glTranslated(width, height, 0);

            GL11.glRotated(rotation, 0, 0, 1.0f);
            GL11.glTranslated(-width, -height, 0);
        }

        gridRenderer.updateGL(rotation);
    }

    private void stopMapRotation(double rotation)
    {
        GL11.glPopMatrix();
        gridRenderer.updateGL(rotation);
    }

    private boolean isOnScreen(Point2D.Double objectPixel, Point2D centerPixel, Rectangle2D.Double centerRect)
    {
        if (dv.shape == Shape.Circle)
        {
            return centerPixel.distance(objectPixel) < dv.minimapWidth / 2;
        }
        else
        {
            return centerRect.contains(gridRenderer.getWindowPosition(objectPixel));
        }
    }

    private Point2D.Double getPointOnFrame(Point2D.Double objectPixel, Point2D centerPixel, double offset)
    {
        if (dv.shape == Shape.Circle)
        {

            // Get the bearing from center to object
            double bearing = Math.atan2(
                    objectPixel.getY() - centerPixel.getY(),
                    objectPixel.getX() - centerPixel.getX()
            );


            Point2D.Double framePos = new Point2D.Double(
                    (dv.minimapWidth / 2 * Math.cos(bearing)) + centerPixel.getX(),
                    (dv.minimapHeight / 2 * Math.sin(bearing)) + centerPixel.getY()
            );

            return framePos;
        }
        else
        {
            Rectangle2D.Double rect = new Rectangle2D.Double(dv.textureX - dv.translateX, dv.textureY - dv.translateY, dv.minimapWidth, dv.minimapHeight);

            if (objectPixel.x > rect.getMaxX())
            {
                objectPixel.x = rect.getMaxX();
            }
            else if (objectPixel.x < rect.getMinX())
            {
                objectPixel.x = rect.getMinX();
            }

            if (objectPixel.y > rect.getMaxY())
            {
                objectPixel.y = rect.getMaxY();
            }
            else if (objectPixel.y < rect.getMinY())
            {
                objectPixel.y = rect.getMinY();
            }
            return objectPixel;
        }
    }

    private void beginStencil()
    {
        try
        {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glColorMask(false, false, false, false);
            GL11.glDepthMask(true);
            GL11.glDepthFunc(GL11.GL_ALWAYS);
            GL11.glColor4f(1, 1, 1, 1);
            DrawUtil.zLevel = 1000;
            dv.minimapFrame.drawMask();
            DrawUtil.zLevel = 0;
            GL11.glColorMask(true, true, true, true);
            GL11.glDepthMask(false);
            GL11.glDepthFunc(GL11.GL_GREATER);
        }
        catch (Throwable t)
        {
            JMLogger.logOnce("Error during MiniMap1.beginStencil()", t);
        }

    }

    private void endStencil()
    {
        try
        {
            GL11.glDepthMask(false);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDepthFunc(GL11.GL_LEQUAL);
            GL11.glColor4f(1, 1, 1, 1);
        }
        catch (Throwable t)
        {
            JMLogger.logOnce("Error during MiniMap1.endStencil()", t);
        }
    }

    private void cleanup()
    {
        try
        {
            DrawUtil.zLevel = 0;
            GL11.glDepthFunc(GL11.GL_LEQUAL);
            GL11.glDepthMask(true);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }
        catch (Throwable t)
        {
            JMLogger.logOnce("Error during MiniMap1.cleanup()", t);
        }
    }

    public void reset()
    {
        initTime = System.currentTimeMillis();
        initGridRenderer();
        updateDisplayVars(miniMapProperties.shape.get(), miniMapProperties.position.get(), true);
        MiniMapOverlayHandler.checkEventConfig();
    }


    public void updateDisplayVars(boolean force)
    {
        if (dv != null)
        {
            updateDisplayVars(dv.shape, dv.position, force);
        }
    }

    public void updateDisplayVars(Shape shape, Position position, boolean force)
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

        initGridRenderer();

        if (force)
        {
            shape = miniMapProperties.shape.get();
            position = miniMapProperties.position.get();
        }

        miniMapProperties.shape.set(shape);
        miniMapProperties.position.set(position);
        miniMapProperties.save();

        DisplayVars oldDv = this.dv;
        this.dv = new DisplayVars(mc, shape, position, miniMapProperties.fontSmall.get() ? 1 : 2, miniMapProperties);

        if (oldDv == null || oldDv.shape != this.dv.shape)
        {
            this.drawTimer = StatTimer.get("MiniMap1." + shape.name(), 500);
            this.drawTimer.reset();
            this.refreshStateTimer = StatTimer.get("MiniMap1." + shape.name() + "+refreshState", 5);
            this.refreshStateTimer.reset();
        }

        // Update labels
        updateLabels();

        // Set viewport
        double xpad = 0;
        double ypad = 0;
        Rectangle2D.Double viewPort = new Rectangle2D.Double(this.dv.textureX + xpad, this.dv.textureY + ypad, this.dv.minimapWidth - (2 * xpad), this.dv.minimapHeight - (2 * ypad));
        gridRenderer.setViewPort(viewPort);
    }

    private void updateLabels()
    {
        // FPS key
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

        // Location key
        final int playerX = MathHelper.floor_double(player.posX);
        final int playerZ = MathHelper.floor_double(player.posZ);
        final int playerY = MathHelper.floor_double(player.boundingBox.minY);

        locationLabelText = dv.locationFormatKeys.format(dv.locationFormatVerbose, playerX, playerZ, playerY, mc.thePlayer.chunkCoordY);

        // Biome key
        biomeLabelText = state.getPlayerBiome();

        // Update timestamp
        lastLabelRefresh = System.currentTimeMillis();
    }
}

