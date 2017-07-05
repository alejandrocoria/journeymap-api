package journeymap.client.world;

import journeymap.client.data.DataCache;
import journeymap.client.model.ChunkMD;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.client.FMLClientHandler;

import javax.annotation.Nullable;

/**
 * Created by Mark on 2/12/2017.
 */
@MethodsReturnNonnullByDefault
public enum JmBlockAccess implements IBlockAccess
{
    INSTANCE;

    @Override
    public TileEntity getTileEntity(BlockPos pos)
    {
        return getWorld().getTileEntity(pos);
    }

    @Override
    public int getCombinedLight(BlockPos pos, int min)
    {
        return getWorld().getCombinedLight(pos, min);
    }

    @Override
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

    @Override
    public boolean isAirBlock(BlockPos pos)
    {
        return getWorld().isAirBlock(pos);
    }

    @Override
    public Biome getBiome(final BlockPos pos)
    {
        return getBiome(pos, Biomes.DEFAULT);
    }

    @Nullable
    public Biome getBiome(final BlockPos pos, Biome defaultBiome)
    {
        ChunkMD chunkMD = getChunkMDFromBlockCoords(pos);
        if (chunkMD != null && chunkMD.hasChunk())
        {
            return chunkMD.getWorld().getBiome(pos);
        }

        if (Minecraft.getMinecraft().isSingleplayer())
        {
            MinecraftServer server = Minecraft.getMinecraft().getIntegratedServer();
            if (server != null)
            {
                return server.getEntityWorld().getBiomeProvider().getBiome(pos);
            }
        }

        return defaultBiome;
    }

    @Override
    public int getStrongPower(BlockPos pos, EnumFacing direction)
    {
        return getWorld().getStrongPower(pos, direction);
    }

    public World getWorld()
    {
        return FMLClientHandler.instance().getClient().world;
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

    @Nullable
    private ChunkMD getChunkMDFromBlockCoords(BlockPos pos)
    {
        return DataCache.INSTANCE.getChunkMD(new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4));
    }

}
