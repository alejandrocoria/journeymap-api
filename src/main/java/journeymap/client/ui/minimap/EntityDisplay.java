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
public enum EntityDisplay implements KeyedEnum
{
    LargeDots("jm.common.entity_display.large_dots"),
    SmallDots("jm.common.entity_display.small_dots"),
    LargeIcons("jm.common.entity_display.large_icons"),
    SmallIcons("jm.common.entity_display.small_icons");

    public final String key;

    EntityDisplay(String key)
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

    public boolean isDots()
    {
        return this == LargeDots || this == SmallDots;
    }

    public boolean isLarge()
    {
        return this == LargeDots || this == LargeIcons;
    }
}
