/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.render.map;

import com.google.common.base.Objects;
import net.minecraft.world.ChunkCoordIntPair;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.Constants.MapType;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.io.RegionImageHandler;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
    final List<TileDrawStep> drawSteps = new ArrayList<TileDrawStep>();

    private final Logger logger = JourneyMap.getLogger();
    private final boolean debug = logger.isTraceEnabled();
    long lastImageTime = 0;
    Integer lastVSlice;
    MapType lastMapType;
    //    Future<DelayedTexture> futureTex;
//    TextureImpl textureImpl;
    int renderType = 0;
    int textureFilter = 0;
    int textureWrap = 0;

    public Tile(final File worldDir, final MapType mapType, final int tileX, final int tileZ, final int zoom, final int dimension)
    {
        //System.out.println("NEW TILE");
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
        updateRenderType();
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

    public boolean updateTexture(final TilePos pos, final MapType mapType, Constants.MapTileQuality quality, final Integer vSlice)
    {
        boolean forceReset = (lastMapType != mapType) || !Objects.equal(lastVSlice, vSlice);
        lastMapType = mapType;
        lastVSlice = vSlice;

        if (forceReset)
        {
            drawSteps.clear();
        }

        updateRenderType();

        if (drawSteps.isEmpty())
        {
            this.drawSteps.addAll(RegionImageHandler.getTileDrawSteps(worldDir, ulChunk, lrChunk, mapType, zoom, quality, vSlice, dimension));
        }
        else
        {
            for (TileDrawStep tileDrawStep : drawSteps)
            {
                tileDrawStep.refreshIfDirty(lastImageTime);
            }
        }
        lastImageTime = System.currentTimeMillis();

        if (drawSteps.size() > 1)
        {
            return true;
        }

        return true;
    }

    public boolean hasTexture()
    {
        for (TileDrawStep tileDrawStep : drawSteps)
        {
            if (tileDrawStep.hasTexture())
            {
                return true;
            }
        }
        return false;
    }

    public void clear()
    {
        drawSteps.clear();
    }

    private void updateRenderType()
    {
        this.renderType = JourneyMap.getCoreProperties().tileRenderType.get();
        switch (renderType)
        {
            case (4):
            {
                textureFilter = GL11.GL_NEAREST;
                textureWrap = GL12.GL_CLAMP_TO_EDGE;
                break;
            }
            case (3):
            {
                textureFilter = GL11.GL_NEAREST;
                textureWrap = GL14.GL_MIRRORED_REPEAT;
                break;
            }
            case (2):
            {
                textureFilter = GL11.GL_LINEAR;
                textureWrap = GL12.GL_CLAMP_TO_EDGE;
                break;
            }
            case (1):
            default:
            {
                textureFilter = GL11.GL_LINEAR;
                textureWrap = GL14.GL_MIRRORED_REPEAT;
            }
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

    void draw(final TilePos pos, final double offsetX, final double offsetZ, float alpha)
    {
        boolean showGrid = JourneyMap.getFullMapProperties().showGrid.get();

        for (TileDrawStep tileDrawStep : drawSteps)
        {
            //tileDrawStep.draw(pos, offsetX, offsetZ, 1f, GL11.GL_LINEAR, GL12.GL_CLAMP_TO_EDGE, showGrid);
            //tileDrawStep.draw(pos, offsetX, offsetZ, 1f, GL11.GL_NEAREST, GL12.GL_CLAMP_TO_EDGE, showGrid);

            tileDrawStep.draw(pos, offsetX, offsetZ, alpha, textureFilter, textureWrap, showGrid); // TODO
        }
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
}
