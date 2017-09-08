/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.theme;

import com.google.common.base.Strings;
import com.google.gson.annotations.Since;
import journeymap.client.cartography.color.RGB;
import net.minecraftforge.fml.common.FMLLog;

import java.awt.*;

/**
 * Theme specification for JourneyMap 5.0
 */
public class Theme
{
    /**
     * Current version of this specification
     */
    public static final double VERSION = 2;

    /**
     * Theme schema.
     */
    @Since(2)
    public int schema;

    /**
     * Theme author.
     */
    @Since(1)
    public String author;

    /**
     * Theme name.
     */
    @Since(1)
    public String name;

    /**
     * Parent directory name of theme files.
     */
    @Since(1)
    public String directory;

    /**
     * Container specs for images in the /container directory.
     * Currently just Toolbar.
     */
    @Since(1)
    public Container container = new Container();

    /**
     * UI Control specs for images in the /control directory.
     * Currently: Button & Toggle.
     */
    @Since(1)
    public Control control = new Control();

    /**
     * FullMap Map specs for images in the /fullscreen directory.
     */
    @Since(1)
    public Fullscreen fullscreen = new Fullscreen();

    /**
     * General size for all icons in the /icon directory
     */
    @Since(1)
    public ImageSpec icon = new ImageSpec();

    /**
     * Circular Minimap specs for images in the /minimap/circle directory.
     */
    @Since(1)
    public Minimap minimap = new Minimap();

