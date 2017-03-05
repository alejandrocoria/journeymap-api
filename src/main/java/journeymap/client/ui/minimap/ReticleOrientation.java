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
public enum ReticleOrientation implements KeyedEnum
{
    /**
     * Compass reticle orientation.
     */
    Compass("jm.minimap.orientation.compass"),
    /**
     * Player heading reticle orientation.
     */
    PlayerHeading("jm.minimap.orientation.playerheading");

    /**
     * The Key.
     */
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
