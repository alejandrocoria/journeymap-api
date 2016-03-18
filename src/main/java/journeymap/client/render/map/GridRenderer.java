/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.render.map;

import journeymap.client.JourneymapClient;
import journeymap.client.api.display.Context;
import journeymap.client.api.event.ClientEvent;
import journeymap.client.api.event.DisplayUpdateEvent;
import journeymap.client.api.impl.ClientAPI;
import journeymap.client.api.impl.ClientEventManager;
import journeymap.client.api.util.UIState;
import journeymap.client.cartography.RGB;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.log.StatTimer;
import journeymap.client.model.GridSpec;
import journeymap.client.model.MapType;
import journeymap.client.model.RegionImageCache;
import journeymap.client.render.draw.DrawStep;
import journeymap.client.render.draw.DrawUtil;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;
import java.util.List;

/**
 * Contains a set of 9 tiles organized along compass Point2D.Doubles.
 * Has basic logic to center on a tile and arrange neighboring tiles around it.
 *
 * @author mwoodman
 */
public class GridRenderer
{
    private static boolean enabled = true;
    private static HashMap<String, String> messages = new HashMap<String, String>();

    // Update pixel offsets for center
    private final TilePos centerPos = new TilePos(0, 0);
    private final Logger logger = Journeymap.getLogger();
    private final boolean debug = logger.isDebugEnabled();
    private final TreeMap<TilePos, Tile> grid = new TreeMap<TilePos, Tile>();
    private final Point2D.Double centerPixelOffset = new Point2D.Double();
    private final int maxGlErrors = 20;
    private final Context.UI contextUi;
    StatTimer updateTilesTimer1 = StatTimer.get("GridRenderer.updateTiles(1)", 5, 500);
    StatTimer updateTilesTimer2 = StatTimer.get("GridRenderer.updateTiles(2)", 5, 500);
    private UIState uiState;
    private int glErrors = 0;

    private int gridSize; // 5 = 2560px.
    private double srcSize;
    private Rectangle2D.Double viewPort = null;
    private Rectangle2D.Double screenBounds = null;
    private AxisAlignedBB blockBounds = null;
    private int lastHeight = -1;
    private int lastWidth = -1;
    private MapType mapType;
    private String centerTileKey = "";
    private int zoom;
    private double centerBlockX;
    private double centerBlockZ;
    private File worldDir;
    private double currentRotation;
    private IntBuffer viewportBuf;
    private FloatBuffer modelMatrixBuf;
    private FloatBuffer projMatrixBuf;
    private FloatBuffer winPosBuf;
    private FloatBuffer objPosBuf;

    public GridRenderer(Context.UI contextUi, int gridSize)
    {
        this.contextUi = contextUi;
        this.uiState = UIState.newInactive(contextUi);
        viewportBuf = BufferUtils.createIntBuffer(16);
        modelMatrixBuf = BufferUtils.createFloatBuffer(16);
        projMatrixBuf = BufferUtils.createFloatBuffer(16);
        winPosBuf = BufferUtils.createFloatBuffer(16);
        objPosBuf = BufferUtils.createFloatBuffer(16);
        setGridSize(gridSize);
    }

    public static void addDebugMessage(String key, String message)
    {
        messages.put(key, message);
    }

    public static void removeDebugMessage(String key, String message)
    {
        messages.remove(key);
    }

    public static void clearDebugMessages()
    {
        messages.clear();
    }

    /**
     * Be sure this is called on the main thread only
     *
     * @param enabled
     */
    public static void setEnabled(boolean enabled)
    {
        GridRenderer.enabled = enabled;
        if (!enabled)
        {
            TileDrawStepCache.clear();
        }
    }

    public Context.UI getDisplay()
    {
        return contextUi;
    }

    public void setViewPort(Rectangle2D.Double viewPort)
    {
        this.viewPort = viewPort;
        this.screenBounds = null;
        updateBounds(lastWidth, lastHeight);
    }

