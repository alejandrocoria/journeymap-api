/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.cartography;

import sun.awt.image.IntegerComponentRaster;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.util.Arrays;

/**
 * A simple combo-implementation of Paint and PaintContext designed for frequent reuse.
 * Based on code in java.awt.Color and java.awt.ColorPaintContext.
 */
class PixelPaint implements Paint, PaintContext
{
    final ColorModel colorModel = ColorModel.getRGBdefault();
    IntegerComponentRaster intRaster;
    Integer rgbColor;

    /**
     * Sets the rgb color to use.
     */
    Paint setColor(int rgbColor)
    {
        this.rgbColor = rgbColor;
        return this;
    }

    @Override
    public PaintContext createContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds, AffineTransform xform, RenderingHints hints)
    {
        return this; // The hints can all be ignored, including the ColorModel.
    }

    @Override
    public int getTransparency()
    {
        return Transparency.OPAQUE;
    }

    @Override
    public void dispose()
    {
        // Nothing needs to be reclaimed
    }

    @Override
    public ColorModel getColorModel()
    {
        return colorModel;
    }

    @Override
    public Raster getRaster(int x, int y, int w, int h)
    {
        synchronized (this)
        {
            IntegerComponentRaster raster = this.intRaster;

            if (raster == null || w > raster.getWidth() || h > raster.getHeight())
            {
                raster = (IntegerComponentRaster) getColorModel().createCompatibleWritableRaster(w, h);

                // Only reuse if the raster is for a single pixel
                if (w == 1 && h == 1)
                {
                    this.intRaster = raster;
                }
            }

            Arrays.fill(raster.getDataStorage(), this.rgbColor);

            return raster;
        }
    }
}
