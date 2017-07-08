package journeymap.client.mod.vanilla;

import journeymap.client.mod.IBlockColorProxy;
import journeymap.client.mod.ModBlockDelegate;
import journeymap.client.model.BlockMD;
import journeymap.client.model.ChunkMD;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockFlowerPot;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.FMLClientHandler;

/**
 * Example of how BlockMD handling can be overridden to return a different BlockMD.
 * This allows the color of a flowerpot to be determined by what it contains.
 */
public enum FlowerBlockProxy implements IBlockColorProxy
{
    INSTANCE;

    boolean enabled = true;
    private final BlockColors blockColors = FMLClientHandler.instance().getClient().getBlockColors();

    @Override
    public int deriveBlockColor(BlockMD blockMD)
    {
        if(blockMD.getBlock() instanceof BlockFlower)
        {
            Integer color = getFlowerColor(blockMD.getBlockState());
            if(color!=null)
            {
                return color;
            }
        }
        return ModBlockDelegate.INSTANCE.getDefaultBlockColorProxy().deriveBlockColor(blockMD);
    }

    @Override
    public int getBlockColor(ChunkMD chunkMD, BlockMD blockMD, BlockPos blockPos)
    {
        if(blockMD.getBlock() instanceof BlockFlower)
        {
            return blockMD.getTextureColor();
        }

        if(blockMD.getBlock() instanceof BlockFlowerPot && Journeymap.getClient().getCoreProperties().mapPlants.get())
        {
            try
            {
                IBlockState blockState = blockMD.getBlockState();
                ItemStack stack = ((BlockFlowerPot) blockState.getBlock()).getItem(chunkMD.getWorld(), blockPos, blockState);
                if (stack != null)
                {
                    IBlockState contentBlockState = Block.getBlockFromItem(stack.getItem()).getStateFromMeta(stack.getItem().getDamage(stack));
                    return BlockMD.get(contentBlockState).getTextureColor();
                }
            }
            catch (Exception e)
            {
                Journeymap.getLogger().error("Error checking FlowerPot: " + e, LogFormatter.toPartialString(e));
                enabled = false;
            }
        }
        return ModBlockDelegate.INSTANCE.getDefaultBlockColorProxy().getBlockColor(chunkMD, blockMD, blockPos);
    }

    /**
     * Flower colors look bad because the stem color is averaged in, overriding them is easier.
     * @return null if not handled
     */
    private Integer getFlowerColor(IBlockState blockState)
    {
        if(blockState.getBlock() instanceof BlockFlower) {
            IProperty<BlockFlower.EnumFlowerType> typeProperty = ((BlockFlower) blockState.getBlock()).getTypeProperty();
            BlockFlower.EnumFlowerType flowerType = (BlockFlower.EnumFlowerType) blockState.getProperties().get(typeProperty);
            if (flowerType != null) {
                switch (flowerType) {
                    case POPPY: return 0x980406;
                    case BLUE_ORCHID: return 0x1E7EB6;
                    case ALLIUM: return 0x8549B6;
                    case HOUSTONIA: return 0x9DA1A7;
                    case RED_TULIP: return 0x980406;
                    case ORANGE_TULIP: return 0xA3581A;
                    case WHITE_TULIP: return 0xB0B0B0;
                    case PINK_TULIP: return 0xB09AB0;
                    case OXEYE_DAISY: return 0xB3B3B3;
                    case DANDELION: return 0xAFB401;
                }
                return 0x00ff00;
            }
        }
        return null;
    }
}
