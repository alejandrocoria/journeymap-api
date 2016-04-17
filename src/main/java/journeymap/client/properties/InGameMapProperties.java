/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */
package journeymap.client.properties;

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
}
