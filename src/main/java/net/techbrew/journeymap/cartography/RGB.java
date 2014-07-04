package net.techbrew.journeymap.cartography;

import java.awt.*;
import java.util.Collection;

/**
 * Color operations utility class.
 */
public final class RGB
{
    transient private static final PixelPaint PIXEL_PAINT = new PixelPaint();

    /**
     * Don't instantiate.
     */
    private RGB() {}

    /**
     * Gets the PixelPaint singleton and sets the rgb color.
     */
    public static Paint paintOf(int rgb)
    {
        return PIXEL_PAINT.setColor(rgb);
    }

    /**
     * Returns an average color from a collection.
     *
     * @param colors
     * @return
     */
    public static Integer average(Collection<Integer> colors)
    {
        int[] out = {0,0,0};

        int used = 0;
        for (Integer color : colors)
        {
            if (color == null)
            {
                continue;
            }
            int[] cInts = ints(color);
            out[0] += cInts[0];
            out[1] += cInts[1];
            out[2] += cInts[2];

            used++;
        }

        if(used==0)
        {
            return null;
        }

        out[0] /= used;
        out[1] /= used;
        out[2] /= used;

        return toInteger(out);
    }

    /**
     * Returns an average color from an array.
     * Nulls allowed but ignored.
     *
     * @param colors
     * @return
     */
    public static Integer average(Integer... colors)
    {
        int[] out = {0,0,0};

        int used = 0;
        for (Integer color : colors)
        {
            if (color == null)
            {
                continue;
            }
            int[] cInts = ints(color);
            out[0] += cInts[0];
            out[1] += cInts[1];
            out[2] += cInts[2];

            used++;
        }

        if(used==0)
        {
            return null;
        }

        out[0] /= used;
        out[1] /= used;
        out[2] /= used;

        return toInteger(out);
    }

    public static Integer max(Integer... colors)
    {
        int[] out = {0,0,0};

        int used = 0;
        for (Integer color : colors)
        {
            if (color == null)
            {
                continue;
            }
            int[] cInts = ints(color);
            out[0] = Math.max(out[0], cInts[0]);
            out[1] = Math.max(out[1], cInts[1]);
            out[2] = Math.max(out[2], cInts[2]);

            used++;
        }

        if(used==0)
        {
            return null;
        }

        return toInteger(out);
    }


    public static int toInteger(float r, float g, float b)
    {
        return ((255 & 0xFF) << 24) |
                (((int) (r * 255 + 0.5) & 0xFF) << 16) |
                (((int) (g * 255 + 0.5) & 0xFF) << 8) |
                (((int) (b * 255 + 0.5) & 0xFF));
    }

    public static int toInteger(float[] rgb)
    {
        return ((255 & 0xFF) << 24) |
                (((int) (rgb[0] * 255 + 0.5) & 0xFF) << 16) |
                (((int) (rgb[1] * 255 + 0.5) & 0xFF) << 8) |
                (((int) (rgb[2] * 255 + 0.5) & 0xFF));
    }

    public static int toInteger(int r, int g, int b)
    {
        return ((255 & 0xFF) << 24) |
                ((r & 0xFF) << 16) |
                ((g & 0xFF) << 8) |
                ((b & 0xFF));
    }

    public static int toInteger(int[] rgb)
    {
        return ((255 & 0xFF) << 24) |
                ((rgb[0] & 0xFF) << 16) |
                ((rgb[1] & 0xFF) << 8) |
                ((rgb[2] & 0xFF));
    }

    public static Color toColor(Integer rgb)
    {
        return rgb == null ? null : new Color(rgb);
    }

    public static String toString(Integer rgb)
    {
        if(rgb==null)
        {
            return "null";
        }
        int[] ints = ints(rgb);
        return String.format("r=%s,g=%s,b=%s", ints[0], ints[1], ints[2]);
    }

    /**
     * Darken a color by a factor.
     */
    public static int darken(int rgb, float factor)
    {
        if(factor==1F) return rgb;
        return toInteger(clampFloats(floats(rgb), factor));
    }

    /**
     * Darken or lighten a color by a factor.
     * If darken, add a blue tint to simulate shadow.
     */
    public static int bevelSlope(int rgb, float factor)
    {
        final float bluer = (factor < 1) ? .8f : 1f;
        float[] floats = floats(rgb);
        floats[0] = floats[0] * bluer * factor;
        floats[1] = floats[1] * bluer * factor;
        floats[2] = floats[2] * factor;
        return toInteger(clampFloats(floats, 1f));
    }

    /**
     * Darken a color by a factor, add a blue tint for moonlight.
     */
    public static int moonlight(int rgb, float factor)
    {
        float[] floats = floats(rgb);
        floats[0] = floats[0] * factor;
        floats[1] = floats[1] * factor;
        floats[2] = floats[2] * (factor + .15f);
        return toInteger(clampFloats(floats, 1f));
    }

    public static int[] ints(int rgb)
    {
        return new int[]{(rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, (rgb) & 0xFF};
    }

    public static float[] floats(int rgb)
    {
        return new float[]{((rgb >> 16) & 0xFF)/255f, ((rgb >> 8) & 0xFF)/255f, ((rgb) & 0xFF)/255f};
    }

    /**
     * Blends otherRgb into rgb using alpha as a percentage.
     */
    public static int blendWith(int rgb, int otherRgb, float otherAlpha)
    {
        if(otherAlpha==1f)
        {
            return otherRgb;
        }
        if(otherAlpha==0f)
        {
            return rgb;
        }

        float[] floats = floats(rgb);
        float[] otherFloats = floats(otherRgb);

        floats[0] = otherFloats[0] * otherAlpha / 1f + floats[0] * (1 - otherAlpha);
        floats[1] = otherFloats[1] * otherAlpha / 1f + floats[1] * (1 - otherAlpha);
        floats[2] = otherFloats[2] * otherAlpha / 1f + floats[2] * (1 - otherAlpha);

        return toInteger(floats);
    }

    /**
     * Returns a float guaranteed to be between 0 and 1, inclusive.
     *
     * @param value
     * @return
     */
    public static float clampFloat(float value)
    {
        return value < 0f ? 0f : (value > 1f ? 1f : value);
    }

    /**
     * Returns an rgb array of floats clamped between 0 and 1 after a factor is applied.
     */
    public static float[] clampFloats(float[] rgbFloats, float factor)
    {
        float r = rgbFloats[0] * factor;
        float g = rgbFloats[1] * factor;
        float b = rgbFloats[2] * factor;
        rgbFloats[0] = r < 0f ? 0f : (r > 1f ? 1f : r);
        rgbFloats[1] = g < 0f ? 0f : (g > 1f ? 1f : g);
        rgbFloats[2] = b < 0f ? 0f : (b > 1f ? 1f : b);

        return rgbFloats;
    }

    /**
     * Returns an int guaranteed to be between 0 and 1, inclusive.
     *
     * @param value
     * @return
     */
    public static int clampInt(int value)
    {
        return value < 0 ? 0 : (value > 255 ? 255 : value);
    }




}
