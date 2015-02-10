/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.model;

import net.minecraft.world.ChunkCoordIntPair;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RegionCoord implements Comparable<RegionCoord>
{

    public transient static final int SIZE = 5;
    private transient static final int chunkSqRt = (int) Math.pow(2, SIZE);
    // TODO: worldDir should serialize as a relative path to allow data files to be usable after being moved
    public final File worldDir;
    public final Path dimDir;
    public final int regionX;
    public final int regionZ;
    public final Integer vSlice;
    public final Integer dimension;

    public RegionCoord(File worldDir, int regionX, Integer vSlice, int regionZ, int dimension)
    {
        this.worldDir = worldDir;
        this.dimDir = new File(worldDir, "DIM" + dimension).toPath();
        this.regionX = regionX;
        this.regionZ = regionZ;
        this.vSlice = vSlice;
        this.dimension = dimension;
    }

    public static RegionCoord fromChunkPos(File worldDir, int chunkX, Integer vSlice, int chunkZ, int dimension)
    {
        return new RegionCoord(worldDir, getRegionPos(chunkX), vSlice, getRegionPos(chunkZ), dimension);
    }

    public static int getMinChunkX(int rX)
    {
        return rX << SIZE;
    }

    public static int getMaxChunkX(int rX)
    {
        return getMinChunkX(rX) + (int) Math.pow(2, SIZE) - 1;
    }

    public static int getMinChunkZ(int rZ)
    {
        return rZ << SIZE;
    }

    public static int getMaxChunkZ(int rZ)
    {
        return getMinChunkZ(rZ) + (int) Math.pow(2, SIZE) - 1;
    }

    public static int getRegionPos(int chunkPos)
    {
        return chunkPos >> SIZE;
    }

    public static int toHash(RegionCoord regionCoord)
    {
        return Objects.hash(regionCoord.regionX, regionCoord.regionZ, regionCoord.vSlice, regionCoord.dimension, regionCoord.worldDir);
    }

    public int getXOffset(int chunkX)
    {
        if (chunkX >> SIZE != regionX)
        {
            throw new IllegalArgumentException("chunkX " + chunkX + " out of bounds for regionX " + regionX); //$NON-NLS-1$ //$NON-NLS-2$
        }
        int offset = ((chunkX % chunkSqRt) * 16);
        if (offset < 0)
        {
            offset = (chunkSqRt * 16) + offset;
        }
        return offset;
    }

    public int getZOffset(int chunkZ)
    {
        if (getRegionPos(chunkZ) != regionZ)
        {
            throw new IllegalArgumentException("chunkZ " + chunkZ + " out of bounds for regionZ " + regionZ); //$NON-NLS-1$ //$NON-NLS-2$
        }
        int offset = ((chunkZ % chunkSqRt) * 16);
        if (offset < 0)
        {
            offset = (chunkSqRt * 16) + offset;
        }
        return offset;
    }

    public int getMinChunkX()
    {
        return getMinChunkX(regionX);
    }

    public int getMaxChunkX()
    {
        return getMaxChunkX(regionX);
    }

    public int getMinChunkZ()
    {
        return getMinChunkZ(regionZ);
    }

    public int getMaxChunkZ()
    {
        return getMaxChunkZ(regionZ);
    }

    public ChunkCoord getMinChunkCoord()
    {
        return ChunkCoord.fromChunkPos(worldDir, getMinChunkX(), vSlice, getMinChunkZ(), dimension);
    }

    public ChunkCoord getMaxChunkCoord()
    {
        return ChunkCoord.fromChunkPos(worldDir, getMaxChunkX(), vSlice, getMaxChunkZ(), dimension);
    }

    public List<ChunkCoordIntPair> getChunkCoordsInRegion()
    {
        final List<ChunkCoordIntPair> list = new ArrayList<ChunkCoordIntPair>(1024);
        final ChunkCoord min = getMinChunkCoord();
        final ChunkCoord max = getMaxChunkCoord();

        for (int x = min.chunkX; x <= max.chunkX; x++)
        {
            for (int z = min.chunkZ; z <= max.chunkZ; z++)
            {
                list.add(new ChunkCoordIntPair(x, z));
            }
        }

        return list;
    }

    public Boolean isUnderground()
    {
        return vSlice != null ? vSlice != -1 : false;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("RegionCoord ["); //$NON-NLS-1$
        builder.append(regionX);
        builder.append(","); //$NON-NLS-1$
        builder.append(regionZ);
        builder.append("]"); //$NON-NLS-1$
        return builder.toString();
    }

    public Integer getVerticalSlice()
    {
        return vSlice;
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

        RegionCoord that = (RegionCoord) o;

        if (regionX != that.regionX)
        {
            return false;
        }
        if (regionZ != that.regionZ)
        {
            return false;
        }
        if (!dimension.equals(that.dimension))
        {
            return false;
        }
        if (vSlice != null ? !vSlice.equals(that.vSlice) : that.vSlice != null)
        {
            return false;
        }
        if (!worldDir.equals(that.worldDir))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return toHash(this);
    }

    @Override
    public int compareTo(RegionCoord o)
    {
        int cx = Double.compare(this.regionX, o.regionX);
        return (cx == 0) ? Double.compare(this.regionZ, o.regionZ) : cx;
    }
}