    private void populateGrid(Tile centerTile)
    {
        final int endRow = (gridSize - 1) / 2;
        final int endCol = (gridSize - 1) / 2;
        final int startRow = -endRow;
        final int startCol = -endCol;

        for (int z = startRow; z <= endRow; z++)
        {
            for (int x = startCol; x <= endCol; x++)
            {
                TilePos pos = new TilePos(x, z);
                Tile tile = findNeighbor(centerTile, pos);
                grid.put(pos, tile);
            }
        }

        //if(debug) logger.info("Grid cen done for cols " + startCol + " to " + endCol + " and rows " + startRow + " to " + endRow);
    }

    public void move(final int deltaBlockX, final int deltaBlockZ)
    {
        center(worldDir, mapType, centerBlockX + deltaBlockX, centerBlockZ + deltaBlockZ, zoom);
    }

    public boolean center()
    {
        return center(worldDir, mapType, centerBlockX, centerBlockZ, zoom);
    }

    public boolean hasUnloadedTile()
    {
        return hasUnloadedTile(false);
    }

    public int getGridSize()
    {
        return gridSize;
    }

    public void setGridSize(int gridSize)
    {
        this.gridSize = gridSize;  // Must be an odd number so as to have a center tile.
        srcSize = gridSize * Tile.TILESIZE;
    }

