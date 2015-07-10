/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package net.techbrew.journeymap.ui.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.techbrew.journeymap.properties.PropertiesBase;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Based on GuiCheckBox
 */
public class CheckBox extends BooleanPropertyButton
{
    public int boxWidth = 11;
    AtomicBoolean property;
    PropertiesBase properties;
    String glyph = "\u2714";

    public CheckBox(String displayString, boolean checked)
    {
        this(displayString, null, null);
        this.toggled = checked;
    }

    public CheckBox(String displayString, AtomicBoolean property, PropertiesBase properties)
    {
        super(displayString, displayString, properties, property);
        this.property = property;
        this.properties = properties;

        this.height = fontRenderer.FONT_HEIGHT + 2;
        this.width = getFitWidth(fontRenderer);
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

            this.setEnabled(isEnabled() && mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height);

            int yoffset = (this.height - this.boxWidth) / 2;
            GuiUtils.drawContinuousTexturedBox(buttonTextures, this.xPosition, this.yPosition + yoffset, 0, 46, this.boxWidth, this.boxWidth, 200, 20, 2, 3, 2, 2, this.zLevel);
            this.mouseDragged(mc, mouseX, mouseY);
            int color = 14737632;

            if (this.isEnabled())
            {
                color = 16777120;
            }
            else if (!isEnabled())
            {
                color = Color.DARK_GRAY.getRGB();
            }
            else if (labelColor != null)
            {
                color = labelColor.getRGB();
            }
            else if (packedFGColour != 0)
            {
                color = packedFGColour;
            }

            int labelPad = 4;

            if (this.toggled)
            {
                this.drawCenteredString(fontRenderer, glyph, this.xPosition + this.boxWidth / 2 + 1, this.yPosition + 1 + yoffset, color);
            }

            this.drawString(fontRenderer, displayString, xPosition + this.boxWidth + labelPad, yPosition + 2 + yoffset, color);
        }
    }

    /**
     * Returns true if the mouse has been pressed on this control. Equivalent of MouseListener.mousePressed(MouseEvent
     * e).
     */
    @Override
    public boolean mousePressed(Minecraft p_146116_1_, int p_146116_2_, int p_146116_3_)
    {
        if (this.isEnabled() && this.visible && p_146116_2_ >= this.xPosition && p_146116_3_ >= this.yPosition && p_146116_2_ < this.xPosition + this.width && p_146116_3_ < this.yPosition + this.height)
        {
            toggle();
            if (property != null)
            {
                property.set(this.toggled);
            }
            if (properties != null)
            {
                properties.save();
            }
            return true;
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