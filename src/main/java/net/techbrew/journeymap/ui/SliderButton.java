/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.ui;

import net.minecraft.client.Minecraft;
import net.techbrew.journeymap.Constants;
import org.lwjgl.opengl.GL11;

import java.text.NumberFormat;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Adapted from GuiSlider
 */
public class SliderButton extends Button
{
    /**
     * Additional ID for this slider control.
     */
    private final ValueHolder valueHolder;
    /**
     * Is this slider control being dragged.
     */
    public boolean dragging;
    /**
     * The value of this slider control.
     */
    protected float sliderValue = 1.0F;

    public SliderButton(int buttonId, ValueHolder valueHolder)
    {
        super(buttonId, 0, 0, valueHolder.getDisplayString());
        this.valueHolder = valueHolder;
        this.sliderValue = valueHolder.getValueForSlider();
    }

    /**
     * Create a SliderField with a ValueHolder to wrap an AtomicInteger and its range.
     */
    public static SliderButton create(int buttonId, final AtomicInteger property, final int min, final int max, final String messageKey, final boolean displayAsPercent)
    {
        ValueHolder intValueHolder = new ValueHolder()
        {
            boolean displayPercentage = displayAsPercent;

            @Override
            public void setValueFromSlider(float sliderValue)
            {
                if(sliderValue==0)
                {
                    property.set(min);
                }
                else if(sliderValue==1)
                {
                    property.set(max);
                }
                else
                {
                    property.set((int) (sliderValue * (max + min)));
                }
            }

            @Override
            public float getValueForSlider()
            {
                return ((property.get() * 1f) / (max + min));
            }

            @Override
            public String getDisplayString()
            {
                Object displayValue = null;
                if(displayPercentage)
                {
                    NumberFormat percentFormat = NumberFormat.getPercentInstance();
                    percentFormat.setMaximumFractionDigits(0);
                    displayValue = percentFormat.format(getValueForSlider());
                }
                else
                {
                    displayValue = property.get();
                }
                return Constants.getString(messageKey, displayValue);
            }
        };

        return new SliderButton(buttonId, intValueHolder);
    }

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
    protected void mouseDragged(Minecraft par1Minecraft, int par2, int par3)
    {
        if (this.drawButton)
        {
            if (this.dragging)
            {
                this.sliderValue = (float) (par2 - (this.xPosition + 4)) / (float) (this.width - 8);

                if (this.sliderValue < 0.0F)
                {
                    this.sliderValue = 0.0F;
                }

                if (this.sliderValue > 1.0F)
                {
                    this.sliderValue = 1.0F;
                }

                valueHolder.setValueFromSlider(this.sliderValue);
                this.displayString = valueHolder.getDisplayString();
            }

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.drawTexturedModalRect(this.xPosition + (int) (this.sliderValue * (float) (this.width - 8)), this.yPosition, 0, 66, 4, 20);
            this.drawTexturedModalRect(this.xPosition + (int) (this.sliderValue * (float) (this.width - 8)) + 4, this.yPosition, 196, 66, 4, 20);
        }
    }

    /**
     * Returns true if the mouse has been pressed on this control. Equivalent of MouseListener.mousePressed(MouseEvent
     * e).
     */
    public boolean mousePressed(Minecraft par1Minecraft, int par2, int par3)
    {
        if (super.mousePressed(par1Minecraft, par2, par3))
        {
            this.sliderValue = (float) (par2 - (this.xPosition + 4)) / (float) (this.width - 8);
            updateValue();
            this.dragging = true;
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Fired when the mouse button is released. Equivalent of MouseListener.mouseReleased(MouseEvent e).
     */
    public void mouseReleased(int par1, int par2)
    {
        this.dragging = false;
        updateValue();
    }

    /**
     * Clamps the slider value, sets the value from the slider, updates display string.
     */
    public void updateValue()
    {
        if (this.sliderValue < 0.0F)
        {
            this.sliderValue = 0.0F;
        }

        if (this.sliderValue > 1.0F)
        {
            this.sliderValue = 1.0F;
        }

        valueHolder.setValueFromSlider(this.sliderValue);
        this.displayString = valueHolder.getDisplayString();
    }

    /**
     * Encapsulates how the slider value is actually use.
     */
    interface ValueHolder
    {
        void setValueFromSlider(float sliderValue);

        float getValueForSlider();

        String getDisplayString();
    }
}
