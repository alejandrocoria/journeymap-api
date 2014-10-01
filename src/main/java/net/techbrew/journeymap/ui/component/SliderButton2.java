package net.techbrew.journeymap.ui.component;

import cpw.mods.fml.client.config.GuiUtils;
import net.minecraft.client.Minecraft;
import net.techbrew.journeymap.properties.PropertiesBase;
import org.lwjgl.opengl.GL11;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Mark on 9/29/2014.
 */
public class SliderButton2 extends Button
{
    /**
     * The value of this slider control.
     */
    public double sliderValue = 1.0F;
    public String dispString = "";
    /**
     * Is this slider control being dragged.
     */
    public boolean dragging = false;
    public double minValue = 0.0D;
    public double maxValue = 5.0D;
    public int precision = 1;
    public String suffix = "";
    public boolean drawString = true;
    PropertiesBase properties;
    AtomicInteger property;

    public SliderButton2(int id, PropertiesBase properties, AtomicInteger property, String prefix, String suf, double minVal, double maxVal, boolean drawStr)
    {
        super(id, prefix);
        minValue = minVal;
        maxValue = maxVal;
        sliderValue = (property.get() - minValue) / (maxValue - minValue);
        dispString = prefix;
        suffix = suf;
        String val;
        this.property = property;
        this.properties = properties;

        val = Integer.toString((int) Math.round(sliderValue * (maxValue - minValue) + minValue));
        precision = 0;

        displayString = dispString + val + suffix;

        drawString = drawStr;
        if (!drawString)
        {
            displayString = "";
        }
    }

    /**
     * Returns 0 if the button is disabled, 1 if the mouse is NOT hovering over this button and 2 if it IS hovering over
     * this button.
     */
    /**
     * Returns 0 if the button is disabled, 1 if the mouse is NOT hovering over this button and 2 if it IS hovering over
     * this button.
     */
    public int getHoverState(boolean par1)
    {
        return 0;
    }

    /**
     * Fired when the mouse button is dragged. Equivalent of MouseListener.mouseDragged(MouseEvent e).
     */
    /**
     * Fired when the mouse button is dragged. Equivalent of MouseListener.mouseDragged(MouseEvent e).
     */
    protected void mouseDragged(Minecraft par1Minecraft, int par2, int par3)
    {
        if (this.visible)
        {
            if (this.dragging)
            {
                this.sliderValue = (par2 - (this.xPosition + 4)) / (float) (this.width - 8);
                updateSlider();
            }

            int k = this.getHoverState(this.field_146123_n);

            if (this.field_146123_n || this.dragging)
            {

                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

                GuiUtils.drawContinuousTexturedBox(buttonTextures, this.xPosition + 1 + (int) (this.sliderValue * (float) (this.width - 10)), this.yPosition + 1, 0, 66, 8, height - 2, 200, 20, 2, 3, 2, 2, this.zLevel);
                //this.drawTexturedModalRect(this.xPosition + (int)(this.sliderValue * (float)(this.width - 8)), this.yPosition, 0, 66, 4, height);
                //this.drawTexturedModalRect(this.xPosition + (int)(this.sliderValue * (float)(this.width - 8)) + 4, this.yPosition, 196, 66, 4, height);
            }
        }
    }

    /**
     * Returns true if the mouse has been pressed on this control. Equivalent of MouseListener.mousePressed(MouseEvent
     * e).
     */
    /**
     * Returns true if the mouse has been pressed on this control. Equivalent of MouseListener.mousePressed(MouseEvent
     * e).
     */
    public boolean mousePressed(Minecraft par1Minecraft, int par2, int par3)
    {
        if (super.mousePressed(par1Minecraft, par2, par3))
        {
            this.sliderValue = (float) (par2 - (this.xPosition + 4)) / (float) (this.width - 8);
            updateSlider();
            this.dragging = true;
            return true;
        }
        else
        {
            return false;
        }
    }

    public void updateSlider()
    {
        if (this.sliderValue < 0.0F)
        {
            this.sliderValue = 0.0F;
        }

        if (this.sliderValue > 1.0F)
        {
            this.sliderValue = 1.0F;
        }

        int intVal = (int) Math.round(sliderValue * (maxValue - minValue) + minValue);
        String val = Integer.toString(intVal);

        if (drawString)
        {
            displayString = dispString + val + suffix;
        }

        if (property.get() != intVal)
        {
            property.set(intVal);
            properties.save();
        }

    }

    /**
     * Fired when the mouse button is released. Equivalent of MouseListener.mouseReleased(MouseEvent e).
     */
    /**
     * Fired when the mouse button is released. Equivalent of MouseListener.mouseReleased(MouseEvent e).
     */
    public void mouseReleased(int par1, int par2)
    {
        this.dragging = false;
    }

    public int getValueInt()
    {
        return (int) Math.round(sliderValue * (maxValue - minValue) + minValue);
    }

    public double getValue()
    {
        return sliderValue * (maxValue - minValue) + minValue;
    }

    public void setValue(double d)
    {
        this.sliderValue = (d - minValue) / (maxValue - minValue);
    }
}
