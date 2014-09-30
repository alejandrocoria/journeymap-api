package net.techbrew.journeymap.ui.component;

import net.minecraft.client.Minecraft;
import net.techbrew.journeymap.properties.PropertiesBase;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Mark on 8/28/2014.
 */
public class EnumPropertyButton<E extends Enum> extends Button
{
    final PropertiesBase properties;
    final AtomicReference<E> valueHolder;
    final List<E> values;
    final String baseLabel;
    final String glyph = "\u21D5";

    public EnumPropertyButton(int id, E[] enumValues, String label, PropertiesBase properties, AtomicReference<E> valueHolder)
    {
        super(id, "");
        this.valueHolder = valueHolder;
        this.properties = properties;
        this.values = Arrays.asList(enumValues);
        this.baseLabel = label;
        setValue(valueHolder.get());
    }

    public void setValue(E value)
    {
        valueHolder.set(value);
        properties.save();
        displayString = String.format("%1$s:  %2$s %3$s %2$s", baseLabel, glyph, value.toString());
    }

    public AtomicReference<E> getValueHolder()
    {
        return valueHolder;
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
}
