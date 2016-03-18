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
import journeymap.common.properties.config.BooleanField;
import journeymap.common.properties.config.IntegerField;
import journeymap.common.properties.config.StringField;

import static journeymap.common.properties.Category.Inherit;

/**
 * Shared Properties for in-game map types.
 */
public abstract class InGameMapProperties extends MapProperties
{
    public final BooleanField showCaves = new BooleanField(Inherit, "jm.common.show_caves", true);
    public final IntegerField fontScale = new IntegerField(Inherit, "jm.common.font_scale", 1, 4, 1);
    public final BooleanField textureSmall = new BooleanField(Inherit, "jm.minimap.texture_size", true);
    public final BooleanField showWaypointLabels = new BooleanField(Inherit, "jm.minimap.show_waypointlabels", true);
    public final BooleanField locationFormatVerbose = new BooleanField(Inherit, "jm.common.location_format_verbose", true);
    public final StringField locationFormat = new StringField(Inherit, "jm.common.location_format", LocationFormat.IdProvider.class);
    public final BooleanField showMobHeading = new BooleanField(Inherit, "jm.minimap.mob_heading", true);
    public final BooleanField showPlayerHeading = new BooleanField(Inherit, "jm.minimap.player_heading", true);

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
