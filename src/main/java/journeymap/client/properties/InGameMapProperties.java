/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.properties;

import com.google.common.base.Objects;
import journeymap.client.ui.option.LocationFormat;
import journeymap.common.properties.config.Config;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static journeymap.common.properties.config.Config.Category.Inherit;

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

    @Config(category = Inherit, key = "jm.minimap.mob_heading")
    public final AtomicBoolean showMobHeading = new AtomicBoolean(true);

    @Config(category = Inherit, key = "jm.minimap.player_heading")
    public final AtomicBoolean showPlayerHeading = new AtomicBoolean(true);

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
        result = 31 * result + showCaves.hashCode();
        result = 31 * result + fontScale.hashCode();
        result = 31 * result + textureSmall.hashCode();
        result = 31 * result + showWaypointLabels.hashCode();
        result = 31 * result + locationFormatVerbose.hashCode();
        result = 31 * result + locationFormat.hashCode();
        result = 31 * result + showMobHeading.hashCode();
        result = 31 * result + showPlayerHeading.hashCode();
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
                .add("showMobHeading", showMobHeading)
                .add("showPlayerHeading", showPlayerHeading)
                .add("textureSmall", textureSmall);
    }
}
