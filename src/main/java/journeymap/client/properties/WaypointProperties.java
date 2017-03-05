/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */
package journeymap.client.properties;

import journeymap.common.properties.config.BooleanField;
import journeymap.common.properties.config.IntegerField;

import static journeymap.client.properties.ClientCategory.Waypoint;
import static journeymap.client.properties.ClientCategory.WaypointBeacon;

/**
 * Properties for in-game waypoint management and display.
 */
public class WaypointProperties extends ClientPropertiesBase implements Comparable<WaypointProperties>
{
    /**
     * The Manager enabled.
     */
    public final BooleanField managerEnabled = new BooleanField(Waypoint, "jm.waypoint.enable_manager", true, true);
    /**
     * The Beacon enabled.
     */
    public final BooleanField beaconEnabled = new BooleanField(WaypointBeacon, "jm.waypoint.enable_beacons", true, true);
    /**
     * The Show texture.
     */
    public final BooleanField showTexture = new BooleanField(WaypointBeacon, "jm.waypoint.show_texture", true);
    /**
     * The Show static beam.
     */
    public final BooleanField showStaticBeam = new BooleanField(WaypointBeacon, "jm.waypoint.show_static_beam", true);
    /**
     * The Show rotating beam.
     */
    public final BooleanField showRotatingBeam = new BooleanField(WaypointBeacon, "jm.waypoint.show_rotating_beam", true);
    /**
     * The Show name.
     */
    public final BooleanField showName = new BooleanField(WaypointBeacon, "jm.waypoint.show_name", true);
    /**
     * The Show distance.
     */
    public final BooleanField showDistance = new BooleanField(WaypointBeacon, "jm.waypoint.show_distance", true);
    /**
     * The Auto hide label.
     */
    public final BooleanField autoHideLabel = new BooleanField(WaypointBeacon, "jm.waypoint.auto_hide_label", true);
    /**
     * The Bold label.
     */
    public final BooleanField boldLabel = new BooleanField(WaypointBeacon, "jm.waypoint.bold_label", false);
    /**
     * The Font scale.
     */
    public final IntegerField fontScale = new IntegerField(WaypointBeacon, "jm.waypoint.font_scale", 1, 3, 2);
    /**
     * The Texture small.
     */
    public final BooleanField textureSmall = new BooleanField(WaypointBeacon, "jm.waypoint.texture_size", true);
    /**
     * The Max distance.
     */
    public final IntegerField maxDistance = new IntegerField(Waypoint, "jm.waypoint.max_distance", 0, 10000, 0);
    /**
     * The Min distance.
     */
    public final IntegerField minDistance = new IntegerField(WaypointBeacon, "jm.waypoint.min_distance", 0, 64, 4);
    /**
     * The Create deathpoints.
     */
    public final BooleanField createDeathpoints = new BooleanField(Waypoint, "jm.waypoint.create_deathpoints", true);

    @Override
    public String getName()
    {
        return "waypoint";
    }

    @Override
    public int compareTo(WaypointProperties other)
    {
        return Integer.valueOf(this.hashCode()).compareTo(other.hashCode());
    }
}
