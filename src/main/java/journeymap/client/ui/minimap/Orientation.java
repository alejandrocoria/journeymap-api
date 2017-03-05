/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.minimap;

import journeymap.client.Constants;
import journeymap.client.ui.option.KeyedEnum;

/**
 * @author techbrew 9/26/2014.
 */
public enum Orientation implements KeyedEnum
{
    /**
     * North orientation.
     */
    North("jm.minimap.orientation.north"),
    /**
     * Old north orientation.
     */
    OldNorth("jm.minimap.orientation.oldnorth"),
    /**
     * Player heading orientation.
     */
    PlayerHeading("jm.minimap.orientation.playerheading");

    /**
     * The Key.
     */
    public final String key;

    Orientation(String key)
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
