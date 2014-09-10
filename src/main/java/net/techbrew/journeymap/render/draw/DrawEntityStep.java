/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.render.draw;

import com.google.common.cache.CacheLoader;
import net.minecraft.entity.player.EntityPlayer;
import net.techbrew.journeymap.model.EntityDTO;
import net.techbrew.journeymap.render.map.GridRenderer;
import net.techbrew.journeymap.render.texture.TextureImpl;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * Created by mwoodman on 12/26/13.
 */
public class DrawEntityStep implements DrawStep
{
    static final Color labelBg = Color.darkGray.darker();
    static final int labelBgAlpha = 205;
    static final Color labelFg = Color.white;
    static final int labelFgAlpha = 225;

    EntityDTO entityDTO;
    TextureImpl texture;
    TextureImpl locatorTexture;
    boolean flip;

    private DrawEntityStep(EntityDTO entityDTO)
    {
        super();
        this.entityDTO = entityDTO;
    }

    public void update(boolean flip, TextureImpl locatorTexture, TextureImpl texture)
    {
        this.locatorTexture = locatorTexture;
        this.texture = texture;
        this.flip = flip;
    }

    @Override
    public void draw(double xOffset, double yOffset, GridRenderer gridRenderer, float drawScale, double fontScale, double rotation)
    {
        if (entityDTO.entityLiving==null || entityDTO.entityLiving.isDead || !entityDTO.entityLiving.addedToChunk || entityDTO.entityLiving.isSneaking())
        {
            return;
        }

        Point2D pixel = gridRenderer.getPixel(entityDTO.entityLiving.posX, entityDTO.entityLiving.posZ);
        if (pixel != null)
        {
            double heading = entityDTO.entityLiving.rotationYawHead;
            double drawX = pixel.getX() + xOffset;
            double drawY = pixel.getY() + yOffset;

            if (entityDTO.entityLiving instanceof EntityPlayer)
            {
                int blockSize = (int) Math.pow(2, gridRenderer.getZoom());
                float labelOffset = texture != null ? texture.height / blockSize : 0;
                drawPlayer(drawX, drawY, heading, drawScale, fontScale, rotation);

            }
            else
            {
                drawCreature(drawX, drawY, heading, drawScale, fontScale, rotation);
            }
        }
    }

    private void drawPlayer(double drawX, double drawY, double heading, float drawScale, double fontScale, double rotation)
    {
        if (texture != null)
        {
            DrawUtil.drawEntity(drawX, drawY, heading, true, texture, drawScale * .75f, rotation);
        }
        float labelOffset = texture != null ? texture.height : 0;
        double labelX = drawX + (Math.cos(rotation+90));
        double labelY = drawY - labelOffset + (Math.sin(rotation+90));

        DrawUtil.drawLabel(entityDTO.entityLiving.getCommandSenderName(), drawX, drawY, DrawUtil.HAlign.Center, DrawUtil.VAlign.Above, Color.black, 205, Color.green, 255, fontScale, false, rotation);
        //DrawUtil.drawCenteredLabel(entityDTO.entityLiving.getCommandSenderName(), drawX, drawY - labelOffset, Color.black, 205, Color.green, 255, fontScale, rotation);
    }

    private void drawCreature(double drawX, double drawY, double heading, float drawScale, double fontScale, double rotation)
    {
        if (locatorTexture != null)
        {
            DrawUtil.drawEntity(drawX, drawY, heading, false, locatorTexture, drawScale, rotation);
        }

        if (texture != null)
        {
            DrawUtil.drawEntity(drawX, drawY, heading, true, texture, drawScale, rotation);
        }

        int labelYOffset = (texture != null) ? (texture.height / 2) : 8;
        if (entityDTO.customName != null)
        {
            DrawUtil.drawCenteredLabel(entityDTO.customName, drawX, drawY + labelYOffset, labelBg, labelBgAlpha, labelFg, labelFgAlpha, fontScale, rotation);
        }
    }

    public static class SimpleCacheLoader extends CacheLoader<EntityDTO, DrawEntityStep>
    {
        @Override
        public DrawEntityStep load(EntityDTO entityDTO) throws Exception
        {
            return new DrawEntityStep(entityDTO);
        }
    }
}
