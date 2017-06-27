/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.model;

import journeymap.client.world.JmBlockAccess;
import journeymap.common.Journeymap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.EnumSkyBlock;
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
 * @author techbrew
 */
public class ChunkMD
{
    /**
     * The constant PROP_IS_SLIME_CHUNK.
     */
    public static final String PROP_IS_SLIME_CHUNK = "isSlimeChunk";
    /**
     * The constant PROP_LOADED.
     */
    public static final String PROP_LOADED = "loaded";
    /**
     * The constant PROP_LAST_RENDERED.
     */
    public static final String PROP_LAST_RENDERED = "lastRendered";
    private final WeakReference<Chunk> chunkReference;
    private final ChunkPos coord;
    private final HashMap<String, Serializable> properties = new HashMap<String, Serializable>();
    private BlockDataArrays blockDataArrays = new BlockDataArrays();

    private Chunk retainedChunk;

    /**
     * Instantiates a new Chunk md.
     *
     * @param chunk the chunk
     */
    public ChunkMD(Chunk chunk)
    {
        this(chunk, false);
    }

    /**
     * Instantiates a new Chunk md.
     *
     * @param chunk       the chunk
     * @param forceRetain the force retain
     */
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

    /**
     * Gets block state.
     *
     * @param localX the local x
     * @param y      the y
     * @param localZ the local z
     * @return the block state
     */
    public IBlockState getBlockState(int localX, int y, int localZ)
    {
        if (localX < 0 || localX > 15 || localZ < 0 || localZ > 15)
        {
            Journeymap.getLogger().warn("Expected local coords, got global coords");
        }
        return getBlockState(new BlockPos(toWorldX(localX), y, toWorldZ(localZ)));
    }

    /**
     * Gets block state.
     *
     * @param blockPos the block pos
     * @return the block state
     */
    public IBlockState getBlockState(BlockPos blockPos)
    {
        return JmBlockAccess.INSTANCE.getBlockState(blockPos);
    }

    /**
     * Gets block md.
     *
     * @param blockPos the block pos
     * @return the block md
     */
    public BlockMD getBlockMD(BlockPos blockPos)
    {
        return BlockMD.getBlockMD(this, blockPos);
    }

    /**
     * Added to do a safety check on the world height value
     *
     * @param localX the local x
     * @param y      the y
     * @param localZ the local z
     * @return the saved light value
     */
    public int getSavedLightValue(int localX, int y, int localZ)
    {
        try
        {
            return getChunk().getLightFor(EnumSkyBlock.BLOCK, getBlockPos(localX, y, localZ));
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            // Encountered on a custom-gen world where the value was 16
            return 1; // At least let it show up
        }
    }

    /**
     * Get the top block ignoring transparent roof blocks, air. etc.
     *
     * @param localX the local x
     * @param y      the y
     * @param localZ the local z
     * @return the top block md
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
     *
     * @param localX the local x
     * @param localZ the local z
     * @return the int
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
                else if (chunk.canSeeSky(blockPos))
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

    /**
     * Has chunk boolean.
     *
     * @return the boolean
     */
    public boolean hasChunk()
    {
        Chunk chunk = chunkReference.get();
        return chunk != null && !(chunk instanceof EmptyChunk) && chunk.isLoaded();
    }

    /**
     * Gets height.
     *
     * @param blockPos the block pos
     * @return the height
     */
    public int getHeight(BlockPos blockPos)
    {
        return getChunk().getHeight(blockPos);
    }

    /**
     * Gets precipitation height.
     *
     * @param localX the local x
     * @param localZ the local z
     * @return the precipitation height
     */
    public int getPrecipitationHeight(int localX, int localZ)
    {
        return getChunk().getPrecipitationHeight(getBlockPos(localX, 0, localZ)).getY();
    }

    /**
     * Gets precipitation height.
     *
     * @param blockPos the block pos
     * @return the precipitation height
     */
    public int getPrecipitationHeight(BlockPos blockPos)
    {
        return getChunk().getPrecipitationHeight(blockPos).getY();
    }

    /**
     * Gets light opacity.
     *
     * @param blockMD the block md
     * @param localX  the local x
     * @param y       the y
     * @param localZ  the local z
     * @return the light opacity
     */
    public int getLightOpacity(BlockMD blockMD, int localX, int y, int localZ)
    {
        BlockPos pos = getBlockPos(localX, y, localZ);
        return blockMD.getBlockState().getBlock().getLightOpacity(blockMD.getBlockState(), JmBlockAccess.INSTANCE, pos);
    }

    /**
     * Gets property.
     *
     * @param name the name
     * @return the property
     */
    public Serializable getProperty(String name)
    {
        return properties.get(name);
    }

    /**
     * Gets property.
     *
     * @param name         the name
     * @param defaultValue the default value
     * @return the property
     */
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

    /**
     * Sets property.
     *
     * @param name  the name
     * @param value the value
     * @return the property
     */
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

    /**
     * Gets chunk.
     *
     * @return the chunk
     */
    public Chunk getChunk()
    {
        Chunk chunk = chunkReference.get();
        if (chunk == null)
        {
            throw new ChunkMissingException(getCoord());
        }
        return chunk;
    }

