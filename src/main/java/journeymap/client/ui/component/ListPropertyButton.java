/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.component;

import journeymap.client.cartography.color.RGB;
import journeymap.common.properties.config.ConfigField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Base class for a field that has a list of values.
 *
 * @param <T> the type parameter
 */
public class ListPropertyButton<T> extends Button implements IConfigFieldHolder<ConfigField<T>>
{
    /**
     * The Field.
     */
    protected final ConfigField<T> field;
    /**
     * The Values.
     */
    protected final List<T> values;
    /**
     * The Base label.
     */
    protected final String baseLabel;
    /**
     * The Glyph.
     */
    protected final String glyph = "\u21D5";
    /**
     * The Label pattern.
     */
    protected final String labelPattern = "%1$s : %2$s %3$s %2$s";

    /**
     * Instantiates a new List property button.
     *
     * @param values the values
     * @param label  the label
     * @param field  the field
     */
    public ListPropertyButton(Collection<T> values, String label, ConfigField<T> field)
    {
        super("");
        this.field = field;
        this.values = new ArrayList<T>(values);
        this.baseLabel = label;
        setValue(field.get());
        disabledLabelColor = RGB.DARK_GRAY_RGB;
    }

    /**
     * Sets value.
     *
     * @param value the value
     */
    public void setValue(T value)
    {
        if (!field.get().equals(value))
        {
            field.set(value);
            field.save();
        }
        displayString = getFormattedLabel(value.toString());
    }

    /**
     * Gets field.
     *
     * @return the field
     */
    public ConfigField<T> getField()
    {
        return field;
    }

    /**
     * Next option.
     */
    public void nextOption()
    {
        int index = values.indexOf(field.get()) + 1;
        if (index == values.size())
        {
            index = 0;
        }
        setValue(values.get(index));
    }

    /**
     * Prev option.
     */
    public void prevOption()
    {
        int index = values.indexOf(field.get()) - 1;
        if (index == -1)
        {
            index = values.size() - 1;
        }
        setValue(values.get(index));
    }

    @Override
    public boolean mousePressed(Minecraft minecraft, int i, int j)
    {
        if (super.mousePressed(minecraft, i, j))
        {
            nextOption();
            return true;
        }
        return false;
    }

    /**
     * Gets formatted label.
     *
     * @param value the value
     * @return the formatted label
     */
    protected String getFormattedLabel(String value)
    {
        return String.format(labelPattern, baseLabel, glyph, value);
    }

    @Override
    public int getFitWidth(FontRenderer fr)
    {
        int max = fr.getStringWidth(displayString);
        for (T value : values)
        {
            max = Math.max(max, fr.getStringWidth(getFormattedLabel(value.toString())));
        }
        return max + WIDTH_PAD;
    }

    public boolean keyTyped(char c, int i)
    {
        if (this.isMouseOver())
        {
            if (i == Keyboard.KEY_LEFT || i == Keyboard.KEY_DOWN || i == Keyboard.KEY_SUBTRACT)
            {
                prevOption();
                return true;
            }
            if (i == Keyboard.KEY_RIGHT || i == Keyboard.KEY_UP || i == Keyboard.KEY_ADD)
            {
                nextOption();
                return true;
            }
        }
        return false;
    }

    @Override
    public void refresh()
    {
        setValue(field.get());
    }

    @Override
    public ConfigField<T> getConfigField()
    {
        return field;
    }
}
