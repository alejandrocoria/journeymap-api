package net.techbrew.journeymap.ui.theme;

import com.google.common.base.Strings;
import com.google.gson.annotations.Since;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.io.ThemeFileHandler;

import java.awt.*;

/**
 * Created by Mark on 9/2/2014.
 */
public class Theme implements Comparable<Theme>
{
    public static final int VERSION = 1;

    @Since(1)
    public String author;
    @Since(1)
    public String name;
    @Since(1)
    public String directory;
    @Since(1)
    public Container container = new Container();
    @Since(1)
    public Control control = new Control();
    @Since(1)
    public Fullscreen fullscreen = new Fullscreen();
    @Since(1)
    public ImageSpec icon = new ImageSpec();
    @Since(1)
    public Minimap minimap = new Minimap();


    public static class Container
    {
        @Since(1)
        public Toolbar toolbar = new Toolbar();

        public static class Toolbar
        {
            @Since(1)
            public ToolbarSpec horizontal = new ToolbarSpec();
            @Since(1)
            public ToolbarSpec vertical = new ToolbarSpec();

            public static class ToolbarSpec
            {
                @Since(1)
                public boolean useBackgroundImages;
                @Since(1)
                public String prefix = "";
                @Since(1)
                public int margin;
                @Since(1)
                public int padding;
                @Since(1)
                public int beginHeight;
                @Since(1)
                public int beginWidth;
                @Since(1)
                public int innerHeight;
                @Since(1)
                public int innerWidth;
                @Since(1)
                public int endHeight;
                @Since(1)
                public int endWidth;
            }
        }
    }

    public static class Control
    {
        @Since(1)
        public ButtonSpec button = new ButtonSpec();
        @Since(1)
        public ButtonSpec toggle = new ButtonSpec();

        public static class ButtonSpec
        {
            @Since(1)
            public boolean useBackgroundImage;
            @Since(1)
            public int width;
            @Since(1)
            public int height;
            @Since(1)
            public String prefix = "";
            @Since(1)
            public String tooltipOnStyle = "";
            @Since(1)
            public String tooltipOffStyle = "";
            @Since(1)
            public String tooltipDisabledStyle = "";
            @Since(1)
            public String iconOnColor = "";
            @Since(1)
            public String iconOffColor = "";
            @Since(1)
            public String iconHoverColor = "";
            @Since(1)
            public String iconDisabledColor = "";
        }
    }

    public static class Fullscreen
    {
        @Since(1)
        public String mapBackgroundColor = "";
        @Since(1)
        public String statusBackgroundColor = "";
        @Since(1)
        public String statusForegroundColor = "";

    }

    public static class ImageSpec
    {
        @Since(1)
        public int width;
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

    public static class Minimap
    {
        @Since(1)
        public String prefix = "";
        @Since(1)
        public int margin;
        @Since(1)
        public ImageSpec topLeft = new ImageSpec();
        @Since(1)
        public ImageSpec top = new ImageSpec();
        @Since(1)
        public ImageSpec topRight = new ImageSpec();
        @Since(1)
        public ImageSpec right = new ImageSpec();
        @Since(1)
        public ImageSpec bottomRight = new ImageSpec();
        @Since(1)
        public ImageSpec bottom = new ImageSpec();
        @Since(1)
        public ImageSpec bottomLeft = new ImageSpec();
        @Since(1)
        public ImageSpec left = new ImageSpec();

    }

    public static String toHexColor(Color color)
    {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    public static Color getColor(String hexColor)
    {
        try
        {
            int color = Integer.parseInt(hexColor.replaceFirst("#", ""), 16);
            return new Color(color);
        }
        catch(Exception e)
        {
            JourneyMap.getLogger().error("Invalid color string in theme: " + hexColor);
            return Color.lightGray;
        }
    }

    public void save()
    {
        ThemeFileHandler.save(this);
    }

    public String getJson()
    {
        return ThemeFileHandler.GSON.toJson(this);
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
