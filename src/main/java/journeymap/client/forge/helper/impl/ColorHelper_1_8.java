/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.forge.helper.impl;

import journeymap.client.cartography.RGB;
import journeymap.client.data.DataCache;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.forge.helper.IColorHelper;
import journeymap.client.log.LogFormatter;
import journeymap.client.log.StatTimer;
import journeymap.client.model.BlockMD;
import journeymap.common.Journeymap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashSet;

/**
 * IColorHelper implementation for 1.8.   Formerly IconLoader.
 */
public class ColorHelper_1_8 implements IColorHelper
{
    final BufferedImage blocksTexture;
    Logger logger = Journeymap.getLogger();
    DataCache dataCache = DataCache.instance();
    HashSet<BlockMD> failed = new HashSet<BlockMD>();

    /**
     * Must be instantiated on main minecraft thread where GL context is viable.
     */
    public ColorHelper_1_8()
    {
        blocksTexture = initBlocksTexture();
    }

    @Override
    public boolean failedFor(BlockMD blockMD)
    {
        return failed.contains(blockMD);
    }

    @Override
    public int getColorMultiplier(World world, Block block, int x, int y, int z)
    {
        // 1.7
        // return block.colorMultiplier(world, x, 78, z)

        // 1.8
        return block.colorMultiplier(world, new BlockPos(x, y, z));
    }

    /**
     * @deprecated use getColorMultiplier() instead
     */
    @Deprecated
    @Override
    public int getRenderColor(BlockMD blockMD)
    {
        // 1.7
        // return blockMD.getBlock().getRenderColor(blockMD.meta);

        // 1.8
        Block block = blockMD.getBlock();
        IBlockState blockState = block.getStateFromMeta(blockMD.meta);
        return block.getRenderColor(blockState);
    }

    @Override
    public int getMapColor(BlockMD blockMD)
    {
        // 1.7
        // return blockMD.getBlock().getMapColor(blockMD.meta);

        // 1.8
        Block block = blockMD.getBlock();
        IBlockState blockState = block.getStateFromMeta(blockMD.meta);
        return block.getMapColor(blockState).colorValue;
    }

