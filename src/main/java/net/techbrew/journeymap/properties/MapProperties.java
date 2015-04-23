/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.properties;

import com.google.common.base.Objects;
import net.techbrew.journeymap.io.IconSetFileHandler;
import net.techbrew.journeymap.model.MapType;
import net.techbrew.journeymap.properties.config.Config;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static net.techbrew.journeymap.properties.config.Config.Category.Inherit;

/**
 * Shared Properties for the various map types.
 */
public abstract class MapProperties extends PropertiesBase implements Comparable<MapProperties>
{
    @Config(category = Inherit, key = "jm.common.show_mobs")
    public final AtomicBoolean showMobs = new AtomicBoolean(true);

    @Config(category = Inherit, key = "jm.common.show_animals")
    public final AtomicBoolean showAnimals = new AtomicBoolean(true);

    @Config(category = Inherit, key = "jm.common.show_villagers")
    public final AtomicBoolean showVillagers = new AtomicBoolean(true);

    @Config(category = Inherit, key = "jm.common.show_pets")
    public final AtomicBoolean showPets = new AtomicBoolean(true);

    @Config(category = Inherit, key = "jm.common.show_players")
    public final AtomicBoolean showPlayers = new AtomicBoolean(true);

    @Config(category = Inherit, key = "jm.common.show_waypoints")
    public final AtomicBoolean showWaypoints = new AtomicBoolean(true);

    @Config(category = Inherit, key = "jm.common.show_self")
    public final AtomicBoolean showSelf = new AtomicBoolean(true);

    @Config(category = Inherit, key = "jm.common.show_grid")
    public final AtomicBoolean showGrid = new AtomicBoolean(true);

    @Config(category = Inherit, key = "jm.common.mob_icon_set", stringListProvider = IconSetFileHandler.IconSetStringListProvider.class)
    public final AtomicReference<String> entityIconSetName = new AtomicReference<String>("2D");

    public final AtomicInteger zoomLevel = new AtomicInteger(0);

    protected MapProperties()
    {
    }

    public abstract AtomicReference<String> getEntityIconSetName();

    public abstract AtomicReference<MapType.Name> getPreferredMapType();

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

        MapProperties that = (MapProperties) o;
        return this.compareTo(that) == 0;
    }

    @Override
    public int hashCode()
    {
        int result = 31 * showMobs.hashCode();
        result = 31 * result + showAnimals.hashCode();
        result = 31 * result + showVillagers.hashCode();
        result = 31 * result + showPets.hashCode();
        result = 31 * result + showPlayers.hashCode();
        result = 31 * result + showWaypoints.hashCode();
        result = 31 * result + showSelf.hashCode();
        result = 31 * result + getEntityIconSetName().hashCode();
        return result;
    }

    @Override
    public int compareTo(MapProperties other)
    {
        return Integer.valueOf(this.hashCode()).compareTo(other.hashCode());
    }

    protected Objects.ToStringHelper toStringHelper(MapProperties me)
    {
        return Objects.toStringHelper(me)
                .add("entityIconSetName", entityIconSetName)
                .add("showAnimals", showAnimals)
                .add("showMobs", showMobs)
                .add("showPets", showPets)
                .add("showPlayers", showPlayers)
                .add("showSelf", showSelf)
                .add("showVillagers", showVillagers)
                .add("showWaypoints", showWaypoints)
                .add("zoomLevel", zoomLevel);
    }


}
