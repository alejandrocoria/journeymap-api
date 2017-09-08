package journeymap.client.ui.theme.impl;

import journeymap.client.ui.theme.Theme;

/**
 * Properties holder to abstract as many style-related values
 * away from where the concrete values are set in a theme.
 */
class Style
{
    Theme.LabelSpec label = new Theme.LabelSpec();
    Colors button = new Colors();
    Colors toggle = new Colors();
    Colors text = new Colors();
    String minimapTexPrefix = "";
    String buttonTexPrefix = "";
    String tooltipOnStyle = "§f";
    String tooltipOffStyle = "§f";
    String tooltipDisabledStyle = "§8§o";
    int iconSize = 24;
    Theme.ColorSpec frameColorSpec = new Theme.ColorSpec();
    Theme.ColorSpec toolbarColorSpec = new Theme.ColorSpec();
    Theme.ColorSpec fullscreenColorSpec = new Theme.ColorSpec();
    int squareFrameThickness = 8;
    int circleFrameThickness = 8;
    int toolbarMargin = 4;
    int toolbarPadding = 0;
    boolean useThemeImages = true;

    Style()
    {
        label.margin = 0;
    }

    static class Colors
    {
        String on;
        String off;
        String hover;
        String disabled;
    }
}
