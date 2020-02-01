/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.minimap;

import journeymap.client.api.display.Context;
import journeymap.client.api.impl.ClientAPI;
import journeymap.client.api.util.UIState;
import journeymap.client.data.DataCache;
import journeymap.client.feature.Feature;
import journeymap.client.feature.FeatureManager;
import journeymap.client.forge.event.MiniMapOverlayHandler;
import journeymap.client.log.JMLogger;
import journeymap.client.log.StatTimer;
import journeymap.client.model.EntityDTO;
import journeymap.client.model.MapState;
import journeymap.client.model.MapType;
import journeymap.client.properties.CoreProperties;
import journeymap.client.properties.MiniMapProperties;
import journeymap.client.render.draw.DrawStep;
import journeymap.client.render.draw.DrawUtil;
import journeymap.client.render.draw.DrawWayPointStep;
import journeymap.client.render.draw.RadarDrawStepFactory;
import journeymap.client.render.draw.WaypointDrawStepFactory;
import journeymap.client.render.map.GridRenderer;
import journeymap.client.render.texture.TextureCache;
import journeymap.client.render.texture.TextureImpl;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.lwjgl.opengl.GL11;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Displays the map as a minimap overlay in-game.
 *
 * @author techbrew
 */
public class MiniMap
{
    private static final MapState state = new MapState();
    private static final float lightmapS = (float) (15728880 % 65536) / 1f;
    private static final float lightmapT = (float) (15728880 / 65536) / 1f;
    private final static GridRenderer gridRenderer = new GridRenderer(Context.UI.Minimap, 3);
    private final Minecraft mc = FMLClientHandler.instance().getClient();
    private final WaypointDrawStepFactory waypointRenderer = new WaypointDrawStepFactory();
    private final RadarDrawStepFactory radarRenderer = new RadarDrawStepFactory();
    private TextureImpl playerArrowFg;
    private TextureImpl playerArrowBg;
    private int playerArrowColor;
    private MiniMapProperties miniMapProperties;
    private StatTimer drawTimer;
    private StatTimer refreshStateTimer;
    private DisplayVars dv;

    private Point2D.Double centerPoint;
    private Rectangle2D.Double centerRect;

    private long initTime;
    private long lastAutoDayNightTime = -1;

    private Boolean lastPlayerUnderground;

    /**
     * Default constructor
     *
     * @param miniMapProperties the mini map properties
     */
    public MiniMap(MiniMapProperties miniMapProperties)
    {
        initTime = System.currentTimeMillis();
        setMiniMapProperties(miniMapProperties);
    }

    /**
     * State map state.
     *
     * @return the map state
     */
    public static synchronized MapState state()
    {
        return state;
    }

    /**
     * Ui state ui state.
     *
     * @return the ui state
     */
    public static synchronized UIState uiState()
    {
        return gridRenderer.getUIState();
    }

    /**
     * Update ui state.
     *
     * @param isActive the is active
     */
    public static void updateUIState(boolean isActive)
    {
        if (FMLClientHandler.instance().getClient().world != null)
        {
            gridRenderer.updateUIState(isActive);
        }
    }

    private void initGridRenderer()
    {
        gridRenderer.clear();
        state.requireRefresh();
        if (mc.player == null || mc.player.isDead)
        {
            return;
        }

        //boolean showCaves = shouldShowCaves();
        state.refresh(mc, mc.player, miniMapProperties);

        MapType mapType = state.getMapType();

        int gridSize = miniMapProperties.getSize() <= 768 ? 3 : 5;
        gridRenderer.setGridSize(gridSize);
        gridRenderer.setContext(state.getWorldDir(), mapType);
        gridRenderer.center(state.getWorldDir(), mapType, mc.player.posX, mc.player.posZ, miniMapProperties.zoomLevel.get());

        boolean highQuality = Journeymap.getClient().getCoreProperties().tileHighDisplayQuality.get();
        gridRenderer.updateTiles(state.getMapType(), state.getZoom(), highQuality, mc.displayWidth, mc.displayHeight, true, 0, 0);
    }

