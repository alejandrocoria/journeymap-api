/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.model;

import com.google.common.cache.Cache;
import net.techbrew.journeymap.data.DataCache;

import java.io.File;
import java.util.Objects;

public class ChunkCoord
{

    private static final Cache<Integer, ChunkCoord> cache = DataCache.instance().getChunkCoords();
    // TODO: worldDir should serialize as a relative path to allow data files to be usable after being moved
    public final File worldDir;
    public final int chunkX;
    public final int chunkZ;
    public final Integer vSlice;
    public final int dimension;
    private RegionCoord rCoord = null;

    private ChunkCoord(File worldDir, int chunkX, Integer vSlice, int chunkZ, int dimension)
    {
        this.worldDir = worldDir;
        this.chunkX = chunkX;
        if (vSlice != null && vSlice > 16)
        {
            throw new IllegalArgumentException("Need the vSlice, not a y"); //$NON-NLS-1$
        }
        this.vSlice = vSlice;
        this.chunkZ = chunkZ;
        this.dimension = dimension;
    }

    public static ChunkCoord fromChunkMD(File worldDir, ChunkMD chunkMd, Integer vSlice, int dimension)
    {
        return ChunkCoord.fromChunkPos(worldDir, chunkMd.getCoord().chunkXPos, vSlice, chunkMd.getCoord().chunkZPos, dimension);
    }

    public static ChunkCoord fromChunkPos(final File worldDir, final int chunkX, final Integer vSlice, final int chunkZ, final int dimension)
    {
        // There's no real need to synchronize this, it's harmless if there are occasional duplicate puts.  It's primarily
        // just used to reduce heap thrash.
        ChunkCoord chunkCoord = cache.getIfPresent(toHash(worldDir, chunkX, vSlice, chunkZ, dimension));
        if (chunkCoord == null)
        {
            chunkCoord = new ChunkCoord(worldDir, chunkX, vSlice, chunkZ, dimension);
            cache.put(chunkCoord.hashCode(), chunkCoord);
        }
        return chunkCoord;
    }

    public static int toHash(File worldDir, int chunkX, Integer vSlice, int chunkZ, int dimension)
    {
        return Objects.hash(worldDir, chunkX, vSlice, chunkZ, dimension);
    }

    public RegionCoord getRegionCoord()
    {
        if (rCoord == null)
        {
            rCoord = RegionCoord.fromChunkPos(worldDir, chunkX, vSlice, chunkZ, dimension);
        }
        return rCoord;
    }

    public Boolean isUnderground()
    {
        return vSlice != null ? vSlice != -1 : false;
    }

    public int getVerticalSlice()
    {
        if (vSlice == null)
        {
            return -1;
        }
        else
        {
            return vSlice;
        }
    }

    @Override
    public int hashCode()
    {
        return toHash(worldDir, chunkX, vSlice, chunkZ, dimension);
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
        ChunkCoord other = (ChunkCoord) obj;
        if (dimension != other.dimension)
        {
            return false;
        }
        if (chunkX != other.chunkX)
        {
            return false;
        }
        if (chunkZ != other.chunkZ)
        {
            return false;
        }
        if (other.getVerticalSlice() != getVerticalSlice())
        {
            return false;
        }
        if (worldDir == null)
        {
            if (other.worldDir != null)
            {
                return false;
            }
        }
        else if (!worldDir.equals(other.worldDir))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        //		builder.append("ChunkCoord [worldDir=");
//		builder.append(worldDir.getName());
//		builder.append(", chunkX=");
//		builder.append(chunkX);
//		builder.append(", chunkZ=");
//		builder.append(chunkZ);
//		builder.append(", vSlice=");
//		builder.append(vSlice);
//		builder.append(", worldProviderType=");
//		builder.append(worldProviderType);
//		builder.append("]");
        return "ChunkCoord [" + chunkX + "," + chunkZ + "]";
    }
}
