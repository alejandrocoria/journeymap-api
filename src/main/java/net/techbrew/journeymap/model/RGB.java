package net.techbrew.journeymap.model;

import java.awt.*;
import java.util.Arrays;
import java.util.Collection;

/**
 * A mutable alternative to java.awt.Color that is
 * suited for float-based color manipulations.
 */
public final class RGB {

    private final float[] frgb = new float[3];

    /**
     * Default constructor. Color is black.
     */
    private RGB() {
    }

    /**
     * Constructor from floats.
     * @param red
     * @param green
     * @param blue
     */
    public RGB(float red, float green, float blue) {
        frgb[0] = clamp(red);
        frgb[1] = clamp(green);
        frgb[1] = clamp(blue);
    }

    /**
     * Constructor from a java.awt.Color
     * @param original
     */
    public RGB(Color original) {
        if(original==null) {
            throw new IllegalArgumentException("Color may not be null");
        }
        setFrom(original.getRGB());
    }

    /**
     * Sets the values from an integer sRGB.
     * Alpha is ignored.
     * @param newColor
     */
    public void setFrom(int newColor) {
        frgb[0] = ((float) ((newColor >> 16) & 0xFF)) /255f;
        frgb[1] = ((float) ((newColor >> 8) & 0xFF)) /255f;
        frgb[2] = ((float) ((newColor >> 0) & 0xFF)) /255f;
    }

    /**
     * Returns an average color from a collection.
     * @param colors
     * @return
     */
    public static RGB average(Collection<RGB> colors)
    {
        RGB avg = new RGB();
        for(RGB color : colors) {
            avg.frgb[0] += color.frgb[0];
            avg.frgb[1] += color.frgb[1];
            avg.frgb[2] += color.frgb[2];
        }
        final int count = colors.size();

        avg.frgb[0] /= count;
        avg.frgb[1] /= count;
        avg.frgb[2] /= count;

        return avg;
    }

    /**
     * Darken a color by a factor.
     * @param factor
     * @return
     */
    public void darken(float factor) {
        frgb[0] = clamp(frgb[0] * factor);
        frgb[1] = clamp(frgb[1] * factor);
        frgb[2] = clamp(frgb[2] * factor);
    }

    /**
     * Darken or lighten a color by a factor.
     * If darken, add a blue tint to simulate shadow.
     * @param factor
     * @return
     */
    public void bevelSlope(float factor) {
        final float bluer = (factor<1) ? .8f : 1f;
        frgb[0] = clamp(frgb[0] * bluer * factor);
        frgb[1] = clamp(frgb[1] * bluer * factor);
        frgb[2] = clamp(frgb[2] * factor);
    }

    /**
     * Darken a color by a factor, add a blue tint for moonlight.
     * @param factor
     * @return
     */
    public void moonlight(float factor) {
        frgb[0] = clamp(frgb[0] * factor);
        frgb[1] = clamp(frgb[1] * factor);
        frgb[2] = clamp(frgb[2] * (factor+.1f));
    }

    /**
     * Adjust color to indicate it's outside but on the surface.
     * @return
     */
    public void ghostSurface() {

        //Color c = toColor();

        final float factor = .4f;
        float hsb[] = Color.RGBtoHSB((int) (frgb[0]*255+0.5), (int) (frgb[1]*255+0.5), (int) (frgb[2]*255+0.5), null);
        int grey = Color.HSBtoRGB(hsb[0], 0, hsb[2]);
        this.setFrom(grey);
        frgb[0] = ((frgb[0]+.5f)/2*factor);
        frgb[1] = ((frgb[1]+.5f)/2*factor);
        frgb[2] = ((frgb[2]+.6f)/2*factor);
    }

    /**
     * Provides a java.awt.Color representation.
     * @return
     */
    public Color toColor() {
        return new Color(frgb[0], frgb[1], frgb[2]);
    }

    /**
     * Returns a float guaranteed to be between 0 and 1, inclusive.
     * @param value
     * @return
     */
    private float clamp(float value)
    {
        return value < 0f ? 0f : (value > 1f ? 1f : value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RGB rgb = (RGB) o;
        if (!Arrays.equals(frgb, rgb.frgb)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(frgb);
    }
}
