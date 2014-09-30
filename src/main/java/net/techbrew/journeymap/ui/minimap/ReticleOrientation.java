package net.techbrew.journeymap.ui.minimap;

import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.ui.option.KeyedEnum;

/**
 * Created by Mark on 9/26/2014.
 */
public enum ReticleOrientation implements KeyedEnum
{
    Compass("jm.minimap.orientation.compass"),
    PlayerHeading("jm.minimap.orientation.playerheading");

    public final String key;

    ReticleOrientation(String key)
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
