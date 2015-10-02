package journeymap.client.model.mod.vanilla;

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

/**
 * Default color determination for mod blocks.
 */
public class VanillaColorHandler implements ModBlockDelegate.IModBlockColorHandler
{
    public static final VanillaColorHandler INSTANCE = new VanillaColorHandler();

    private final IForgeHelper forgeHelper = ForgeHelper.INSTANCE;
    private final IColorHelper colorHelper = forgeHelper.getColorHelper();

    @Override
    public Integer getTextureColor(BlockMD blockMD)
    {
        return colorHelper.getTextureColor(blockMD);
    }

    /**
     * Get the color of the block at global coordinates
     */
    @Override
    public Integer getBlockColor(BlockMD blockMD, int globalX, int y, int globalZ)
    {
        Integer color = getBaseColor(blockMD, globalX, y, globalZ);
        if (blockMD.isBiomeColored())
        {
            color = getBiomeColor(blockMD, globalX, y, globalZ);
        }

        // Fallback to Minecraft's own map color
        if (color == null)
        {
            color = colorHelper.getMapColor(blockMD);
        }

        return color;
    }

    /**
     * Get the biome-based block color at the world coordinates
     */
    protected Integer getBiomeColor(BlockMD blockMD, int globalX, int y, int globalZ)
    {
        if (blockMD.isGrass())
        {
            return getGrassColor(blockMD, globalX, y, globalZ);
        }

        if (blockMD.isFoliage())
        {
            return getFoliageColor(blockMD, globalX, y, globalZ);
        }

        if (blockMD.isWater())
        {
            return getWaterColor(blockMD, globalX, y, globalZ);
        }

        // Anything else, including those with CustomBiomeColor
        return getCustomBiomeColor(blockMD, globalX, y, globalZ);
    }

    /**
     * Gets the color for the block.  If one isn't set yet, it is loaded from the block texture.
     */
    protected int getBaseColor(BlockMD blockMD, int globalX, int y, int globalZ)
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
                color = loadTextureColor(blockMD, globalX, y, globalZ);
            }
            blockMD.setColor(color);
        }
        return color;
    }

    /**
     * Get the block's tint based on the biome position it's in.
     */
    protected Integer getCustomBiomeColor(BlockMD blockMD, int globalX, int y, int globalZ)
    {
        Integer color = getBaseColor(blockMD, globalX, y, globalZ);
        int tint = getTint(blockMD, globalX, y, globalZ);

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
            Journeymap.getLogger().debug("Custom biome tint not found for " + blockMD);
        }
        return color;
    }

    /**
     * Get the foliage color for the block.
     */
    protected Integer getFoliageColor(BlockMD blockMD, int globalX, int y, int globalZ)
    {
        return RGB.adjustBrightness(RGB.multiply(getBaseColor(blockMD, globalX, y, globalZ), getTint(blockMD, globalX, y, globalZ)), .8f);
    }

    /**
     * Get the grass color for the block.
     */
    protected Integer getGrassColor(BlockMD blockMD, int globalX, int y, int globalZ)
    {
        // Base color is just a grey that gets the tint close to the averaged texture color on screen. - tb
        return RGB.multiply(0x929292, getTint(blockMD, globalX, y, globalZ));
    }

    /**
     * Get the water color for the block.
     */
    protected Integer getWaterColor(BlockMD blockMD, int globalX, int y, int globalZ)
    {
        return RGB.multiply(getBaseColor(blockMD, globalX, y, globalZ), getTint(blockMD, globalX, y, globalZ));
    }

    /**
     * Get the tint (color multiplier) for the block.
     */
    protected Integer getTint(BlockMD blockMD, int globalX, int y, int globalZ)
    {
        try
        {
            return colorHelper.getColorMultiplier(blockMD.getBlock(), globalX, y, globalZ);
        }
        catch (Exception e)
        {
            // Bugfix for NPE thrown by uk.co.shadeddimensions.ep3.block.BlockFrame.func_71920_b
            JMLogger.logOnce("Block throws exception when calling colorMultiplier(): " + blockMD.getBlock().getUnlocalizedName(), e);
            return RGB.WHITE_ARGB;
        }
    }

    /**
     * Provides a color using the icon loader.
     * For non-biome blocks, the base color is multiplied against the block's render color.
     *
     * @return
     */
    protected Integer loadTextureColor(BlockMD blockMD, int globalX, int y, int globalZ)
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
                int tint = getTint(blockMD, globalX, y, globalZ);
                if (!RGB.isWhite(tint) && !RGB.isBlack(tint))
                {
                    blockMD.addFlags(BlockMD.Flag.CustomBiomeColor);
                    Journeymap.getLogger().info("Custom biome color will be used with " + blockMD);
                }
                else
                {
                    // Check for render color
                    int renderColor = colorHelper.getRenderColor(blockMD);
                    if (!RGB.isWhite(renderColor))
                    {
                        baseColor = RGB.multiply(baseColor, RGB.ALPHA_OPAQUE | renderColor); // Force opaque render color
                        Journeymap.getLogger().info("Applied render color for " + blockMD);
                    }
                }
            }
        }

        if (baseColor == null)
        {
            baseColor = RGB.BLACK_ARGB;
            if (blockMD.hasFlag(BlockMD.Flag.TileEntity))
            {
                // TODO: What to do about this?
                Journeymap.getLogger().info("Iconloader ignoring tile entity: " + blockMD);
            }
            else if (colorHelper.failedFor(blockMD))
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
        BlockMD.get(Blocks.red_flower, BlockFlower.EnumFlowerType.POPPY.getMeta()).setColor(0x980406);
        BlockMD.get(Blocks.red_flower, BlockFlower.EnumFlowerType.BLUE_ORCHID.getMeta()).setColor(0x1E7EB6);
        BlockMD.get(Blocks.red_flower, BlockFlower.EnumFlowerType.ALLIUM.getMeta()).setColor(0x8549B6);
        BlockMD.get(Blocks.red_flower, BlockFlower.EnumFlowerType.HOUSTONIA.getMeta()).setColor(0x9DA1A7);
        BlockMD.get(Blocks.red_flower, BlockFlower.EnumFlowerType.RED_TULIP.getMeta()).setColor(0x980406);
        BlockMD.get(Blocks.red_flower, BlockFlower.EnumFlowerType.ORANGE_TULIP.getMeta()).setColor(0xA3581A);
        BlockMD.get(Blocks.red_flower, BlockFlower.EnumFlowerType.WHITE_TULIP.getMeta()).setColor(0xB0B0B0);
        BlockMD.get(Blocks.red_flower, BlockFlower.EnumFlowerType.PINK_TULIP.getMeta()).setColor(0xB09AB0);
        BlockMD.get(Blocks.red_flower, BlockFlower.EnumFlowerType.OXEYE_DAISY.getMeta()).setColor(0xB3B3B3);
        BlockMD.get(Blocks.yellow_flower, BlockFlower.EnumFlowerType.DANDELION.getMeta()).setColor(0xAFB401);
    }
}