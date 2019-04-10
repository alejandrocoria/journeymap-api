package journeymap.client.ui.component;

import journeymap.client.Constants;
import journeymap.client.cartography.color.RGB;
import journeymap.client.render.draw.DrawUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class Label extends Button
{
    /**
     * The H align.
     */
    private DrawUtil.HAlign hAlign = DrawUtil.HAlign.Left;

    /**
     * Instantiates a new Label button.
     *
     * @param width     the width
     * @param key       the key
     * @param labelArgs the label args
     */
    public Label(int width, String key, Object... labelArgs)
    {
        super(Constants.getString(key, labelArgs));
        setTooltip(Constants.getString(key + ".tooltip"));
        setDrawBackground(false);
        setDrawFrame(false);
        setEnabled(false);
        setLabelColors(RGB.LIGHT_GRAY_RGB, RGB.LIGHT_GRAY_RGB, RGB.LIGHT_GRAY_RGB);
        setWidth(width);
    }

    @Override
    public int getFitWidth(FontRenderer fr)
    {
        return fontRenderer.getStringWidth(displayString);
    }

    @Override
    public void fitWidth(FontRenderer fr)
    {

    }

    /**
     * Sets h align.
     *
     * @param hAlign the h align
     */
    public void setHAlign(DrawUtil.HAlign hAlign)
    {
        this.hAlign = hAlign;
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY, float ticks)
    {
        int labelX;
        switch (hAlign)
        {
            case Left:
            {
                labelX = this.getRightX();
                break;
            }
            case Right:
            {
                labelX = this.getX();
                break;
            }
            default:
            {
                labelX = this.getCenterX();
            }
        }

        DrawUtil.drawLabel(this.displayString, labelX, this.getMiddleY(), hAlign, DrawUtil.VAlign.Middle, null, 0, labelColor, 1f, 1, drawLabelShadow);
    }
}
