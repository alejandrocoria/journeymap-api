/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.ui.minimap;

import journeymap.client.Constants;
import journeymap.client.ui.option.KeyedEnum;

/**
 * Enum for showing mobs as icons or dots
 */
public enum MobDisplay implements KeyedEnum
{
    Dots("jm.minimap.mob_display.dots"),
    Icons("jm.minimap.mob_display.icons");

    public final String key;

    MobDisplay(String key)
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
