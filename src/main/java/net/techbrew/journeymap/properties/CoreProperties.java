/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.properties;

import net.techbrew.journeymap.io.ThemeFileHandler;
import net.techbrew.journeymap.ui.theme.Theme;
import net.techbrew.journeymap.ui.theme.ThemePresets;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static net.techbrew.journeymap.properties.Config.Category.*;

/**
 * Properties for basic mod configuration.
 */
public class CoreProperties extends PropertiesBase implements Comparable<CoreProperties>
{
    protected transient static final int CODE_REVISION = 3;

    @Config(category = Advanced)
    public final AtomicReference<String> logLevel = new AtomicReference<String>("INFO");

    @Config(category = Advanced)
    public final AtomicInteger chunkOffset = new AtomicInteger(8);

    @Config(category = Advanced)
    public final AtomicInteger entityPoll = new AtomicInteger(1800);

    @Config(category = Advanced)
    public final AtomicInteger playerPoll = new AtomicInteger(1900);

    @Config(category = Advanced)
    public final AtomicInteger chunkPoll = new AtomicInteger(2000);

    @Config(category = Advanced)
    public final AtomicInteger autoMapPoll = new AtomicInteger(2000);

    @Config(category = Advanced)
    public final AtomicInteger cacheAnimalsData = new AtomicInteger(3100);

    @Config(category = Advanced)
    public final AtomicInteger cacheMobsData = new AtomicInteger(3000);

    @Config(category = Advanced)
    public final AtomicInteger cachePlayerData = new AtomicInteger(1000);

    @Config(category = Advanced)
    public final AtomicInteger cachePlayersData = new AtomicInteger(2000);

    @Config(category = Advanced)
    public final AtomicInteger cacheVillagersData = new AtomicInteger(2200);

    @Config(category = Advanced)
    public final AtomicBoolean announceMod = new AtomicBoolean(true);

    @Config(category = Advanced)
    public final AtomicBoolean checkUpdates = new AtomicBoolean(true);

    @Config(category = Advanced)
    public final AtomicBoolean recordCacheStats = new AtomicBoolean(false);

    @Config(category = General, key="jm.common.ui_theme")
    public final AtomicReference<String> themeName = new AtomicReference<String>(ThemePresets.THEME_VICTORIAN.name);

    @Config(category = MapStyle, key="jm.common.map_style_caveignoreglass")
    public final AtomicBoolean caveIgnoreGlass = new AtomicBoolean(true);

    @Config(category = MapStyle, key="jm.common.map_style_bathymetry")
    public final AtomicBoolean mapBathymetry = new AtomicBoolean(false);

    @Config(category = MapStyle, key="jm.common.map_style_transparency")
    public final AtomicBoolean mapTransparency = new AtomicBoolean(true);

    @Config(category = MapStyle, key="jm.common.map_style_cavelighting")
    public final AtomicBoolean mapCaveLighting = new AtomicBoolean(true);

    @Config(category = MapStyle, key="jm.common.map_style_antialiasing")
    public final AtomicBoolean mapAntialiasing = new AtomicBoolean(true);

    @Config(category = MapStyle, key="jm.common.map_style_plantshadows")
    public final AtomicBoolean mapPlantShadows = new AtomicBoolean(false);

    @Config(category = MapStyle, key="jm.common.map_style_plants")
    public final AtomicBoolean mapPlants = new AtomicBoolean(true);

    @Config(category = MapStyle, key="jm.common.map_style_crops")
    public final AtomicBoolean mapCrops = new AtomicBoolean(true);

    @Config(category = MapStyle, key="jm.common.map_style_caveshowsurface")
    public final AtomicBoolean mapSurfaceAboveCaves = new AtomicBoolean(true);

    @Config(category = Radar, key="jm.common.radar_max_animals")
    public final AtomicInteger maxAnimalsData = new AtomicInteger(32);

    @Config(category = Radar, key="jm.common.radar_max_mobs")
    public final AtomicInteger maxMobsData = new AtomicInteger(32);

    @Config(category = Radar, key="jm.common.radar_max_players")
    public final AtomicInteger maxPlayersData = new AtomicInteger(32);

