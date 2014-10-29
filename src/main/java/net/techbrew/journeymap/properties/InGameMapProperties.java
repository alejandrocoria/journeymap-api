/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.properties;

import com.google.common.base.Objects;
import net.techbrew.journeymap.properties.config.Config;
import net.techbrew.journeymap.ui.option.LocationFormat;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static net.techbrew.journeymap.properties.config.Config.Category.Inherit;

/**
 * Shared Properties for in-game map types.
 */
public abstract class InGameMapProperties extends MapProperties
{
    @Config(category = Inherit, key = "jm.common.show_caves", defaultBoolean = true)
    public final AtomicBoolean showCaves = new AtomicBoolean(true);

    @Config(category = Inherit, key = "jm.common.font_scale", minValue = 1, maxValue = 4, defaultValue = 1)
    public final AtomicInteger fontScale = new AtomicInteger(1);

    @Config(category = Inherit, key = "jm.minimap.texture_size")
    public final AtomicBoolean textureSmall = new AtomicBoolean(true);

    @Config(category = Inherit, key = "jm.minimap.show_waypointlabels")
    public final AtomicBoolean showWaypointLabels = new AtomicBoolean(true);

    @Config(category = Inherit, key = "jm.common.location_format_verbose")
    public final AtomicBoolean locationFormatVerbose = new AtomicBoolean(true);

    @Config(category = Inherit, key = "jm.common.location_format", stringListProvider = LocationFormat.IdProvider.class)
    public final AtomicReference<String> locationFormat = new AtomicReference<String>(new LocationFormat.IdProvider().getDefaultString());

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
        result = 31 * result + fontScale.hashCode();
        result = 31 * result + textureSmall.hashCode();
        return result;
    }

    @Override
    protected Objects.ToStringHelper toStringHelper(MapProperties me)
    {
        return super.toStringHelper(me)
                .add("fontScale", fontScale)
                .add("locationFormat", locationFormat)
                .add("locationFormatVerbose", locationFormatVerbose)
                .add("showCaves", showCaves)
                .add("showWaypointLabels", showWaypointLabels)
                .add("textureSmall", textureSmall);
    }
}
