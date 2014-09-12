package net.techbrew.journeymap.ui.theme;

import net.minecraft.util.EnumChatFormatting;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

/**
 * Themes that come.
 */
public class ThemePresets
{
    public static final Theme THEME_VICTORIAN = createVictorian();
    public static final Theme THEME_PURIST = createPurist();

    public static List<Theme> getPresets()
    {
        return Arrays.asList(THEME_PURIST, THEME_VICTORIAN);
    }

    private static Theme createVictorian()
    {
        Theme theme = new Theme();
        theme.name = "Victorian";
        theme.author = "techbrew";
        theme.directory = "Victorian";

        Theme.ImageSpec icon = theme.icon;
        icon.height = 24;
        icon.width = 24;

        Theme.Control.ButtonSpec button = theme.control.button;
        button.useBackgroundImage = true;
        button.width = 24;
        button.height = 24;
        button.tooltipOnStyle = EnumChatFormatting.WHITE.toString();
        button.tooltipOffStyle = EnumChatFormatting.WHITE.toString();
        button.tooltipDisabledStyle = EnumChatFormatting.DARK_GRAY.toString() + EnumChatFormatting.ITALIC.toString();
        button.iconOnColor = Theme.toHexColor(new Color(132, 125, 102));
        button.iconOffColor = Theme.toHexColor(new Color(132, 125, 102));
        button.iconHoverColor = Theme.toHexColor(Color.white);
        button.iconDisabledColor = Theme.toHexColor(Color.darkGray);

        Theme.Control.ButtonSpec toggle = theme.control.toggle;
        toggle.useBackgroundImage = true;
        toggle.width = 24;
        toggle.height = 24;
        toggle.tooltipOnStyle = EnumChatFormatting.WHITE.toString();
        toggle.tooltipOffStyle = EnumChatFormatting.WHITE.toString();
        toggle.tooltipDisabledStyle = EnumChatFormatting.DARK_GRAY.toString() + EnumChatFormatting.ITALIC.toString();
        toggle.iconOnColor = Theme.toHexColor(Color.darkGray);
        toggle.iconOffColor = Theme.toHexColor(new Color(132, 125, 102));
        toggle.iconHoverColor = Theme.toHexColor(Color.white);
        toggle.iconDisabledColor = Theme.toHexColor(Color.darkGray);

        Theme.Container.Toolbar.ToolbarSpec hToolbar = theme.container.toolbar.horizontal;
        hToolbar.useBackgroundImages = true;
        hToolbar.prefix = "h_";
        hToolbar.margin = 4;
        hToolbar.padding = 4;
        hToolbar.begin = hToolbar.end = new Theme.ImageSpec(8,32);
        hToolbar.inner = new Theme.ImageSpec(28,32);

        Theme.Container.Toolbar.ToolbarSpec vToolbar = theme.container.toolbar.vertical;
        vToolbar.useBackgroundImages = true;
        vToolbar.prefix = "v_";
        vToolbar.margin = 4;
        vToolbar.padding = 4;
        vToolbar.begin = vToolbar.end = new Theme.ImageSpec(32,8);
        vToolbar.inner = new Theme.ImageSpec(32,28);

        Theme.Fullscreen fullscreen = theme.fullscreen;
        fullscreen.mapBackgroundColor = Theme.toHexColor(new Color(0x22, 0x22, 0x22));
        fullscreen.statusBackgroundColor = Theme.toHexColor(new Color(0x22, 0x22, 0x22));
        fullscreen.statusForegroundColor = Theme.toHexColor(Color.lightGray);

        Theme.Minimap.MinimapSquare minimapSquare = theme.minimap.square;
        minimapSquare.prefix = "vic_";
        minimapSquare.margin = 8;
        minimapSquare.labelTopMargin = 4;
        minimapSquare.labelBottomMargin = 4;
        minimapSquare.top = minimapSquare.bottom = new Theme.ImageSpec(1,20);
        minimapSquare.left = minimapSquare.right = new Theme.ImageSpec(20,1);
        minimapSquare.topLeft = minimapSquare.topRight = minimapSquare.bottomRight = minimapSquare.bottomLeft = new Theme.ImageSpec(20,20);
        minimapSquare.frameColor = Theme.toHexColor(new Color(132, 125, 102));

        Theme.Minimap.MinimapCircle minimapCircle = theme.minimap.circle;
        minimapCircle.prefix = "";
        minimapCircle.margin = 8;
        minimapCircle.labelTopMargin = 4;
        minimapCircle.labelBottomMargin = 4;
        minimapCircle.frameColor = Theme.toHexColor(new Color(132, 125, 102));

        return theme;
    }

