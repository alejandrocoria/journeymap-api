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
    public final MapType mapType;
    private RegionCoord rCoord = null;

    private ChunkCoord(File worldDir, int chunkX, Integer vSlice, int chunkZ, int dimension)
    {
        this.worldDir = worldDir;
        this.chunkX = chunkX;
        this.mapType = MapType.from(vSlice, dimension);
        this.chunkZ = chunkZ;
    }

    public static ChunkCoord fromChunkMD(File worldDir, MapType mapType, ChunkMD chunkMd)
    {
        return ChunkCoord.fromChunkPos(worldDir, mapType, chunkMd.getCoord().chunkXPos, chunkMd.getCoord().chunkZPos);
    }

    public static ChunkCoord fromChunkPos(final File worldDir, final MapType mapType, final int chunkX, final int chunkZ)
    {
        // There's no real need to synchronize this, it's harmless if there are occasional duplicate puts.  It's primarily
        // just used to reduce heap thrash.
        ChunkCoord chunkCoord = cache.getIfPresent(toHash(worldDir, mapType, chunkX, chunkZ));
        if (chunkCoord == null)
        {
            chunkCoord = new ChunkCoord(worldDir, mapType.vSlice, mapType.dimension, chunkX, chunkZ);
            cache.put(chunkCoord.hashCode(), chunkCoord);
        }
        if (!chunkCoord.mapType.vSlice.equals(mapType.vSlice))
        {
            throw new IllegalStateException("ChunkCoord vSlice doesn't match");
        }
        return chunkCoord;
    }

    public static int toHash(File worldDir, final MapType mapType, int chunkX, int chunkZ)
    {
        return Objects.hash(worldDir, mapType, chunkX, chunkZ);
    }

    public RegionCoord getRegionCoord()
    {
        if (rCoord == null)
        {
            rCoord = RegionCoord.fromChunkPos(worldDir, mapType, chunkX, chunkZ);
        }
        return rCoord;
    }

    public Boolean isUnderground()
    {
        return mapType.isUnderground();
    }

    public int getVerticalSlice()
    {
        return isUnderground() ? mapType.vSlice : -1;
    }

    @Override
    public int hashCode()
    {
        return toHash(worldDir, mapType, chunkX, chunkZ);
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
        if (!mapType.equals(other.mapType))
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
