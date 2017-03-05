/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.world;

import journeymap.client.data.DataCache;
import journeymap.client.model.BlockMD;
import journeymap.client.model.ChunkMD;
import journeymap.common.Journeymap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.client.FMLClientHandler;

/**
 * @author techbrew 2/12/2017.
 */
public enum JmBlockAccess implements IBlockAccess
{
    /**
     * Instance jm block access.
     */
    INSTANCE;

    /**
     * Gets light opacity.
     *
     * @param blockMD  the block md
     * @param blockPos the block pos
     * @return the light opacity
     */
    public int getLightOpacity(BlockMD blockMD, BlockPos blockPos)
    {
        return blockMD.getBlockState().getBlock().getLightOpacity(blockMD.getBlockState(), this, blockPos);
    }

    public TileEntity getTileEntity(BlockPos pos)
    {
        return getWorld().getTileEntity(pos);
    }

    public int getCombinedLight(BlockPos pos, int min)
    {
        return getWorld().getCombinedLight(pos, min);
    }

    public IBlockState getBlockState(BlockPos pos)
    {
        if (!this.isValid(pos))
        {
            return Blocks.AIR.getDefaultState();
        }
        else
        {
            ChunkMD chunkMD = getChunkMDFromBlockCoords(pos);
            if (chunkMD != null && chunkMD.hasChunk())
            {
                return chunkMD.getChunk().getBlockState(pos.getX() & 15, pos.getY(), pos.getZ() & 15);
            }
            return Blocks.AIR.getDefaultState();
        }
    }

    public boolean isAirBlock(BlockPos pos)
    {
        return getWorld().isAirBlock(pos);
    }

    // 1.10.2
    public Biome getBiome(BlockPos pos)
    {
        ChunkMD chunkMD = getChunkMDFromBlockCoords(pos);
        if (chunkMD != null && chunkMD.hasChunk())
        {
            try
            {
                Chunk chunk = chunkMD.getChunk();
                Biome biome = chunk.getBiome(pos, getWorld().getBiomeProvider());
                if (biome == null)
                {
                    return null;
                }
                return biome;
            }
            catch (Throwable throwable)
            {
                Journeymap.getLogger().error("Error in getBiome(): " + throwable);
                // 1.10.2
                return getWorld().getBiome(pos);
            }
        }
        else
        {
            // 1.10.2
            return getWorld().getBiomeProvider().getBiome(pos, Biomes.PLAINS);
        }
    }

    // Does not exist in 1.10.2
//        @Override
//        public boolean extendedLevelsInChunkCache()
//        {
//            return getWorld().extendedLevelsInChunkCache();
//        }


    public int getStrongPower(BlockPos pos, EnumFacing direction)
    {
        return getWorld().getStrongPower(pos, direction);
    }

    /**
     * Gets world.
     *
     * @return the world
     */
    public World getWorld()
    {
        return FMLClientHandler.instance().getClient().theWorld;
    }

    public WorldType getWorldType()
    {
        return getWorld().getWorldType();
    }


    public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default)
    {
        return getWorld().isSideSolid(pos, side, _default);
    }

    /**
     * Check if the given BlockPos has valid coordinates
     */
    private boolean isValid(BlockPos pos)
    {
        return pos.getX() >= -30000000 && pos.getZ() >= -30000000 && pos.getX() < 30000000 && pos.getZ() < 30000000 && pos.getY() >= 0 && pos.getY() < 256;
    }

    private ChunkMD getChunkMDFromBlockCoords(BlockPos pos)
    {
        return DataCache.INSTANCE.getChunkMD(new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4));
    }
}
