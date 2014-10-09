package net.techbrew.journeymap.ui.minimap;

import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.ui.option.KeyedEnum;

import java.util.Arrays;

/**
 * Shape (and size) of minimap
 */
public enum Shape implements KeyedEnum
{
    Square("jm.minimap.shape_square"),
    Rectangle("jm.minimap.shape_rectangle"),
    Circle("jm.minimap.shape_circle");
    public static Shape[] Enabled = {Square, Rectangle, Circle};
    public final String key;

    Shape(String key)
    {
        this.key = key;
    }

    public static Shape safeValueOf(String name)
    {
        Shape value = null;
        try
        {
            value = Shape.valueOf(name);
        }
        catch (IllegalArgumentException e)
        {
            JourneyMap.getLogger().warn("Not a valid minimap shape: " + name);
        }

        if (value == null || !value.isEnabled())
        {
            value = Shape.Square;
        }
        return value;
    }

    public boolean isEnabled()
    {
        return Arrays.binarySearch(Shape.Enabled, this) >= 0;
    }

    @Override
    public String getKey()
    {
        return key;
    }

    @Override
    public String toString()
    {
        return Constants.getString(this.key);
    }
}
