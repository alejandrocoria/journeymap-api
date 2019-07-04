package journeymap.client.mod.impl;

import journeymap.client.mod.IBlockColorProxy;
import journeymap.client.mod.IModBlockHandler;
import journeymap.client.mod.ModBlockDelegate;
import journeymap.client.model.BlockMD;
import journeymap.client.model.ChunkMD;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.FMLClientHandler;

import javax.annotation.Nullable;

/**
 * This handles getting the block colors for mods that use CodeChickenLib as a library for their models.
 *
 * Deprecated -> currently not used, the getting of states was integrated into the vanilla get color logic.
 */
@Deprecated
public class CodeChickenLibMod implements IModBlockHandler, IBlockColorProxy
{
    @Override
    public void initialize(BlockMD blockMD)
    {
        blockMD.setBlockColorProxy(this);
    }

    @Override
    public int deriveBlockColor(BlockMD blockMD, @Nullable ChunkMD chunkMD, @Nullable BlockPos blockPos)
    {
        return ModBlockDelegate.INSTANCE.getDefaultBlockColorProxy().deriveBlockColor(blockMD, chunkMD, blockPos);
    }

    @Override
    public int getBlockColor(ChunkMD chunkMD, BlockMD blockMD, BlockPos blockPos)
    {
        IBlockState blockState = blockMD.getBlockState();
        try
        {
            // gets the actual state.
            blockState = blockMD.getBlock().getActualState(blockState, chunkMD.getWorld(), blockPos);
            // gets the extended state from the actual state.
            blockState = blockMD.getBlock().getExtendedState(blockState, chunkMD.getWorld(), blockPos);
        }
        catch (Exception ignore)
        {
            //do nothing use default blockstate
        }

        return FMLClientHandler.instance().getClient().getBlockColors().colorMultiplier(
                blockState,
                chunkMD.getWorld(),
                blockPos,
                blockMD.getBlock().getBlockLayer().ordinal()
        );
    }
}
