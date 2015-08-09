/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.model;

import com.google.common.cache.Cache;
import journeymap.client.data.DataCache;

import java.io.File;
import java.util.Objects;

public class ChunkCoord
{

    private static final Cache<String, ChunkCoord> cache = DataCache.instance().getChunkCoords();
    // TODO: worldDir should serialize as a relative path to allow data files to be usable after being moved
    public final File worldDir;
    public final int chunkX;
    public final int chunkZ;
    public final MapType mapType;
    private final int theHashCode;
    private final String theCacheKey;

    private ChunkCoord(File worldDir, final MapType mapType, int chunkX, int chunkZ)
    {
        this.worldDir = worldDir;
        this.mapType = mapType;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.theCacheKey = toCacheKey(worldDir, mapType, chunkX, chunkZ);
        this.theHashCode = theCacheKey.hashCode();
    }

    public static ChunkCoord fromChunkMD(File worldDir, MapType mapType, ChunkMD chunkMd)
    {
        return ChunkCoord.fromChunkPos(worldDir, mapType, chunkMd.getCoord().chunkXPos, chunkMd.getCoord().chunkZPos);
    }

    public static ChunkCoord fromChunkPos(final File worldDir, final MapType mapType, final int chunkX, final int chunkZ)
    {
        String key = toCacheKey(worldDir, mapType, chunkX, chunkZ);
        ChunkCoord chunkCoord = cache.getIfPresent(key);
        if (chunkCoord == null || chunkCoord.chunkX != chunkX || chunkCoord.chunkZ != chunkZ || !Objects.equals(chunkCoord.mapType, mapType))
        {
            //JourneyMap.getLogger().info("ChunkCoord from cache had hash collision: " + chunkCoord + " vs " + mapType + ", chunkX:" + chunkX + ", chunkZ:" + chunkZ);
            chunkCoord = new ChunkCoord(worldDir, mapType, chunkX, chunkZ);
            cache.put(key, chunkCoord);
        }
        return chunkCoord;
    }

    public static String toCacheKey(File worldDir, final MapType mapType, int chunkX, int chunkZ)
    {
        return "" + chunkX + "," + chunkZ + mapType.toCacheKey() + worldDir.getName();
    }


    public RegionCoord getRegionCoord()
    {
        return RegionCoord.fromChunkPos(worldDir, mapType, chunkX, chunkZ);
    }

    public Boolean isUnderground()
    {
        return mapType.isUnderground();
    }

    public Integer getVerticalSlice()
    {
        return mapType.vSlice;
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

        ChunkCoord that = (ChunkCoord) o;

        if (chunkX != that.chunkX)
        {
            return false;
        }
        if (chunkZ != that.chunkZ)
        {
            return false;
        }
        if (!mapType.equals(that.mapType))
        {
            return false;
        }
        if (!worldDir.equals(that.worldDir))
        {
            return false;
        }

        return true;
    }

    public String cacheKey()
    {
        return theCacheKey;
    }

    @Override
    public int hashCode()
    {
        return theHashCode;
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
