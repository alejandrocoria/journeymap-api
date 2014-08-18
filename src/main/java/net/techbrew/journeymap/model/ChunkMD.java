/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.model;

import com.google.common.base.Optional;
import com.google.common.cache.CacheLoader;
import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.client.Minecraft;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.data.DataCache;
import net.techbrew.journeymap.io.nbt.ChunkLoader;

import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.util.HashMap;

/**
 * ChunkMD is a MetaData wrapper for a chunk and the various bits
 * of metadata that need to accompany a ChunkStub.
 *
 * @author mwoodman
 */
public class ChunkMD
{
    final static DataCache dataCache = DataCache.instance();
    private final SoftReference<Chunk> chunkReference;
    private final ChunkCoordIntPair coord;
    private final HashMap<String, Serializable> properties = new HashMap<String, Serializable>();

    public static final String PROP_IS_SLIME_CHUNK = "isSlimeChunk";
    public static final String PROP_LOADED = "loaded";
    public static final String PROP_LAST_RENDERED = "lastRendered";

    public ChunkMD(Chunk chunk)
    {
        if(chunk==null)
        {
            throw new IllegalArgumentException("Chunk can't be null");
        }
        this.chunkReference = new SoftReference<Chunk>(chunk);
        this.coord = new ChunkCoordIntPair(chunk.xPosition, chunk.zPosition); // avoid GC issue holding onto chunk's coord ref

        // Set load time
        setProperty(PROP_LOADED, System.currentTimeMillis());

        // https://github.com/OpenMods/OpenBlocks/blob/master/src/main/java/openblocks/common/item/ItemSlimalyzer.java#L44
        properties.put(PROP_IS_SLIME_CHUNK, chunk.getRandomWithSeed(987234911L).nextInt(10) == 0);
    }

    public Block getBlock(int x, int y, int z)
    {
        return getChunk().getBlock(x, y, z);
    }

    /**
     * Added to do a safety check on the world height value
     */
    public int getSavedLightValue(EnumSkyBlock par1EnumSkyBlock, int x, int y, int z)
    {
        return getChunk().getSavedLightValue(par1EnumSkyBlock, x, Math.min(y, getWorldActualHeight() - 1), z);
    }

    /**
     * Get the top block ignoring transparent roof blocks, air. etc.
     */
    public final BlockMD getTopBlockMD(final int x, int y, final int z)
    {
        BlockMD topBlockMd = null;

        do
        {
            topBlockMd = dataCache.getBlockMD(this, x, y, z);

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
    public int ceiling(final int x, final int z)
    {
        BlockMDCache blockMDCache = dataCache.getBlockMetadata();
        final int chunkHeight = getHeightValue(x, z);
        int y = chunkHeight;

        try
        {
            Block block;
            while (y >= 0)
            {
                block = getBlock(x, y, z);

                if (block instanceof BlockAir)
                {
                    y--;
                }
                else if (blockMDCache.hasFlag(block, BlockMD.Flag.HasAir) || blockMDCache.hasFlag(block, BlockMD.Flag.OpenToSky))
                {
                    y--;
                }
                else if (getChunk().canBlockSeeTheSky(x, y, z))
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
            JourneyMap.getLogger().warning(e + " at " + x + "," + y + "," + z);
        }

        return Math.max(0, y);
    }

    public boolean hasChunk()
    {
        return chunkReference.get()!=null;
    }

    public int getHeightValue(int x, int z)
    {
        return getChunk().getHeightValue(x, z);
    }

    public int getAbsoluteHeightValue(int x, int z)
    {
        return getChunk().getPrecipitationHeight(x, z);
    }

    public int getLightOpacity(BlockMD blockMD, int localX, int y, int localZ)
    {
        return blockMD.getBlock().getLightOpacity(this.getWorldObj(), (this.getCoord().chunkXPos << 4) + localX, y, (this.getCoord().chunkZPos << 4) + localZ);
    }

    public Serializable getProperty(String name)
    {
        return properties.get(name);
    }

    public Serializable getProperty(String name, Serializable defaultValue)
    {
        Serializable currentValue = getProperty(name);
        if(currentValue==null)
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
        if(chunk==null)
        {
            throw new ChunkMissingException(getCoord());
        }
        return chunk;
    }

    public World getWorldObj()
    {
        return getChunk().worldObj;
    }

    public int getWorldActualHeight()
    {
        return getChunk().worldObj.getActualHeight();
    }

    public Boolean getHasNoSky()
    {
        return getChunk().worldObj.provider.hasNoSky;
    }

    public ChunkCoordIntPair getCoord()
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

    @Override
    public String toString()
    {
        return "ChunkMD{" +
                "coord=" + coord +
                ", properties=" + properties +
                '}';
    }

    public static class ChunkMissingException extends RuntimeException
    {
        ChunkMissingException(ChunkCoordIntPair coord)
        {
            super("Chunk missing: " + coord);
        }
    }

    public static class SimpleCacheLoader extends CacheLoader<ChunkCoordIntPair, Optional<ChunkMD>>
    {
        Minecraft mc = FMLClientHandler.instance().getClient();

        @Override
        public Optional<ChunkMD> load(ChunkCoordIntPair coord) throws Exception
        {
            ChunkMD chunkMD = ChunkLoader.getChunkMdFromMemory(coord.chunkXPos, coord.chunkZPos, mc.theWorld);
            return Optional.fromNullable(chunkMD);
        }
    }


}
