package net.techbrew.journeymap.ui.minimap;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.gui.FontRenderer;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.ui.theme.Theme;

import java.awt.*;

/**
 * Encapsulation of key attributes.
 */
class LabelVars
{
    private DisplayVars displayVars;
    final double x;
    final double y;
    final double fontScale;
    final boolean fontShadow;
    DrawUtil.HAlign hAlign;
    DrawUtil.VAlign vAlign;
    Color bgColor;
    int bgAlpha;
    Color fgColor;

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
            fontRenderer = FMLClientHandler.instance().getClient().fontRenderer;
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
