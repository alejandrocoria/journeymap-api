/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.minimap;

import journeymap.client.render.draw.DrawUtil;
import journeymap.client.ui.theme.Theme;
import net.minecraft.client.renderer.GlStateManager;

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
     * The H align.
     */
    final DrawUtil.HAlign hAlign;
    /**
     * The V align.
     */
    final DrawUtil.VAlign vAlign;

    /**
     * The Display vars.
     */
    final DisplayVars displayVars;

    /**
     * Label spec.
     */
    final Theme.LabelSpec labelSpec;

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
        this.labelSpec = labelSpec;
    }

    /**
     * Draw.
     *
     * @param text the text
     */
    void draw(String text)
    {
        GlStateManager.enableBlend();
        DrawUtil.drawLabel(text, labelSpec, (int) x, (int) y, hAlign, vAlign, fontScale, 0);
        GlStateManager.disableBlend();
    }
}
