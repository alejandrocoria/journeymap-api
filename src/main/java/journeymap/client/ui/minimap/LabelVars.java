/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.ui.minimap;

import net.minecraft.client.gui.FontRenderer;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.render.draw.DrawUtil;
import journeymap.client.ui.theme.Theme;

import java.awt.*;

/**
 * Encapsulation of key attributes.
 */
class LabelVars
{
    final double x;
    final double y;
    final double fontScale;
    final boolean fontShadow;
    DrawUtil.HAlign hAlign;
    DrawUtil.VAlign vAlign;
    Color bgColor;
    int bgAlpha;
    Color fgColor;
    private DisplayVars displayVars;

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
        this.bgAlpha = labelSpec.backgroundAlpha;
        this.fgColor = Theme.getColor(labelSpec.foregroundColor);
    }

    void draw(String text)
    {
        boolean isUnicode = false;
        FontRenderer fontRenderer = null;
        if (displayVars.forceUnicode)
        {
            fontRenderer = ForgeHelper.INSTANCE.getFontRenderer();
            isUnicode = fontRenderer.getUnicodeFlag();
            if (!isUnicode)
            {
                fontRenderer.setUnicodeFlag(true);
            }
        }
        DrawUtil.drawLabel(text, x, y, hAlign, vAlign, bgColor, bgAlpha, fgColor, 255, fontScale, fontShadow);
        if (displayVars.forceUnicode && !isUnicode)
        {
            fontRenderer.setUnicodeFlag(false);
        }
    }
}
