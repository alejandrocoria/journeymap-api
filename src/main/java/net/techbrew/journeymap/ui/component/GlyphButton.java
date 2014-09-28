/*
 * Forge Mod Loader
 * Copyright (c) 2012-2014 cpw.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * Contributors (this class):
 *     bspkrs - implementation
 */

package net.techbrew.journeymap.ui.component;

import cpw.mods.fml.client.config.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.opengl.GL11;

/**
 * Adaptation of GuiUnicodeGlyphButton.
 */
public class GlyphButton extends Button
{
    public String glyph;
    public float glyphScale;

    public GlyphButton(int id, String displayString, String glyph, float glyphScale)
    {
        super(id, displayString);
        this.glyph = glyph;
        this.glyphScale = glyphScale;
    }

    @Override
    public int getFitWidth(FontRenderer fr)
    {
        int glyphWidth = (int) (fr.getStringWidth(glyph) * glyphScale);
        if (StringUtils.isEmpty(displayString))
        {
            return glyphWidth + WIDTH_PAD;
        }
        else
        {
            return super.getFitWidth(fr) + glyphWidth;
        }
    }

    /**
     * Draws this button to the screen.
     */
    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY)
    {
        if (this.visible)
        {
            this.field_146123_n = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            int k = this.getHoverState(this.field_146123_n);
            GuiUtils.drawContinuousTexturedBox(buttonTextures, this.xPosition, this.yPosition, 0, 46 + k * 20, this.width, this.height, 200, 20, 2, 3, 2, 2, this.zLevel);
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
            else if (this.field_146123_n)
            {
                color = 16777120;
            }

            String buttonText = this.displayString;
            int glyphWidth = (int) (mc.fontRenderer.getStringWidth(glyph) * glyphScale);
            int strWidth = mc.fontRenderer.getStringWidth(buttonText);
            int elipsisWidth = mc.fontRenderer.getStringWidth("...");
            int totalWidth = strWidth + glyphWidth;

            if (totalWidth > width - 6 && totalWidth > elipsisWidth)
            {
                buttonText = mc.fontRenderer.trimStringToWidth(buttonText, width - 6 - elipsisWidth).trim() + "...";
            }

            strWidth = mc.fontRenderer.getStringWidth(buttonText);
            totalWidth = glyphWidth + strWidth;

            GL11.glPushMatrix();
            GL11.glScalef(glyphScale, glyphScale, 1.0F);
            this.drawCenteredString(mc.fontRenderer, glyph,
                    (int) (((this.xPosition + (this.width / 2) - (strWidth / 2)) / glyphScale) - (glyphWidth / (2 * glyphScale)) + 2),
                    (int) (((this.yPosition + ((this.height - 8) / glyphScale) / 2) - 1) / glyphScale), color);
            GL11.glPopMatrix();

            this.drawCenteredString(mc.fontRenderer, buttonText, (int) (this.xPosition + (this.width / 2) + (glyphWidth / glyphScale)),
                    this.yPosition + (this.height - 8) / 2, color);
        }
    }
}