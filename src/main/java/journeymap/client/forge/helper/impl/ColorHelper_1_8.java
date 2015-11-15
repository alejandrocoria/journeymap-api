/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.forge.helper.impl;

import journeymap.client.cartography.RGB;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.forge.helper.IColorHelper;
import journeymap.client.log.LogFormatter;
import journeymap.client.log.StatTimer;
import journeymap.client.model.BlockMD;
import journeymap.client.model.ChunkMD;
import journeymap.common.Journeymap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.BlockPos;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.image.BufferedImage;
import java.nio.IntBuffer;
import java.util.HashSet;

/**
 * IColorHelper implementation for 1.8.   Formerly IconLoader.
 */
public class ColorHelper_1_8 implements IColorHelper
{
    BufferedImage blocksTexture;
    Logger logger = Journeymap.getLogger();
    HashSet<BlockMD> failed = new HashSet<BlockMD>();

    /**
     * Must be instantiated on main minecraft thread where GL context is viable.
     */
    public ColorHelper_1_8()
    {

    }

    public boolean hasBlocksTexture()
    {
        return blocksTexture != null;
    }

    @Override
    public boolean failedFor(BlockMD blockMD)
    {
        return failed.contains(blockMD);
    }

    @Override
    public int getColorMultiplier(ChunkMD chunkMD, Block block, int x, int y, int z)
    {
        if(chunkMD==null)
        {
            return block.colorMultiplier(ForgeHelper.INSTANCE.getIBlockAccess(), new BlockPos(x, y, z));
        }
        else
        {
            return block.colorMultiplier(chunkMD.getWorld(), new BlockPos(x, y, z));
        }
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
        IBlockState blockState = block.getStateFromMeta(blockMD.getMeta());
        return block.getRenderColor(blockState);
    }

    @Override
    public int getMapColor(BlockMD blockMD)
    {
        // 1.7
        // return blockMD.getBlock().getMapColor(blockMD.meta);

        // 1.8
        Block block = blockMD.getBlock();
        IBlockState blockState = block.getStateFromMeta(blockMD.getMeta());
        MapColor mapColor = block.getMapColor(blockState);
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
     *
     * @param blockMD
     * @return
     */
    @Override
    public Integer getTextureColor(BlockMD blockMD)
    {

        Integer color = null;

        boolean ok = blocksTexture != null || initBlocksTexture();
        if (!ok)
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
            if (blockMD.getUid().modId.equals("minecraft"))
            {
                logger.warn("Error getting block color. Plese report this exception to the JourneyMap mod author regarding " + blockMD.getUid() + ": " + LogFormatter.toPartialString(t));
            }
            else
            {
                logger.warn("Error getting block color from mod. Plese report this exception to the mod author of " + blockMD.getUid() + ": " + LogFormatter.toPartialString(t));
            }
            return null;
        }
    }

    private TextureAtlasSprite getDirectIcon(BlockMD blockMD)
    {
        boolean ok = blocksTexture != null || initBlocksTexture();
        if (!ok)
        {
            logger.warn("BlocksTexture not yet loaded");
            return null;
        }

        Block block = blockMD.getBlock();
        Integer overrideMeta = null;
        if (blockMD.hasOverrideMeta())
        {
            overrideMeta = blockMD.getOverrideMeta();
        }
        int meta = overrideMeta != null ? overrideMeta : blockMD.getMeta();

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
        boolean ok = blocksTexture != null || initBlocksTexture();
        if (!ok)
        {
            logger.warn("BlocksTexture not yet loaded");
            return null;
        }

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
                logger.warn("Couldn't get texture for " + icon.getIconName() + " using blockid " + blockMD.getUid());
            }

            if (unusable)
            {
                blockMD.addFlags(BlockMD.Flag.Error);
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
            //dataCache.getBlockMetadata().setAllAlpha(block, blockAlpha);
            blockMD.setAlpha(blockAlpha);
            blockMD.setIconName(icon.getIconName());
        }
        catch (Throwable e1)
        {
            if (blockMD.getUid().modId.equals("minecraft"))
            {
                logger.warn("Error getting block color. Plese report this exception to the JourneyMap mod author regarding "
                        + blockMD.getUid() + ": " + LogFormatter.toPartialString(e1));
            }
            else
            {
                logger.warn("Error getting block color from a mod. Plese report this exception to the mod author of "
                        + blockMD.getUid() + ": " + LogFormatter.toPartialString(e1));
            }
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

    @Override
    public boolean initBlocksTexture()
    {
        StatTimer timer = StatTimer.get("ColorHelper.initBlocksTexture", 0);

        try
        {
            if (!Display.isCurrent())
            {
                return false;
            }

            timer.start();

            ForgeHelper.INSTANCE.getClient().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
            GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);

            int miplevel = 0;
            int width = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, miplevel, GL11.GL_TEXTURE_WIDTH);
            int height = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, miplevel, GL11.GL_TEXTURE_HEIGHT);
            IntBuffer intbuffer = BufferUtils.createIntBuffer(width * height);
            int[] aint = new int[width * height];
            GL11.glGetTexImage(GL11.GL_TEXTURE_2D, miplevel, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, intbuffer);
            intbuffer.get(aint);
            BufferedImage bufferedimage = new BufferedImage(width, height, 2);
            bufferedimage.setRGB(0, 0, width, height, aint, 0, width);

            Journeymap.getLogger().info("initBlocksTexture: " + timer.elapsed() + "ms");
            timer.stop();

            blocksTexture = bufferedimage;
            return true;

        }
        catch (Throwable t)
        {
            logger.error("Could not load blocksTexture :" + t);
            timer.cancel();
            return false;
        }
    }
}
