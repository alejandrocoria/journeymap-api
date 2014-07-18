/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.render.draw;

import net.techbrew.journeymap.render.overlay.GridRenderer;
import net.techbrew.journeymap.render.texture.TextureImpl;

import java.awt.geom.Point2D;

/**
 * Created by mwoodman on 12/26/13.
 */
public class DrawRotatedImageStep implements DrawStep
{

    final double posX;
    final double posZ;
    final TextureImpl texture;
    final float heading;

    public DrawRotatedImageStep(double posX, double posZ, TextureImpl texture, float heading)
    {
        super();
        this.posX = posX;
        this.posZ = posZ;
        this.texture = texture;
        this.heading = heading;
    }

    @Override
    public void draw(double xOffset, double yOffset, GridRenderer gridRenderer, float drawScale, double fontScale)
    {
        Point2D pixel = gridRenderer.getPixel(posX, posZ);
        if (pixel != null)
        {
            DrawUtil.drawRotatedImage(texture, pixel.getX() + xOffset, pixel.getY() + yOffset, heading, drawScale);
        }
    }
}
