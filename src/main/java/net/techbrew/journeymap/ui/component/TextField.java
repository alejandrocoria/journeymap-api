/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package net.techbrew.journeymap.ui.component;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.awt.*;

/**
 * Created by mwoodman on 3/1/14.
 */
public class TextField extends GuiTextField
{
    // ReflectionHelper field indices
    protected static final int INDEX_X = 1;
    protected static final int INDEX_Y = 2;
    protected static final int INDEX_WIDTH = 3;
    protected static final int INDEX_HEIGHT = 4;

    protected final String numericRegex;
    protected final boolean numeric;
    protected final boolean allowNegative;
    protected int minLength;
    protected Integer clampMin;
    protected Integer clampMax;

    public TextField(Object text, FontRenderer fontRenderer, int width, int height)
    {
        this(text, fontRenderer, width, height, false, false);
    }

    public TextField(Object text, FontRenderer fontRenderer, int width, int height, boolean isNumeric, boolean negative)
    {
        // 1.7
        // super(0, fontRenderer, 0, 0, width, height);

        // 1.8
        super(0, fontRenderer, 0, 0, width, height);
        setText(text.toString());
        numeric = isNumeric;
        allowNegative = negative;

        String regex = null;
        if (numeric)
        {
            if (allowNegative)
            {
                regex = "[^-?\\d]";
            }
            else
            {
                regex = "[^\\d]";
            }
        }

        numericRegex = regex;
    }

    public void setClamp(Integer min, Integer max)
    {
        this.clampMin = min;
        this.clampMax = max;
    }

    public void setMinLength(int minLength)
    {
        this.minLength = minLength;
    }

    @Override
    public void writeText(String par1Str)
    {
        super.writeText(par1Str);
        if (numeric)
        {
            String fixed = getText().replaceAll(numericRegex, "");
            if (allowNegative)
            {
                // TODO: figure out how to make regex disallow "-" after first char
                String start = fixed.startsWith("-") ? "-" : "";
                fixed = start + fixed.replaceAll("-", "");
            }
            super.setText(fixed);
        }
    }

    public void setText(Object object)
    {
        super.setText(object.toString());
    }

    public boolean isNumeric()
    {
        return numeric;
    }

    public boolean hasMinLength()
    {
        String text = getText();
        int textLen = text == null ? 0 : text.length();
        return minLength <= textLen;
    }

    public boolean textboxKeyTyped(char par1, int par2)
    {
        boolean res = super.textboxKeyTyped(par1, par2);
        if (numeric && this.isFocused())
        {
            clamp();
        }
        return res;
    }

    /**
     * Draws the textbox
     */
    public void drawTextBox()
    {
        super.drawTextBox();
        if (this.getVisible())
        {
            if (!hasMinLength())
            {
                int red = Color.red.getRGB();
                int x1 = getX() - 1;
                int y1 = getY() - 1;
                int x2 = x1 + getWidth() + 1;
                int y2 = y1 + getHeight() + 1;

                drawRect(x1, y1, x2, y1 + 1, red);
                drawRect(x1, y2, x2, y2 + 1, red);

                drawRect(x1, y1, x1 + 1, y2, red);
                drawRect(x2, y1, x2 + 1, y2, red);
            }
        }

    }

    /**
     * If numeric field, clamp values in range.
     */
    public Integer clamp()
    {
        if (!numeric)
        {
            return null;
        }

        String text = getText();
        if (clampMin != null)
        {

            if (text == null || text.length() == 0 || text.equals("-"))
            {
                return null;
            }

            try
            {
                setText(Math.max(clampMin, Integer.parseInt(text)));
            }
            catch (Exception e)
            {
                setText(clampMin);
            }
            if (clampMax != null)
            {
                try
                {
                    setText(Math.min(clampMax, Integer.parseInt(text)));
                }
                catch (Exception e)
                {
                    setText(clampMax);
                }
            }
        }
        try
        {
            return Integer.parseInt(text);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public int getX()
    {
        return (Integer) ReflectionHelper.getPrivateValue(GuiTextField.class, this, INDEX_X);
    }

    public void setX(int x)
    {
        ReflectionHelper.setPrivateValue(GuiTextField.class, this, x, INDEX_X);
    }

    public int getY()
    {
        return (Integer) ReflectionHelper.getPrivateValue(GuiTextField.class, this, INDEX_Y);
    }

    public void setY(int y)
    {
        ReflectionHelper.setPrivateValue(GuiTextField.class, this, y, INDEX_Y);
    }

    public int getWidth()
    {
        return (Integer) ReflectionHelper.getPrivateValue(GuiTextField.class, this, INDEX_WIDTH);
    }

    public void setWidth(int w)
    {
        ReflectionHelper.setPrivateValue(GuiTextField.class, this, w, INDEX_WIDTH);
    }

    public int getHeight()
    {
        return (Integer) ReflectionHelper.getPrivateValue(GuiTextField.class, this, INDEX_HEIGHT);
    }

    public int getCenterX()
    {
        return getX() + (getWidth() / 2);
    }

    public int getMiddleY()
    {
        return getY() + (getHeight() / 2);
    }

    public int getBottomY()
    {
        return getY() + getHeight();
    }

    public int getRightX()
    {
        return getX() + getWidth();
    }
}