    /**
     * Color to hex string.
     *
     * @param color the color
     * @return the string
     */
    public static String toHexColor(Color color)
    {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * Color to hex string.
     *
     * @param rgb the color
     * @return the string
     */
    public static String toHexColor(int rgb)
    {
        return toHexColor(new Color(rgb));
    }

    /**
     * Hex string to Color int.
     *
     * @param hexColor the hex color
     * @return the color
     */
    private static int getColor(String hexColor)
    {
        if (!Strings.isNullOrEmpty(hexColor))
        {
            try
            {
                int color = Integer.parseInt(hexColor.replaceFirst("#", ""), 16);
                return color;
            }
            catch (Exception e)
            {
                FMLLog.warning("Journeymap theme has an invalid color string: " + hexColor);
            }
        }
        return RGB.WHITE_RGB;
    }

    @Override
    public String toString()
    {
        if (Strings.isNullOrEmpty(name))
        {
            return "???";
        }
        else
        {
            return name;
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        Theme theme = (Theme) o;

        if (directory != null ? !directory.equals(theme.directory) : theme.directory != null)
        {
            return false;
        }
        if (name != null ? !name.equals(theme.name) : theme.name != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (directory != null ? directory.hashCode() : 0);
        return result;
    }

    /**
     * Container class for images in /container.
     */
    public static class Container
    {
        /**
         * Specs for toolbar images in /container.
         */
        @Since(1)
        public Toolbar toolbar = new Toolbar();

        /**
         * Toolbar class for toolbar images in /container.
         */
        public static class Toolbar
        {
            /**
             * Specs for horizontal toolbar images.
             */
            @Since(1)
            public ToolbarSpec horizontal = new ToolbarSpec();

            /**
             * Specs for vertical toolbar images.
             */
            @Since(1)
            public ToolbarSpec vertical = new ToolbarSpec();

            /**
             * ToolbarSpec class. A toolbar consists of a beginning image, a
             * repeating inner image (one rep per button), and an end image.
             * <p/>
             * Filenames expected by the Theme loader are:
             * toolbar_begin.png, toolbar_inner.png, toolbar_end.png
             */
            public static class ToolbarSpec
            {
                /**
                 * True to use theme images, false for invisible.
                 */
                @Since(1)
                public boolean useThemeImages;

                /**
                 * Filename prefix. Example: "h" for horizontal, "v" for vertical.
                 */
                @Since(1)
                public String prefix = "";

                /**
                 * Margin in pixels around toolbar.
                 */
                @Since(1)
                public int margin;

                /**
                 * Padding in pixels between buttons in toolbar.
                 */
                @Since(1)
                public int padding;

                /**
                 * Image dimensions for the beginning of the toolbar.
                 */
                @Since(1)
                public ImageSpec begin;

                /**
                 * Image dimensions for the inner repeating section of the toolbar.
                 */
                @Since(1)
                public ImageSpec inner;

                /**
                 * Image dimensions for the end of the toolbar.
                 */
                @Since(1)
                public ImageSpec end;
            }
        }
    }

    /**
     * Control class for images in /control
     */
    public static class Control
    {
        /**
         * Specs for a normal button.
         */
        @Since(1)
        public ButtonSpec button = new ButtonSpec();

        /**
         * Specs for a toggle button.
         */
        @Since(1)
        public ButtonSpec toggle = new ButtonSpec();

        /**
         * Specification for a button.
         * Filenames expected by the Theme loader are:
         * on.png, off.png, hover.png, disabled.png
         */
        public static class ButtonSpec
        {
            /**
             * True to use theme images.  False to use current resource pack's button texture.
             */
            @Since(1)
            public boolean useThemeImages;

            /**
             * Button width.
             */
            @Since(1)
            public int width;

            /**
             * Button height.
             */
            @Since(1)
            public int height;

            /**
             * Filename prefix. Optional.
             */
            @Since(1)
            public String prefix = "";

            /**
             * String format style (using § control codes) for tooltips when the button is on.
             */
            @Since(1)
            public String tooltipOnStyle = "";

            /**
             * String format style (using § control codes) for tooltips when the button is off.
             */
            @Since(1)
            public String tooltipOffStyle = "";

            /**
             * String format style (using § control codes) for tooltips when the button is disabled.
             */
            @Since(1)
            public String tooltipDisabledStyle = "";

            /**
             * Hex color for icons when button is on.
             */
            @Since(2)
            public ColorSpec iconOn = new ColorSpec();

            /**
             * Hex color for icons when button is off.
             */
            @Since(2)
            public ColorSpec iconOff = new ColorSpec();

            /**
             * Hex color for icons when button is hovered and is on.
             */
            @Since(2)
            public ColorSpec iconHoverOn = new ColorSpec();

            /**
             * Hex color for icons when button is hovered and is off.
             */
            @Since(2)
            public ColorSpec iconHoverOff = new ColorSpec();

            /**
             * Hex color for icons when button is disabled.
             */
            @Since(2)
            public ColorSpec iconDisabled = new ColorSpec();

            /**
             * Hex color for buttons when button is on.
             */
            @Since(2)
            public ColorSpec buttonOn = new ColorSpec();

            /**
             * Hex color for buttons when button is off.
             */
            @Since(2)
            public ColorSpec buttonOff = new ColorSpec();

            /**
             * Hex color for buttons when button is hovered and is on.
             */
            @Since(2)
            public ColorSpec buttonHoverOn = new ColorSpec();

            /**
             * Hex color for buttons when button is hovered and is off.
             */
            @Since(2)
            public ColorSpec buttonHoverOff = new ColorSpec();

            /**
             * Hex color for buttons when button is disabled.
             */
            @Since(2)
            public ColorSpec buttonDisabled = new ColorSpec();
        }
    }

    /**
     * FullMap class for images in /fullscreen.
     */
    public static class Fullscreen
    {
        /**
         * Hex color for map background (behind tiles).
         */
        @Since(2)
        public ColorSpec background = new ColorSpec();

        /**
         * Hex color for background of status text on the bottom of the screen.
         */
        @Since(1)
        public LabelSpec statusLabel = new LabelSpec();
    }

    /**
     * Color and alpha.
     */
    public static class ColorSpec implements Cloneable
    {
        /**
         * Constructor.
         */
        public ColorSpec()
        {
        }

        /**
         * Constructor.
         */
        public ColorSpec(String color, float alpha)
        {
            this.color = color;
            this.alpha = alpha;
        }

        /**
         * Hex color.
         * Default is white
         */
        @Since(2)
        public String color = "#ffffff";
        private transient Integer _color;

        /**
         * Alpha transparency (0-1).
         * Default is 1.
         */
        @Since(2)
        public float alpha = 1;

        /**
         * Get the color as an rgb integer, caching the result.
         *
         * @return rgb
         */
        public int getColor()
        {
            if (_color == null)
            {
                _color = Theme.getColor(color);
            }
            return _color;
        }

        @Override
        public ColorSpec clone()
        {
            ColorSpec clone = new ColorSpec();
            clone.color = this.color;
            clone.alpha = this.alpha;
            return clone;
        }

    }

    /**
     * Image dimensions.
     */
    public static class ImageSpec extends ColorSpec
    {
        /**
         * Image width.
         */
        @Since(1)
        public int width;

        /**
         * Image height.
         */
        @Since(1)
        public int height;

        /**
         * Instantiates a new Image spec.
         */
        public ImageSpec()
        {
        }

        /**
         * Instantiates a new Image spec.
         *
         * @param width  the width
         * @param height the height
         */
        public ImageSpec(int width, int height)
        {
            this.width = width;
            this.height = height;
        }
    }

    /**
     * Class for images in /minimap
     */
    public static class Minimap
    {
        /**
         * Circular minimap specifications, corresponds to /minimap/circle
         */
        @Since(1)
        public MinimapCircle circle = new MinimapCircle();

        /**
         * Square minimap specifications, corresponds to /minimap/square
         */
        @Since(1)
        public MinimapSquare square = new MinimapSquare();

        /**
         * Shared minimap spec
         */
        public static abstract class MinimapSpec
        {
            /**
             * Minimum margin outside of minimap frame.
             */
            @Since(1)
            public int margin;

            /**
             * Label spec for top Info slots
             */
            @Since(2)
            public LabelSpec labelTop = new LabelSpec();

            /**
             * Whether info text should be inside the frame (true), or outside/above it (false)
             */
            @Since(2)
            public boolean labelTopInside = false;

            /**
             * Label spec for bottom Info slots
             */
            @Since(2)
            public LabelSpec labelBottom = new LabelSpec();

            /**
             * Whether info text should be inside the frame (true), or outside/above it (false)
             */
            @Since(2)
            public boolean labelBottomInside = false;

            /**
             * Label spec for showing compass points
             */
            @Since(1)
            public LabelSpec compassLabel = new LabelSpec();

            /**
             * Background image on which to place compass points.
             * Expecting filename "compass_point.png"
             */
            @Since(1)
            public ImageSpec compassPoint = new ImageSpec();

            /**
             * Number of pixels to pad around a compass point's key. Effects the scaled size of the compass point image.
             */
            @Since(1)
            public int compassPointLabelPad;

            /**
             * Number of pixels to shift the center of a compass point away from the map center.
             * Use this to adjust how it overlays the minimap frame.
             */
            @Since(1)
            public double compassPointOffset;

            /**
             * Whether to show the North compass point.
             * Style to true.
             */
            @Since(1)
            public boolean compassShowNorth = true;

            /**
             * Whether to show the South compass point.
             * Style to true.
             */
            @Since(1)
            public boolean compassShowSouth = true;

            /**
             * Whether to show the East compass point.
             * Style to true.
             */
            @Since(1)
            public boolean compassShowEast = true;

            /**
             * Whether to show the West compass point.
             * Style to true.
             */
            @Since(1)
            public boolean compassShowWest = true;

            /**
             * Number of pixels to shift the center of an "off-map" waypoint away from the map center.
             * Use this to adjust how it overlays the minimap frame.
             */
            @Since(1)
            public double waypointOffset;

            /**
             * Color and alpha for reticle, except for the heading segment.
             */
            @Since(2)
            public ColorSpec reticle = new ColorSpec();

            /**
             * Color and alpha for the heading segment of the reticle
             */
            @Since(2)
            public ColorSpec reticleHeading = new ColorSpec();

            /**
             * General reticle thickness in pixels.
             * Default is 2.25 pixels.
             */
            @Since(1)
            public double reticleThickness = 2.25;

            /**
             * Reticle thickness in pixels for the heading segment of reticle.
             * Default is 2.5 pixels.
             */
            @Since(1)
            public double reticleHeadingThickness = 2.5;

            /**
             * Number of pixels to shift the outer endpoint of a reticle segment away from the map edge.
             * Use this to adjust how it underlays the minimap frame.
             */
            @Since(2)
            public int reticleOffsetOuter = 16;

            /**
             * Number of pixels to shift the inner endpoint of a reticle segment away from the map center.
             * Default is 16.
             */
            @Since(2)
            public int reticleOffsetInner = 16;

            /**
             * Hex color to apply to frame image. Use #ffffff to keep unchanged.
             */
            @Since(2)
            public ColorSpec frame = new ColorSpec();

            /**
             * Image prefix (optional) for images in /minimap/square
             */
            @Since(1)
            public String prefix = "";
        }

        /**
         * Class for images in /minimap/circle.
         * <p/>
         * The mask defines the area of the minimap that will be shown.
         * The rim is the frame/overlay placed atop the minimap.
         * <p/>
         * Filenames expected by the Theme loader are:
         * mask_256.png, rim_256.png, mask_512.png, rim_512.png, compass_point.png
         * <p/>
         * Minimap sizes <=256 will use mask_256 and rim_256. Anything
         * larger will use mask_512 and rim_512.
         */
        public static class MinimapCircle extends MinimapSpec
        {
            /**
             * Size of rim_256.png (Ideally 256x256px)
             */
            @Since(1)
            public ImageSpec rim256 = new ImageSpec(256, 256);

            /**
             * Size of mask_256.png (Ideally 256x256px)
             */
            @Since(1)
            public ImageSpec mask256 = new ImageSpec(256, 256);

            /**
             * Size of rim_512.png (Ideally 512x512px)
             */
            @Since(1)
            public ImageSpec rim512 = new ImageSpec(512, 512);

            /**
             * Size of mask_512.png (Ideally 512x512px)
             */
            @Since(1)
            public ImageSpec mask512 = new ImageSpec(512, 512);

            /**
             * Whether the circle texture should rotate when the map rotates.
             * Necessary if compass points are integrated into the frame.
             */
            @Since(2)
            public boolean rotates = false;
        }

        /**
         * Class for images in /minimap/square.
         * <p/>
         * Filenames expected by the Theme loader are:
         * topleft.png, top.png, topright.png, left.png, right.png,
         * bottomleft.png, bottom.png, bottomright.png, compass_point.png
         * <p/>
         * Images are centered along the edges of the minimap area.
         */
        public static class MinimapSquare extends MinimapSpec
        {
            /**
             * Dimensions of topleft.png
             */
            @Since(1)
            public ImageSpec topLeft = new ImageSpec();

            /**
             * Dimensions of top.png. It will be stretched to fit minimap size.
             */
            @Since(1)
            public ImageSpec top = new ImageSpec();

            /**
             * Dimensions of topright.png
             */
            @Since(1)
            public ImageSpec topRight = new ImageSpec();


            /**
             * Dimensions of right.png. It will be stretched to fit minimap size.
             */
            @Since(1)
            public ImageSpec right = new ImageSpec();

            /**
             * Dimensions of bottomright.png
             */
            @Since(1)
            public ImageSpec bottomRight = new ImageSpec();

            /**
             * Dimensions of bottom.png. It will be stretched to fit minimap size.
             */
            @Since(1)
            public ImageSpec bottom = new ImageSpec();

            /**
             * Dimensions of bottomleft.png
             */
            @Since(1)
            public ImageSpec bottomLeft = new ImageSpec();

            /**
             * Dimensions of left.png. It will be stretched to fit minimap size.
             */
            @Since(1)
            public ImageSpec left = new ImageSpec();
        }
    }

    /**
     * Class for defining key characteristics.
     */
    public static class LabelSpec implements Cloneable
    {
        /**
         * Margin around label
         */
        @Since(2)
        public int margin = 2;

        /**
         * Color and alpha for label background.
         * Default background is not shown.
         */
        @Since(2)
        public ColorSpec background = new ColorSpec("#000000", 0);

        /**
         * Color and alpha for label text.
         */
        @Since(2)
        public ColorSpec foreground = new ColorSpec();


        @Since(2)
        public ColorSpec highlight = new ColorSpec();

        /**
         * Whether to use font shadow.
         * Default is false.
         */
        @Since(1)
        public boolean shadow = false;

        public LabelSpec clone()
        {
            LabelSpec clone = new LabelSpec();
            clone.margin = this.margin;
            clone.background = this.background.clone();
            clone.foreground = this.foreground.clone();
            clone.highlight = this.highlight.clone();
            return clone;
        }
    }

    /**
     * The basis of default.theme.json, which
     * is used to init a specific theme as the default.
     */
    public static class DefaultPointer
    {
        /**
         * Parent directory name of theme files.
         */
        @Since(1)
        public String directory;

        /**
         * Theme filename.
         */
        @Since(1)
        public String filename;

        /**
         * Theme name.
         */
        @Since(1)
        public String name;

        /**
         * Instantiates a new Default pointer.
         */
        protected DefaultPointer()
        {
        }

        /**
         * Instantiates a new Default pointer.
         *
         * @param theme the theme
         */
        public DefaultPointer(Theme theme)
        {
            this.name = theme.name;
            this.filename = theme.name;
            this.directory = theme.directory;
        }
    }
}
