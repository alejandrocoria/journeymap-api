/*
 * JourneyMap API (http://journeymap.info)
 * http://bitbucket.org/TeamJM/journeymap-api
 *
 * Copyright (c) 2011-2016 Techbrew.  All Rights Reserved.
 * The following limited rights are granted to you:
 *
 * You MAY:
 *  + Write your own code that uses the API source code in journeymap.* packages as a dependency.
 *  + Write and distribute your own code that uses, modifies, or extends the example source code in example.* packages
 *  + Fork and modify any source code for the purpose of submitting Pull Requests to the TeamJM/journeymap-api repository.
 *    Submitting new or modified code to the repository means that you are granting Techbrew all rights to the submitted code.
 *
 * You MAY NOT:
 *  - Distribute source code or classes (whether modified or not) from journeymap.* packages.
 *  - Submit any code to the TeamJM/journeymap-api repository with a different license than this one.
 *  - Use code or artifacts from the repository in any way not explicitly granted by this license.
 *
 */

package journeymap.client.api.model;

import com.google.common.base.Objects;
import journeymap.client.api.display.Displayable;

/**
 * Defines attributes needed to display text on the map.
 * <p>
 * Setters use the Builder pattern so they can be chained.
 */
public class MapText<T extends MapText>
{
    /**
     * Default font scale.
     */
    public static float DEFAULT_SCALE = 1;

    /**
     * Default font color.
     */
    public static int DEFAULT_COLOR = 0xffffff;

    /**
     * Default font opacity.
     */
    public static float DEFAULT_OPACITY = 1.0f;

    /**
     * Default background color.
     */
    public static int DEFAULT_BACKGROUND_COLOR = 0x000000;

    /**
     * Default background opacity.
     */
    public static float DEFAULT_BACKGROUND_OPACITY = 0.7f;

    /**
     * Default minimum zoom level where text is visible.
     */
    public static int DEFAULT_MIN_ZOOM = 0;

    /**
     * Default maximum zoom level where text is visible.
     */
    public static int DEFAULT_MAX_ZOOM = 0;

    /**
     * Default horizontal offset of text in pixels.
     */
    public static int DEFAULT_OFFSET_X = 0;

    /**
     * Default vertical offset of text in pixels.
     */
    public static int DEFAULT_OFFSET_Y = 0;

    /**
     * Default of whether to display a font shadow.
     */
    public static boolean DEFAULT_FONT_SHADOW = true;
    
    protected Float scale;
    protected Integer color;
    protected Integer backgroundColor;
    protected Float opacity;
    protected Float backgroundOpacity;
    protected Boolean fontShadow;
    protected Integer minZoom;
    protected Integer maxZoom;
    protected Integer offsetX;
    protected Integer offsetY;

    /**
     * Default constructor.
     */
    public MapText()
    {
    }

    /**
     * Constructor to copy another MapText.
     * @param other another instance
     */
    public MapText(MapText other)
    {
        this.scale = other.scale;
        this.color = other.color;
        this.backgroundColor = other.backgroundColor;
        this.opacity = other.opacity;
        this.backgroundOpacity = other.backgroundOpacity;
        this.fontShadow = other.fontShadow;
        this.minZoom = other.minZoom;
        this.maxZoom = other.maxZoom;
        this.offsetX = other.offsetX;
        this.offsetY = other.offsetY;
    }

    /**
     * Font scale.
     *
     * @return 1 scale
     */
    public float getScale()
    {
        return scale==null ? DEFAULT_SCALE : scale;
    }

    /**
     * Sets the font scale. Best results are powers of 2: 1,2,4,8.
     * Range is 1f - 8f;
     *
     * @param scale the scale
     * @return this
     */
    public T setScale(float scale)
    {
        this.scale = Math.max(1f, Math.min(scale, 8f));
        return (T) this;
    }

    /**
     * Gets the font color.
     *
     * @return rgb color or DEFAULT_COLOR if not set.
     */
    public int getColor()
    {
        return color==null ? DEFAULT_COLOR : color;
    }

    /**
     * Sets the font color (rgb).  Range is 0x000000 - 0xffffff.
     *
     * @param color rgb
     * @return this
     */
    public T setColor(int color)
    {
        this.color = Displayable.clampRGB(color);
        return (T) this;
    }

    /**
     * Gets background color.
     *
     * @return the background color or DEFAULT_BACKGROUND_COLOR if not set.
     */
    public int getBackgroundColor()
    {
        return backgroundColor==null ? DEFAULT_BACKGROUND_COLOR : backgroundColor;
    }

    /**
     * Sets background color.
     *
     * @param backgroundColor the background color
     * @return this
     */
    public T setBackgroundColor(int backgroundColor)
    {
        this.backgroundColor = Displayable.clampRGB(backgroundColor);
        return (T) this;
    }

