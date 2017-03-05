/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.minimap;

import journeymap.client.render.draw.DrawUtil;
import journeymap.client.ui.theme.Theme;

/**
 * Encapsulation of key attributes needed to render a minimap label.
 */
class LabelVars
{
    /**
     * The X.
     */
    final double x;
    /**
     * The Y.
     */
    final double y;
    /**
     * The Font scale.
     */
    final double fontScale;
    /**
     * The Font shadow.
     */
    final boolean fontShadow;
    /**
     * The H align.
     */
    final DrawUtil.HAlign hAlign;
    /**
     * The V align.
     */
    final DrawUtil.VAlign vAlign;
    /**
     * The Bg color.
     */
    final Integer bgColor;
    /**
     * The Bg alpha.
     */
    final float bgAlpha;
    /**
     * The Fg color.
     */
    final Integer fgColor;
    /**
     * The Display vars.
     */
    final DisplayVars displayVars;

    /**
     * Instantiates a new Label vars.
     *
     * @param displayVars the display vars
     * @param x           the x
     * @param y           the y
     * @param hAlign      the h align
     * @param vAlign      the v align
     * @param fontScale   the font scale
     * @param labelSpec   the label spec
     */
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

    /**
     * Draw.
     *
     * @param text the text
     */
    void draw(String text)
    {
        DrawUtil.drawLabel(text, x, y, hAlign, vAlign, bgColor, bgAlpha, fgColor, 1f, fontScale, fontShadow);
    }
}
