package net.techbrew.journeymap.ui.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.techbrew.journeymap.properties.PropertiesBase;
import org.lwjgl.input.Keyboard;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Mark on 8/28/2014.
 */
public class StringPropertyButton extends Button
{
    final PropertiesBase properties;
    final AtomicReference<String> valueHolder;
    final List<String> values;
    final String baseLabel;
    final String glyph = "\u21D5";
    final String labelPattern = "%1$s:  %2$s %3$s %2$s";

    public StringPropertyButton(int id, String[] stringValues, String label, PropertiesBase properties, AtomicReference<String> valueHolder)
    {
        super(id, "");
        this.valueHolder = valueHolder;
        this.properties = properties;
        this.values = Arrays.asList(stringValues);
        this.baseLabel = label;
        setValue(valueHolder.get());
    }

    public void setValue(String value)
    {
        if (!valueHolder.get().equals(value))
        {
            valueHolder.set(value);
            properties.save();
        }
        displayString = getFormattedLabel(value);
    }

    private String getFormattedLabel(String value)
    {
        return String.format(labelPattern, baseLabel, glyph, value);
    }

    public AtomicReference<String> getValueHolder()
    {
        return valueHolder;
    }

    public void prevOption()
    {
        int index = values.indexOf(valueHolder.get()) - 1;
        if (index == -1)
        {
            index = values.size() - 1;
        }
        setValue(values.get(index));
    }

    public void nextOption()
    {
        int index = values.indexOf(valueHolder.get()) + 1;
        if (index == values.size())
        {
            index = 0;
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

    @Override
    public int getFitWidth(FontRenderer fr)
    {
        int max = fr.getStringWidth(displayString);
        for (String value : values)
        {
            max = Math.max(max, fr.getStringWidth(getFormattedLabel(value)));
        }
        return max + WIDTH_PAD;
    }

    public boolean keyTyped(char c, int i)
    {
        if (this.field_146123_n)
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
        setValue(valueHolder.get());
    }
}
