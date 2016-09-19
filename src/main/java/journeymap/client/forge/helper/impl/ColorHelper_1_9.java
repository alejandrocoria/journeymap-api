/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.forge.helper.impl;

import journeymap.client.cartography.RGB;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.forge.helper.IColorHelper;
import journeymap.client.model.BlockMD;
import journeymap.client.model.ChunkMD;
import journeymap.client.render.texture.TextureCache;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.apache.logging.log4j.Logger;

import java.awt.image.BufferedImage;
import java.util.HashSet;

/**
 * IColorHelper implementation for 1.8.   Formerly IconLoader.
 */
public class ColorHelper_1_9 implements IColorHelper
{
    Logger logger = Journeymap.getLogger();
    HashSet<BlockMD> failed = new HashSet<BlockMD>();
    BlockColors blockColors = FMLClientHandler.instance().getClient().getBlockColors();

    /**
     * Must be instantiated on main minecraft thread where GL context is viable.
     */
    public ColorHelper_1_9()
    {
    }

    @Override
    public boolean failedFor(BlockMD blockMD)
    {
        return failed.contains(blockMD);
    }

    @Override
    public int getColorMultiplier(ChunkMD chunkMD, BlockMD blockMD, BlockPos blockPos)
    {
        if (chunkMD == null || !chunkMD.hasChunk())
        {
            return blockColors.colorMultiplier(blockMD.getBlockState(), ForgeHelper.INSTANCE.getIBlockAccess(), blockPos, 2);
        }
        else
        {
            IBlockState blockState = chunkMD.getChunk().getBlockState(blockPos);
            if (Blocks.AIR.getDefaultState().equals(blockState))
            {
                return RGB.WHITE_RGB;
            }
            else
            {
                return blockColors.colorMultiplier(blockMD.getBlockState(), ForgeHelper.INSTANCE.getIBlockAccess(), blockPos, 2);
            }
        }
    }

    @Override
    public int getMapColor(BlockMD blockMD)
    {
        MapColor mapColor = blockMD.getBlockState().getBlock().getMapColor(blockMD.getBlockState());
        if (mapColor != null)
        {
            return mapColor.colorValue;
        }
        else
        {
            return RGB.BLACK_RGB;
        }
    }

    /**
     * Derive block color from the corresponding texture.
     */
    @Override
    public Integer getTextureColor(BlockMD blockMD)
    {

        Integer color = null;

//        boolean ok = blocksTexture != null || initBlocksTexture();
//        if (!ok)
//        {
//            logger.warn("BlocksTexture not yet loaded");
//            return null;
//        }

//        if (failed.contains(blockMD))
//        {
//            return null;
//        }

        try
        {

            TextureAtlasSprite blockIcon = getDirectIcon(blockMD);

            if (blockIcon == null)
            {
                if (blockMD.getBlockState().getBlock() instanceof ITileEntityProvider)
                {
                    logger.debug("Ignoring TitleEntity without standard block texture: " + blockMD);
                    blockMD.addFlags(BlockMD.Flag.TileEntity, BlockMD.Flag.HasAir);
                    return null;
                }
            }

            if (blockIcon == null)
            {
                blockMD.addFlags(BlockMD.Flag.Error);
                failed.add(blockMD);
                return null;
            }

            color = getColorForIcon(blockMD, blockIcon);
            if (color == null)
            {
                failed.add(blockMD);
                return null;
            }

            return color;
        }
        catch (Throwable t)
        {
            failed.add(blockMD);
            if (blockMD.getUid().startsWith("minecraft"))
            {
                logger.warn(String.format("Error getting block color for %s. Cause: %s", blockMD, LogFormatter.toPartialString(t)));
            }
            return null;
        }
    }

    private TextureAtlasSprite getDirectIcon(BlockMD blockMD)
    {
//        boolean ok = blocksTexture != null || initBlocksTexture();
//        if (!ok)
//        {
//            logger.warn("BlocksTexture not yet loaded");
//            return null;
//        }

        IBlockState blockState = blockMD.getBlockState();
        Block block = blockState.getBlock();

        // Always get the upper portion of a double plant for rendering
        if (block instanceof BlockDoublePlant)
        {
            if (blockState.getValue(BlockDoublePlant.HALF).toString().equals("lower"))
            {
                // cycle to upper
                blockState = blockState.cycleProperty(BlockDoublePlant.HALF);
            }
        }

        TextureAtlasSprite icon = FMLClientHandler.instance().getClient().getBlockRendererDispatcher().getBlockModelShapes().getTexture(blockState);
        return icon;
        //return getClient().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(state).getFaceQuads(EnumFacing.UP)
    }

