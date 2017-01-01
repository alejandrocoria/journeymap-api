package journeymap.client.render;

import java.awt.image.BufferedImage;
import java.util.stream.IntStream;

/**
 * Simple class extension to track whether the image has been altered.
 */
public class MonitoredBufferedImage extends BufferedImage
{
    private boolean changed = false;

    public MonitoredBufferedImage(BufferedImage other)
    {
        super(other.getWidth(), other.getHeight(), other.getType());
        int width = other.getWidth();
        int height = other.getHeight();
        int[] otherPixels = new int[width * height];
        other.getRGB(0, 0, width, height, otherPixels, 0, width);
        this.setRGB(0, 0, width, height, otherPixels, 0, width);
    }

    public MonitoredBufferedImage(int width, int height, int imageType)
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
        return identicalTo(other, this.getWidth(), this.getHeight());
    }

    public boolean identicalTo(BufferedImage other, int width, int height)
    {
        int[] pixels = new int[width * height];
        this.getRGB(0, 0, width, height, pixels, 0, width);

        int[] otherPixels = new int[width * height];
        other.getRGB(0, 0, width, height, otherPixels, 0, width);

        boolean unchanged = IntStream.range(0, pixels.length)
                .map(i -> ~pixels[i] | otherPixels[i])
                .allMatch(n -> n == ~0);

        return unchanged;
    }
}
