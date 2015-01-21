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
import net.techbrew.journeymap.JourneyMap;
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
    boolean hideSneaks = JourneyMap.getCoreProperties().hideSneakingEntities.get();
    boolean showHeading = true;

    EntityDTO entityDTO;
    TextureImpl texture;
    TextureImpl locatorTexture;
    boolean flip;

    private DrawEntityStep(EntityDTO entityDTO)
    {
        super();
        this.entityDTO = entityDTO;
    }

    public void update(boolean flip, TextureImpl locatorTexture, TextureImpl texture, boolean showHeading)
    {
        this.locatorTexture = locatorTexture;
        this.texture = texture;
        this.flip = flip;
        this.showHeading = showHeading;
    }

    @Override
    public void draw(double xOffset, double yOffset, GridRenderer gridRenderer, float drawScale, double fontScale, double rotation)
    {
        if (entityDTO.entityLiving == null || entityDTO.entityLiving.isDead || !entityDTO.entityLiving.addedToChunk || (hideSneaks && entityDTO.entityLiving.isSneaking()))
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
                float labelOffset = texture != null ? texture.getHeight() / blockSize : 0;
                drawPlayer(drawX, drawY, gridRenderer, heading, drawScale, fontScale, rotation);

            }
            else
            {
                drawCreature(drawX, drawY, gridRenderer, heading, drawScale, fontScale, rotation);
            }
        }
    }

    private void drawPlayer(double drawX, double drawY, GridRenderer gridRenderer, double heading, float drawScale, double fontScale, double rotation)
    {
        if (locatorTexture != null && showHeading)
        {
            DrawUtil.drawEntity(drawX, drawY, heading, false, locatorTexture, drawScale, rotation);
        }

        if (texture != null)
        {
            DrawUtil.drawEntity(drawX, drawY, heading, true, texture, drawScale * .75f, rotation);
        }
        int labelOffset = texture == null ? 0 : rotation == 0 ? -texture.getHeight() / 2 : texture.getHeight() / 2;
        Point2D labelPoint = gridRenderer.shiftWindowPosition(drawX, drawY, 0, -labelOffset);

        DrawUtil.drawLabel(entityDTO.entityLiving.getCommandSenderName(), labelPoint.getX(), labelPoint.getY(), DrawUtil.HAlign.Center, DrawUtil.VAlign.Middle, Color.black, 205, Color.green, 255, fontScale, false, rotation);
        //DrawUtil.drawCenteredLabel(entityDTO.entityLiving.getCommandSenderName(), drawX, drawY - labelOffset, Color.black, 205, Color.green, 255, fontScale, rotation);
    }

    private void drawCreature(double drawX, double drawY, GridRenderer gridRenderer, double heading, float drawScale, double fontScale, double rotation)
    {
        if (locatorTexture != null && showHeading)
        {
            DrawUtil.drawEntity(drawX, drawY, heading, false, locatorTexture, drawScale, rotation);
        }

        if (texture != null)
        {
            DrawUtil.drawEntity(drawX, drawY, heading, true, texture, drawScale, rotation);
        }

        int labelOffset = texture == null ? 8 : rotation == 0 ? texture.getHeight() : -texture.getHeight();
        if (entityDTO.customName != null)
        {
            Point2D labelPoint = gridRenderer.shiftWindowPosition(drawX, drawY, 0, labelOffset);
            DrawUtil.drawCenteredLabel(entityDTO.customName, labelPoint.getX(), labelPoint.getY(), labelBg, labelBgAlpha, labelFg, labelFgAlpha, fontScale, rotation);
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
