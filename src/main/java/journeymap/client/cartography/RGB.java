/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.cartography;

import java.awt.*;
import java.nio.ByteOrder;
import java.util.Collection;

/**
 * Color operations utility class.
 */
public final class RGB
{
    transient private static final PixelPaint PIXEL_PAINT = new PixelPaint();
    public static final int ALPHA_OPAQUE = 0xff000000;
    public static final int BLACK = -16777216;

    /**
     * Don't instantiate.
     */
    private RGB()
    {
    }

    /**
     * Gets the PixelPaint singleton and sets the rgb color.
     */
    public static Paint paintOf(int rgb)
    {
        return PIXEL_PAINT.setColor(ALPHA_OPAQUE | rgb);
    }

    /**
     * Returns an average color from a collection.
     *
     * @param colors
     * @return
     */
    public static Integer average(Collection<Integer> colors)
    {
        int[] out = {0, 0, 0};

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

        if (used == 0)
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
        int[] out = {0, 0, 0};

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

        if (used == 0)
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
        int[] out = {0, 0, 0};

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

        if (used == 0)
        {
            return null;
        }

        return toInteger(out);
    }


    public static int toInteger(float r, float g, float b)
    {
        return ((0xFF) << 24) |
                (((int) (r * 255 + 0.5) & 0xFF) << 16) |
                (((int) (g * 255 + 0.5) & 0xFF) << 8) |
                (((int) (b * 255 + 0.5) & 0xFF));
    }

    public static int toInteger(float[] rgb)
    {
        return ((0xFF) << 24) |
                (((int) (rgb[0] * 255 + 0.5) & 0xFF) << 16) |
                (((int) (rgb[1] * 255 + 0.5) & 0xFF) << 8) |
                (((int) (rgb[2] * 255 + 0.5) & 0xFF));
    }

    public static int toInteger(int r, int g, int b)
    {
        return ((0xFF) << 24) |
                ((r & 0xFF) << 16) |
                ((g & 0xFF) << 8) |
                ((b & 0xFF));
    }

    public static int toInteger(int[] rgb)
    {
        return  ((0xFF) << 24) |
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
        if (rgb == null)
        {
            return "null";
        }
        int[] ints = ints(rgb);
        return String.format("r=%s,g=%s,b=%s", ints[0], ints[1], ints[2]);
    }

    public static String toHexString(Integer rgb)
    {
        int[] ints = ints(rgb);
        return String.format("#%02x%02x%02x", ints[0], ints[1], ints[2]);
    }

    /**
     * Darken/Lighten a color by a factor.
     */
    public static int adjustBrightness(int rgb, float factor)
    {
        if (factor == 1F)
        {
            return rgb;
        }
        return toInteger(clampFloats(floats(rgb), factor));
    }

    /**
     * Magic number adjustments.  Do not examine too closely.
     */
    public static int biomeDarken(int rgb)
    {
        float[] floats = floats(rgb);
        float r,g,b;
        r = floats[0]*floats[0]*.236f;
        g = floats[1]*floats[1]*.601f;
        b = floats[2]*floats[2]*.163f;
        return toInteger(r,g,b);
    }

    /**
     * Darken or lighten a color by a factor.
     * If adjustBrightness, add a blue tint to simulate shadow.
     */
    public static int bevelSlope(int rgb, float factor)
    {
        final float bluer = (factor < 1) ? .85f : 1f;
        float[] floats = floats(rgb);
        floats[0] = floats[0] * bluer * factor;
        floats[1] = floats[1] * bluer * factor;
        floats[2] = floats[2] * factor;
        return toInteger(clampFloats(floats, 1f));
    }

    /**
     * Darken a color by a factor, add a fog tint.
     */
    public static int darkenAmbient(int rgb, float factor, float[] ambient)
    {
        float[] floats = floats(rgb);
        floats[0] = floats[0] * (factor + ambient[0]);
        floats[1] = floats[1] * (factor + ambient[1]);
        floats[2] = floats[2] * (factor + ambient[2]);
        return toInteger(clampFloats(floats, 1f));
    }

    public static int[] ints(int rgb)
    {
        return new int[]{(rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, (rgb) & 0xFF};
    }

    public static float[] floats(int rgb)
    {
        return new float[]{((rgb >> 16) & 0xFF) / 255f, ((rgb >> 8) & 0xFF) / 255f, ((rgb) & 0xFF) / 255f};
    }

    /**
     * Blends otherRgb into rgb using alpha as a percentage.
     */
    public static int blendWith(int rgb, int otherRgb, float otherAlpha)
    {
        if (otherAlpha == 1f)
        {
            return otherRgb;
        }
        if (otherAlpha == 0f)
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
     * Adjust color rgb using a multiplier
     * @param rgb
     * @param multiplier
     * @return
     */
    public static int multiply(int rgb, int multiplier)
    {
        float[] rgbFloats = floats(rgb);
        float[] multFloats = floats(multiplier);

        rgbFloats[0] = rgbFloats[0]*multFloats[0];
        rgbFloats[1] = rgbFloats[1]*multFloats[1];
        rgbFloats[2] = rgbFloats[2]*multFloats[2];

        return toInteger(rgbFloats);
    }

    public static int multiply2(int rgb, int multiplier)
    {
        float[] multFloats = floats(multiplier);
        float rMult = multFloats[0];
        float gMult = multFloats[1];
        float bMult = multFloats[2];

        int rgb2 = rgb;
        int r;
        int g;
        int b;

        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN)
        {
            r = (int)((float)(rgb2 & 255) * rMult);
            g = (int)((float)(rgb2 >> 8 & 255) * gMult);
            b = (int)((float)(rgb2 >> 16 & 255) * bMult);
            rgb2 &= -16777216;
            rgb2 |= b << 16 | g << 8 | r;
        }
        else
        {
            r = (int)((float)(rgb2 >> 24 & 255) * rMult);
            g = (int)((float)(rgb2 >> 16 & 255) * gMult);
            b = (int)((float)(rgb2 >> 8 & 255) * bMult);
            rgb2 &= 255;
            rgb2 |= r << 24 | g << 16 | b << 8;
        }

        return rgb2;
    }

    /**
     * Adjust color rgb using a diff
     * @param rgb
     * @param diff
     * @return
     */
    public static int subtract(int rgb, int diff)
    {
        int alpha1 = rgb >> 24 & 0xFF;
        int red1 = rgb >> 16 & 0xFF;
        int green1 = rgb >> 8 & 0xFF;
        int blue1 = rgb & 0xFF;

        int alpha2 = diff >> 24 & 0xFF;
        int red2 = diff >> 16 & 0xFF;
        int green2 = diff >> 8 & 0xFF;
        int blue2 = diff & 0xFF;

        int alpha = clampInt(alpha1 - alpha2);
        int red = clampInt(red1 - red2);
        int green = clampInt(green1 - green2);
        int blue = clampInt(blue1 - blue2);

        int result = (alpha & 0xFF) << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;

        return result | -16777216;
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
