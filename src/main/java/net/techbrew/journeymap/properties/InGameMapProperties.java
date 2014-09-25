/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.properties;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static net.techbrew.journeymap.properties.Config.Category.MapUI;

/**
 * Shared Properties for in-game map types.
 */
public abstract class InGameMapProperties extends MapProperties
{
    @Config(category = MapUI, key="jm.minimap.force_unicode")
    public final AtomicBoolean forceUnicode = new AtomicBoolean(false);

    @Config(category = MapUI, key="jm.common.font", onKey = "jm.common.font_small", offKey = "jm.common.font_large")
    public final AtomicBoolean fontSmall = new AtomicBoolean(true);

    @Config(category = MapUI, key="jm.minimap.force_unicode", onKey = "jm.common.font_small", offKey = "jm.common.font_large")
    public final AtomicBoolean textureSmall = new AtomicBoolean(true);

    @Config(category = MapUI, key="jm.minimap.force_unicode", minInt = 0, maxInt = 255)
    public final AtomicInteger terrainAlpha = new AtomicInteger(255);

    protected InGameMapProperties()
    {
    }

    @Override
    protected boolean validate()
    {
        boolean saveNeeded = super.validate();

        if (terrainAlpha.get() < 0)
        {
            terrainAlpha.set(0);
            saveNeeded = true;
        }
        else if (terrainAlpha.get() > 255)
        {
            terrainAlpha.set(255);
            saveNeeded = true;
        }

        return saveNeeded;
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
        result = 31 * result + terrainAlpha.hashCode();
        return result;
    }
}
