/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */
package journeymap.client.properties;

import com.google.common.base.Objects;
import journeymap.common.properties.config.BooleanField;
import journeymap.common.properties.config.IntegerField;

import static journeymap.common.properties.Category.Waypoint;
import static journeymap.common.properties.Category.WaypointBeacon;

/**
 * Properties for in-game waypoint management and display.
 */
public class WaypointProperties extends ClientProperties implements Comparable<WaypointProperties>
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
    public final BooleanField createDeathpoints = new BooleanField(Waypoint, "jm.waypoint.create_deathpoints", true);

    @Override
    public String getName()
    {
        return "waypoint";
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
        WaypointProperties that = (WaypointProperties) o;
        return 0 == this.compareTo(that);
    }

    @Override
    public int hashCode()
    {
        int result = managerEnabled.hashCode();
        result = 31 * result + beaconEnabled.hashCode();
        result = 31 * result + showTexture.hashCode();
        result = 31 * result + showStaticBeam.hashCode();
        result = 31 * result + showRotatingBeam.hashCode();
        result = 31 * result + showName.hashCode();
        result = 31 * result + showDistance.hashCode();
        result = 31 * result + autoHideLabel.hashCode();
        result = 31 * result + boldLabel.hashCode();
        result = 31 * result + fontScale.hashCode();
        result = 31 * result + textureSmall.hashCode();
        result = 31 * result + maxDistance.hashCode();
        result = 31 * result + createDeathpoints.hashCode();
        result = 31 * result + getName().hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("autoHideLabel", autoHideLabel)
                .add("beaconEnabled", beaconEnabled)
                .add("boldLabel", boldLabel)
                .add("createDeathpoints", createDeathpoints)
                .add("fontScale", fontScale)
                .add("managerEnabled", managerEnabled)
                .add("maxDistance", maxDistance)
                .add("name", getName())
                .add("showDistance", showDistance)
                .add("showName", showName)
                .add("showRotatingBeam", showRotatingBeam)
                .add("showStaticBeam", showStaticBeam)
                .add("showTexture", showTexture)
                .add("textureSmall", textureSmall)
                .toString();
    }

    @Override
    public int compareTo(WaypointProperties other)
    {
        return Integer.valueOf(this.hashCode()).compareTo(other.hashCode());
    }
}
