package net.techbrew.journeymap.ui.component;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.config.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

/**
 * Based on GuiCheckBox
 */
public class CheckBox extends Button
{
    private final int boxWidth = 11;

    public CheckBox(int id, String displayString)
    {
        this(id, displayString, false);
    }

    public CheckBox(int id, String displayString, boolean isChecked)
    {
        super(id, displayString);
        this.toggled = isChecked;
        FontRenderer fr = FMLClientHandler.instance().getClient().fontRenderer;
        this.height = fr.FONT_HEIGHT + 2;
        this.width = getFitWidth(fr);
    }

    @Override
    public int getFitWidth(FontRenderer fr)
    {
        return super.getFitWidth(fr) + this.boxWidth + 2;
    }

    /**
     * Draws this button to the screen.
     */
    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY)
    {
        if (this.visible)
        {
            this.field_146123_n = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.boxWidth && mouseY < this.yPosition + this.height;
            GuiUtils.drawContinuousTexturedBox(buttonTextures, this.xPosition, this.yPosition, 0, 46, this.boxWidth, this.height, 200, 20, 2, 3, 2, 2, this.zLevel);
            this.mouseDragged(mc, mouseX, mouseY);
            int color = 14737632;

            if (packedFGColour != 0)
            {
                color = packedFGColour;
            }
            else if (!this.enabled)
            {
                color = 10526880;
            }

            int labelPad = 4;

            if (this.toggled)
            {
                this.drawCenteredString(mc.fontRenderer, "x", this.xPosition + this.boxWidth / 2 + 1, this.yPosition + 1, 14737632);
            }

            this.drawString(mc.fontRenderer, displayString, xPosition + this.boxWidth + labelPad, yPosition + 2, color);
        }
    }

    /**
     * Returns true if the mouse has been pressed on this control. Equivalent of MouseListener.mousePressed(MouseEvent
     * e).
     */
    @Override
    public boolean mousePressed(Minecraft p_146116_1_, int p_146116_2_, int p_146116_3_)
    {
        if (this.enabled && this.visible && p_146116_2_ >= this.xPosition && p_146116_3_ >= this.yPosition && p_146116_2_ < this.xPosition + this.width && p_146116_3_ < this.yPosition + this.height)
        {
            toggle();
            return true;
        }

        return false;
    }

    public boolean isChecked()
    {
        return this.toggled;
    }

    public void setIsChecked(boolean isChecked)
    {
        this.toggled = isChecked;
    }
}