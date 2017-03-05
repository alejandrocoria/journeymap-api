/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.render;

import java.awt.image.BufferedImage;
import java.util.stream.IntStream;

/**
 * Simple class extension to track whether the image has been altered.
 */
public class ComparableBufferedImage extends BufferedImage
{
    private boolean changed = false;

    /**
     * Instantiates a new Comparable buffered image.
     *
     * @param other the other
     */
    public ComparableBufferedImage(BufferedImage other)
    {
        super(other.getWidth(), other.getHeight(), other.getType());
        int width = other.getWidth();
        int height = other.getHeight();
        this.setRGB(0, 0, width, height, getPixelData(other), 0, width);
    }

    /**
     * Instantiates a new Comparable buffered image.
     *
     * @param width     the width
     * @param height    the height
     * @param imageType the image type
     */
    public ComparableBufferedImage(int width, int height, int imageType)
    {
        super(width, height, imageType);
    }

    @Override
    public synchronized void setRGB(int x, int y, int rgb)
    {
        if (!changed)
        {
            if (super.getRGB(x, y) != rgb)
            {
                changed = true;
            }
        }
        super.setRGB(x, y, rgb);
    }

    @Override
    public void setRGB(int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize)
    {
        // TODO: Usecase to justify counting these one at a time?
        super.setRGB(startX, startY, w, h, rgbArray, offset, scansize);
    }

    /**
     * Is changed boolean.
     *
     * @return the boolean
     */
    public boolean isChanged()
    {
        return changed;
    }

    /**
     * Sets changed.
     *
     * @param val the val
     */
    public void setChanged(boolean val)
    {
        this.changed = val;
    }

    /**
     * Identical to boolean.
     *
     * @param other the other
     * @return the boolean
     */
    public boolean identicalTo(BufferedImage other)
    {
        return areIdentical(getPixelData(), getPixelData(other));
    }

    /**
     * Are identical boolean.
     *
     * @param pixels      the pixels
     * @param otherPixels the other pixels
     * @return the boolean
     */
    public static boolean areIdentical(final int[] pixels, final int[] otherPixels)
    {
        return IntStream.range(0, pixels.length)
                .map(i -> ~pixels[i] | otherPixels[i])
                .allMatch(n -> n == ~0);
    }

    /**
     * Get pixel data int [ ].
     *
     * @return the int [ ]
     */
    public int[] getPixelData()
    {
        return getPixelData(this);
    }

    /**
     * Copy comparable buffered image.
     *
     * @return the comparable buffered image
     */
    public ComparableBufferedImage copy()
    {
        return new ComparableBufferedImage(this);
    }

    /**
     * Copy to.
     *
     * @param other the other
     */
    public void copyTo(BufferedImage other)
    {
        other.setRGB(0, 0, getWidth(), getHeight(), getPixelData(), 0, getWidth());
    }

    /**
     * Get pixel data int [ ].
     *
     * @param image the image
     * @return the int [ ]
     */
    public static int[] getPixelData(BufferedImage image)
    {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] data = new int[width * height];
        image.getRGB(0, 0, width, height, data, 0, width);
        return data;
    }
}
