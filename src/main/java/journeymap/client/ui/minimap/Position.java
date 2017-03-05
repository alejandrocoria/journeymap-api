/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.minimap;

import journeymap.client.Constants;
import journeymap.client.ui.option.KeyedEnum;

/**
 * Position of minimap on screen
 */
public enum Position implements KeyedEnum
{
    /**
     * Top right position.
     */
    TopRight("jm.minimap.position_topright"),
    /**
     * Bottom right position.
     */
    BottomRight("jm.minimap.position_bottomright"),
    /**
     * Bottom left position.
     */
    BottomLeft("jm.minimap.position_bottomleft"),
    /**
     * Top left position.
     */
    TopLeft("jm.minimap.position_topleft"),
    /**
     * Top center position.
     */
    TopCenter("jm.minimap.position_topcenter"),
    /**
     * Center position.
     */
    Center("jm.minimap.position_center");

    /**
     * The Key.
     */
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