    /**
     * Derive block color from the corresponding texture.
     *
     * @param blockMD
     * @return
     */
    @Override
    public Integer loadBlockColor(BlockMD blockMD)
    {

        Integer color = null;

        if (blocksTexture == null)
        {
            logger.warn("BlocksTexture not yet loaded");
            return null;
        }

//        if (failed.contains(blockMD))
//        {
//            return null;
//        }

        try
        {

            TextureAtlasSprite blockIcon = getDirectIcon(blockMD);

            if (blockIcon == null)
            {
                if (blockMD.getBlock() instanceof ITileEntityProvider)
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
            logger.error("Error getting color: " + LogFormatter.toString(t));
            return null;
        }
    }

    private TextureAtlasSprite getDirectIcon(BlockMD blockMD)
    {
        TextureAtlasSprite blockIcon = null;

        Block block = blockMD.getBlock();
        Integer overrideMeta = null;
        if (blockMD.hasOverrideMeta())
        {
            overrideMeta = blockMD.getOverrideMeta();
        }
        int meta = overrideMeta != null ? overrideMeta : blockMD.meta;

        IBlockState state = blockMD.getBlock().getStateFromMeta(meta);

        // Always get the upper portion of a double plant for rendering
        if (block instanceof BlockDoublePlant)
        {
            if (state.getValue(BlockDoublePlant.HALF).toString().equals("lower"))
            {
                // cycle to upper
                state = state.cycleProperty(BlockDoublePlant.HALF);
            }
        }

        TextureAtlasSprite icon = ForgeHelper.INSTANCE.getClient().getBlockRendererDispatcher().getBlockModelShapes().getTexture(state);
        return icon;
        //return getClient().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(state).getFaceQuads(EnumFacing.UP)
    }

    Integer getColorForIcon(BlockMD blockMD, TextureAtlasSprite icon)
    {
        Integer color = null;

        try
        {
            int count = 0;
            int argb, alpha;
            int a = 0, r = 0, g = 0, b = 0;
            int x = 0, y = 0;

            int xStart, yStart, xStop, yStop;
            BufferedImage textureImg = blocksTexture.getSubimage(icon.getOriginX(), icon.getOriginY(), icon.getIconWidth(), icon.getIconHeight());
            xStart = yStart = 0;
            xStop = textureImg.getWidth();
            yStop = textureImg.getHeight();

            boolean unusable = true;
            if (textureImg != null && textureImg.getWidth() > 0)
            {
                outer:
                for (x = xStart; x < xStop; x++)
                {
                    inner:
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
                    unusable = false;
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
                }
            }
            else
            {
                logger.warn("Couldn't get texture for " + icon.getIconName() + " using blockid ");
            }

            if (unusable)
            {
                blockMD.addFlags(BlockMD.Flag.Error);
                dataCache.getBlockMetadata().setFlags(blockMD.getBlock(), BlockMD.Flag.Error);
                String pattern = "Unusable texture for %s, icon=%s,texture coords %s,%s - %s,%s";
                logger.debug(String.format(pattern, blockMD, icon.getIconName(), xStart, yStart, xStop, yStop));
                r = g = b = 0;
            }


            // Set color
            color = RGB.toInteger(r, g, b);

            // Determine alpha
            Block block = blockMD.getBlock();
            float blockAlpha = 1f;
            if (unusable)
            {
                blockAlpha = 0f;
            }
            else
            {
                if (blockMD.hasFlag(BlockMD.Flag.Transparency))
                {
                    blockAlpha = blockMD.getAlpha();
                }
                else if (block.getRenderType() > 0)
                {
                    // 1.8 check translucent because lava's opacity = 0;
                    if (block.isTranslucent())
                    { // try to use light opacity
                        blockAlpha = block.getLightOpacity() / 255f;
                    }

                    // try to use texture alpha
                    if (blockAlpha == 0 || blockAlpha == 1)
                    {
                        blockAlpha = a * 1.0f / 255;
                    }
                }
            }
            //dataCache.getBlockMetadata().setAlpha(block, blockAlpha);
            blockMD.setAlpha(blockAlpha);
            blockMD.setIconName(icon.getIconName());
        }
        catch (Throwable e1)
        {
            logger.warn("Error deriving color for " + blockMD + ": " + LogFormatter.toString(e1));
        }

        if (color != null)
        {
            if (logger.isTraceEnabled())
            {
                logger.debug("Derived color for " + blockMD + ": " + Integer.toHexString(color));
            }
        }

        return color;
    }

    private BufferedImage initBlocksTexture()
    {

        StatTimer timer = StatTimer.get("IconLoader.initBlocksTexture", 0);
        timer.start();

        BufferedImage image = null;

        try
        {
            int glid = ForgeHelper.INSTANCE.getClient().getTextureManager().getTexture(TextureMap.locationBlocksTexture).getGlTextureId();
            GL11.glBindTexture(3553, glid);
            int width = GL11.glGetTexLevelParameteri(3553, 0, 4096);
            int height = GL11.glGetTexLevelParameteri(3553, 0, 4097);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(width * height * 4).order(ByteOrder.nativeOrder());

            GL11.glGetTexImage(3553, 0, 6408, 5121, byteBuffer);
            image = new BufferedImage(width, height, 6);
            byteBuffer.position(0);
            byte[] var4 = new byte[byteBuffer.remaining()];
            byteBuffer.get(var4);

            for (int var5 = 0; var5 < width; var5++)
            {
                for (int var6 = 0; var6 < height; var6++)
                {
                    int var7 = var6 * width * 4 + var5 * 4;
                    byte var8 = 0;
                    int var10 = var8 | (var4[(var7 + 2)] & 0xFF);
                    var10 |= (var4[(var7 + 1)] & 0xFF) << 8;
                    var10 |= (var4[(var7 + 0)] & 0xFF) << 16;
                    var10 |= (var4[(var7 + 3)] & 0xFF) << 24;
                    image.setRGB(var5, var6, var10);
                }
            }

            timer.stop();

        }
        catch (Throwable t)
        {
            logger.error("Could not load blocksTexture :" + t);
            timer.cancel();
        }

        return image;
    }

}
