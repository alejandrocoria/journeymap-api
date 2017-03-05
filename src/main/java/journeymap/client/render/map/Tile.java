/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.render.map;

import journeymap.client.Constants;
import journeymap.client.io.RegionImageHandler;
import journeymap.client.log.ChatLog;
import journeymap.client.model.GridSpec;
import journeymap.client.model.MapType;
import journeymap.client.model.RegionImageCache;
import journeymap.client.properties.CoreProperties;
import journeymap.client.ui.fullscreen.Fullscreen;
import journeymap.client.ui.minimap.MiniMap;
import journeymap.common.Journeymap;
import net.minecraft.util.math.ChunkPos;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;

/**
 * The type Tile.
 */
public class Tile
{
    /**
     * The constant TILESIZE.
     */
    public final static int TILESIZE = 512;
    /**
     * The constant LOAD_RADIUS.
     */
    public final static int LOAD_RADIUS = (int) (TILESIZE * 1.5);
    /**
     * The Debug gl settings.
     */
    static String debugGlSettings = "";
    /**
     * The Zoom.
     */
    final int zoom;
    /**
     * The Tile x.
     */
    final int tileX;
    /**
     * The Tile z.
     */
    final int tileZ;
    /**
     * The Ul chunk.
     */
    final ChunkPos ulChunk;
    /**
     * The Lr chunk.
     */
    final ChunkPos lrChunk;
    /**
     * The Ul block.
     */
    final Point ulBlock;
    /**
     * The Lr block.
     */
    final Point lrBlock;
    /**
     * The Draw steps.
     */
    final ArrayList<TileDrawStep> drawSteps = new ArrayList<TileDrawStep>();
    private final Logger logger = Journeymap.getLogger();
    private final int theHashCode;
    private final String theCacheKey;
    /**
     * The Render type.
     */
    int renderType = 0;
    /**
     * The Texture filter.
     */
    int textureFilter = 0;
    /**
     * The Texture wrap.
     */
    int textureWrap = 0;

    private Tile(final int tileX, final int tileZ, final int zoom)
    {
        this.tileX = tileX;
        this.tileZ = tileZ;
        this.zoom = zoom;
        this.theCacheKey = toCacheKey(tileX, tileZ, zoom);
        this.theHashCode = theCacheKey.hashCode();
        final int distance = 32 / (int) Math.pow(2, zoom);
        ulChunk = new ChunkPos(tileX * distance, tileZ * distance);
        lrChunk = new ChunkPos(ulChunk.chunkXPos + distance - 1, ulChunk.chunkZPos + distance - 1);
        ulBlock = new Point(ulChunk.chunkXPos * 16, ulChunk.chunkZPos * 16);
        lrBlock = new Point((lrChunk.chunkXPos * 16) + 15, (lrChunk.chunkZPos * 16) + 15);
        updateRenderType();
    }

    /**
     * Create tile.
     *
     * @param tileX       the tile x
     * @param tileZ       the tile z
     * @param zoom        the zoom
     * @param worldDir    the world dir
     * @param mapType     the map type
     * @param highQuality the high quality
     * @return the tile
     */
    public static Tile create(final int tileX, final int tileZ, final int zoom, File worldDir, final MapType mapType, boolean highQuality)
    {
        Tile tile = new Tile(tileX, tileZ, zoom);
        tile.updateTexture(worldDir, mapType, highQuality);
        return tile;
    }

    /**
     * Block pos to tile int.
     *
     * @param b    the b
     * @param zoom the zoom
     * @return the int
     */
    public static int blockPosToTile(int b, int zoom)
    {
        int tile = b >> (9 - zoom);  // (2 pow 9 = 512)
        return tile;
    }

    /**
     * Tile to block int.
     *
     * @param t    the t
     * @param zoom the zoom
     * @return the int
     */
    public static int tileToBlock(int t, int zoom)
    {
        return t << (9 - zoom);
    }

    /**
     * To cache key string.
     *
     * @param tileX the tile x
     * @param tileZ the tile z
     * @param zoom  the zoom
     * @return the string
     */
    public static String toCacheKey(final int tileX, final int tileZ, final int zoom)
    {
        return "" + tileX + "," + tileZ + "@" + zoom;
    }

    /**
     * Switch tile render type.
     */
    public static void switchTileRenderType()
    {
        // Switch Tile Render Type
        CoreProperties coreProperties = Journeymap.getClient().getCoreProperties();
        int type = coreProperties.tileRenderType.incrementAndGet();
        if (type > 4)
        {
            type = 1;
            coreProperties.tileRenderType.set(type);
        }
        coreProperties.save();
        String msg = String.format("%s: %s (%s)", Constants.getString("jm.advanced.tile_render_type"), type, Tile.debugGlSettings);
        ChatLog.announceError(msg);
        resetTileDisplay();
    }

    /**
     * Switch tile display quality.
     */
    public static void switchTileDisplayQuality()
    {
        CoreProperties coreProperties = Journeymap.getClient().getCoreProperties();
        boolean high = !coreProperties.tileHighDisplayQuality.get();
        coreProperties.tileHighDisplayQuality.set(high);
        coreProperties.save();
        ChatLog.announceError(Constants.getString("jm.common.tile_display_quality") + ": " + (high ? Constants.getString("jm.common.on") : Constants.getString("jm.common.off")));
        resetTileDisplay();
    }