    /**
     * Reset init time.
     */
    public void resetInitTime()
    {
        initTime = System.currentTimeMillis();
    }

    /**
     * Sets mini map properties.
     *
     * @param miniMapProperties the mini map properties
     */
    public void setMiniMapProperties(MiniMapProperties miniMapProperties)
    {
        this.miniMapProperties = miniMapProperties;
        state().requireRefresh();
        reset();
    }

    /**
     * Gets current minimap properties.
     *
     * @return the current minimap properties
     */
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
     *
     * @param preview the preview
     */
    public void drawMap(boolean preview)
    {
        StatTimer timer = drawTimer;

        RenderHelper.disableStandardItemLighting();

        try
        {
            // Check player status
            if (mc.player == null || mc.player.isDead)
            {
                return;
            }

            // Clear GL error queue of anything that happened before JM starts drawing; don't log them
            gridRenderer.clearGlErrors(false);

            // Check state
            final boolean doStateRefresh = state.shouldRefresh(mc, miniMapProperties);

            // Update the state first
            if (doStateRefresh)
            {
                timer = refreshStateTimer.start();
                autoDayNight();
                gridRenderer.setContext(state.getWorldDir(), state.getMapType());
                if (!preview)
                {
                    state.refresh(mc, mc.player, miniMapProperties);
                }
                ClientAPI.INSTANCE.flagOverlaysForRerender();
            }
            else
            {
                timer.start();
            }

            // Update the grid
            boolean moved = gridRenderer.center(state.getWorldDir(), state.getMapType(), mc.player.posX, mc.player.posZ, miniMapProperties.zoomLevel.get());
            if (moved || doStateRefresh)
            {
                //boolean showCaves = shouldShowCaves();
                gridRenderer.updateTiles(state.getMapType(), state.getZoom(), state.isHighQuality(), mc.displayWidth, mc.displayHeight, doStateRefresh || preview, 0, 0);
            }

            // Refresh state
            if (doStateRefresh)
            {
                boolean checkWaypointDistance = Journeymap.getClient().getWaypointProperties().maxDistance.get() > 0;
                state.generateDrawSteps(mc, gridRenderer, waypointRenderer, radarRenderer, miniMapProperties, checkWaypointDistance);
                state.updateLastRefresh();
            }

            // Update display vars if needed
            updateDisplayVars(false);

            // Update labels if needed
            long now = System.currentTimeMillis();

            // Use 1:1 resolution for minimap regardless of how Minecraft UI is scaled
            DrawUtil.sizeDisplay(mc.displayWidth, mc.displayHeight);

            // Experimental fix for overly-dark screens with some graphics cards
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightmapS, lightmapT);

            // Ensure colors and alpha reset
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ZERO);
            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.enableDepth();

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
                        rotation = (180 - mc.player.rotationYawHead);
                    }
                    break;
                }
            }

            /***** BEGIN MATRIX: ROTATION *****/
            startMapRotation(rotation);

            try
            {
                // Move origin to top-left corner
                GlStateManager.translate(dv.translateX, dv.translateY, 0);

                // Draw grid
                gridRenderer.draw(dv.terrainAlpha, 0, 0, miniMapProperties.showGrid.get());

                // Draw entities, etc
                gridRenderer.draw(state.getDrawSteps(), 0, 0, dv.fontScale, rotation);

                // Get center of minimap and rect of minimap
                centerPoint = gridRenderer.getPixel(mc.player.posX, mc.player.posZ);
                centerRect = new Rectangle2D.Double(centerPoint.x - dv.minimapWidth / 2, centerPoint.y - dv.minimapHeight / 2, dv.minimapWidth, dv.minimapHeight);

                // Draw waypoints
                drawOnMapWaypoints(rotation);

                // Draw player
                if (miniMapProperties.showSelf.get() && playerArrowFg != null)
                {
                    if (centerPoint != null)
                    {
                        DrawUtil.drawColoredEntity(centerPoint.getX(), centerPoint.getY(), playerArrowBg, 0xffffff, 1f, 1f, mc.player.rotationYawHead);
                        DrawUtil.drawColoredEntity(centerPoint.getX(), centerPoint.getY(), playerArrowFg, playerArrowColor, 1f, 1f, mc.player.rotationYawHead);
                    }
                }

                // Return centerPoint to mid-screen
                GlStateManager.translate(-dv.translateX, -dv.translateY, 0);

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
                        startMapRotation(mc.player.rotationYawHead);
                        dv.minimapFrame.drawReticle();
                        /***** END MATRIX: ROTATION *****/
                        stopMapRotation(mc.player.rotationYawHead);
                    }
                }

                // Draw Map Type icon
                long lastMapChangeTime = state.getLastMapTypeChange();
                if (now - lastMapChangeTime <= 1000)
                {
                    stopMapRotation(rotation);
                    GlStateManager.translate(dv.translateX, dv.translateY, 0);
                    float alpha = Math.min(255, Math.max(0, 1100 - (now - lastMapChangeTime))) / 255f;
                    Point2D.Double windowCenter = gridRenderer.getWindowPosition(centerPoint);
                    dv.getMapTypeStatus(state.getMapType()).draw(windowCenter, alpha, 0);
                    GlStateManager.translate(-dv.translateX, -dv.translateY, 0);
                    startMapRotation(rotation);
                }

                // Draw Minimap Preset Id
                if (now - initTime <= 1000)
                {
                    stopMapRotation(rotation);
                    GlStateManager.translate(dv.translateX, dv.translateY, 0);
                    float alpha = Math.min(255, Math.max(0, 1100 - (now - initTime))) / 255f;
                    Point2D.Double windowCenter = gridRenderer.getWindowPosition(centerPoint);
                    dv.getMapPresetStatus(state.getMapType(), miniMapProperties.getId()).draw(windowCenter, alpha, 0);
                    GlStateManager.translate(-dv.translateX, -dv.translateY, 0);
                    startMapRotation(rotation);
                }

                // Finish stencil
                endStencil();

                // Draw Frame
                if(!dv.frameRotates && rotation!=0)
                {
                    stopMapRotation(rotation);
                }

                dv.minimapFrame.drawFrame();

                if(!dv.frameRotates && rotation!=0)
                {
                    startMapRotation(rotation);
                }

                // Draw cardinal compass points
                if (dv.showCompass)
                {
                    dv.minimapCompassPoints.drawPoints(rotation);
                }

                // Move origin to top-left corner
                GlStateManager.translate(dv.translateX, dv.translateY, 0);

                // Draw off-screen waypoints on top of frame
                drawOffMapWaypoints(rotation);

                // Draw cardinal compass point labels
                if (dv.showCompass)
                {
                    // Return centerPoint to mid-screen
                    GlStateManager.translate(-dv.translateX, -dv.translateY, 0);
                    dv.minimapCompassPoints.drawLabels(rotation);
                }

            }
            finally
            {
                /* END MATRIX: ROTATION */
                GlStateManager.popMatrix();
            }

            // Draw minimap info labels
            dv.drawInfoLabels(now);

            // Return resolution to how it is normally scaled
            DrawUtil.sizeDisplay(dv.scaledResolution.getScaledWidth_double(), dv.scaledResolution.getScaledHeight_double());
        }
        catch (Throwable t)
        {
            JMLogger.logOnce("Error during MiniMap.drawMap(): " + t.getMessage(), t);
        }
        finally
        {
            cleanup();
            timer.stop();

            // Clear GL error queue of anything that happened during drawing, and log them
            gridRenderer.clearGlErrors(true);
        }
    }

    private void drawOnMapWaypoints(double rotation)
    {
        boolean showLabel = miniMapProperties.showWaypointLabels.get();

        for (DrawStep.Pass pass : DrawStep.Pass.values())
        {
            for (DrawWayPointStep drawWayPointStep : state.getDrawWaypointSteps())
            {
                boolean onScreen = false;
                if (pass == DrawStep.Pass.Object)
                {
                    Point2D.Double waypointPos = drawWayPointStep.getPosition(0, 0, gridRenderer, true);
                    onScreen = isOnScreen(waypointPos, centerPoint, centerRect);
                    drawWayPointStep.setOnScreen(onScreen);
                }
                else
                {
                    onScreen = drawWayPointStep.isOnScreen();
                }

                if (onScreen)
                {
                    drawWayPointStep.setShowLabel(showLabel);
                    drawWayPointStep.draw(pass, 0, 0, gridRenderer, dv.fontScale, rotation);
                }
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
                drawWayPointStep.drawOffscreen(DrawStep.Pass.Object, point, rotation);
            }
        }
    }

    private void startMapRotation(double rotation)
    {
        GlStateManager.pushMatrix();
        if (rotation % 360 != 0)
        {
            double width = dv.displayWidth / 2 + (dv.translateX);
            double height = dv.displayHeight / 2 + (dv.translateY);

            GlStateManager.translate(width, height, 0);
            GlStateManager.rotate((float) rotation, 0, 0, 1.0f);
            GlStateManager.translate(-width, -height, 0);
        }

        gridRenderer.updateRotation(rotation);
    }

    private void stopMapRotation(double rotation)
    {
        GlStateManager.popMatrix();
        gridRenderer.updateRotation(rotation);
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
            cleanup();

            DrawUtil.zLevel = 1000;

            GlStateManager.colorMask(false, false, false, false); // allows map tiles to be partially opaque
            dv.minimapFrame.drawMask();
            GlStateManager.colorMask(true, true, true, true);

            DrawUtil.zLevel = 0;
            GlStateManager.depthMask(false); // otherwise entities and reticle not shown
            GlStateManager.depthFunc(GL11.GL_GREATER); // otherwise circle doesn't mask map tiles
        }
        catch (Throwable t)
        {
            JMLogger.logOnce("Error during MiniMap.beginStencil()", t);
        }

    }

    private void endStencil()
    {
        try
        {
            //GlStateManager.depthMask(false); // doesn't seem to matter
            GlStateManager.disableDepth(); // otherwise minimap frame not shown
            //GlStateManager.depthFunc(renderHelper.GL_LEQUAL);
            //GlStateManager.color(1, 1, 1, 1);
        }
        catch (Throwable t)
        {
            JMLogger.logOnce("Error during MiniMap.endStencil()", t);
        }
    }

    private void cleanup()
    {
        try
        {
            DrawUtil.zLevel = 0; // default

            GlStateManager.depthMask(true); // default
            GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT); // defensive
            GlStateManager.enableDepth(); // default
            GlStateManager.depthFunc(GL11.GL_LEQUAL); // not default, but required by toolbar
            GlStateManager.enableAlpha(); // default
            GlStateManager.color(1, 1, 1, 1); // default
            GlStateManager.clearColor(1, 1, 1, 1f); // defensive against shaders

        }
        catch (Throwable t)
        {
            JMLogger.logOnce("Error during MiniMap.cleanup()", t);
        }
    }

    private void autoDayNight()
    {
        if(mc.world!=null)
        {
            // Switch to caves/surface if needed
            boolean wasInCaves = false;
            if (miniMapProperties.showCaves.get() && FeatureManager.isAllowed(Feature.MapCaves))
            {
                EntityDTO player = DataCache.getPlayer();
                boolean neverChecked = (lastPlayerUnderground == null);
                boolean playerUnderground = player.underground;
                if(neverChecked || playerUnderground != lastPlayerUnderground)
                {
                    lastPlayerUnderground = playerUnderground;
                    if(playerUnderground)
                    {
                        state.setMapType(MapType.underground(player));
                    }
                    else
                    {
                        state.setMapType(MapType.from(miniMapProperties.preferredMapType.get(), player));
                        wasInCaves = true;
                    }
                }

                MapType currentMapType = state.getMapType();
                if (playerUnderground && currentMapType.isUnderground() && currentMapType.vSlice != player.chunkCoordY)
                {
                    state.setMapType(MapType.underground(player));
                }
            }

            // Switch to day/night if needed
            if (miniMapProperties.showDayNight.get() && (wasInCaves || state.getMapType().isDayOrNight()))
            {
                final long NIGHT = 13800;
                long worldTime = mc.world.getWorldTime() % 24000L;
                boolean neverChecked = (lastAutoDayNightTime==-1);
                if(worldTime>=NIGHT && (neverChecked || lastAutoDayNightTime<NIGHT))
                {
                    lastAutoDayNightTime = worldTime;
                    state.setMapType(MapType.night(mc.world.provider.getDimension()));
                }
                else if(worldTime<NIGHT && (neverChecked || lastAutoDayNightTime>=NIGHT))
                {
                    lastAutoDayNightTime = worldTime;
                    state.setMapType(MapType.day(mc.world.provider.getDimension()));
                }
            }

            //
        }
    }

    /**
     * Reset.
     */
    public void reset()
    {
        initTime = System.currentTimeMillis();
        lastAutoDayNightTime = -1;
        initGridRenderer();
        updateDisplayVars(miniMapProperties.shape.get(), miniMapProperties.position.get(), true);
        MiniMapOverlayHandler.checkEventConfig();
        GridRenderer.clearDebugMessages();

        CoreProperties coreProperties = Journeymap.getClient().getCoreProperties();
        playerArrowColor = coreProperties.getColor(coreProperties.colorSelf);
        if (miniMapProperties.playerDisplay.get().isLarge())
        {
            playerArrowBg = TextureCache.getTexture(TextureCache.PlayerArrowBG_Large);
            playerArrowFg = TextureCache.getTexture(TextureCache.PlayerArrow_Large);
        }
        else
        {
            playerArrowBg = TextureCache.getTexture(TextureCache.PlayerArrowBG);
            playerArrowFg = TextureCache.getTexture(TextureCache.PlayerArrow);
        }
    }


    /**
     * Update display vars.
     *
     * @param force the force
     */
    public void updateDisplayVars(boolean force)
    {
        if (dv != null)
        {
            updateDisplayVars(dv.shape, dv.position, force);
        }
    }

    /**
     * Update display vars.
     *
     * @param shape    the shape
     * @param position the position
     * @param force    the force
     */
    public void updateDisplayVars(Shape shape, Position position, boolean force)
    {
        if (dv != null
                && !force
                && mc.displayHeight == dv.displayHeight
                && mc.displayWidth == dv.displayWidth
                && this.dv.shape == shape
                && this.dv.position == position
                && this.dv.fontScale == miniMapProperties.fontScale.get())
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
        this.dv = new DisplayVars(mc, miniMapProperties);

        if (oldDv == null || oldDv.shape != this.dv.shape)
        {
            String timerName = String.format("MiniMap%s.%s", miniMapProperties.getId(), shape.name());
            this.drawTimer = StatTimer.get(timerName, 100);
            this.drawTimer.reset();
            this.refreshStateTimer = StatTimer.get(timerName + "+refreshState", 5);
            this.refreshStateTimer.reset();
        }

        // Set viewport
        double xpad = 0;
        double ypad = 0;
        Rectangle2D.Double viewPort = new Rectangle2D.Double(this.dv.textureX + xpad, this.dv.textureY + ypad, this.dv.minimapWidth - (2 * xpad), this.dv.minimapHeight - (2 * ypad));
        gridRenderer.setViewPort(viewPort);

        // Fire display update
        updateUIState(true);
    }

    public String getLocation()
    {
        final int playerX = MathHelper.floor(mc.player.posX);
        final int playerZ = MathHelper.floor(mc.player.posZ);
        final int playerY = MathHelper.floor(mc.player.getEntityBoundingBox().minY);
        return dv.locationFormatKeys.format(dv.locationFormatVerbose, playerX, playerZ, playerY, mc.player.chunkCoordY);
    }

    public String getBiome()
    {
        return state.getPlayerBiome();
    }


}

