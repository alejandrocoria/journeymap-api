/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */
package journeymap.client.properties;

import journeymap.common.properties.Category;
import journeymap.common.properties.config.BooleanField;
import journeymap.common.properties.config.IntegerField;
import journeymap.common.properties.config.StringField;

import static journeymap.client.properties.ClientCategory.Waypoint;
import static journeymap.client.properties.ClientCategory.WaypointBeacon;

/**
 * Properties for in-game waypoint management and display.
 */
public class WaypointProperties extends ClientPropertiesBase implements Comparable<WaypointProperties>
{
    public final BooleanField managerEnabled = new BooleanField(Waypoint, "jm.waypoint.enable_manager", true, true);
    public final BooleanField beaconEnabled = new BooleanField(WaypointBeacon, "jm.waypoint.enable_beacons", true, true);
    public final BooleanField showTexture = new BooleanField(WaypointBeacon, "jm.waypoint.show_texture", true);
    public final BooleanField showStaticBeam = new BooleanField(WaypointBeacon, "jm.waypoint.show_static_beam", true);
    public final BooleanField showRotatingBeam = new BooleanField(WaypointBeacon, "jm.waypoint.show_rotating_beam", true);
    public final BooleanField showName = new BooleanField(WaypointBeacon, "jm.waypoint.show_name", true);
    public final BooleanField showDistance = new BooleanField(WaypointBeacon, "jm.waypoint.show_distance", true);
    public final BooleanField autoHideLabel = new BooleanField(WaypointBeacon, "jm.waypoint.auto_hide_label", true);
    public final BooleanField boldLabel = new BooleanField(WaypointBeacon, "jm.waypoint.bold_label", false);
    public final IntegerField fontScale = new IntegerField(WaypointBeacon, "jm.waypoint.font_scale", 1, 3, 2);
    public final BooleanField textureSmall = new BooleanField(WaypointBeacon, "jm.waypoint.texture_size", true);
    public final IntegerField maxDistance = new IntegerField(Waypoint, "jm.waypoint.max_distance", 0, 10000, 0);
    public final IntegerField minDistance = new IntegerField(WaypointBeacon, "jm.waypoint.min_distance", 0, 64, 4);
    public final BooleanField createDeathpoints = new BooleanField(Waypoint, "jm.waypoint.create_deathpoints", true);
    public final StringField teleportCommand = new StringField(Category.Hidden, "jm.advanced.teleport_command", null, "/tp {name} {x} {y} {z}");

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