    /**
     * Gets opacity.
     *
     * @return the opacity or DEFAULT_OPACITY if not set.
     */
    public float getOpacity()
    {
        return opacity==null ? DEFAULT_OPACITY : opacity;
    }

    /**
     * Sets opacity.
     *
     * @param opacity the opacity
     * @return this
     */
    public T setOpacity(float opacity)
    {
        this.opacity = Displayable.clampOpacity(opacity);
        return (T) this;
    }

    /**
     * Gets background opacity.
     *
     * @return the background opacity or DEFAULT_BACKGROUND_OPACITY if not set.
     */
    public float getBackgroundOpacity()
    {
        return backgroundOpacity==null ? DEFAULT_BACKGROUND_OPACITY : backgroundOpacity;
    }

    /**
     * Sets background opacity.  Range is 0f - 1f.
     *
     * @param backgroundOpacity the background opacity
     * @return this
     */
    public T setBackgroundOpacity(float backgroundOpacity)
    {
        this.backgroundOpacity = Displayable.clampOpacity(backgroundOpacity);
        return (T) this;
    }

    /**
     * Whether font shadow should be used.
     *
     * @return true if shadowed or DEFAULT_FONT_SHADOW if not set.
     */
    public boolean hasFontShadow()
    {
        return fontShadow==null ? DEFAULT_FONT_SHADOW : fontShadow;
    }

    /**
     * Sets whether font shadow should be used.
     *
     * @param fontShadow true if shadow
     * @return this
     */
    public T setFontShadow(boolean fontShadow)
    {
        this.fontShadow = fontShadow;
        return (T) this;
    }

    /**
     * The minimum zoom level (0 is lowest) where the text should be visible.
     *
     * @return the min zoom or DEFAULT_MIN_ZOOM if not set.
     */
    public int getMinZoom()
    {
        return minZoom==null ? DEFAULT_MIN_ZOOM : minZoom;
    }

    /**
     * Sets the minimum zoom level (0 is lowest) where text should be visible.
     *
     * @param minZoom the min zoom
     * @return this
     */
    public T setMinZoom(int minZoom)
    {
        this.minZoom = Math.max(0, minZoom);
        return (T) this;
    }

    /**
     * The maximum zoom level (8 is highest) where text should be visible.
     *
     * @return the max zoom or DEFAULT_MAX_ZOOM if not set.
     */
    public int getMaxZoom()
    {
        return maxZoom==null ? DEFAULT_MAX_ZOOM : maxZoom;
    }

    /**
     * Sets the maximum zoom level (8 is highest) where the polygon should be visible.
     *
     * @param maxZoom the max zoom
     * @return this
     */
    public T setMaxZoom(int maxZoom)
    {
        this.maxZoom = Math.min(8, maxZoom);
        return (T) this;
    }

    /**
     * Gets how many horizontal pixels to shift the center of the label from the center of the overlay.
     *
     * @return pixels to offset or DEFAULT_OFFSET_X if not set.
     */
    public int getOffsetX()
    {
        return offsetX==null ? DEFAULT_OFFSET_X : offsetX;
    }

    /**
     * Sets how many horizontal pixels to shift the center of the label from the center of the overlay.
     * (For MarkerOverlays, the "center" is directly over MarkerOverlay.getPoint(), regardless of how
     * it's icon is placed.)
     *
     * @param offsetX pixels
     * @return this
     */
    public T setOffsetX(int offsetX)
    {
        this.offsetX = offsetX;
        return (T) this;
    }

    /**
     * Gets how many vertical pixels to shift the center of the label from the center of the overlay.
     *
     * @return pixels to offset or DEFAULT_OFFSET_Y if not set.
     */
    public int getOffsetY()
    {
        return offsetY==null ? DEFAULT_OFFSET_Y : offsetY;
    }

    /**
     * Sets how many vertical pixels to shift the center of the label from the center of the overlay.
     * (For MarkerOverlays, the "center" is directly over MarkerOverlay.getPoint(), regardless of how
     * it's icon is placed.)
     *
     * @param offsetY pixels
     * @return this
     */
    public T setOffsetY(int offsetY)
    {
        this.offsetY = offsetY;
        return (T) this;
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("backgroundColor", backgroundColor)
                .add("backgroundOpacity", backgroundOpacity)
                .add("color", color)
                .add("opacity", opacity)
                .add("fontShadow", fontShadow)
                .add("maxZoom", maxZoom)
                .add("minZoom", minZoom)
                .add("offsetX", offsetX)
                .add("offsetY", offsetY)
                .add("scale", scale)
                .toString();
    }
}
