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
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
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
        return blockMD.getBlockState().getMaterial().getMaterialMapColor().colorValue;
    }

    /**
     * Derive block color from the corresponding texture.
     */
    @Override
    public Integer getTextureColor(BlockMD blockMD)
    {
        try
        {
            setBlockColor(blockMD, getDirectIcon(blockMD));
            return blockMD.getColor();
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

        return FMLClientHandler.instance().getClient().getBlockRendererDispatcher().getBlockModelShapes().getTexture(blockState);
    }

    void setBlockColorToError(BlockMD blockMD)
    {
        blockMD.setAlpha(0);
        blockMD.addFlags(BlockMD.Flag.HasAir, BlockMD.Flag.Error);
        blockMD.setColor(null);
        failed.add(blockMD);
    }

    void setBlockColorToAir(BlockMD blockMD)
    {
        blockMD.setAlpha(0);
        blockMD.addFlags(BlockMD.Flag.HasAir);
        blockMD.setColor(RGB.WHITE_RGB);
    }

    void setBlockColorToMaterial(BlockMD blockMD)
    {
        try
        {
            blockMD.setAlpha(1);
            blockMD.addFlags(BlockMD.Flag.HasAir);
            blockMD.setColor(getMapColor(blockMD));
        }
        catch (Exception e)
        {
            logger.warn(String.format("Failed to use MaterialMapColor, marking as error: %s", blockMD));
            setBlockColorToError(blockMD);
        }
    }

    boolean setBlockColor(BlockMD blockMD, TextureAtlasSprite icon, BufferedImage textureImg, String pass)
    {
        try
        {
            // Bail if the texture's not usable
            if (textureImg == null || textureImg.getWidth() == 0)
            {
                Journeymap.getLogger().warn(String.format("ColorHelper.setBlockColor(pass=" + pass + "): Null/empty texture for %s", blockMD));
                return false;
            }

            // Get average rgba values from texture
            float[] rgba = getAverageColor(blockMD, textureImg);
            if (rgba.length < 4)
            {
                Journeymap.getLogger().warn(String.format("ColorHelper.setBlockColor(pass=" + pass + "): Couldn't derive RGBA from %s", blockMD));
                return false;
            }

            // Set color
            int color = RGB.toInteger(rgba);
            float blockAlpha;

            if (blockMD.hasFlag(BlockMD.Flag.Transparency))
            {
                // Use preset alpha
                blockAlpha = blockMD.getAlpha();
            }
            else
            {
                blockAlpha = rgba[3];
            }

            blockMD.setColor(color);
            blockMD.setAlpha(blockAlpha);

            return true;
        }
        catch (Exception e)
        {
            logger.error(String.format("ColorHelper.setBlockColor(pass=" + pass + "): Error for %s", blockMD));
            logger.error(LogFormatter.toPartialString(e));
            return false;
        }
    }

    void setBlockColor(BlockMD blockMD, TextureAtlasSprite icon)
    {
        try
        {
            // No icon?
            if (icon == null)
            {
                if (blockMD.getBlockState().getBlock() instanceof ITileEntityProvider)
                {
                    logger.debug("Ignoring TitleEntity without standard block texture: " + blockMD);
                    blockMD.addFlags(BlockMD.Flag.TileEntity);
                    setBlockColorToAir(blockMD);
                }
                else
                {
                    setBlockColorToError(blockMD);
                }
                return;
            }

            // Set icon name
            blockMD.setIconName(icon.getIconName());

            // Invisible?
            if (blockMD.getBlockState().getRenderType() == EnumBlockRenderType.INVISIBLE)
            {
                setBlockColorToAir(blockMD);
                return;
            }

            // Air?
            if (Material.AIR.equals(blockMD.getBlockState().getMaterial()))
            {
                setBlockColorToAir(blockMD);
                return;
            }

            // Missing texture?
            if (new ResourceLocation(icon.getIconName()).equals(TextureMap.LOCATION_MISSING_TEXTURE))
            {
                setBlockColorToAir(blockMD);
                return;
            }

            // Plan A: Get image from texture data
            if (setBlockColor(blockMD, icon, getImageFromFrameTextureData(icon), "frameTextureData"))
            {
                return;
            }

            // Plan B: Get image from texture's source PNG
            if (setBlockColor(blockMD, icon, getImageFromResourceLocation(icon), "resourceLocation"))
            {
                return;
            }

            // Plan C: Use Material's map color
            logger.warn(String.format("ColorHelper.setBlockColor(): Texture unusable. Using MaterialMapColor instead for: %s", blockMD));
            setBlockColorToMaterial(blockMD);
        }
        catch (Throwable e1)
        {
            logger.warn("Error deriving color for " + blockMD + ": " + LogFormatter.toString(e1));
            setBlockColorToMaterial(blockMD);
        }
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