    private static void resetTileDisplay()
    {
        TileDrawStepCache.instance().invalidateAll();
        RegionImageCache.INSTANCE.clear();
        MiniMap.state().requireRefresh();
        Fullscreen.state().requireRefresh();
    }

    /**
     * Update texture boolean.
     *
     * @param worldDir    the world dir
     * @param mapType     the map type
     * @param highQuality the high quality
     * @return the boolean
     */
    public boolean updateTexture(File worldDir, final MapType mapType, boolean highQuality)
    {
        updateRenderType();
        drawSteps.clear();
        drawSteps.addAll(RegionImageHandler.getTileDrawSteps(worldDir, ulChunk, lrChunk, mapType, zoom, highQuality));
        return drawSteps.size() > 1;
    }

    /**
     * Has texture boolean.
     *
     * @param mapType the map type
     * @return the boolean
     */
    public boolean hasTexture(MapType mapType)
    {
        if (drawSteps.isEmpty())
        {
            return false;
        }
        for (TileDrawStep tileDrawStep : drawSteps)
        {
            if (tileDrawStep.hasTexture(mapType))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Clear.
     */
    public void clear()
    {
        drawSteps.clear();
    }

    private void updateRenderType()
    {
        this.renderType = Journeymap.getClient().getCoreProperties().tileRenderType.get();
        switch (renderType)
        {
            case (4):
            {
                textureFilter = GL11.GL_NEAREST;
                textureWrap = GL12.GL_CLAMP_TO_EDGE;
                debugGlSettings = "GL_NEAREST, GL_CLAMP_TO_EDGE";
                break;
            }
            case (3):
            {
                textureFilter = GL11.GL_NEAREST;
                textureWrap = GL14.GL_MIRRORED_REPEAT;
                debugGlSettings = "GL_NEAREST, GL_MIRRORED_REPEAT";
                break;
            }
            case (2):
            {
                textureFilter = GL11.GL_LINEAR;
                textureWrap = GL12.GL_CLAMP_TO_EDGE;
                debugGlSettings = "GL_LINEAR, GL_CLAMP_TO_EDGE";
                break;
            }
            case (1):
            default:
            {
                textureFilter = GL11.GL_LINEAR;
                textureWrap = GL14.GL_MIRRORED_REPEAT;
                debugGlSettings = "GL_LINEAR, GL_MIRRORED_REPEAT";
            }
        }
    }

    @Override
    public String toString()
    {
        return "Tile [ r" + tileX + ", r" + tileZ + " (zoom " + zoom + ") ]";
    }

    /**
     * Cache key string.
     *
     * @return the string
     */
    public String cacheKey()
    {
        return theCacheKey;
    }

    @Override
    public int hashCode()
    {
        return theHashCode;
    }

    /**
     * Block pixel offset in tile point 2 d.
     *
     * @param x the x
     * @param z the z
     * @return the point 2 d
     */
    public Point2D blockPixelOffsetInTile(double x, double z)
    {
        if (x < ulBlock.x || Math.floor(x) > lrBlock.x || z < ulBlock.y || Math.floor(z) > lrBlock.y)
        {
            throw new RuntimeException("Block " + x + "," + z + " isn't in " + this);
        }

        double localBlockX = ulBlock.x - x;
        if (x < 0)
        {
            localBlockX++;
        }

        double localBlockZ = ulBlock.y - z;
        if (z < 0)
        {
            localBlockZ++;
        }

//		int tileCenterBlockX = lrBlock.x-ulBlock.x;
//		int tileCenterBlockZ = lrBlock.y-ulBlock.y;

        int blockSize = (int) Math.pow(2, zoom);
        double pixelOffsetX = (TILESIZE / 2) + (localBlockX * blockSize) - (blockSize / 2);
        double pixelOffsetZ = (TILESIZE / 2) + (localBlockZ * blockSize) - (blockSize / 2);

        return new Point2D.Double(pixelOffsetX, pixelOffsetZ);
    }

    /**
     * Draw boolean.
     *
     * @param pos      the pos
     * @param offsetX  the offset x
     * @param offsetZ  the offset z
     * @param alpha    the alpha
     * @param gridSpec the grid spec
     * @return the boolean
     */
    boolean draw(final TilePos pos, final double offsetX, final double offsetZ, float alpha, GridSpec gridSpec)
    {
        boolean somethingDrew = false;
        for (TileDrawStep tileDrawStep : drawSteps)
        {
            boolean ok = tileDrawStep.draw(pos, offsetX, offsetZ, alpha, textureFilter, textureWrap, gridSpec);
            if (ok)
            {
                somethingDrew = true;
            }
        }
        return somethingDrew;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        Tile tile = (Tile) o;

        if (tileX != tile.tileX)
        {
            return false;
        }
        if (tileZ != tile.tileZ)
        {
            return false;
        }
        if (zoom != tile.zoom)
        {
            return false;
        }

        return true;
    }
}
