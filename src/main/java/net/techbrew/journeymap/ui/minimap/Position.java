package net.techbrew.journeymap.ui.minimap;

import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.ui.option.KeyedEnum;

/**
 * Position of minimap on screen
 */
public enum Position implements KeyedEnum
{
    TopRight("jm.minimap.position_topright"),
    BottomRight("jm.minimap.position_bottomright"),
    BottomLeft("jm.minimap.position_bottomleft"),
    TopLeft("jm.minimap.position_topleft"),
    TopCenter("jm.minimap.position_topcenter"),
    Center("jm.minimap.position_center");

    public final String key;

    Position(String key)
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
