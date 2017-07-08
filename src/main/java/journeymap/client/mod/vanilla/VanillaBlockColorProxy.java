/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.mod.vanilla;

import journeymap.client.cartography.color.ColorManager;
import journeymap.client.cartography.color.ColoredSprite;
import journeymap.client.cartography.color.RGB;
import journeymap.client.mod.IBlockColorProxy;
import journeymap.client.model.BlockFlag;
import journeymap.client.model.BlockMD;
import journeymap.client.model.ChunkMD;
import journeymap.client.properties.CoreProperties;
import journeymap.client.world.JmBlockAccess;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Default color determination for mod blocks.
 */
public class VanillaBlockColorProxy implements IBlockColorProxy
{
    static Logger logger = Journeymap.getLogger();

    private final BlockColors blockColors = FMLClientHandler.instance().getClient().getBlockColors();
    private boolean blendFoliage;
    private boolean blendGrass;
    private boolean blendWater;

    public VanillaBlockColorProxy()
    {
        CoreProperties coreProperties = Journeymap.getClient().getCoreProperties();
        blendFoliage = coreProperties.mapBlendFoliage.get();
        blendGrass = coreProperties.mapBlendGrass.get();
        blendWater = coreProperties.mapBlendWater.get();
    }

    @Override
    public int deriveBlockColor(BlockMD blockMD)
    {
        IBlockState blockState = blockMD.getBlockState();
        try
        {
            // Fluid?
            if (blockState.getBlock() instanceof IFluidBlock)
            {
                return getSpriteColor(blockMD, 0xBCBCBC);
            }

            Integer color = getSpriteColor(blockMD, null);
            if(color==null)
            {
                color = setBlockColorToMaterial(blockMD);
            }
            return color;
        }
        catch (Throwable e)
        {
            logger.error("Error deriving color for " + blockMD + ": " + LogFormatter.toPartialString(e));
            blockMD.addFlags(BlockFlag.Error);
            return setBlockColorToMaterial(blockMD);
        }
    }

    /**
     * Get color for block based on world position.
     *
     * @param chunkMD  the chunk md
     * @param blockMD  the block md
     * @param blockPos the block pos
     * @return the color at the given position
     */
    @Override
    public int getBlockColor(ChunkMD chunkMD, BlockMD blockMD, BlockPos blockPos)
    {
        int result = blockMD.getTextureColor();

        if (blockMD.isFoliage())
        {
            // Approximate the light opacity reduction by leaves
            result = RGB.adjustBrightness(result, .8f);
        }
        else if (blockMD.isFluid())
        {
            return RGB.multiply(result, ((IFluidBlock) blockMD.getBlock()).getFluid().getColor());
        }

        return RGB.multiply(result, getColorMultiplier(chunkMD, blockMD, blockPos, blockMD.getBlock().getBlockLayer().ordinal()));
    }

    /**
     * Gets color multiplier.
     *
     * @param chunkMD   the chunk md
     * @param blockMD   the block md
     * @param blockPos  the block pos
     * @param tintIndex tintIndex
     * @return the color multiplier
     */
    public int getColorMultiplier(ChunkMD chunkMD, BlockMD blockMD, BlockPos blockPos, int tintIndex)
    {
        if (!blendGrass && blockMD.isGrass())
        {
            return chunkMD.getBiome(blockPos).getGrassColorAtPos(blockPos);
        }

        if (!blendFoliage && blockMD.isFoliage())
        {
            return chunkMD.getBiome(blockPos).getFoliageColorAtPos(blockPos);
        }

        if (!blendWater && blockMD.isWater())
        {
            return chunkMD.getBiome(blockPos).getWaterColorMultiplier();
        }

        return blockColors.colorMultiplier(blockMD.getBlockState(), JmBlockAccess.INSTANCE, blockPos, tintIndex);
    }

    /**
     * Average sprite textures for a blockstate into a rgb color.
     *
     * @param blockMD      Block MD
     * @param defaultColor optional default
     * @return result or defaultColor
     */
    public static Integer getSpriteColor(@Nonnull BlockMD blockMD, @Nullable Integer defaultColor)
    {
        Collection<ColoredSprite> sprites = blockMD.getBlockSpritesProxy().getSprites(blockMD);
        float[] rgba = ColorManager.INSTANCE.getAverageColor(sprites);
        if (rgba != null)
        {
            return RGB.toInteger(rgba);
        }
        return defaultColor;
    }

    public static int setBlockColorToError(BlockMD blockMD)
    {
        blockMD.setAlpha(0);
        blockMD.addFlags(BlockFlag.Ignore, BlockFlag.Error);
        blockMD.setColor(-1);
        return -1;
    }

    public static int setBlockColorToMaterial(BlockMD blockMD)
    {
        try
        {
            blockMD.setAlpha(1);
            blockMD.addFlags(BlockFlag.Ignore);
            return blockMD.setColor(blockMD.getBlockState().getMapColor().colorValue);
        }
        catch (Exception e)
        {
            logger.warn(String.format("Failed to use MaterialMapColor, marking as error: %s", blockMD));
            return setBlockColorToError(blockMD);
        }
    }
}
