package net.techbrew.journeymap.ui.theme;

import com.google.common.base.Strings;
import com.google.gson.annotations.Since;
import cpw.mods.fml.common.FMLLog;

import java.awt.*;

/**
 * Theme specification for JourneyMap 5.0
 */
public class Theme implements Comparable<Theme>
{
    /** Current version of this specification */
    public static final int VERSION = 1;

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
     * Fullscreen Map specs for images in the /fullscreen directory.
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
             *
             * Filenames expected by the Theme loader are:
             * toolbar_begin.png, toolbar_inner.png, toolbar_end.png
             */
            public static class ToolbarSpec
            {
                /**
                 * True to use theme images, false for invisible.
                 */
                @Since(1)
                public boolean useBackgroundImages;

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
         */
        public static class ButtonSpec
        {
            /**
             * True to use theme image.  False to use current resource pack's button texture.
             */
            @Since(1)
            public boolean useBackgroundImage;

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
            @Since(1)
            public String iconOnColor = "";

            /**
             * Hex color for icons when button is off.
             */
            @Since(1)
            public String iconOffColor = "";

            /**
             * Hex color for icons when button is hovered.
             */
            @Since(1)
            public String iconHoverColor = "";

            /**
             * Hex color for icons when button is disabled.
             */
            @Since(1)
            public String iconDisabledColor = "";
        }
    }

    /**
     * Fullscreen class for images in /fullscreen.
     */
    public static class Fullscreen
    {
        /**
         * Hex color for map background.
         */
        @Since(1)
        public String mapBackgroundColor = "";

        /**
         * Hex color for background of status text on the bottom of the screen.
         */
        @Since(1)
        public String statusBackgroundColor = "";

        /**
         * Hex color for foreground of status text on the bottom of the screen.
         */
        @Since(1)
        public String statusForegroundColor = "";
    }

    /**
     * Image dimensions.
     */
    public static class ImageSpec
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

        public ImageSpec()
        {
        }

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
             * Whether label shown at the top of the minimap (FPS) should
             * be inside the frame (true), or outside/above it (false)
             */
            @Since(1)
            public boolean labelTopInside;

            /**
             * Margin around top labels in minimap.
             */
            @Since(1)
            public int labelTopMargin;

            /**
             * Whether labels shown at the bottom of the minimap (location) should
             * be inside the frame (true), or outside/below it (false)
             */
            @Since(1)
            public boolean labelBottomInside;

            /**
             * Margin around bottom labels in minimap.
             */
            @Since(1)
            public int labelBottomMargin;

            /**
             * Hex color for background of labels (fps, biome, location).
             */
            @Since(1)
            public String labelBackgroundColor = "";

            /**
             * Alpha transparency (0-255) of background
             */
            @Since(1)
            public int labelBackgroundAlpha;

            /**
             * Hex color for foreground of labels (fps, biome, location).
             */
            @Since(1)
            public String labelForegroundColor = "";

            /**
             * Whether to use font shadows with the labels.
             */
            @Since(1)
            public boolean labelShadow;

            /***
             * Hex color to apply to frame image. Use #ffffff to keep unchanged.
             */
            @Since(1)
            public String frameColor = "";

            /**
             * Image prefix (optional) for images in /minimap/square
             */
            @Since(1)
            public String prefix = "";
        }

        /**
         * Class for images in /minimap/circle.
         *
         * The mask defines the area of the minimap that will be shown.
         * The rim is the frame/overlay placed atop the minimap.
         *
         * Filenames expected by the Theme loader are:
         * mask_256.png, rim_256.png, mask_512.png, rim_512.png
         *
         * Minimap sizes <=256 will use mask_256 and rim_256. Anything
         * larger will use mask_512 and rim_512.
         */
        public static class MinimapCircle extends MinimapSpec
        {
            /**
             * Size of rim_256.png (Ideally 256x256px)
             */
            @Since(1)
            public ImageSpec rim256 = new ImageSpec(256,256);

            /**
             * Size of mask_256.png (Ideally 256x256px)
             */
            @Since(1)
            public ImageSpec mask256 = new ImageSpec(256,256);

            /**
             * Size of rim_512.png (Ideally 512x512px)
             */
            @Since(1)
            public ImageSpec rim512 = new ImageSpec(512,512);

            /**
             * Size of mask_512.png (Ideally 512x512px)
             */
            @Since(1)
            public ImageSpec mask512 = new ImageSpec(512,512);
        }

        /**
         * Class for images in /minimap/square.
         *
         * Filenames expected by the Theme loader are:
         * topleft.png, top.png, topright.png, left.png, right.png, bottomleft.png, bottom.png, bottomright.png
         *
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
     * Color to hex string.
     */
    public static String toHexColor(Color color)
    {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * Hex string to Color.
     */
    public static Color getColor(String hexColor)
    {
        if(!Strings.isNullOrEmpty(hexColor))
        {
            try
            {
                int color = Integer.parseInt(hexColor.replaceFirst("#", ""), 16);
                return new Color(color);
            }
            catch (Exception e)
            {
                FMLLog.warning("Journeymap theme has an invalid color string: " + hexColor);

            }
        }
        return Color.white;
    }

    @Override
    public String toString()
    {
        if(Strings.isNullOrEmpty(name)) {
            return "???";
        } else {
            return name;
        }
    }

    @Override
    public int compareTo(Theme other)
    {
        if(Strings.isNullOrEmpty(name)) {
            return Strings.isNullOrEmpty(other.name) ? 0 : 1;
        }
        return name.compareTo(other.name);
    }
}
