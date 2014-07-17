package net.techbrew.journeymap.model;

import com.google.common.base.Optional;
import com.google.common.cache.CacheLoader;
import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.data.DataCache;
import net.techbrew.journeymap.io.nbt.ChunkLoader;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * ChunkStub MetaData wrapper for the various bits
 * of metadata that need to accompany a ChunkStub.
 *
 * @author mwoodman
 */
public class ChunkMD
{
    final static DataCache dataCache = DataCache.instance();

    public final World worldObj;
    public final int worldHeight;
    public final Boolean hasNoSky;
    public final ChunkStub stub;
    public final ChunkCoordIntPair coord;
    public volatile Integer[][] surfaceHeights;
    public volatile Float[][] surfaceSlopes;
    public volatile HashMap<Integer, Integer[][]> sliceHeights;
    public volatile HashMap<Integer, Float[][]> sliceSlopes;
    public Boolean render;
    protected boolean current;

    public ChunkMD(Chunk chunk, Boolean render, World worldObj)
    {
        this(chunk, render, worldObj, false);
    }

    public ChunkMD(Chunk chunk, Boolean render, World worldObj, boolean doErrorChecks)
    {
        this(new ChunkStub(chunk), render, worldObj);
        if (chunk.isEmpty() || !chunk.isChunkLoaded)
        {
            render = false;
        }
    }

    public ChunkMD(ChunkStub stub, Boolean render, World worldObj)
    {
        this.stub = stub;
        this.render = render;
        this.worldObj = worldObj;
        this.worldHeight = worldObj.getActualHeight();
        this.hasNoSky = worldObj.provider.hasNoSky;
        this.coord = new ChunkCoordIntPair(stub.xPosition, stub.zPosition);
        this.current = true;
        this.sliceSlopes = new HashMap<Integer, Float[][]>(8);
        this.surfaceHeights = new Integer[16][16];
        this.sliceHeights = new HashMap<Integer, Integer[][]>(8);
    }


    public boolean isCurrent()
    {
        return current;
    }

    public void setCurrent(boolean current)
    {
        this.current = current;
    }

    public Block getBlock(int x, int y, int z) {
        return stub.getBlock(x, y, z);
    }

    /**
     * Added to do a safety check on the world height value
     */
    public int getSavedLightValue(EnumSkyBlock par1EnumSkyBlock, int x, int y, int z)
    {
        return stub.getSavedLightValue(par1EnumSkyBlock, x, Math.min(y, worldHeight - 1), z);
    }

    /**
     * Added because getHeightValue() sometimes returns an air block.
     * Returns the value in the height map at this x, z coordinate in the chunk, disregarding
     * blocks that shouldn't be used as the top block.
     */
    public int getSurfaceBlockHeight(int x, int z, boolean ignoreWater)
    {
        Integer y = this.surfaceHeights[x][z];

        if(y!=null)
        {
            return this.surfaceHeights[x][z];
        }

        y = Math.max(0, stub.getHeightValue(x, z));

        try
        {
            BlockMD blockMD = dataCache.getBlockMD(this, x, y, z);

            while (y > 0 && blockMD != null && (blockMD.isAir() || (ignoreWater && (blockMD.isWater())) || blockMD.hasFlag(BlockMD.Flag.NoShadow)))
            {
                y--;
                blockMD = dataCache.getBlockMD(this, x, y, z);
            }
        }
        catch (Exception e)
        {
            JourneyMap.getLogger().warning("Couldn't get safe surface block height at " + x + "," + z + ": " + e);
        }

        this.surfaceHeights[x][z] = y;

        return Math.max(0, y);
    }

    /**
     * Get the height of the block at the coordinates + offsets.  Uses chunkMd.slopes.
     */
    public Float getSurfaceBlockHeight(int x, int z, int offsetX, int offsetZ, ChunkMD.Set neighbors, float defaultVal, boolean ignoreWater)
    {
        ChunkMD chunk = null;
        int blockX = ((this.coord.chunkXPos<<4) + x + offsetX);
        int blockZ = ((this.coord.chunkZPos<<4) + z + offsetZ);
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;

        if(chunkX==this.coord.chunkXPos && chunkZ==this.coord.chunkZPos)
        {
            chunk = this;
        }
        else
        {
            ChunkCoordIntPair coord = new ChunkCoordIntPair(chunkX, chunkZ);
            chunk =  neighbors.get(coord);
        }

        if (chunk != null)
        {
            return (float) chunk.getSurfaceBlockHeight(blockX & 15, blockZ & 15, ignoreWater);
        }
        else
        {
            return defaultVal;
        }
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
                else if (dataCache.getBlockMetadata().hasAnyFlags(block, BlockMD.Flag.HasAir, BlockMD.Flag.OpenToSky))
                {
                    y--;
                }
                else if (stub.canBlockSeeTheSky(x, y, z))
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

    public int getHeightValue(int x, int z)
    {
        return stub.getHeightValue(x, z);
    }

    public int getAbsoluteHeightValue(int x, int z)
    {
        return stub.getPrecipitationHeight(x, z);
    }

    public int getLightOpacity(BlockMD blockMD, int localX, int y, int localZ)
    {
        return blockMD.getBlock().getLightOpacity(this.worldObj, (this.coord.chunkXPos << 4) + localX, y, (this.coord.chunkZPos << 4) + localZ);
    }

    @Override
    public int hashCode()
    {
        return coord.hashCode();
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
        if (stub.xPosition != other.stub.xPosition)
        {
            return false;
        }
        if (stub.zPosition != other.stub.zPosition)
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "ChunkStubMD [" + stub.xPosition + ", " + stub.zPosition + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static class Set extends LinkedHashMap<ChunkCoordIntPair, ChunkMD> implements Iterable<ChunkMD>
    {

        public Set(int i)
        {
            super(i);
        }

        public Set(ChunkMD... chunkMDs)
        {
            for (ChunkMD chunkMD : chunkMDs)
            {
                super.put(chunkMD.coord, chunkMD);
            }
        }

        public void put(ChunkMD chunkMd)
        {
            super.put(chunkMd.coord, chunkMd);
        }

        public void add(ChunkMD chunkMd)
        {
            super.put(chunkMd.coord, chunkMd);
        }

        public ChunkMD remove(ChunkMD chunkMd)
        {
            return super.remove(chunkMd.coord);
        }

        public ChunkMD remove(ChunkCoordIntPair coord)
        {
            return super.remove(coord);
        }

        @Override
        public Iterator<ChunkMD> iterator()
        {
            return this.values().iterator();
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
