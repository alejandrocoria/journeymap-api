/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.component;

import journeymap.client.cartography.color.RGB;
import journeymap.common.properties.config.BooleanField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.lwjgl.input.Keyboard;

/**
 * Based on GuiCheckBox
 */
public class CheckBox extends BooleanPropertyButton
{
    /**
     * The Box width.
     */
    public int boxWidth = 11;
    /**
     * The Glyph.
     */
    String glyph = "\u2714";

    /**
     * Instantiates a new Check box.
     *
     * @param displayString the display string
     * @param checked       the checked
     */
    public CheckBox(String displayString, boolean checked)
    {
        this(displayString, null);
        this.toggled = checked;
    }

    /**
     * Instantiates a new Check box.
     *
     * @param displayString the display string
     * @param field         the field
     */
    public CheckBox(String displayString, BooleanField field)
    {
        super(displayString, displayString, field);

        setHeight(fontRenderer.FONT_HEIGHT + 2);
        setWidth(getFitWidth(fontRenderer));
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
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float ticks)
    {
        if (this.visible)
        {

            this.setHovered(isEnabled() && mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height);

            int yoffset = (this.height - this.boxWidth) / 2;
            GuiUtils.drawContinuousTexturedBox(BUTTON_TEXTURES, this.x, this.y + yoffset, 0, 46, this.boxWidth, this.boxWidth, 200, 20, 2, 3, 2, 2, this.zLevel);
            this.mouseDragged(mc, mouseX, mouseY);
            int color = 14737632;

            if (this.isHovered())
            {
                color = 16777120;
            }
            else if (!isEnabled())
            {
                color = RGB.DARK_GRAY_RGB;
            }
            else if (labelColor != null)
            {
                color = labelColor;
            }
            else if (packedFGColour != 0)
            {
                color = packedFGColour;
            }

            int labelPad = 4;

            if (this.toggled)
            {
                this.drawCenteredString(fontRenderer, glyph, this.x + this.boxWidth / 2 + 1, this.y + 1 + yoffset, color);
            }

            this.drawString(fontRenderer, displayString, x + this.boxWidth + labelPad, y + 2 + yoffset, color);
        }
    }

    /**
     * Returns true if the mouse has been pressed on this control. Equivalent of MouseListener.mousePressed(MouseEvent
     * e).
     */
    @Override
    public boolean mousePressed(Minecraft p_146116_1_, int mouseX, int mouseY)
    {
        if (this.isEnabled() && this.visible && mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height)
        {
            toggle();
            return checkClickListeners();
        }

        return false;
    }

    public boolean keyTyped(char c, int i)
    {
        if (this.isEnabled())
        {
            if (i == Keyboard.KEY_SPACE)
            {
                toggle();
                return true;
            }
        }
        return false;
    }
}