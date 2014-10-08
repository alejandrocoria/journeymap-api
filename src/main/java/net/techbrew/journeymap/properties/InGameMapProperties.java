/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.properties;

import net.techbrew.journeymap.properties.config.Config;

import java.util.concurrent.atomic.AtomicBoolean;

import static net.techbrew.journeymap.properties.config.Config.Category.Inherit;

/**
 * Shared Properties for in-game map types.
 */
public abstract class InGameMapProperties extends MapProperties
{
    @Config(category = Inherit, key = "jm.minimap.force_unicode", defaultBoolean = false)
    public final AtomicBoolean forceUnicode = new AtomicBoolean(false);

    @Config(category = Inherit, key = "jm.common.font")
    public final AtomicBoolean fontSmall = new AtomicBoolean(true);

    @Config(category = Inherit, key = "jm.minimap.texture_size")
    public final AtomicBoolean textureSmall = new AtomicBoolean(true);

    protected InGameMapProperties()
    {
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        InGameMapProperties that = (InGameMapProperties) o;
        return this.compareTo(that) == 0;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + forceUnicode.hashCode();
        result = 31 * result + fontSmall.hashCode();
        result = 31 * result + textureSmall.hashCode();
        return result;
    }
}
