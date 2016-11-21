package journeymap.client.model.mod.vanilla;

import journeymap.client.cartography.RGB;
import journeymap.client.forge.helper.ColorHelper;
import journeymap.client.model.BlockMD;
import journeymap.client.model.ChunkMD;
import journeymap.client.model.mod.ModBlockDelegate;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.block.BlockFlower;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

/**
 * Default color determination for mod blocks.
 */
public class VanillaColorHandler implements ModBlockDelegate.IModBlockColorHandler
{
    public static final VanillaColorHandler INSTANCE = new VanillaColorHandler();

    @Override
    public Integer getTextureColor(BlockMD blockMD)
    {
        return ColorHelper.INSTANCE.getTextureColor(blockMD);
    }

    /**
     * Get the color of the block at global coordinates
     */
    @Override
    public Integer getBlockColor(ChunkMD chunkMD, BlockMD blockMD, BlockPos blockPos)
    {
        Integer color = getBaseColor(chunkMD, blockMD, blockPos);
        if (blockMD.isBiomeColored())
        {
            color = getBiomeColor(chunkMD, blockMD, blockPos);
        }

        // Fallback to Minecraft's own map color
        if (color == null)
        {
            color = ColorHelper.INSTANCE.getMapColor(blockMD);
        }

        return color;
    }

    /**
     * Get the biome-based block color at the world coordinates
     */
    protected Integer getBiomeColor(ChunkMD chunkMD, BlockMD blockMD, BlockPos blockPos)
    {
        if (blockMD.isGrass())
        {
            return getGrassColor(chunkMD, blockMD, blockPos);
        }

        if (blockMD.isFoliage())
        {
            return getFoliageColor(chunkMD, blockMD, blockPos);
        }

        if (blockMD.isWater())
        {
            return getWaterColor(chunkMD, blockMD, blockPos);
        }

        // Anything else, including those with CustomBiomeColor
        return getCustomBiomeColor(chunkMD, blockMD, blockPos);
    }

    /**
     * Gets the color for the block.  If one isn't set yet, it is loaded from the block texture.
     */
    protected int getBaseColor(ChunkMD chunkMD, BlockMD blockMD, BlockPos blockPos)
    {
        Integer color = blockMD.getColor();
        if (color == null)
        {
            if (blockMD.isAir())
            {
                color = RGB.WHITE_ARGB;
                blockMD.setAlpha(0f);
                blockMD.addFlags(BlockMD.Flag.HasAir, BlockMD.Flag.OpenToSky, BlockMD.Flag.NoShadow);
            }
            else
            {
                // May update flags based on tint (CustomBiomeColor)
                color = loadTextureColor(blockMD, blockPos);
            }
            blockMD.setColor(color);
        }
        return color;
    }

    /**
     * Get the block's tint based on the biome position it's in.
     */
    protected Integer getCustomBiomeColor(ChunkMD chunkMD, BlockMD blockMD, BlockPos blockPos)
    {
        Integer color = getBaseColor(chunkMD, blockMD, blockPos);
        int tint = getTint(chunkMD, blockMD, blockPos);

        if (!RGB.isWhite(tint) && !RGB.isBlack(tint))
        {
            color = RGB.multiply(color, tint);
            if (!blockMD.hasFlag(BlockMD.Flag.CustomBiomeColor))
            {
                blockMD.addFlags(BlockMD.Flag.CustomBiomeColor);
                Journeymap.getLogger().info("Custom biome tint set for " + blockMD);
            }
        }
        else
        {
            blockMD.getFlags().remove(BlockMD.Flag.CustomBiomeColor);
            //Journeymap.getLogger().info("Custom biome tint not found for " + blockMD);
        }
        return color;
    }

    /**
     * Get the foliage color for the block.
     */
    protected Integer getFoliageColor(ChunkMD chunkMD, BlockMD blockMD, BlockPos blockPos)
    {
//        return RGB.adjustBrightness(RGB.multiply(getBaseColor(chunkMD, blockMD, blockPos),
//                getTint(chunkMD, blockMD, blockPos)), .8f);

        return RGB.adjustBrightness(RGB.multiply(getBaseColor(chunkMD, blockMD, blockPos),
                chunkMD.getChunk().getWorld().getBiomeGenForCoords(blockPos).getFoliageColorAtPos(blockPos)), .8f);
    }

    /**
     * Get the grass color for the block.
     */
    protected Integer getGrassColor(ChunkMD chunkMD, BlockMD blockMD, BlockPos blockPos)
    {
        // Base color is just a grey that gets the tint close to the averaged texture color on screen. - tb

        // Current approach:
        //return RGB.multiply(0x929292, getTint(chunkMD, blockMD, blockPos));

        // TODO: See if this calls into different results than current approach
        return RGB.multiply(0x929292, chunkMD.getChunk().getWorld().getBiomeGenForCoords(blockPos).getGrassColorAtPos(blockPos));
    }

    /**
     * Get the water color for the block.
     */
    protected Integer getWaterColor(ChunkMD chunkMD, BlockMD blockMD, BlockPos blockPos)
    {
        return RGB.multiply(getBaseColor(chunkMD, blockMD, blockPos), getTint(chunkMD, blockMD, blockPos));
    }

