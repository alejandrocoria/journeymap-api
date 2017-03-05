/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.minimap;

import journeymap.client.Constants;
import journeymap.client.ui.option.KeyedEnum;

/**
 * Shape (and size) of minimap
 */
public enum Shape implements KeyedEnum
{
    /**
     * Square shape.
     */
    Square("jm.minimap.shape_square"),
    /**
     * Rectangle shape.
     */
    Rectangle("jm.minimap.shape_rectangle"),
    /**
     * Circle shape.
     */
    Circle("jm.minimap.shape_circle");
    /**
     * The Key.
     */
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