    /**
     * Gets world.
     *
     * @return the world
     */
    public World getWorld()
    {
        return FMLClientHandler.instance().getClient().world;
    }

    /**
     * Gets world actual height.
     *
     * @return the world actual height
     */
    public int getWorldActualHeight()
    {
        // add one to get above the top block for some worlds that paste in to 256
        return getWorld().getActualHeight() + 1;
    }

    /**
     * Gets has no sky.
     *
     * @return the has no sky
     */
    public Boolean getHasNoSky()
    {
        return getWorld().provider.hasNoSky();
    }

    /**
     * Can block see the sky boolean.
     *
     * @param localX the local x
     * @param y      the y
     * @param localZ the local z
     * @return the boolean
     */
    public boolean canBlockSeeTheSky(int localX, int y, int localZ)
    {
        return getChunk().canSeeSky(getBlockPos(localX, y, localZ));
    }

    /**
     * Gets coord.
     *
     * @return the coord
     */
    public ChunkPos getCoord()
    {
        return coord;
    }

    /**
     * Is slime chunk boolean.
     *
     * @return the boolean
     */
    public boolean isSlimeChunk()
    {
        return (Boolean) getProperty(PROP_IS_SLIME_CHUNK, Boolean.FALSE);
    }

    /**
     * Gets loaded.
     *
     * @return the loaded
     */
    public long getLoaded()
    {
        return (Long) getProperty(PROP_LOADED, 0L);
    }

    /**
     * Reset render times.
     */
    public void resetRenderTimes()
    {
        getRenderTimes().clear();
    }

    /**
     * Reset render time.
     *
     * @param mapType the map type
     */
    public void resetRenderTime(MapType mapType)
    {
        getRenderTimes().put(mapType, 0L);
    }

    /**
     * Reset block data.
     *
     * @param mapType the map type
     */
    public void resetBlockData(MapType mapType)
    {
        getBlockData().get(mapType).clear();
    }

    /**
     * Gets render times.
     *
     * @return the render times
     */
    protected HashMap<MapType, Long> getRenderTimes()
    {
        Serializable obj = properties.get(PROP_LAST_RENDERED);
        if (!(obj instanceof HashMap))
        {
            obj = new HashMap<MapType, Long>();
            properties.put(PROP_LAST_RENDERED, obj);
        }
        return (HashMap<MapType, Long>) obj;
    }

    /**
     * Gets last rendered.
     *
     * @param mapType the map type
     * @return the last rendered
     */
    public long getLastRendered(MapType mapType)
    {
        return getRenderTimes().getOrDefault(mapType, 0L);
    }

    /**
     * Sets rendered.
     *
     * @param mapType the map type
     * @return the rendered
     */
    public long setRendered(MapType mapType)
    {
        long now = System.currentTimeMillis();
        getRenderTimes().put(mapType, now);
        return now;
    }

    /**
     * Gets block pos.
     *
     * @param localX the local x
     * @param y      the y
     * @param localZ the local z
     * @return the block pos
     */
    public BlockPos getBlockPos(int localX, int y, int localZ)
    {
        return new BlockPos(toWorldX(localX), y, toWorldZ(localZ));
    }

    /**
     * To world x int.
     *
     * @param localX the local x
     * @return the int
     */
    public int toWorldX(int localX)
    {
        return (coord.chunkXPos << 4) + localX;
    }

    /**
     * To world z int.
     *
     * @param localZ the local z
     * @return the int
     */
    public int toWorldZ(int localZ)
    {
        return (coord.chunkZPos << 4) + localZ;
    }

    /**
     * Gets block data.
     *
     * @return the block data
     */
    public BlockDataArrays getBlockData()
    {
        return blockDataArrays;
    }

    /**
     * Gets block data ints.
     *
     * @param mapType the map type
     * @return the block data ints
     */
    public BlockDataArrays.DataArray<Integer> getBlockDataInts(MapType mapType)
    {
        return blockDataArrays.get(mapType).ints();
    }

    /**
     * Gets block data floats.
     *
     * @param mapType the map type
     * @return the block data floats
     */
    public BlockDataArrays.DataArray<Float> getBlockDataFloats(MapType mapType)
    {
        return blockDataArrays.get(mapType).floats();
    }

    /**
     * Gets block data booleans.
     *
     * @param mapType the map type
     * @return the block data booleans
     */
    public BlockDataArrays.DataArray<Boolean> getBlockDataBooleans(MapType mapType)
    {
        return blockDataArrays.get(mapType).booleans();
    }

    @Override
    public String toString()
    {
        return "ChunkMD{" +
                "coord=" + coord +
                ", properties=" + properties +
                '}';
    }

    /**
     * Gets dimension.
     *
     * @return the dimension
     */
    public int getDimension()
    {
        return getWorld().provider.getDimension();
    }

    /**
     * Stop chunk retention.
     */
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

    /**
     * The type Chunk missing exception.
     */
    public static class ChunkMissingException extends RuntimeException
    {
        /**
         * Instantiates a new Chunk missing exception.
         *
         * @param coord the coord
         */
        ChunkMissingException(ChunkPos coord)
        {
            super("Chunk missing: " + coord);
        }
    }
}
