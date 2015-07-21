/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.ui.minimap;

import journeymap.common.Constants;
import journeymap.client.ui.option.KeyedEnum;

/**
 * Created by Mark on 9/26/2014.
 */
public enum Orientation implements KeyedEnum
{
    North("jm.minimap.orientation.north"),
    OldNorth("jm.minimap.orientation.oldnorth"),
    PlayerHeading("jm.minimap.orientation.playerheading");

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
