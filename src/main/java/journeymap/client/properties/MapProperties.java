/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */
package journeymap.client.properties;

import journeymap.client.model.MapType;
import journeymap.common.properties.config.BooleanField;
import journeymap.common.properties.config.EnumField;
import journeymap.common.properties.config.IntegerField;

import static journeymap.common.properties.Category.Hidden;
import static journeymap.common.properties.Category.Inherit;

/**
 * Shared Properties for the various map types.
 */
public abstract class MapProperties extends ClientPropertiesBase implements Comparable<MapProperties>
{
    public final BooleanField showWaypoints = new BooleanField(Inherit, "jm.common.show_waypoints", true);
    public final BooleanField showSelf = new BooleanField(Inherit, "jm.common.show_self", true);
    public final BooleanField showGrid = new BooleanField(Inherit, "jm.common.show_grid", true);
    public final BooleanField showCaves = new BooleanField(Inherit, "jm.common.show_caves", true);
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
