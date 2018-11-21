/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */
package journeymap.client.properties;

import journeymap.client.ui.minimap.EntityDisplay;
import journeymap.client.ui.option.LocationFormat;
import journeymap.common.properties.config.BooleanField;
import journeymap.common.properties.config.EnumField;
import journeymap.common.properties.config.IntegerField;
import journeymap.common.properties.config.StringField;

import static journeymap.common.properties.Category.Inherit;

/**
 * Shared Properties for in-game map types.
 */
public abstract class InGameMapProperties extends MapProperties
{
    public final EnumField<EntityDisplay> playerDisplay = new EnumField<>(Inherit, "jm.minimap.player_display", EntityDisplay.SmallDots);
    public final BooleanField showPlayerHeading = new BooleanField(Inherit, "jm.minimap.player_heading", true);
    public final EnumField<EntityDisplay> mobDisplay = new EnumField<>(Inherit, "jm.minimap.mob_display", EntityDisplay.SmallDots);
    public final BooleanField showMobHeading = new BooleanField(Inherit, "jm.minimap.mob_heading", true);
    public final BooleanField showMobs = new BooleanField(Inherit, "jm.common.show_mobs", true);
    public final BooleanField showAnimals = new BooleanField(Inherit, "jm.common.show_animals", true);
    public final BooleanField showVillagers = new BooleanField(Inherit, "jm.common.show_villagers", true);
    public final BooleanField showPets = new BooleanField(Inherit, "jm.common.show_pets", true);
    public final BooleanField showPlayers = new BooleanField(Inherit, "jm.common.show_players", true);
    public final IntegerField fontScale = new IntegerField(Inherit, "jm.common.font_scale", 1, 4, 1);
    public final BooleanField showWaypointLabels = new BooleanField(Inherit, "jm.minimap.show_waypointlabels", true);
    public final BooleanField locationFormatVerbose = new BooleanField(Inherit, "jm.common.location_format_verbose", true);
    public final StringField locationFormat = new StringField(Inherit, "jm.common.location_format", LocationFormat.IdProvider.class);

    protected InGameMapProperties()
    {
    }
}
