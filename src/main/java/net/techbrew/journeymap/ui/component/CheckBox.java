package net.techbrew.journeymap.ui.component;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.config.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.techbrew.journeymap.properties.PropertiesBase;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Based on GuiCheckBox
 */
public class CheckBox extends Button
{
    public int boxWidth = 11;
    AtomicBoolean property;
    PropertiesBase properties;

    public CheckBox(int id, String displayString, AtomicBoolean property, PropertiesBase properties)
    {
        this(id, displayString, property.get());
        this.property = property;
        this.properties = properties;
    }

    public CheckBox(int id, String displayString, boolean checked)
    {
        super(id, displayString);
        this.toggled = checked;
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

            this.field_146123_n = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;

            int yoffset = (this.height - this.boxWidth) / 2;
            GuiUtils.drawContinuousTexturedBox(buttonTextures, this.xPosition, this.yPosition + yoffset, 0, 46, this.boxWidth, this.boxWidth, 200, 20, 2, 3, 2, 2, this.zLevel);
            this.mouseDragged(mc, mouseX, mouseY);
            int color = 14737632;

            if (this.field_146123_n)
            {
                color = 16777120;
            }
            else if (packedFGColour != 0)
            {
                //color = packedFGColour;
            }

            int labelPad = 4;

            if (this.toggled)
            {
                this.drawCenteredString(mc.fontRenderer, "x", this.xPosition + this.boxWidth / 2 + 1, this.yPosition + 1 + yoffset, 14737632);
            }

            this.drawString(mc.fontRenderer, displayString, xPosition + this.boxWidth + labelPad, yPosition + 2 + yoffset, color);
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

}