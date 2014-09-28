package net.techbrew.journeymap.ui.minimap;

import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.ui.config.KeyedEnum;

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
