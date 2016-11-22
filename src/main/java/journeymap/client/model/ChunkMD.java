/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.model;

import com.google.common.cache.CacheLoader;
import journeymap.client.data.DataCache;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.io.nbt.ChunkLoader;
import journeymap.common.Journeymap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.HashMap;

/**
 * ChunkMD is a MetaData wrapper for a Chunk.
 *
 * @author mwoodman
 */
public class ChunkMD
{
    public static final String PROP_IS_SLIME_CHUNK = "isSlimeChunk";
    public static final String PROP_LOADED = "loaded";
    public static final String PROP_LAST_RENDERED = "lastRendered";
    final static DataCache dataCache = DataCache.INSTANCE; // TODO REMOVE
    private final WeakReference<Chunk> chunkReference;
    private final ChunkPos coord;
    private final HashMap<String, Serializable> properties = new HashMap<String, Serializable>();
    private Chunk retainedChunk;

    public ChunkMD(Chunk chunk)
    {
        this(chunk, false);
    }

    public ChunkMD(Chunk chunk, boolean forceRetain)
    {
        if (chunk == null)
        {
            throw new IllegalArgumentException("Chunk can't be null");
        }
        this.coord = new ChunkPos(chunk.xPosition, chunk.zPosition); // avoid GC issue holding onto chunk's coord ref

        // Set load time
        setProperty(PROP_LOADED, System.currentTimeMillis());

        // https://github.com/OpenMods/OpenBlocks/blob/master/src/main/java/openblocks/common/item/ItemSlimalyzer.java#L44
        properties.put(PROP_IS_SLIME_CHUNK, chunk.getRandomWithSeed(987234911L).nextInt(10) == 0);

        this.chunkReference = new WeakReference<Chunk>(chunk);
        if (forceRetain)
        {
            retainedChunk = chunk;
        }
    }

    public IBlockState getBlockState(int localX, int y, int localZ)
    {
        if (localX < 0 || localX > 15 || localZ < 0 || localZ > 15)
        {
            Journeymap.getLogger().warn("Expected local coords, got global coords");
        }
        return getBlockState(new BlockPos(toWorldX(localX), y, toWorldZ(localZ)));
    }

    public IBlockState getBlockState(BlockPos blockPos)
    {
        return ForgeHelper.INSTANCE.getIBlockAccess().getBlockState(blockPos);
    }

    public BlockMD getBlockMD(BlockPos blockPos)
    {
        return BlockMD.getBlockMD(this, blockPos);
    }

    /**
     * Added to do a safety check on the world height value
     */
    public int getSavedLightValue(int localX, int y, int localZ)
    {
        return ForgeHelper.INSTANCE.getSavedLightValue(getChunk(), getBlockPos(localX, y, localZ));
    }

    /**
     * Get the top block ignoring transparent roof blocks, air. etc.
     */
    public final BlockMD getTopBlockMD(final int localX, int y, final int localZ)
    {
        BlockMD topBlockMd = null;

        do
        {
            topBlockMd = BlockMD.getBlockMD(this, getBlockPos(localX, y, localZ));

            // Null check
            if (topBlockMd == null)
            {
                break;
            }

            if (topBlockMd.isTransparentRoof() || topBlockMd.isAir() || topBlockMd.getAlpha() == 0)
            {
                y--;
            }
            else
            {
                break;
            }
        } while (y >= 0);

        return topBlockMd;
    }

    /**
     * Finds the top sky-obscuring block in the column.
     */
    public int ceiling(final int localX, final int localZ)
    {
        final int chunkHeight = getPrecipitationHeight(getBlockPos(localX, 0, localZ));
        int y = chunkHeight;
        BlockPos blockPos = null;
        try
        {
            Chunk chunk = getChunk();
            BlockMD blockMD;
            while (y >= 0)
            {
                blockPos = getBlockPos(localX, y, localZ);
                blockMD = getBlockMD(blockPos);

                if (blockMD == null)
                {
                    y--;
                }
                else if (blockMD.isAir() || blockMD.hasFlag(BlockMD.Flag.OpenToSky))
                {
                    y--;
                }
                else if (ForgeHelper.INSTANCE.canBlockSeeTheSky(chunk, blockPos))
                {
                    y--;
                }
                else
                {
                    break;
                }
            }
        }
        catch (Exception e)
        {
            Journeymap.getLogger().warn(e + " at " + blockPos, e);
        }

        return Math.max(0, y);
    }

