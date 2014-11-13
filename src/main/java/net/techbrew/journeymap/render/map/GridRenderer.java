/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.render.map;

import com.google.common.cache.Cache;
import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;
import net.techbrew.journeymap.Constants.MapType;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.model.BlockCoordIntPair;
import net.techbrew.journeymap.render.draw.DrawStep;
import net.techbrew.journeymap.render.draw.DrawUtil;
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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Contains a set of 9 tiles organized along compass Point2D.Doubles.
 * Has basic logic to center on a tile and arrange neighboring tiles around it.
 *
 * @author mwoodman
 */
public class GridRenderer
{

    // Update pixel offsets for center
    private final Cache<Integer, Tile> tc = TileCache.instance();
    private final TilePos centerPos = new TilePos(0, 0);
    private final Logger logger = JourneyMap.getLogger();
    private final boolean debug = logger.isTraceEnabled();
    private final TreeMap<TilePos, Integer> grid = new TreeMap<TilePos, Integer>();
    private final Point2D.Double centerPixelOffset = new Point2D.Double();
    private final Color bgColor = new Color(0x22, 0x22, 0x22);
    private int gridSize; // 5 = 2560px.
    private double srcSize;
    private Rectangle2D.Double viewPort = null;
    private Rectangle2D.Double screenBounds = null;
    private int lastHeight = -1;
    private int lastWidth = -1;
    private MapType mapType = MapType.day;
    private int centerTileHash = Integer.MIN_VALUE;
    private int zoom;
    private double centerBlockX;
    private double centerBlockZ;
    private Integer dimension;
    private File worldDir;

    private double currentRotation;
    private IntBuffer viewportBuf;
    private FloatBuffer modelMatrixBuf;
    private FloatBuffer projMatrixBuf;
    private FloatBuffer winPosBuf;
    private FloatBuffer objPosBuf;

    public GridRenderer(int gridSize)
    {
        viewportBuf = BufferUtils.createIntBuffer(16);
        modelMatrixBuf = BufferUtils.createFloatBuffer(16);
        projMatrixBuf = BufferUtils.createFloatBuffer(16);
        winPosBuf = BufferUtils.createFloatBuffer(16);
        objPosBuf = BufferUtils.createFloatBuffer(16);
        setGridSize(gridSize);
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
        Cache<Integer, Tile> tc = TileCache.instance();

        for (int z = startRow; z <= endRow; z++)
        {
            for (int x = startCol; x <= endCol; x++)
            {
                TilePos pos = new TilePos(x, z);
                Tile tile = findNeighbor(centerTile, pos);
                grid.put(pos, tile.hashCode());
            }
        }

        //if(debug) logger.info("Grid cen done for cols " + startCol + " to " + endCol + " and rows " + startRow + " to " + endRow);
    }

    public void move(final int deltaBlockX, final int deltaBlockZ)
    {
        center(centerBlockX + deltaBlockX, centerBlockZ + deltaBlockZ, zoom);
    }