    private static Theme createPurist()
    {
        Theme theme = new Theme();
        theme.name = "Purist";
        theme.author = "techbrew";
        theme.directory = "Victorian";

        Theme.ImageSpec icon = theme.icon;
        icon.height = 20;
        icon.width = 20;

        Theme.Control.ButtonSpec button = theme.control.button;
        button.useBackgroundImage = false;
        button.width = 20;
        button.height = 20;
        button.tooltipOnStyle = EnumChatFormatting.WHITE.toString();
        button.tooltipOffStyle = EnumChatFormatting.WHITE.toString();
        button.tooltipDisabledStyle = EnumChatFormatting.DARK_GRAY.toString() + EnumChatFormatting.ITALIC.toString();
        button.iconOnColor = Theme.toHexColor(new Color(16777120));
        button.iconOffColor = Theme.toHexColor(new Color(14737632));
        button.iconHoverColor = Theme.toHexColor(new Color(16777120));
        button.iconDisabledColor = Theme.toHexColor(new Color(10526880));

        Theme.Control.ButtonSpec toggle = theme.control.toggle;
        toggle.useBackgroundImage = false;
        toggle.width = 20;
        toggle.height = 20;
        toggle.tooltipOnStyle = EnumChatFormatting.WHITE.toString();
        toggle.tooltipOffStyle = EnumChatFormatting.WHITE.toString();
        toggle.tooltipDisabledStyle = EnumChatFormatting.DARK_GRAY.toString() + EnumChatFormatting.ITALIC.toString();
        toggle.iconOnColor = Theme.toHexColor(Color.white);
        toggle.iconOffColor = Theme.toHexColor(Color.gray);
        toggle.iconHoverColor = Theme.toHexColor(new Color(16777120));
        toggle.iconDisabledColor = Theme.toHexColor(Color.darkGray);

        Theme.Container.Toolbar.ToolbarSpec hToolbar = theme.container.toolbar.horizontal;
        hToolbar.useBackgroundImages = false;
        hToolbar.prefix = "h_";
        hToolbar.margin = 4;
        hToolbar.padding = 2;
        hToolbar.begin = hToolbar.end = new Theme.ImageSpec(4,24);
        hToolbar.inner = new Theme.ImageSpec(24,24);

        Theme.Container.Toolbar.ToolbarSpec vToolbar = theme.container.toolbar.vertical;
        vToolbar.useBackgroundImages = false;
        vToolbar.prefix = "v_";
        vToolbar.margin = 4;
        vToolbar.padding = 2;
        vToolbar.begin = vToolbar.end = new Theme.ImageSpec(24,4);
        vToolbar.inner = new Theme.ImageSpec(24,24);

        Theme.Fullscreen fullscreen = theme.fullscreen;
        fullscreen.mapBackgroundColor = Theme.toHexColor(new Color(0x22, 0x22, 0x22));
        fullscreen.statusBackgroundColor = Theme.toHexColor(new Color(0x22, 0x22, 0x22));
        fullscreen.statusForegroundColor = Theme.toHexColor(Color.lightGray);

        Theme.Minimap.MinimapSquare minimapSquare = theme.minimap.square;
        minimapSquare.prefix = "pur_";
        minimapSquare.margin = 8;
        minimapSquare.labelTopInside = false;
        minimapSquare.labelTopMargin = 4;
        minimapSquare.labelBottomInside = false;
        minimapSquare.labelBottomMargin = 4;
        minimapSquare.top = minimapSquare.bottom = new Theme.ImageSpec(1,8);
        minimapSquare.left = minimapSquare.right = new Theme.ImageSpec(8,1);
        minimapSquare.topLeft = minimapSquare.topRight = minimapSquare.bottomRight = minimapSquare.bottomLeft = new Theme.ImageSpec(8,8);
        minimapSquare.frameColor = Theme.toHexColor(Color.lightGray);

        Theme.Minimap.MinimapCircle minimapCircle = theme.minimap.circle;
        minimapCircle.prefix = "";
        minimapCircle.margin = 8;
        minimapCircle.labelTopMargin = 4;
        minimapCircle.labelBottomMargin = 4;
        minimapCircle.frameColor = Theme.toHexColor(Color.white);

        return theme;
    }
}

