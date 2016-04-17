/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.ui.minimap;

import journeymap.client.render.draw.DrawUtil;
import journeymap.client.ui.theme.Theme;

/**
 * Encapsulation of key attributes needed to render a minimap label.
 */
class LabelVars
{
    final double x;
    final double y;
    final double fontScale;
    final boolean fontShadow;
    final DrawUtil.HAlign hAlign;
    final DrawUtil.VAlign vAlign;
    final Integer bgColor;
    final float bgAlpha;
    final Integer fgColor;
    final DisplayVars displayVars;

    LabelVars(DisplayVars displayVars, double x, double y, DrawUtil.HAlign hAlign, DrawUtil.VAlign vAlign, double fontScale, Theme.LabelSpec labelSpec)
    {
        this.displayVars = displayVars;
        this.x = x;
        this.y = y;
        this.hAlign = hAlign;
        this.vAlign = vAlign;
        this.fontScale = fontScale;
        this.fontShadow = labelSpec.shadow;
        this.bgColor = Theme.getColor(labelSpec.backgroundColor);
        this.bgAlpha = Theme.getAlpha(labelSpec.backgroundAlpha);
        this.fgColor = Theme.getColor(labelSpec.foregroundColor);
    }

    void draw(String text)
    {
        DrawUtil.drawLabel(text, x, y, hAlign, vAlign, bgColor, bgAlpha, fgColor, 1f, fontScale, fontShadow);
    }
}