    public boolean center()
    {
        return center(centerBlockX, centerBlockZ, zoom);
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
        for (Map.Entry<TilePos, Integer> entry : grid.entrySet())
        {
            if (isOnScreen(entry.getKey()))
            {
                tile = tc.getIfPresent(entry.getValue());
                if (tile == null || (!preview && !tile.hasTexture()))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean center(final double blockX, final double blockZ, final int zoom)
    {

        if (blockX == centerBlockX && blockZ == centerBlockZ && zoom == this.zoom && !grid.isEmpty())
        {
            return false;
        }

        centerBlockX = blockX;
        centerBlockZ = blockZ;
        this.zoom = zoom;

        // Get zoomed tile coords
        final int tileX = Tile.blockPosToTile((int) Math.floor(centerBlockX), this.zoom);
        final int tileZ = Tile.blockPosToTile((int) Math.floor(centerBlockZ), this.zoom);

        // Chech hash of tile coords
        final int newCenterHash = Tile.toHashCode(tileX, tileZ, zoom, dimension);
        final boolean centerTileChanged = newCenterHash != centerTileHash;
        centerTileHash = newCenterHash;

        if (centerTileChanged || grid.isEmpty())
        {

            // Center on tile
            Tile newCenterTile = findTile(tileX, tileZ);
            populateGrid(newCenterTile);

            if (debug)
            {
                logger.debug("Centered on " + newCenterTile + " with pixel offsets of " + centerPixelOffset.x + "," + centerPixelOffset.y);
                Minecraft mc = FMLClientHandler.instance().getClient();
                BufferedImage tmp = new BufferedImage(mc.displayWidth, mc.displayHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = tmp.createGraphics();
                g.setStroke(new BasicStroke(1));
                g.setColor(Color.GREEN);
                g.drawLine(mc.displayWidth / 2, 0, mc.displayWidth / 2, mc.displayHeight);
                g.drawLine(0, mc.displayHeight / 2, mc.displayWidth, mc.displayHeight / 2);
            }
        }
        return true;
    }

    public boolean updateTextures(MapType mapType, Integer vSlice, int width, int height, boolean fullUpdate, double xOffset, double yOffset)
    {

        // Update screen dimensions
        updateBounds(width, height);

        // Get center tile
        this.mapType = mapType;
        Integer centerHash = grid.get(centerPos);
        if (centerHash == null)
        {
            return false;
        }

        // Corner case where center tile is here but not in cache, not sure why
        Tile centerTile = tc.getIfPresent(centerHash);
        if (centerTile == null)
        {
            final int tileX = Tile.blockPosToTile((int) Math.floor(centerBlockX), this.zoom);
            final int tileZ = Tile.blockPosToTile((int) Math.floor(centerBlockZ), this.zoom);
            centerTile = findTile(tileX, tileZ);
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

        if (!fullUpdate)
        {
            return false;
        }

        boolean updated = false;
        TilePos pos;
        Tile tile;
        Integer hashCode;

        // Get tiles
        for (Map.Entry<TilePos, Integer> entry : grid.entrySet())
        {
            pos = entry.getKey();
            hashCode = entry.getValue();
            tile = tc.getIfPresent(hashCode);

            // Ensure grid populated
            if (tile == null)
            {
                tile = findNeighbor(centerTile, pos);
                grid.put(pos, hashCode);
            }

            // Update texture only if on-screen
            if (isOnScreen(pos))
            {
                if (tile != null && tile.updateTexture(pos, this.mapType, vSlice))
                {
                    updated = true;
                }
            }
        }

        return updated;
    }

    public Point2D.Double getCenterPixelOffset()
    {
        return centerPixelOffset;
    }

    public BlockCoordIntPair getBlockUnderMouse(double mouseX, double mouseY, int screenWidth, int screenHeight)
    {
        double centerPixelX = screenWidth / 2.0;
        double centerPixelZ = screenHeight / 2.0;

        double blockSize = (int) Math.pow(2, zoom);

        double deltaX = (centerPixelX - mouseX) / blockSize;
        double deltaZ = (centerPixelZ - mouseY) / blockSize;

        int x = MathHelper.floor_double(centerBlockX - deltaX);
        int z = MathHelper.floor_double(centerBlockZ + deltaZ);
        return new BlockCoordIntPair(x, z);
    }

    public Point2D.Double getBlockPixelInGrid(double x, double z)
    {

        double localBlockX = x - centerBlockX;
        double localBlockZ = z - centerBlockZ;

        int blockSize = (int) Math.pow(2, zoom);
        double pixelOffsetX = lastWidth / 2 + (localBlockX * blockSize);
        double pixelOffsetZ = lastHeight / 2 + (localBlockZ * blockSize);

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
        if (drawStepList == null || drawStepList.isEmpty())
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

//        GL11.glDisable(GL11.GL_DEPTH_TEST);
//        GL11.glDepthMask(false);
//        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        for (DrawStep drawStep : drawSteps)
        {
            drawStep.draw(xOffset, yOffset, this, drawScale, fontScale, rotation);
        }

//        GL11.glDepthMask(true);
//        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    public void draw(final float alpha, final double offsetX, final double offsetZ)
    {
        if (!grid.isEmpty())
        {
            double centerX = offsetX + centerPixelOffset.x;
            double centerZ = offsetZ + centerPixelOffset.y;
            final Cache<Integer, Tile> tc = TileCache.instance();

            for (Map.Entry<TilePos, Integer> entry : grid.entrySet())
            {
                TilePos pos = entry.getKey();
                Tile tile = tc.getIfPresent(entry.getValue());
                if (tile == null || !tile.hasTexture())
                {
                    final double startX = offsetX + pos.startX;
                    final double startZ = offsetZ + pos.startZ;
                    DrawUtil.drawRectangle(startX, startZ, Tile.TILESIZE, Tile.TILESIZE, bgColor, 255);
                }
                else
                {
                    tile.draw(pos, centerX, centerZ, alpha);
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
     * @param pos tile position in grid
     * @return true if on screen
     */
    private boolean isOnScreen(TilePos pos)
    {
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
        if (screenBounds == null || lastWidth != width || lastHeight != height)
        {
            lastWidth = width;
            lastHeight = height;

            if (viewPort == null)
            {
                screenBounds = new Rectangle2D.Double(0, 0, width, height);
            }
            else
            {
                screenBounds = new Rectangle2D.Double((width - viewPort.width) / 2, (height - viewPort.height) / 2, viewPort.width, viewPort.height);
            }
        }
    }

    private Tile findNeighbor(Tile tile, TilePos pos)
    {
        if (pos.deltaX == 0 && pos.deltaZ == 0)
        {
            return tile;
        }
        return findTile(tile.tileX + pos.deltaX, tile.tileZ + pos.deltaZ);
    }

    private Tile findTile(final int tileX, final int tileZ)
    {
        return TileCache.getOrCreate(worldDir, mapType, tileX, tileZ, zoom, dimension);
    }

    public void setContext(File worldDir, int dimension)
    {
        this.worldDir = worldDir;
        this.dimension = dimension;
    }

    public void updateGL(double rotation)
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

    public int getDimension()
    {
        return dimension;
    }

    public int getZoom()
    {
        return zoom;
    }

    public boolean setZoom(int zoom)
    {
        return center(centerBlockX, centerBlockZ, zoom);
    }

    public int getRenderSize()
    {
        return this.gridSize * Tile.TILESIZE;
    }

    public void clear()
    {
        grid.clear();
    }

    public int getWidth()
    {
        return lastWidth;
    }

    public int getHeight()
    {
        return lastHeight;
    }
}
