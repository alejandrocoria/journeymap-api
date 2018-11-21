/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
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

    public ComparableBufferedImage(BufferedImage other)
    {
        super(other.getWidth(), other.getHeight(), other.getType());
        int width = other.getWidth();
        int height = other.getHeight();
        this.setRGB(0, 0, width, height, getPixelData(other), 0, width);
    }

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

    public boolean isChanged()
    {
        return changed;
    }

    public void setChanged(boolean val)
    {
        this.changed = val;
    }

    public boolean identicalTo(BufferedImage other)
    {
        return areIdentical(getPixelData(), getPixelData(other));
    }

    public static boolean areIdentical(final int[] pixels, final int[] otherPixels)
    {
        return IntStream.range(0, pixels.length)
                .map(i -> ~pixels[i] | otherPixels[i])
                .allMatch(n -> n == ~0);
    }

    public int[] getPixelData()
    {
        return getPixelData(this);
    }

    public ComparableBufferedImage copy()
    {
        return new ComparableBufferedImage(this);
    }

    public void copyTo(BufferedImage other)
    {
        other.setRGB(0, 0, getWidth(), getHeight(), getPixelData(), 0, getWidth());
    }

    public static int[] getPixelData(BufferedImage image)
    {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] data = new int[width * height];
        image.getRGB(0, 0, width, height, data, 0, width);
        return data;
    }
}
