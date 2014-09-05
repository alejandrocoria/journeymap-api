package net.techbrew.journeymap.ui.theme;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Since;
import net.minecraft.util.EnumChatFormatting;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.io.IconSetFileHandler;
import net.techbrew.journeymap.log.LogFormatter;

import java.awt.*;
import java.io.File;
import java.nio.charset.Charset;

/**
 * Created by Mark on 9/2/2014.
 */
public class Theme
{
    public static final int VERSION = 1;
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().setVersion(VERSION).create();
    private static transient Theme currentTheme = null;

    @Since(1)
    public String name;
    @Since(1)
    public String author;
    @Since(1)
    public Container container = new Container();
    @Since(1)
    public Control control = new Control();
    @Since(1)
    public Icon icon = new Icon();

    public static class Container
    {
        @Since(1)
        public Toolbar toolbar = new Toolbar();

        public static class Toolbar
        {
            @Since(1)
            public ToolbarSpec horizontal = new HorizontalToolbar();
            @Since(1)
            public ToolbarSpec vertical = new VerticalToolbar();

            public static class ToolbarSpec
            {
                @Since(1)
                public boolean show;
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

            public static class HorizontalToolbar extends ToolbarSpec
            {
                protected HorizontalToolbar()
                {
                    show = true;
                    prefix = "htoolbar_";
                    margin = 4;
                    padding = 4;
                    beginHeight = innerHeight = endHeight = 32;
                    beginWidth = endWidth = 8;
                    innerWidth = 28;
                }
            }

            public static class VerticalToolbar extends ToolbarSpec
            {
                protected VerticalToolbar()
                {
                    show = true;
                    prefix = "vtoolbar_";
                    margin = 4;
                    padding = 4;
                    beginHeight = endHeight = 8;
                    innerHeight = 28;
                    beginWidth = innerWidth = endWidth = 32;
                }
            }
        }
    }

    public static class Control
    {
        @Since(1)
        public ButtonSpec button = new Button();
        @Since(1)
        public ButtonSpec toggle = new Toggle();

        public static class ButtonSpec
        {
            @Since(1)
            public boolean show;
            @Since(1)
            public int width;
            @Since(1)
            public int height;
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

        public static class Button extends ButtonSpec
        {
            protected Button()
            {
                show = true;
                width = 24;
                height = 24;
                tooltipOnStyle = EnumChatFormatting.WHITE.toString();
                tooltipOffStyle = EnumChatFormatting.WHITE.toString();
                tooltipDisabledStyle = EnumChatFormatting.DARK_GRAY.toString() + EnumChatFormatting.ITALIC.toString();
                iconOnColor = toHexColor(new Color(132,125,102));
                iconOffColor = toHexColor(new Color(132,125,102));
                iconHoverColor = toHexColor(Color.white);
                iconDisabledColor = toHexColor(Color.darkGray);
            }
        }

        public static class Toggle extends ButtonSpec
        {
            protected Toggle()
            {
                show = true;
                width = 24;
                height = 24;
                tooltipOnStyle = EnumChatFormatting.WHITE.toString();
                tooltipOffStyle = EnumChatFormatting.WHITE.toString();
                tooltipDisabledStyle = EnumChatFormatting.DARK_GRAY.toString() + EnumChatFormatting.ITALIC.toString();
                iconOnColor = toHexColor(Color.darkGray);
                iconOffColor = toHexColor(new Color(132,125,102));
                iconHoverColor = toHexColor(Color.white);
                iconDisabledColor = toHexColor(Color.darkGray);
            }
        }
    }

    public static class Icon
    {
        @Since(1)
        public int width = 24;
        @Since(1)
        public int height = 24;
    }

    public static Theme getCurrentTheme()
    {
        return getCurrentTheme(false);
    }

    public synchronized static Theme getCurrentTheme(boolean forceReload)
    {
        String themeName = JourneyMap.getCoreProperties().themeName.get();
        if(forceReload || currentTheme==null || !themeName.equals(currentTheme.name))
        {
            currentTheme = get(themeName);
        }
        return currentTheme;
    }

    public static Theme get(String themeName)
    {
        try
        {
            File themeDir = new File(IconSetFileHandler.getThemeIconDir(), themeName);
            File themeFile = new File(themeDir, "theme.json");
            Charset UTF8 = Charset.forName("UTF-8");
            if (themeFile.exists())
            {
                String json = Files.toString(themeFile, UTF8);
                return GSON.fromJson(json, Theme.class);
            }
            else
            {
                JourneyMap.getLogger().info("Generating Theme json file: " + themeFile);
                Theme theme = new Theme();
                theme.name = themeName;
                theme.save();
                return theme;
            }
        }
        catch(Throwable t)
        {
            JourneyMap.getLogger().error("Could not load Theme json file: " + LogFormatter.toString(t));
            return new Theme();
        }
    }

    public Color getColor(String hexColor)
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

    public static String toHexColor(Color color)
    {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    public void save()
    {
        try
        {
            File themeDir = new File(IconSetFileHandler.getThemeIconDir(), name);
            File themeFile = new File(themeDir, "theme.json");
            Charset UTF8 = Charset.forName("UTF-8");
            if (IconSetFileHandler.getThemeNames().contains(name))
            {
                author = "techbrew";
            }

            if (!themeDir.exists())
            {
                themeDir.mkdirs();
            }
            Files.write(GSON.toJson(this), themeFile, UTF8);
        }
        catch(Throwable t)
        {
            JourneyMap.getLogger().error("Could not save Theme json file: " + t);
        }
    }

    public String getJson()
    {
        return GSON.toJson(this);
    }
}
