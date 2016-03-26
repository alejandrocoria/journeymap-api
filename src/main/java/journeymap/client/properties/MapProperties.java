/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */
package journeymap.client.properties;

import journeymap.client.io.IconSetFileHandler;
import journeymap.client.model.MapType;
import journeymap.common.properties.config.BooleanField;
import journeymap.common.properties.config.EnumField;
import journeymap.common.properties.config.IntegerField;
import journeymap.common.properties.config.StringField;

import static journeymap.client.properties.ClientCategory.Hidden;
import static journeymap.client.properties.ClientCategory.Inherit;

/**
 * Shared Properties for the various map types.
 */
public abstract class MapProperties extends ClientPropertiesBase implements Comparable<MapProperties>
{
    public final BooleanField showMobs = new BooleanField(Inherit, "jm.common.show_mobs", true);
    public final BooleanField showAnimals = new BooleanField(Inherit, "jm.common.show_animals", true);
    public final BooleanField showVillagers = new BooleanField(Inherit, "jm.common.show_villagers", true);
    public final BooleanField showPets = new BooleanField(Inherit, "jm.common.show_pets", true);
    public final BooleanField showPlayers = new BooleanField(Inherit, "jm.common.show_players", true);
    public final BooleanField showWaypoints = new BooleanField(Inherit, "jm.common.show_waypoints", true);
    public final BooleanField showSelf = new BooleanField(Inherit, "jm.common.show_self", true);
    public final BooleanField showGrid = new BooleanField(Inherit, "jm.common.show_grid", true);
    public final StringField entityIconSetName = new StringField(Inherit, "jm.common.mob_icon_set", IconSetFileHandler.IconSetValuesProvider.class);
    public final EnumField<MapType.Name> preferredMapType = new EnumField<MapType.Name>(Hidden, "", MapType.Name.day);
    public final IntegerField zoomLevel = new IntegerField(Hidden, "", 0, 8, 0);

    public MapProperties()
    {
    }

    @Override
    public int compareTo(MapProperties other)
    {
        return Integer.valueOf(this.hashCode()).compareTo(other.hashCode());
    }
}