    public boolean hasUnloadedTile(boolean preview)
    {
        Tile tile;
        for (Map.Entry<TilePos, Tile> entry : grid.entrySet())
        {
            if (isOnScreen(entry.getKey()))
            {
                tile = entry.getValue();
                if (tile == null || !tile.hasTexture(this.mapType))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean center(File worldDir, MapType mapType, final double blockX, final double blockZ, final int zoom)
    {
        boolean mapTypeChanged = !Objects.equals(worldDir, this.worldDir) || !Objects.equals(mapType, this.mapType);

        if (!Objects.equals(worldDir, this.worldDir))
        {
            this.worldDir = worldDir;
        }

        if ((blockX == centerBlockX) && (blockZ == centerBlockZ) && (zoom == this.zoom) && !mapTypeChanged && !grid.isEmpty())
        {
            // Check ui state
            if (!Objects.equals(mapType.apiMapType, this.uiState.mapType))
            {
                updateUIState(true);
            }
            // Nothing needs to change
            return false;
        }

        centerBlockX = blockX;
        centerBlockZ = blockZ;
        this.zoom = zoom;

        // Get zoomed tile coords
        final int tileX = Tile.blockPosToTile((int) Math.floor(blockX), this.zoom);
        final int tileZ = Tile.blockPosToTile((int) Math.floor(blockZ), this.zoom);

        // Check key of center tile
        final String newCenterKey = Tile.toCacheKey(tileX, tileZ, zoom);
        final boolean centerTileChanged = !newCenterKey.equals(centerTileKey);
        centerTileKey = newCenterKey;

        if (mapTypeChanged || centerTileChanged || grid.isEmpty())
        {
            // Center on tile
            Tile newCenterTile = findTile(tileX, tileZ, zoom);
            populateGrid(newCenterTile);

            // Notify plugins
            if (debug)
            {
                logger.debug("Centered on " + newCenterTile + " with pixel offsets of " + centerPixelOffset.x + "," + centerPixelOffset.y);
                Minecraft mc = ForgeHelper.INSTANCE.getClient();
                BufferedImage tmp = new BufferedImage(mc.displayWidth, mc.displayHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = tmp.createGraphics();
                g.setStroke(new BasicStroke(1));
                g.setColor(Color.GREEN);
                g.drawLine(mc.displayWidth / 2, 0, mc.displayWidth / 2, mc.displayHeight);
                g.drawLine(0, mc.displayHeight / 2, mc.displayWidth, mc.displayHeight / 2);
            }
        }

        updateUIState(true);
        return true;
    }

    public void updateTiles(MapType mapType, int zoom, boolean highQuality, int width, int height, boolean fullUpdate, double xOffset, double yOffset)
    {
        updateTilesTimer1.start();
        this.mapType = mapType;
        this.zoom = zoom;

        // Update screen dimensions
        updateBounds(width, height);

        // Get center tile, check if present and current
        Tile centerTile = grid.get(centerPos);
        if (centerTile == null || centerTile.zoom != this.zoom)
        {
            final int tileX = Tile.blockPosToTile((int) Math.floor(centerBlockX), this.zoom);
            final int tileZ = Tile.blockPosToTile((int) Math.floor(centerBlockZ), this.zoom);
            centerTile = findTile(tileX, tileZ, this.zoom);
            populateGrid(centerTile);
        }

        // Derive offsets for centering the map
        Point2D blockPixelOffset = centerTile.blockPixelOffsetInTile(centerBlockX, centerBlockZ);
        final double blockSizeOffset = Math.pow(2, zoom) / 2;
        final int magic = (gridSize == 5 ? 2 : 1) * Tile.TILESIZE; // TODO:  Understand why "2" as it relates to gridSize.  If gridSize is 3, this has to be "1".

        double displayOffsetX = xOffset + magic - ((srcSize - lastWidth) / 2);
        if (centerBlockX < 0)
        {
            displayOffsetX -= blockSizeOffset;
        }
        else
        {
            displayOffsetX += blockSizeOffset;
        }
        double displayOffsetY = yOffset + magic - ((srcSize - lastHeight) / 2);
        if (centerBlockZ < 0)
        {
            displayOffsetY -= blockSizeOffset;
        }
        else
        {
            displayOffsetY += blockSizeOffset;
        }

        centerPixelOffset.setLocation(displayOffsetX + blockPixelOffset.getX(), displayOffsetY + blockPixelOffset.getY());

        updateTilesTimer1.stop();
        if (!fullUpdate)
        {
            return;
        }

        updateTilesTimer2.start();

        TilePos pos;
        Tile tile;
        Integer hashCode;

        // Get tiles
        for (Map.Entry<TilePos, Tile> entry : grid.entrySet())
        {
            pos = entry.getKey();
            tile = entry.getValue();

            // Ensure grid populated
            if (tile == null)
            {
                tile = findNeighbor(centerTile, pos);
                grid.put(pos, tile);
            }

            // Update texture only if on-screen
            //if (isOnScreen(pos))
            {
                if (!tile.hasTexture(this.mapType))
                {
                    tile.updateTexture(worldDir, this.mapType, highQuality);
                }
            }
        }

        updateTilesTimer2.stop();
        return;
    }

    public Point2D.Double getCenterPixelOffset()
    {
        return centerPixelOffset;
    }

    public AxisAlignedBB getBlockBounds()
    {
        return blockBounds;
    }

    public BlockPos getBlockAtPixel(Point2D.Double pixel)
    {
        double centerPixelX = lastWidth / 2.0;
        double centerPixelZ = lastHeight / 2.0;

        double deltaX = ((centerPixelX - pixel.x) / uiState.blockSize);
        double deltaZ = ((centerPixelZ - (lastHeight - pixel.y)) / uiState.blockSize);

        int x = MathHelper.floor_double(centerBlockX - deltaX);
        int z = MathHelper.floor_double(centerBlockZ + deltaZ);
        return new BlockPos(x, 0, z);
    }

    public Point2D.Double getBlockPixelInGrid(BlockPos pos)
    {
        return getBlockPixelInGrid(pos.getX(), pos.getZ());
    }

    public Point2D.Double getBlockPixelInGrid(double blockX, double blockZ)
    {
        Minecraft mc = ForgeHelper.INSTANCE.getClient();
        double localBlockX = blockX - centerBlockX;
        double localBlockZ = blockZ - centerBlockZ;

        int blockSize = (int) Math.pow(2, zoom);
        double pixelOffsetX = mc.displayWidth / 2.0 + (localBlockX * blockSize);
        double pixelOffsetZ = mc.displayHeight / 2.0 + (localBlockZ * blockSize);

        return new Point2D.Double(pixelOffsetX, pixelOffsetZ);
    }

    /**
     * Draw a list of steps
     *
     * @param drawStepList
     * @param xOffset
     * @param yOffset
     */
    public void draw(final List<? extends DrawStep> drawStepList, double xOffset, double yOffset, float drawScale, double fontScale, double rotation)
    {
        if (!enabled || drawStepList == null || drawStepList.isEmpty())
        {
            return;
        }
        draw(xOffset, yOffset, drawScale, fontScale, rotation, drawStepList.toArray(new DrawStep[drawStepList.size()]));
    }

    /**
     * Draw an array of steps
     */
    public void draw(double xOffset, double yOffset, float drawScale, double fontScale, double rotation, DrawStep... drawSteps)
    {
        if (enabled)
        {
            for (DrawStep.Pass pass : DrawStep.Pass.values())
            {
                for (DrawStep drawStep : drawSteps)
                {
                    drawStep.draw(pass, xOffset, yOffset, this, drawScale, fontScale, rotation);
                }
            }
        }
    }

    public void draw(final float alpha, final double offsetX, final double offsetZ, boolean showGrid)
    {
        if (enabled && !grid.isEmpty())
        {
            double centerX = offsetX + centerPixelOffset.x;
            double centerZ = offsetZ + centerPixelOffset.y;
            GridSpec gridSpec = showGrid ? JourneymapClient.getCoreProperties().gridSpecs.getSpec(mapType) : null;

            boolean somethingDrew = false;
            for (Map.Entry<TilePos, Tile> entry : grid.entrySet())
            {
                TilePos pos = entry.getKey();
                Tile tile = entry.getValue();

                if (tile == null)
                {
                    continue;
                }
                else
                {
                    if (tile.draw(pos, centerX, centerZ, alpha, gridSpec))
                    {
                        somethingDrew = true;
                    }
                }
            }

            if (!somethingDrew)
            {
                RegionImageCache.instance().clear();
            }
        }

        // Draw debug messages
        if (!messages.isEmpty())
        {
            double centerX = offsetX + centerPixelOffset.x + (centerPos.endX - centerPos.startX) / 2;
            double centerZ = offsetZ + centerPixelOffset.y + ((centerPos.endZ - centerPos.startZ) / 2) - 60;

            for (String message : messages.values())
            {
                DrawUtil.drawLabel(message, centerX, centerZ += 20, DrawUtil.HAlign.Center, DrawUtil.VAlign.Below, RGB.BLACK_RGB, 1f, RGB.WHITE_RGB, 1f, 1, true);
            }
        }

    }

    /**
     * Clear GL error queue, optionally log them
     */
    public void clearGlErrors(boolean report)
    {
        int err;
        while ((err = GL11.glGetError()) != GL11.GL_NO_ERROR)
        {
            if (report && glErrors <= maxGlErrors)
            {
                glErrors++;
                if (glErrors < maxGlErrors)
                {
                    logger.warn("GL Error occurred during JourneyMap draw: " + err);
                }
                else
                {
                    logger.warn("GL Error reporting during JourneyMap will be suppressed after max errors: " + maxGlErrors);
                }
            }
        }
    }

    /**
     * Returns a pixel Point2D.Double if on screen, null if not.
     *
     * @param blockX pos x
     * @param blockZ pos z
     * @return pixel
     */
    public Point2D.Double getPixel(double blockX, double blockZ)
    {
        Point2D.Double pixel = getBlockPixelInGrid(blockX, blockZ);
        if (isOnScreen(pixel))
        {
            return pixel;
        }
        else
        {
            return null;
        }
    }

    /**
     * Adjusts a pixel to the nearest edge if it is not on screen.
     */
    public void ensureOnScreen(Point2D pixel)
    {
        if (screenBounds == null)
        {
            return;
        }

        double x = pixel.getX();
        if (x < screenBounds.x)
        {
            x = screenBounds.x;
        }
        else if (x > screenBounds.getMaxX())
        {
            x = screenBounds.getMaxX();
        }

        double y = pixel.getY();
        if (y < screenBounds.y)
        {
            y = screenBounds.y;
        }
        else if (y > screenBounds.getMaxY())
        {
            y = screenBounds.getMaxY();
        }

        pixel.setLocation(x, y);
    }

    /**
     * This is a pixel-based area check, not a location check
     *
     * @param pos tile setDimensions in grid
     * @return true if on screen
     */
    private boolean isOnScreen(TilePos pos)
    {
        // TODO
        return true;
        //return isOnScreen(pos.startX + centerPixelOffset.x, pos.startZ + centerPixelOffset.y, Tile.LOAD_RADIUS, Tile.LOAD_RADIUS);
    }

    /**
     * This is a pixel check, not a location check
     *
     * @param pixel checked
     * @return true if on screen
     */
    public boolean isOnScreen(Point2D.Double pixel)
    {
        return screenBounds.contains(pixel);
    }

    /**
     * This is a pixel check, not a location check
     *
     * @param bounds checked
     * @return true if on screen
     */
    public boolean isOnScreen(Rectangle2D.Double bounds)
    {
        return screenBounds.intersects(bounds);
    }

    /**
     * This is a pixel check, not a location check
     *
     * @param x screen x
     * @param y screen y
     * @return true if on screen
     */
    public boolean isOnScreen(double x, double y)
    {
        return screenBounds.contains(x, y);
    }

    /**
     * This is a pixel-based area check, not a location check
     *
     * @param startX upper pixel x
     * @param startY upper pixel y
     * @param width  of area
     * @param height of area
     * @return true if on screen
     */
    public boolean isOnScreen(double startX, double startY, int width, int height)
    {
        if (screenBounds == null)
        {
            return false;
        }

        if (screenBounds.intersects(startX, startY, width, height))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Updates the screenBounds rectangle.
     *
     * @param width
     * @param height
     */
    private void updateBounds(int width, int height)
    {
        if (screenBounds == null || lastWidth != width || lastHeight != height || blockBounds == null)
        {
            lastWidth = width;
            lastHeight = height;

            if (viewPort == null)
            {
                int pad = 32;
                screenBounds = new Rectangle2D.Double(-pad, -pad, width + pad, height + pad);
            }
            else
            {
                screenBounds = new Rectangle2D.Double((width - viewPort.width) / 2, (height - viewPort.height) / 2, viewPort.width, viewPort.height);
            }

            // Flag draw steps to rerender
            ClientAPI.INSTANCE.flagOverlaysForRerender();
        }
    }

    public void updateUIState(boolean isActive)
    {
        if (isActive && (screenBounds == null))
        {
            return;
        }

        UIState newState = null;
        if (isActive)
        {
            // Pad the BB by two chunks
            int worldHeight = ForgeHelper.INSTANCE.getClient().theWorld.getActualHeight();
            int pad = 32;

            BlockPos upperLeft = getBlockAtPixel(new Point2D.Double(screenBounds.getMinX(), screenBounds.getMinY()));
            BlockPos lowerRight = getBlockAtPixel(new Point2D.Double(screenBounds.getMaxX(), screenBounds.getMaxY()));

            blockBounds = new AxisAlignedBB(upperLeft.add(-pad, 0, -pad), lowerRight.add(pad, worldHeight, pad));

            newState = new UIState(contextUi, true, mapType.dimension, zoom, mapType.apiMapType,
                    new BlockPos(centerBlockX, 0, centerBlockZ),
                    blockBounds,
                    screenBounds);
        }
        else
        {
            newState = UIState.newInactive(contextUi);
        }

        if (!newState.equals(this.uiState))
        {
            this.uiState = newState;
            ClientEventManager clientEventManager = ClientAPI.INSTANCE.getClientEventManager();
            if (clientEventManager.canFireClientEvent(ClientEvent.Type.DISPLAY_UPDATE))
            {
                clientEventManager.fireDisplayUpdateEvent(new DisplayUpdateEvent(uiState));
            }
        }
    }

    private Tile findNeighbor(Tile tile, TilePos pos)
    {
        if (pos.deltaX == 0 && pos.deltaZ == 0)
        {
            return tile;
        }
        return findTile(tile.tileX + pos.deltaX, tile.tileZ + pos.deltaZ, tile.zoom);
    }

    private Tile findTile(final int tileX, final int tileZ, final int zoom)
    {
        return Tile.create(tileX, tileZ, zoom, worldDir, mapType, JourneymapClient.getCoreProperties().tileHighDisplayQuality.get());
    }

    public void setContext(File worldDir, MapType mapType)
    {
        this.worldDir = worldDir;
        this.mapType = mapType;
        TileDrawStepCache.setContext(worldDir, mapType);
    }

    public void updateRotation(double rotation)
    {
        currentRotation = rotation;
        GL11.glGetInteger(GL11.GL_VIEWPORT, viewportBuf);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelMatrixBuf);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projMatrixBuf);
    }

    public Point2D shiftWindowPosition(double x, double y, int shiftX, int shiftY)
    {
        if (currentRotation % 360 == 0)
        {
            return new Point2D.Double(x + shiftX, y + shiftY);
        }
        else
        {
            GLU.gluProject((float) x, (float) y, 0f, modelMatrixBuf, projMatrixBuf, viewportBuf, winPosBuf);
            GLU.gluUnProject(winPosBuf.get(0) + shiftX, winPosBuf.get(1) + shiftY, 0, modelMatrixBuf, projMatrixBuf, viewportBuf, objPosBuf);
            return new Point2D.Float(objPosBuf.get(0), objPosBuf.get(1));
        }
    }

    public Point2D.Double getWindowPosition(Point2D.Double matrixPixel)
    {
        if (currentRotation % 360 == 0)
        {
            return matrixPixel;
        }
        else
        {
            GLU.gluProject((float) matrixPixel.getX(), (float) matrixPixel.getY(), 0f, modelMatrixBuf, projMatrixBuf, viewportBuf, winPosBuf);
            return new Point2D.Double(winPosBuf.get(0), winPosBuf.get(1));
        }
    }

    public Point2D.Double getMatrixPosition(Point2D.Double windowPixel)
    {
        GLU.gluUnProject((float) windowPixel.x, (float) windowPixel.y, 0, modelMatrixBuf, projMatrixBuf, viewportBuf, objPosBuf);
        return new Point2D.Double(objPosBuf.get(0), objPosBuf.get(1));
    }

    public double getCenterBlockX()
    {
        return centerBlockX;
    }

    public double getCenterBlockZ()
    {
        return centerBlockZ;
    }

    public File getWorldDir()
    {
        return worldDir;
    }

    public MapType getMapType()
    {
        return mapType;
    }

    public int getZoom()
    {
        return zoom;
    }

    public boolean setZoom(int zoom)
    {
        return center(worldDir, mapType, centerBlockX, centerBlockZ, zoom);
    }

    public int getRenderSize()
    {
        return this.gridSize * Tile.TILESIZE;
    }

    public void clear()
    {
        grid.clear();
        messages.clear();
    }

    public int getWidth()
    {
        return lastWidth;
    }

    public int getHeight()
    {
        return lastHeight;
    }

    public UIState getUIState()
    {
        return uiState;
    }
}
