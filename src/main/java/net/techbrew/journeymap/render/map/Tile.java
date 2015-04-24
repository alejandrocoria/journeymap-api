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
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.io.RegionImageHandler;
import net.techbrew.journeymap.model.GridSpec;
import net.techbrew.journeymap.model.MapType;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;

public class Tile
{
    public final static int TILESIZE = 512;
    public final static int LOAD_RADIUS = (int) (TILESIZE * 1.5);

    final int zoom;
    final int tileX;
    final int tileZ;
    final ChunkCoordIntPair ulChunk;
    final ChunkCoordIntPair lrChunk;
    final Point ulBlock;
    final Point lrBlock;
    final ArrayList<TileDrawStep> drawSteps = new ArrayList<TileDrawStep>();

    private final Logger logger = JourneyMap.getLogger();
    private final boolean debug = logger.isTraceEnabled();

    int renderType = 0;
    int textureFilter = 0;
    int textureWrap = 0;

    private Tile(final int tileX, final int tileZ, final int zoom)
    {
        this.tileX = tileX;
        this.tileZ = tileZ;
        this.zoom = zoom;
        final int distance = 32 / (int) Math.pow(2, zoom);
        ulChunk = new ChunkCoordIntPair(tileX * distance, tileZ * distance);
        lrChunk = new ChunkCoordIntPair(ulChunk.chunkXPos + distance - 1, ulChunk.chunkZPos + distance - 1);
        ulBlock = new Point(ulChunk.chunkXPos * 16, ulChunk.chunkZPos * 16);
        lrBlock = new Point((lrChunk.chunkXPos * 16) + 15, (lrChunk.chunkZPos * 16) + 15);
        updateRenderType();
    }

    public static Tile create(final int tileX, final int tileZ, final int zoom, File worldDir, final MapType mapType, boolean highQuality)
    {
        Tile tile = new Tile(tileX, tileZ, zoom);
        tile.updateTexture(worldDir, mapType, highQuality);
        return tile;
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

    public static int toHashCode(final int tileX, final int tileZ, final int zoom)
    {
        return Objects.hashCode(tileX, tileZ, zoom);
    }

    public boolean updateTexture(File worldDir, final MapType mapType, boolean highQuality)
    {
        updateRenderType();
        drawSteps.clear();
        drawSteps.addAll(RegionImageHandler.getTileDrawSteps(worldDir, ulChunk, lrChunk, mapType, zoom, highQuality));
        return drawSteps.size() > 1;
    }

    public boolean hasTexture(MapType mapType)
    {
        if (drawSteps.isEmpty())
        {
            return false;
        }
        for (TileDrawStep tileDrawStep : drawSteps)
        {
            if (!tileDrawStep.hasTexture(mapType))
            {
                return false;
            }
        }
        return true;
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
        return "Tile [ r" + tileX + ", r" + tileZ + " (zoom " + zoom + ") ]";
    }

    @Override
    public int hashCode()
    {
        return toHashCode(tileX, tileZ, zoom);
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
