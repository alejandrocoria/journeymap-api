package journeymap.client.cartography;

import journeymap.common.Journeymap;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Wraps arrays used to set colors and alphas for a chunk image,
 * does the actual update on Graphics2D in a single method to
 * try to be as efficient as possible.
 */
public class ChunkPainter
{
    public static final AlphaComposite ALPHA_OPAQUE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1F);
    public static final int COLOR_BLACK = Color.black.getRGB();
    public static final int COLOR_VOID = RGB.toInteger(17, 12, 25);
    protected static volatile AtomicLong badBlockCount = new AtomicLong(0);

    BufferedImage image;

    public ChunkPainter(BufferedImage image)
    {
        this.image = image;
    }

    /**
     * Darken the existing color.
     */
    public void paintDimOverlay(int x, int z, float alpha)
    {
        Integer color = image.getRGB(x, z);

        if (color != null)
        {
            paintBlock(x, z, RGB.adjustBrightness(color, alpha));
        }
    }

    /**
     * Paint the block.
     */
    public void paintBlock(final int x, final int z, final int color)
    {
        image.setRGB(x, z, color);
    }

    /**
     * Paint the void.
     */
    public void paintVoidBlock(final int x, final int z)
    {
        paintBlock(x, z, COLOR_VOID);
    }

    /**
     * Paint the void.
     */
    public void paintBlackBlock(final int x, final int z)
    {
        paintBlock(x, z, COLOR_BLACK);
    }

    /**
     * It's a problem
     */
    public void paintBadBlock(final int x, final int y, final int z)
    {
        long count = badBlockCount.incrementAndGet();
        if (count == 1 || count % 10240 == 0)
        {
            Journeymap.getLogger().warn(
                    "Bad block at " + x + "," + y + "," + z + ". Total bad blocks: " + count
            );
        }
    }

    /**
     * Paint the blocks.  ChunkPainter can't be used after calling.
     */
    public void finishPainting()
    {
        // nothing to do

    }
}
