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
    /**
     * The Show waypoints.
     */
    public final BooleanField showWaypoints = new BooleanField(Inherit, "jm.common.show_waypoints", true);
    /**
     * The Show self.
     */
    public final BooleanField showSelf = new BooleanField(Inherit, "jm.common.show_self", true);
    /**
     * The Show grid.
     */
    public final BooleanField showGrid = new BooleanField(Inherit, "jm.common.show_grid", true);
    /**
     * The Show caves.
     */
    public final BooleanField showCaves = new BooleanField(Inherit, "jm.common.show_caves", true);
    /**
     * The Preferred map type.
     */
    public final EnumField<MapType.Name> preferredMapType = new EnumField<MapType.Name>(Hidden, "", MapType.Name.day);
    /**
     * The Zoom level.
     */
    public final IntegerField zoomLevel = new IntegerField(Hidden, "", 0, 8, 0);

    /**
     * Instantiates a new Map properties.
     */
    public MapProperties()
    {
    }

    @Override
    public int compareTo(MapProperties other)
    {
        return Integer.valueOf(this.hashCode()).compareTo(other.hashCode());
    }
}
