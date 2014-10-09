package net.techbrew.journeymap.ui.minimap;

import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.ui.option.KeyedEnum;

/**
 * Shape (and size) of minimap
 */
public enum Shape implements KeyedEnum
{
    Square("jm.minimap.shape_square"),
    Rectangle("jm.minimap.shape_rectangle"),
    Circle("jm.minimap.shape_circle");
    public final String key;

    Shape(String key)
    {
        this.key = key;
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