    @Config(category = Radar, key="jm.common.radar_max_villagers")
    public final AtomicInteger maxVillagersData = new AtomicInteger(32);

    @Config(category = Radar, key="jm.common.radar_hide_sneaking")
    public final AtomicBoolean hideSneakingEntities = new AtomicBoolean(true);

    protected transient final String name = "core";

    public CoreProperties()
    {
    }

    @Override
    protected boolean validate()
    {
        boolean saveNeeded = super.validate();

        Theme theme = ThemeFileHandler.getThemeByName(themeName.get());
        if(!theme.name.equals(themeName.get()))
        {
            themeName.set(theme.name);
            saveNeeded = true;
        }

        return saveNeeded;
    }

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
    public int compareTo(CoreProperties other)
    {
        return Integer.valueOf(this.hashCode()).compareTo(other.hashCode());
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

        CoreProperties that = (CoreProperties) o;
        return 0 == that.compareTo(this);
    }

    @Override
    public int hashCode()
    {
        int result = logLevel.hashCode();
        result = 31 * result + chunkOffset.hashCode();
        result = 31 * result + entityPoll.hashCode();
        result = 31 * result + playerPoll.hashCode();
        result = 31 * result + chunkPoll.hashCode();
        result = 31 * result + autoMapPoll.hashCode();
        result = 31 * result + cacheAnimalsData.hashCode();
        result = 31 * result + maxAnimalsData.hashCode();
        result = 31 * result + cacheMobsData.hashCode();
        result = 31 * result + maxMobsData.hashCode();
        result = 31 * result + cachePlayerData.hashCode();
        result = 31 * result + cachePlayersData.hashCode();
        result = 31 * result + maxPlayersData.hashCode();
        result = 31 * result + cacheVillagersData.hashCode();
        result = 31 * result + maxVillagersData.hashCode();
        result = 31 * result + announceMod.hashCode();
        result = 31 * result + checkUpdates.hashCode();
        result = 31 * result + caveIgnoreGlass.hashCode();
        result = 31 * result + recordCacheStats.hashCode();
        result = 31 * result + mapBathymetry.hashCode();
        result = 31 * result + mapTransparency.hashCode();
        result = 31 * result + mapCaveLighting.hashCode();
        result = 31 * result + mapAntialiasing.hashCode();
        result = 31 * result + mapPlantShadows.hashCode();
        result = 31 * result + mapPlants.hashCode();
        result = 31 * result + mapCrops.hashCode();
        result = 31 * result + mapSurfaceAboveCaves.hashCode();
        result = 31 * result + themeName.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return "CoreProperties{" +
                ", logLevel=" + logLevel +
                ", chunkOffset=" + chunkOffset +
                ", entityPoll=" + entityPoll +
                ", playerPoll=" + playerPoll +
                ", chunkPoll=" + chunkPoll +
                ", autoMapPoll=" + autoMapPoll +
                ", cacheAnimalsData=" + cacheAnimalsData +
                ", maxAnimalsData=" + maxAnimalsData +
                ", cacheMobsData=" + cacheMobsData +
                ", maxMobsData=" + maxMobsData +
                ", cachePlayerData=" + cachePlayerData +
                ", cachePlayersData=" + cachePlayersData +
                ", maxPlayersData=" + maxPlayersData +
                ", cacheVillagersData=" + cacheVillagersData +
                ", maxVillagersData=" + maxVillagersData +
                ", announceMod=" + announceMod +
                ", checkUpdates=" + checkUpdates +
                ", caveIgnoreGlass=" + caveIgnoreGlass +
                ", recordCacheStats=" + recordCacheStats +
                ", mapBathymetry=" + mapBathymetry +
                ", mapTransparency=" + mapTransparency +
                ", mapCaveLighting=" + mapCaveLighting +
                ", mapAntialiasing=" + mapAntialiasing +
                ", mapPlantShadows=" + mapPlantShadows +
                ", mapPlants=" + mapPlants +
                ", mapCrops=" + mapCrops +
                ", mapSurfaceAboveCaves=" + mapSurfaceAboveCaves +
                ", themeName=" + themeName + '\'' +
                '}';
    }
}
