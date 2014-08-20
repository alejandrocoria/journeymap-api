/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.render.draw;

import com.google.common.cache.CacheLoader;
import net.techbrew.journeymap.model.EntityDTO;
import net.techbrew.journeymap.model.EntityHelper;
import net.techbrew.journeymap.render.overlay.GridRenderer;
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
    int bottomMargin;
    boolean flip;

    private DrawEntityStep(EntityDTO entityDTO)
    {
        super();
        this.entityDTO = entityDTO;
    }

    public void update(boolean flip, TextureImpl locatorTexture, TextureImpl texture, int bottomMargin)
    {
        this.locatorTexture = locatorTexture;
        this.texture = texture;
        this.flip = flip;
        this.bottomMargin = bottomMargin;
    }

    @Override
    public void draw(double xOffset, double yOffset, GridRenderer gridRenderer, float drawScale, double fontScale)
    {
        if (entityDTO.entityLiving==null || entityDTO.entityLiving.isDead || !entityDTO.entityLiving.addedToChunk || entityDTO.entityLiving.isSneaking())
        {
            return;
        }

        Point2D pixel = gridRenderer.getPixel(entityDTO.entityLiving.posX, entityDTO.entityLiving.posZ);
        if (pixel != null)
        {
            double heading = EntityHelper.getHeading(entityDTO.entityLiving);
            double drawX = pixel.getX() + xOffset;
            double drawY = pixel.getY() + yOffset;

            if (locatorTexture != null)
            {
                DrawUtil.drawEntity(drawX, drawY, heading, false, locatorTexture, bottomMargin * 8, drawScale);
            }

            if(texture != null)
            {
                DrawUtil.drawEntity(drawX, drawY, heading, true, texture, bottomMargin, drawScale);
            }

            int labelYOffset = (texture != null) ? (texture.height/2) : 8;
            if (entityDTO.customName != null)
            {
                DrawUtil.drawCenteredLabel(entityDTO.customName, drawX, drawY + labelYOffset, labelBg, labelBgAlpha, labelFg, labelFgAlpha, fontScale);
            }

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