    Integer getColorForIcon(BlockMD blockMD, TextureAtlasSprite icon)
    {
        Integer color = null;
        IBlockState blockState = blockMD.getBlockState();

        try
        {
            Block block = blockMD.getBlockState().getBlock();
            float[] rgba = new float[0];
            float blockAlpha = 0f;

            // Plan A: Get image from texture data
            BufferedImage textureImg = getImageFromFrameTextureData(icon);
            if (textureImg == null)
            {
                // Plan B: Get image from texture's source PNG
                textureImg = getImageFromResourceLocation(icon);
            }
            boolean unusable = (textureImg == null || textureImg.getWidth() == 0);

            // Get average rgba values from texture
            if (!unusable)
            {
                rgba = getAverageColor(blockMD, textureImg);
                unusable = rgba.length < 4;
            }
            else
            {
                Journeymap.getLogger().warn(String.format("ColorHelper.getColorForIcon(): No usable texture for %s", blockMD));
            }

            if (!unusable)
            {
                // Set color
                color = RGB.toInteger(rgba);

                if (blockMD.hasFlag(BlockMD.Flag.Transparency))
                {
                    blockAlpha = blockMD.getAlpha();
                }
                else if (block.getRenderType(blockState) == EnumBlockRenderType.MODEL)
                {
                    // 1.8 check translucent because lava's opacity = 0;
                    if (block.isTranslucent(blockState))
                    {
                        // try to use light opacity
                        blockAlpha = block.getLightOpacity(blockState) / 255f;
                    }

                    // try to use texture alpha
                    if (blockAlpha == 0 || blockAlpha == 1)
                    {
                        blockAlpha = rgba[3] * 1.0f / 255;
                    }
                }
            }

            if (unusable)
            {
                if (!block.getMaterial(blockState).isOpaque())
                {
                    unusable = false;
                }
                else
                {
                    logger.warn(String.format("Block is opaque, but texture was completely transparent: %s . Using MaterialMapColor instead for: %s", icon.getIconName(), blockMD));
                    try
                    {
                        color = block.getMaterial(blockState).getMaterialMapColor().colorValue;
                        unusable = false;
                    }
                    catch (Exception e)
                    {
                        logger.warn(String.format("Failed to use MaterialMapColor, marking block transparent: %s", blockMD));
                        blockAlpha = 0F;
                        color = RGB.WHITE_RGB;
                    }
                }
            }

            if (unusable)
            {
                blockMD.addFlags(BlockMD.Flag.Error);
            }

            // Set alpha and color
            //blockMD.setColor(color);
            blockMD.setAlpha(blockAlpha);
            blockMD.setIconName(icon.getIconName());

            if (color != null)
            {
                if (logger.isTraceEnabled())
                {
                    logger.debug("Derived color for " + blockMD + ": " + Integer.toHexString(color));
                }
            }
        }
        catch (Throwable e1)
        {
            logger.warn("Error deriving color for " + blockMD + ": " + LogFormatter.toString(e1));
        }

        return color;
    }

    /**
     * Primary means of getting block texture:  FrameTextureData
     */
    private BufferedImage getImageFromFrameTextureData(TextureAtlasSprite tas)
    {
        try
        {
            final int[] rgb = tas.getFrameTextureData(0)[0];
            if (rgb.length > 0)
            {
                int width = tas.getIconWidth();
                int height = tas.getIconHeight();
                BufferedImage textureImg = new BufferedImage(width, height, 2);
                textureImg.setRGB(0, 0, width, height, rgb, 0, width);
                return textureImg;
            }
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error(String.format("ColorHelper.getColorForIcon(): Unable to use frame data for %s: %s", tas.getIconName(), t.getMessage()));
        }
        return null;
    }

    /**
     * Secondary means of getting block texture:  derived ResourceLocation
     */
    private BufferedImage getImageFromResourceLocation(TextureAtlasSprite tas)
    {
        try
        {
            ResourceLocation iconNameLoc = new ResourceLocation(tas.getIconName());
            ResourceLocation fileLoc = new ResourceLocation(iconNameLoc.getResourceDomain(), "textures/" + iconNameLoc.getResourcePath() + ".png");
            return TextureCache.resolveImage(fileLoc);
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error(String.format("ColorHelper.getColorForIcon(): Unable to use texture file for %s: %s", tas.getIconName(), t.getMessage()));
        }
        return null;
    }

    /**
     * Average r/g/b/a values from BufferedImage
     */
    private float[] getAverageColor(BlockMD blockMD, BufferedImage textureImg)
    {
        int count = 0;
        int argb, alpha;
        int a = 0, r = 0, g = 0, b = 0;
        int x = 0, y = 0;
        int xStart, yStart, xStop, yStop;

        xStart = yStart = 0;
        xStop = textureImg.getWidth();
        yStop = textureImg.getHeight();

        // Average r/g/b/a
        try
        {
            boolean unusable = true;

            outer:
            for (x = xStart; x < xStop; x++)
            {
                for (y = yStart; y < yStop; y++)
                {
                    try
                    {
                        argb = textureImg.getRGB(x, y);
                    }
                    catch (ArrayIndexOutOfBoundsException e)
                    {
                        logger.warn("Bad index at " + x + "," + y + " for " + blockMD + ": " + e.getMessage());
                        continue; // Bugfix for some texturepacks that may not be reporting correct size?
                    }
                    catch (Throwable e)
                    {
                        logger.warn("Couldn't get RGB from BlocksTexture at " + x + "," + y + " for " + blockMD + ": " + e.getMessage());
                        break outer;
                    }
                    alpha = (argb >> 24) & 0xFF;
                    if (alpha > 0)
                    {
                        count++;
                        a += alpha;
                        r += (argb >> 16) & 0xFF;
                        g += (argb >> 8) & 0xFF;
                        b += (argb) & 0xFF;
                    }
                }
            }

            if (count > 0)
            {
                if (a > 0)
                {
                    a = a / count;
                }
                if (r > 0)
                {
                    r = r / count;
                }
                if (g > 0)
                {
                    g = g / count;
                }
                if (b > 0)
                {
                    b = b / count;
                }
                return RGB.floats(RGB.toInteger(r, g, b), a);
            }
            else
            {
                logger.warn("Texture was completely transparent for " + blockMD);
            }
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error(String.format("ColorHelper.getAverageColor(): Error getting average color: %s", t.getMessage()));
        }

        return new float[0];
    }
}
