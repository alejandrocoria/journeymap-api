/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.render.overlay;

import net.minecraft.world.ChunkCoordIntPair;
import net.techbrew.journeymap.Constants.MapType;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.properties.InGameMapProperties;
import net.techbrew.journeymap.render.texture.DelayedTexture;
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.render.texture.TextureImpl;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.Future;

public class Tile
{

    public final static int TILESIZE = 512;
    public final static int LOAD_RADIUS = (int) (TILESIZE * 1.5);

    final int dimension;
    final int zoom;
    final int tileX;
    final int tileZ;
    final File worldDir;
    final ChunkCoordIntPair ulChunk;
    final ChunkCoordIntPair lrChunk;
    final Point ulBlock;
    final Point lrBlock;
    private final Logger logger = JourneyMap.getLogger();
    private final boolean debug = logger.isTraceEnabled();
    long lastImageTime = 0;
    Integer lastVSlice;
    MapType lastMapType;
    Future<DelayedTexture> futureTex;
    TextureImpl textureImpl;

    public Tile(final File worldDir, final MapType mapType, final int tileX, final int tileZ, final int zoom, final int dimension)
    {
        this.worldDir = worldDir;
        this.lastMapType = mapType;
        this.tileX = tileX;
        this.tileZ = tileZ;
        this.zoom = zoom;
        this.dimension = dimension;
        final int distance = 32 / (int) Math.pow(2, zoom);
        ulChunk = new ChunkCoordIntPair(tileX * distance, tileZ * distance);
        lrChunk = new ChunkCoordIntPair(ulChunk.chunkXPos + distance - 1, ulChunk.chunkZPos + distance - 1);
        ulBlock = new Point(ulChunk.chunkXPos * 16, ulChunk.chunkZPos * 16);
        lrBlock = new Point((lrChunk.chunkXPos * 16) + 15, (lrChunk.chunkZPos * 16) + 15);
    }

    public static int blockPosToTile(int b, int zoom)
    {
        int tile = b >> (9 - zoom);  // (2 pow 9 = 512)
        return tile;
    }

    public static int tileToBlock(int t, int zoom)
    {
        return t << (9 - zoom);
    }

    public static int toHashCode(final int tileX, final int tileZ, final int zoom, final int dimension)
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + tileX;
        result = prime * result + tileZ;
        result = prime * result + zoom;
        result = prime * result + dimension;
        return result;
    }

    public boolean updateTexture(final TilePos pos, final MapType mapType, final Integer vSlice, InGameMapProperties mapProperties)
    {
        boolean forceReset = (lastMapType != mapType || lastVSlice != vSlice);

        if (futureTex != null && forceReset)
        {
            futureTex.cancel(true);
            futureTex = null;
        }

        if (futureTex != null)
        {
            return false;
        }

        lastMapType = mapType;
        lastVSlice = vSlice;

        Integer glId = null;
        BufferedImage image = null;
        if (textureImpl != null)
        {
            // Reuse existing buffered image and glId
            glId = textureImpl.getGlTextureId();
            image = textureImpl.getImage();
        }
        boolean showGrid = JourneyMap.getFullMapProperties().showGrid.get();

        float alpha = mapProperties.terrainAlpha.get() / 255f;
        futureTex = TextureCache.instance().prepareImage(glId, image, worldDir, ulChunk, lrChunk, mapType, vSlice, dimension, true, TILESIZE, TILESIZE, showGrid, alpha);

        return true;
    }

    public boolean hasTexture()
    {
        if (textureImpl != null)
        {
            return true;
        }
        if (futureTex != null && futureTex.isDone())
        {
            try
            {
                if (futureTex.get() == null)
                {
                    futureTex = null;
                    lastImageTime = 0;
                    return false;
                }
                else
                {
                    return true;
                }
            }
            catch (Throwable e)
            {
                logger.error(LogFormatter.toString(e));
            }
        }
        return false;
    }

    public TextureImpl getTexture()
    {

        if (futureTex != null && futureTex.isDone())
        {
            try
            {
                DelayedTexture dt = futureTex.get();
                if (dt != null)
                {
                    TextureImpl texture = dt.bindTexture();
                    if (textureImpl == null)
                    { // new
                        textureImpl = texture;
                    }
                }
                futureTex = null;
                //lastImageTime = new Date().getTime();
                //logger.info("FutureTex bound for " + this);
            }
            catch (Throwable e)
            {
                logger.error(LogFormatter.toString(e));
            }
        }

        return textureImpl;
    }

    public void clear()
    {
        if (textureImpl != null)
        {
            textureImpl.deleteTexture();
            textureImpl = null;
        }
    }

    @Override
    public String toString()
    {
        return "Tile [ " + tileX + "," + tileZ + " (zoom " + zoom + ") ]";
    }

    @Override
    public int hashCode()
    {
        return toHashCode(tileX, tileZ, zoom, dimension);
    }

    private String blockBounds()
    {
        return ulBlock.x + "," + ulBlock.y + " - " + lrBlock.x + "," + lrBlock.y;
    }

    private int tileToBlock(int t)
    {
        return t << (9 - zoom);
    }

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

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        Tile other = (Tile) obj;
        if (tileX != other.tileX)
        {
            return false;
        }
        if (tileZ != other.tileZ)
        {
            return false;
        }
        if (zoom != other.zoom)
        {
            return false;
        }
        if (dimension != other.dimension)
        {
            return false;
        }
        if (!worldDir.equals(other.worldDir))
        {
            return false;
        }
        return true;
    }

    @Override
    protected void finalize()
    {

        try
        {
            if (JourneyMap.getInstance().isMapping())
            {
                if (textureImpl != null)
                {
                    logger.warn("Tile wasn't cleared before finalize() called: " + this);
                    clear();
                }
            }
        }
        catch (NullPointerException e)
        {
            // Forced shutdown, JM instance already GC'd
        }
        catch (Throwable t)
        {
            logger.error(t.getMessage());
        }
    }
}
