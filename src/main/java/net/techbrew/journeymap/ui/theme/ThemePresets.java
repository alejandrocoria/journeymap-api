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
        hToolbar.prefix = "htoolbar_";
        hToolbar.margin = 4;
        hToolbar.padding = 4;
        hToolbar.beginHeight = hToolbar.innerHeight = hToolbar.endHeight = 32;
        hToolbar.beginWidth = hToolbar.endWidth = 8;
        hToolbar.innerWidth = 28;

        Theme.Container.Toolbar.ToolbarSpec vToolbar = theme.container.toolbar.vertical;
        vToolbar.useBackgroundImages = true;
        vToolbar.prefix = "vtoolbar_";
        vToolbar.margin = 4;
        vToolbar.padding = 4;
        vToolbar.beginHeight = vToolbar.endHeight = 8;
        vToolbar.innerHeight = 28;
        vToolbar.beginWidth = vToolbar.innerWidth = vToolbar.endWidth = 32;

        Theme.Fullscreen fullscreen = theme.fullscreen;
        fullscreen.mapBackgroundColor = Theme.toHexColor(new Color(0x22, 0x22, 0x22));
        fullscreen.statusBackgroundColor = Theme.toHexColor(new Color(0x22, 0x22, 0x22));
        fullscreen.statusForegroundColor = Theme.toHexColor(Color.lightGray);

        Theme.Minimap minimap = theme.minimap;
        minimap.prefix = "vic_";
        minimap.margin = 8;
        minimap.padding = 4;
        minimap.top = minimap.bottom = new Theme.ImageSpec(1,20);
        minimap.left = minimap.right = new Theme.ImageSpec(20,1);
        minimap.topLeft = minimap.topRight = minimap.bottomRight = minimap.bottomLeft = new Theme.ImageSpec(20,20);
        minimap.circleFrameColor = Theme.toHexColor(new Color(132, 125, 102));

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
        hToolbar.prefix = "htoolbar_";
        hToolbar.margin = 4;
        hToolbar.padding = 2;
        hToolbar.beginHeight = hToolbar.innerHeight = hToolbar.endHeight = 24;
        hToolbar.beginWidth = hToolbar.endWidth = 4;
        hToolbar.innerWidth = 24;

        Theme.Container.Toolbar.ToolbarSpec vToolbar = theme.container.toolbar.vertical;
        vToolbar.useBackgroundImages = false;
        vToolbar.prefix = "vtoolbar_";
        vToolbar.margin = 4;
        vToolbar.padding = 2;
        vToolbar.beginHeight = vToolbar.endHeight = 4;
        vToolbar.innerHeight = 24;
        vToolbar.beginWidth = vToolbar.innerWidth = vToolbar.endWidth = 24;

        Theme.Fullscreen fullscreen = theme.fullscreen;
        fullscreen.mapBackgroundColor = Theme.toHexColor(new Color(0x22, 0x22, 0x22));
        fullscreen.statusBackgroundColor = Theme.toHexColor(new Color(0x22, 0x22, 0x22));
        fullscreen.statusForegroundColor = Theme.toHexColor(Color.lightGray);

        Theme.Minimap minimap = theme.minimap;
        minimap.prefix = "pur_";
        minimap.margin = 4;
        minimap.padding = 4;
        minimap.top = minimap.bottom = new Theme.ImageSpec(1,8);
        minimap.left = minimap.right = new Theme.ImageSpec(8,1);
        minimap.topLeft = minimap.topRight = minimap.bottomRight = minimap.bottomLeft = new Theme.ImageSpec(8,8);
        minimap.circleFrameColor = Theme.toHexColor(Color.lightGray);

        return theme;
    }
}