    public boolean hasChunk()
    {
        Chunk chunk = chunkReference.get();
        return chunk != null && !(chunk instanceof EmptyChunk) && chunk.isLoaded();
    }

    public int getHeight(BlockPos blockPos)
    {
        return getChunk().getHeight(blockPos);
    }

    public int getPrecipitationHeight(int localX, int localZ)
    {
        return getChunk().getPrecipitationHeight(getBlockPos(localX, 0, localZ)).getY();
    }

    public int getPrecipitationHeight(BlockPos blockPos)
    {
        return getChunk().getPrecipitationHeight(blockPos).getY();
    }

    public int getLightOpacity(BlockMD blockMD, int localX, int y, int localZ)
    {
        return ForgeHelper.INSTANCE.getLightOpacity(blockMD, getBlockPos(localX, y, localZ));
    }

    public Serializable getProperty(String name)
    {
        return properties.get(name);
    }

    public Serializable getProperty(String name, Serializable defaultValue)
    {
        Serializable currentValue = getProperty(name);
        if (currentValue == null)
        {
            setProperty(name, defaultValue);
            currentValue = defaultValue;
        }
        return currentValue;
    }

    public Serializable setProperty(String name, Serializable value)
    {
        return properties.put(name, value);
    }

    @Override
    public int hashCode()
    {
        return getCoord().hashCode();
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
        ChunkMD other = (ChunkMD) obj;
        return getCoord().equals(other.getCoord());
    }

    public Chunk getChunk()
    {
        Chunk chunk = chunkReference.get();
        if (chunk == null)
        {
            throw new ChunkMissingException(getCoord());
        }
        return chunk;
    }

    public World getWorld()
    {
        return ForgeHelper.INSTANCE.getWorld();
    }

    public int getWorldActualHeight()
    {
        // add one to get above the top block for some worlds that paste in to 256
        return getWorld().getActualHeight() + 1;
    }

    public Boolean getHasNoSky()
    {
        return ForgeHelper.INSTANCE.hasNoSky(getWorld());
    }

    public boolean canBlockSeeTheSky(int localX, int y, int localZ)
    {
        return ForgeHelper.INSTANCE.canBlockSeeTheSky(getChunk(), getBlockPos(localX, y, localZ));
    }

    public ChunkPos getCoord()
    {
        return coord;
    }

    public boolean isSlimeChunk()
    {
        return (Boolean) getProperty(PROP_IS_SLIME_CHUNK, Boolean.FALSE);
    }

    public long getLoaded()
    {
        return (Long) getProperty(PROP_LOADED, 0L);
    }

    public long getLastRendered()
    {
        return (Long) getProperty(PROP_LAST_RENDERED, 0L);
    }

    public long setRendered()
    {
        long now = System.currentTimeMillis();
        setProperty(PROP_LAST_RENDERED, now);
        return now;
    }

    public BlockPos getBlockPos(int localX, int y, int localZ)
    {
        return new BlockPos(toWorldX(localX), y, toWorldZ(localZ));
    }

    public int toWorldX(int localX)
    {
        return (coord.chunkXPos << 4) + localX;
    }

    public int toWorldZ(int localZ)
    {
        return (coord.chunkZPos << 4) + localZ;
    }

    @Override
    public String toString()
    {
        return "ChunkMD{" +
                "coord=" + coord +
                ", properties=" + properties +
                '}';
    }

    public int getDimension()
    {
        return ForgeHelper.INSTANCE.getDimension();
    }

    public void stopChunkRetention()
    {
        this.retainedChunk = null;
    }

    @Override
    protected void finalize() throws Throwable
    {
        if (retainedChunk != null)
        {
            super.finalize();
        }
    }

    public static class ChunkMissingException extends RuntimeException
    {
        ChunkMissingException(ChunkPos coord)
        {
            super("Chunk missing: " + coord);
        }
    }

    public static class SimpleCacheLoader extends CacheLoader<ChunkPos, ChunkMD>
    {
        Minecraft mc = FMLClientHandler.instance().getClient();

        @Override
        public ChunkMD load(ChunkPos coord) throws Exception
        {
            synchronized (this)
            {
                return ChunkLoader.getChunkMdFromMemory(mc.world, coord.chunkXPos, coord.chunkZPos);
            }
        }
    }
}
