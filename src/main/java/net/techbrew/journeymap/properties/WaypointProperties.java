/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.properties;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static net.techbrew.journeymap.properties.Config.Category.Waypoint;

/**
 * Properties for in-game waypoint management and display.
 */
public class WaypointProperties extends PropertiesBase implements Comparable<WaypointProperties>
{
    protected transient static final int CODE_REVISION = 4;

    @Config(category = Waypoint, master = true, key = "jm.waypoint.enable_manager")
    public final AtomicBoolean managerEnabled = new AtomicBoolean(true);

    @Config(category = Waypoint, key = "jm.waypoint.enable_beacons")
    public final AtomicBoolean beaconEnabled = new AtomicBoolean(true);

    @Config(category = Waypoint, key = "jm.waypoint.show_texture")
    public final AtomicBoolean showTexture = new AtomicBoolean(true);

    @Config(category = Waypoint, key = "jm.waypoint.show_static_beam")
    public final AtomicBoolean showStaticBeam = new AtomicBoolean(true);

    @Config(category = Waypoint, key = "jm.waypoint.show_rotating_beam")
    public final AtomicBoolean showRotatingBeam = new AtomicBoolean(true);

    @Config(category = Waypoint, key = "jm.waypoint.show_name")
    public final AtomicBoolean showName = new AtomicBoolean(true);

    @Config(category = Waypoint, key = "jm.waypoint.show_distance")
    public final AtomicBoolean showDistance = new AtomicBoolean(true);

    @Config(category = Waypoint, key = "jm.waypoint.auto_hide_label")
    public final AtomicBoolean autoHideLabel = new AtomicBoolean(true);

    @Config(category = Waypoint, key = "jm.waypoint.bold_label", defaultBoolean = false)
    public final AtomicBoolean boldLabel = new AtomicBoolean(false);

    @Config(category = Waypoint, key = "jm.waypoint.force_unicode", defaultBoolean = false)
    public final AtomicBoolean forceUnicode = new AtomicBoolean(false);

    @Config(category = Waypoint, key = "jm.common.font")
    public final AtomicBoolean fontSmall = new AtomicBoolean(true);

    @Config(category = Waypoint, key = "jm.waypoint.force_unicode")
    public final AtomicBoolean textureSmall = new AtomicBoolean(true);

    @Config(category = Waypoint, key = "jm.waypoint.max_distance", minInt = -1, maxInt = 10000, defaultInt = 0)
    public final AtomicInteger maxDistance = new AtomicInteger(0);

    @Config(category = Waypoint, key = "jm.waypoint.create_deathpoints")
    public final AtomicBoolean createDeathpoints = new AtomicBoolean(true);

    protected transient final String name = "waypoint";

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public int getCodeRevision()
    {
        return CODE_REVISION;
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
        int result = name.hashCode();
        result = 31 * result + fileRevision;
        result = 31 * result + managerEnabled.hashCode();
        result = 31 * result + beaconEnabled.hashCode();
        result = 31 * result + showTexture.hashCode();
        result = 31 * result + showStaticBeam.hashCode();
        result = 31 * result + showRotatingBeam.hashCode();
        result = 31 * result + showName.hashCode();
        result = 31 * result + showDistance.hashCode();
        result = 31 * result + autoHideLabel.hashCode();
        result = 31 * result + boldLabel.hashCode();
        result = 31 * result + forceUnicode.hashCode();
        result = 31 * result + fontSmall.hashCode();
        result = 31 * result + textureSmall.hashCode();
        result = 31 * result + maxDistance.hashCode();
        return result;
    }

    @Override
    public int compareTo(WaypointProperties other)
    {
        return Integer.valueOf(this.hashCode()).compareTo(other.hashCode());
    }

    @Override
    public String toString()
    {
        return "WaypointProperties: " +
                "fileRevision=" + fileRevision +
                ", managerEnabled=" + managerEnabled +
                ", beaconEnabled=" + beaconEnabled +
                ", showTexture=" + showTexture +
                ", showStaticBeam=" + showStaticBeam +
                ", showRotatingBeam=" + showRotatingBeam +
                ", showName=" + showName +
                ", showDistance=" + showDistance +
                ", autoHideLabel=" + autoHideLabel +
                ", boldLabel=" + boldLabel +
                ", forceUnicode=" + forceUnicode +
                ", fontSmall=" + fontSmall +
                ", textureSmall=" + textureSmall +
                ", maxDistance=" + maxDistance;
    }
}
