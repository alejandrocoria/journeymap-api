package journeymap.client.model.mod.vanilla;

import journeymap.client.cartography.ColorManager;
import journeymap.client.cartography.RGB;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.forge.helper.IColorHelper;
import journeymap.client.forge.helper.IForgeHelper;
import journeymap.client.log.JMLogger;
import journeymap.client.model.BlockMD;
import journeymap.client.model.mod.ModBlockDelegate;
import journeymap.common.Journeymap;
import net.minecraft.block.BlockFlower;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.BiomeGenBase;

/**
 * Handles color determination for vanilla and most mod blocks.
 */
public class VanillaColorHandler implements ModBlockDelegate.IModBlockColorHandler
{
    public static final VanillaColorHandler INSTANCE = new VanillaColorHandler();

    private final IForgeHelper forgeHelper = ForgeHelper.INSTANCE;
    private final IColorHelper colorHelper = forgeHelper.getColorHelper();

    /**
     * Get the block's tint based on the biome position it's in.
     */
    public Integer getCustomBiomeColor(BlockMD blockMD, BiomeGenBase biome, int x, int y, int z)
    {
        Integer color = ColorManager.instance().getBaseColor(blockMD, x, y, z);
        int tint = getTint(blockMD, x, y, z);

        if (!RGB.isWhite(tint) && !RGB.isBlack(tint))
        {
            color = RGB.multiply(color, tint);
            if (!blockMD.hasFlag(BlockMD.Flag.CustomBiomeColor))
            {
                blockMD.addFlags(BlockMD.Flag.CustomBiomeColor);
                Journeymap.getLogger().info("Custom biome tint set for " + blockMD + " in " + biome.biomeName);
            }
        }
        else
        {
            Journeymap.getLogger().debug("Custom biome tint not found for " + blockMD + " in " + biome.biomeName);
        }
        return color;
    }

    /**
     * Get the foliage color for the block.
     */
    public Integer getFoliageColor(BlockMD blockMD, BiomeGenBase biome, int x, int y, int z)
    {
        return RGB.adjustBrightness(RGB.multiply(ColorManager.instance().getBaseColor(blockMD, x, y, z), getTint(blockMD, x, y, z)), .8f);
    }

    /**
     * Get the grass color for the block.
     */
    public Integer getGrassColor(BlockMD blockMD, BiomeGenBase biome, int x, int y, int z)
    {
        // Base color is just a grey that gets the tint close to the averaged texture color on screen. - tb
        return RGB.multiply(0x929292, getTint(blockMD, x, y, z));
    }

    /**
     * Get the water color for the block.
     */
    public Integer getWaterColor(BlockMD blockMD, BiomeGenBase biome, int x, int y, int z)
    {
        return RGB.multiply(ColorManager.instance().getBaseColor(blockMD, x, y, z), getTint(blockMD, x, y, z));
    }

    /**
     * Get the tint (color multiplier) for the block.
     */
    public Integer getTint(BlockMD blockMD, int x, int y, int z)
    {
        try
        {
            return colorHelper.getColorMultiplier(blockMD.getBlock(), x, y, z);
        }
        catch (Exception e)
        {
            // Bugfix for NPE thrown by uk.co.shadeddimensions.ep3.block.BlockFrame.func_71920_b
            JMLogger.logOnce("Block throws exception when calling colorMultiplier(): " + blockMD.getBlock().getUnlocalizedName(), e);
            return RGB.WHITE_ARGB;
        }
    }

    /**
     * Set explicit colors on blocks as needed.
     */
    public void setExplicitColors()
    {
        BlockMD.VOIDBLOCK.setBaseColor(0x110C19);

        // Flower colors look bad because the stem color is averaged in, overriding them is easier.
        // 1.8
        BlockMD.get(Blocks.red_flower, BlockFlower.EnumFlowerType.POPPY.getMeta()).setBaseColor(0x980406);
        BlockMD.get(Blocks.red_flower, BlockFlower.EnumFlowerType.BLUE_ORCHID.getMeta()).setBaseColor(0x1E7EB6);
        BlockMD.get(Blocks.red_flower, BlockFlower.EnumFlowerType.ALLIUM.getMeta()).setBaseColor(0x8549B6);
        BlockMD.get(Blocks.red_flower, BlockFlower.EnumFlowerType.HOUSTONIA.getMeta()).setBaseColor(0x9DA1A7);
        BlockMD.get(Blocks.red_flower, BlockFlower.EnumFlowerType.RED_TULIP.getMeta()).setBaseColor(0x980406);
        BlockMD.get(Blocks.red_flower, BlockFlower.EnumFlowerType.ORANGE_TULIP.getMeta()).setBaseColor(0xA3581A);
        BlockMD.get(Blocks.red_flower, BlockFlower.EnumFlowerType.WHITE_TULIP.getMeta()).setBaseColor(0xB0B0B0);
        BlockMD.get(Blocks.red_flower, BlockFlower.EnumFlowerType.PINK_TULIP.getMeta()).setBaseColor(0xB09AB0);
        BlockMD.get(Blocks.red_flower, BlockFlower.EnumFlowerType.OXEYE_DAISY.getMeta()).setBaseColor(0xB3B3B3);
        BlockMD.get(Blocks.yellow_flower, BlockFlower.EnumFlowerType.DANDELION.getMeta()).setBaseColor(0xAFB401);
    }
}
