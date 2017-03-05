/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.component;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

import java.awt.*;

/**
 * Extension of MC's text field
 */
public class TextField extends GuiTextField
{

    /**
     * The Numeric regex.
     */
    protected final String numericRegex;
    /**
     * The Numeric.
     */
    protected final boolean numeric;
    /**
     * The Allow negative.
     */
    protected final boolean allowNegative;
    /**
     * The Min length.
     */
    protected int minLength;
    /**
     * The Clamp min.
     */
    protected Integer clampMin;
    /**
     * The Clamp max.
     */
    protected Integer clampMax;

    /**
     * Instantiates a new Text field.
     *
     * @param text         the text
     * @param fontRenderer the font renderer
     * @param width        the width
     * @param height       the height
     */
    public TextField(Object text, FontRenderer fontRenderer, int width, int height)
    {
        this(text, fontRenderer, width, height, false, false);
    }

    /**
     * Instantiates a new Text field.
     *
     * @param text         the text
     * @param fontRenderer the font renderer
     * @param width        the width
     * @param height       the height
     * @param isNumeric    the is numeric
     * @param negative     the negative
     */
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

    /**
     * Sets clamp.
     *
     * @param min the min
     * @param max the max
     */
    public void setClamp(Integer min, Integer max)
    {
        this.clampMin = min;
        this.clampMax = max;
    }

    /**
     * Sets min length.
     *
     * @param minLength the min length
     */
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

    /**
     * Sets text.
     *
     * @param object the object
     */
    public void setText(Object object)
    {
        super.setText(object.toString());
    }

    /**
     * Is numeric boolean.
     *
     * @return the boolean
     */
    public boolean isNumeric()
    {
        return numeric;
    }

    /**
     * Has min length boolean.
     *
     * @return the boolean
     */
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
     *
     * @return the integer
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

    /**
     * Gets x.
     *
     * @return the x
     */
    public int getX()
    {
        // 1.7
        // return (Integer) ReflectionHelper.getPrivateValue(GuiTextField.class, this, INDEX_X);

        // 1.8
        return xPosition;
    }

    /**
     * Sets x.
     *
     * @param x the x
     */
    public void setX(int x)
    {
        // 1.7
        // ReflectionHelper.setPrivateValue(GuiTextField.class, this, x, INDEX_X);

        // 1.8
        xPosition = x;
    }

    /**
     * Gets y.
     *
     * @return the y
     */
    public int getY()
    {
        // 1.7
        // return (Integer) ReflectionHelper.getPrivateValue(GuiTextField.class, this, INDEX_Y);

        // 1.8
        return yPosition;
    }

    /**
     * Sets y.
     *
     * @param y the y
     */
    public void setY(int y)
    {
        // 1.7
        // ReflectionHelper.setPrivateValue(GuiTextField.class, this, y, INDEX_Y);

        // 1.8
        yPosition = y;
    }

    public int getWidth()
    {
        // 1.7
        // return (Integer) ReflectionHelper.getPrivateValue(GuiTextField.class, this, INDEX_WIDTH);

        // 1.8
        return width;
    }

    /**
     * Sets width.
     *
     * @param w the w
     */
    public void setWidth(int w)
    {
        // 1.7
        // ReflectionHelper.setPrivateValue(GuiTextField.class, this, w, INDEX_WIDTH);

        // 1.8
        width = w;
    }

    /**
     * Gets height.
     *
     * @return the height
     */
    public int getHeight()
    {
        // 1.7
        // return (Integer) ReflectionHelper.getPrivateValue(GuiTextField.class, this, INDEX_HEIGHT);

        // 1.8
        return height;
    }

    /**
     * Gets center x.
     *
     * @return the center x
     */
    public int getCenterX()
    {
        return getX() + (getWidth() / 2);
    }

    /**
     * Gets middle y.
     *
     * @return the middle y
     */
    public int getMiddleY()
    {
        return getY() + (getHeight() / 2);
    }

    /**
     * Gets bottom y.
     *
     * @return the bottom y
     */
    public int getBottomY()
    {
        return getY() + getHeight();
    }

    /**
     * Gets right x.
     *
     * @return the right x
     */
    public int getRightX()
    {
        return getX() + getWidth();
    }
}