    /**
     * Get the tint (color multiplier) for the block.
     */
    protected Integer getTint(ChunkMD chunkMD, BlockMD blockMD, BlockPos blockPos)
    {
        int tint = RGB.WHITE_RGB;

        if (!blockMD.hasFlag(BlockMD.Flag.TintError))
        {
            if (!blockMD.isUseDefaultState())
            {
                try
                {
                    return ColorHelper.INSTANCE.getColorMultiplier(chunkMD, blockMD, blockPos);
                }
                catch (Exception e)
                {
                    blockMD.setUseDefaultState(true);
                }
            }

            // Use default blockstate if needed
            try
            {
                tint = ColorHelper.INSTANCE.getColorMultiplier(chunkMD, BlockMD.get(blockMD.getBlockState().getBlock().getDefaultState()), blockPos);
            }
            catch (Exception e)
            {
                Journeymap.getLogger().warn(String.format("Error getting block color multiplier. " +
                                "Please report this exception to the mod author of '%s' blockstate '%s': %s",
                        blockMD.getUid(), blockMD.getBlockState(), LogFormatter.toPartialString(e)));

                blockMD.addFlags(BlockMD.Flag.TintError, BlockMD.Flag.Error);
            }
        }

        return tint;
    }

    /**
     * Provides a color using the icon loader.
     * For non-biome blocks, the base color is multiplied against the block's render color.
     */
    protected Integer loadTextureColor(BlockMD blockMD, BlockPos blockPos)
    {
        Integer baseColor = null;

        // Get the color from the texture
        baseColor = getTextureColor(blockMD);

        // Non-biome block colors get multiplied by their render color.
        // Some blocks may have custom biome-based tints as well.
        if (baseColor != null)
        {
            if (!blockMD.isBiomeColored())
            {
                // Check for custom biome-based color multiplier
                int tint = getTint(null, blockMD, blockPos);
                if (!RGB.isWhite(tint) && !RGB.isBlack(tint))
                {
                    blockMD.addFlags(BlockMD.Flag.CustomBiomeColor);
                    Journeymap.getLogger().debug("Custom biome color will be used with " + blockMD);
                }
            }
        }

        if (baseColor == null)
        {
            baseColor = RGB.BLACK_ARGB;
            if (blockMD.hasFlag(BlockMD.Flag.TileEntity))
            {
                // TODO: What to do about this?
                Journeymap.getLogger().debug("Iconloader ignoring tile entity: " + blockMD);
            }
            else if (ColorHelper.INSTANCE.failedFor(blockMD))
            {
                Journeymap.getLogger().warn("Iconloader failed to get base color for " + blockMD);
            }
            else
            {
                Journeymap.getLogger().warn("Unknown failure, could not get base color for " + blockMD);
            }
        }
        return baseColor;
    }

    /**
     * Set explicit colors on blocks as needed.
     */
    public void setExplicitColors()
    {
        BlockMD.VOIDBLOCK.setColor(0x110C19);

        // Flower colors look bad because the stem color is averaged in, overriding them is easier.
        // 1.8
        BlockMD.get(Blocks.RED_FLOWER.getStateFromMeta(BlockFlower.EnumFlowerType.POPPY.getMeta())).setColor(0x980406);
        BlockMD.get(Blocks.RED_FLOWER.getStateFromMeta(BlockFlower.EnumFlowerType.BLUE_ORCHID.getMeta())).setColor(0x1E7EB6);
        BlockMD.get(Blocks.RED_FLOWER.getStateFromMeta(BlockFlower.EnumFlowerType.ALLIUM.getMeta())).setColor(0x8549B6);
        BlockMD.get(Blocks.RED_FLOWER.getStateFromMeta(BlockFlower.EnumFlowerType.HOUSTONIA.getMeta())).setColor(0x9DA1A7);
        BlockMD.get(Blocks.RED_FLOWER.getStateFromMeta(BlockFlower.EnumFlowerType.RED_TULIP.getMeta())).setColor(0x980406);
        BlockMD.get(Blocks.RED_FLOWER.getStateFromMeta(BlockFlower.EnumFlowerType.ORANGE_TULIP.getMeta())).setColor(0xA3581A);
        BlockMD.get(Blocks.RED_FLOWER.getStateFromMeta(BlockFlower.EnumFlowerType.WHITE_TULIP.getMeta())).setColor(0xB0B0B0);
        BlockMD.get(Blocks.RED_FLOWER.getStateFromMeta(BlockFlower.EnumFlowerType.PINK_TULIP.getMeta())).setColor(0xB09AB0);
        BlockMD.get(Blocks.RED_FLOWER.getStateFromMeta(BlockFlower.EnumFlowerType.OXEYE_DAISY.getMeta())).setColor(0xB3B3B3);
        BlockMD.get(Blocks.YELLOW_FLOWER.getStateFromMeta(BlockFlower.EnumFlowerType.DANDELION.getMeta())).setColor(0xAFB401);
    }
}
