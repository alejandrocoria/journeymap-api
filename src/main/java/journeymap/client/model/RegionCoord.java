/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.model;

import com.google.common.cache.Cache;
import journeymap.client.data.DataCache;
import journeymap.client.io.nbt.RegionLoader;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Region coord.
 */
public class RegionCoord implements Comparable<RegionCoord>
{
    /**
     * The constant SIZE.
     */
    public transient static final int SIZE = 5;
    private transient static final int chunkSqRt = (int) Math.pow(2, SIZE);
    /**
     * The World dir.
     */
// TODO: worldDir should serialize as a relative path to allow data files to be usable after being moved
    public final File worldDir;
    /**
     * The Dim dir.
     */
    public final Path dimDir;
    /**
     * The Region x.
     */
    public final int regionX;
    /**
     * The Region z.
     */
    public final int regionZ;
    /**
     * The Dimension.
     */
    public final int dimension;
    private final int theHashCode;
    private final String theCacheKey;

    /**
     * Instantiates a new Region coord.
     *
     * @param worldDir  the world dir
     * @param regionX   the region x
     * @param regionZ   the region z
     * @param dimension the dimension
     */
    public RegionCoord(File worldDir, int regionX, int regionZ, int dimension)
    {
        this.worldDir = worldDir;
        this.dimDir = getDimPath(worldDir, dimension);
        this.regionX = regionX;
        this.regionZ = regionZ;
        this.dimension = dimension;
        this.theCacheKey = toCacheKey(dimDir, regionX, regionZ);
        this.theHashCode = theCacheKey.hashCode();
    }

    /**
     * From chunk pos region coord.
     *
     * @param worldDir the world dir
     * @param mapType  the map type
     * @param chunkX   the chunk x
     * @param chunkZ   the chunk z
     * @return the region coord
     */
    public static RegionCoord fromChunkPos(File worldDir, MapType mapType, int chunkX, int chunkZ)
    {
        final int regionX = getRegionPos(chunkX);
        final int regionZ = getRegionPos(chunkZ);

        return fromRegionPos(worldDir, regionX, regionZ, mapType.dimension);
    }

    /**
     * From region pos region coord.
     *
     * @param worldDir  the world dir
     * @param regionX   the region x
     * @param regionZ   the region z
     * @param dimension the dimension
     * @return the region coord
     */
    public static RegionCoord fromRegionPos(File worldDir, int regionX, int regionZ, int dimension)
    {
        // The cache is primarily just used to reduce heap thrash.  Hashing on x,z has a lot of collisions,
        // unfortunately, so there's no reliable key.  If there's a collision, we put in a new one.
        Cache<String, RegionCoord> cache = DataCache.INSTANCE.getRegionCoords();
        RegionCoord regionCoord = cache.getIfPresent(toCacheKey(getDimPath(worldDir, dimension), regionX, regionZ));
        if (regionCoord == null || regionX != regionCoord.regionX || regionZ != regionCoord.regionZ || dimension != regionCoord.dimension)
        {
            regionCoord = new RegionCoord(worldDir, regionX, regionZ, dimension);
            cache.put(regionCoord.theCacheKey, regionCoord);
        }
        return regionCoord;
    }

    /**
     * Gets dim path.
     *
     * @param worldDir  the world dir
     * @param dimension the dimension
     * @return the dim path
     */
    public static Path getDimPath(File worldDir, int dimension)
    {
        return new File(worldDir, "DIM" + dimension).toPath();
    }

    /**
     * Gets min chunk x.
     *
     * @param rX the r x
     * @return the min chunk x
     */
    public static int getMinChunkX(int rX)
    {
        return rX << SIZE;
    }

    /**
     * Gets max chunk x.
     *
     * @param rX the r x
     * @return the max chunk x
     */
    public static int getMaxChunkX(int rX)
    {
        return getMinChunkX(rX) + (int) Math.pow(2, SIZE) - 1;
    }

    /**
     * Gets min chunk z.
     *
     * @param rZ the r z
     * @return the min chunk z
     */
    public static int getMinChunkZ(int rZ)
    {
        return rZ << SIZE;
    }

    /**
     * Gets max chunk z.
     *
     * @param rZ the r z
     * @return the max chunk z
     */
    public static int getMaxChunkZ(int rZ)
    {
        return getMinChunkZ(rZ) + (int) Math.pow(2, SIZE) - 1;
    }

    /**
     * Gets region pos.
     *
     * @param chunkPos the chunk pos
     * @return the region pos
     */
    public static int getRegionPos(int chunkPos)
    {
        return chunkPos >> SIZE;
    }

    /**
     * To cache key string.
     *
     * @param dimDir  the dim dir
     * @param regionX the region x
     * @param regionZ the region z
     * @return the string
     */
    public static String toCacheKey(Path dimDir, int regionX, int regionZ)
    {
        return regionX + dimDir.toString() + regionZ;
    }

    /**
     * Exists boolean.
     *
     * @return the boolean
     */
    public boolean exists()
    {
        return RegionLoader.getRegionFile(FMLClientHandler.instance().getClient(), getMinChunkX(), getMinChunkZ()).exists();
    }

    /**
     * Gets x offset.
     *
     * @param chunkX the chunk x
     * @return the x offset
     */
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

    /**
     * Gets z offset.
     *
     * @param chunkZ the chunk z
     * @return the z offset
     */
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

    /**
     * Gets min chunk x.
     *
     * @return the min chunk x
     */
    public int getMinChunkX()
    {
        return getMinChunkX(regionX);
    }

    /**
     * Gets max chunk x.
     *
     * @return the max chunk x
     */
    public int getMaxChunkX()
    {
        return getMaxChunkX(regionX);
    }

    /**
     * Gets min chunk z.
     *
     * @return the min chunk z
     */
    public int getMinChunkZ()
    {
        return getMinChunkZ(regionZ);
    }

    /**
     * Gets max chunk z.
     *
     * @return the max chunk z
     */
    public int getMaxChunkZ()
    {
        return getMaxChunkZ(regionZ);
    }

    /**
     * Gets min chunk coord.
     *
     * @return the min chunk coord
     */
    public ChunkPos getMinChunkCoord()
    {
        return new ChunkPos(getMinChunkX(), getMinChunkZ());
    }

    /**
     * Gets max chunk coord.
     *
     * @return the max chunk coord
     */
    public ChunkPos getMaxChunkCoord()
    {
        return new ChunkPos(getMaxChunkX(), getMaxChunkZ());
    }

    /**
     * Gets chunk coords in region.
     *
     * @return the chunk coords in region
     */
    public List<ChunkPos> getChunkCoordsInRegion()
    {
        final List<ChunkPos> list = new ArrayList<ChunkPos>(1024);
        final ChunkPos min = getMinChunkCoord();
        final ChunkPos max = getMaxChunkCoord();

        for (int x = min.chunkXPos; x <= max.chunkXPos; x++)
        {
            for (int z = min.chunkZPos; z <= max.chunkZPos; z++)
            {
                list.add(new ChunkPos(x, z));
            }
        }

        return list;
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

        if (dimension != that.dimension)
        {
            return false;
        }
        if (regionX != that.regionX)
        {
            return false;
        }
        if (regionZ != that.regionZ)
        {
            return false;
        }
        if (!dimDir.equals(that.dimDir))
        {
            return false;
        }
        if (!worldDir.equals(that.worldDir))
        {
            return false;
        }

        return true;
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

    @Override
    public int compareTo(RegionCoord o)
    {
        int cx = Double.compare(this.regionX, o.regionX);
        return (cx == 0) ? Double.compare(this.regionZ, o.regionZ) : cx;
    }
}
