/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.properties;

import net.techbrew.journeymap.io.ThemeFileHandler;
import net.techbrew.journeymap.log.JMLogger;
import net.techbrew.journeymap.properties.config.Config;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static net.techbrew.journeymap.properties.config.Config.Category.*;

/**
 * Properties for basic mod configuration.
 */
public class CoreProperties extends PropertiesBase implements Comparable<CoreProperties>
{
    protected transient static final int CODE_REVISION = 4;

    @Config(category = Advanced, key = "jm.advanced.loglevel", stringListProvider = JMLogger.LogLevelStringProvider.class)
    public final AtomicReference<String> logLevel = new AtomicReference<String>("INFO");

    @Config(category = Advanced, key = "jm.advanced.chunkoffset", minValue = 1, maxValue = 20, defaultValue = 8)
    public final AtomicInteger chunkOffset = new AtomicInteger(8);

    @Config(category = Advanced, key = "jm.advanced.chunkpoll", minValue = 1000, maxValue = 10000, defaultValue = 2000)
    public final AtomicInteger chunkPoll = new AtomicInteger(2000);

    @Config(category = Advanced, key = "jm.advanced.automappoll", minValue = 500, maxValue = 10000, defaultValue = 2000)
    public final AtomicInteger autoMapPoll = new AtomicInteger(2000);

    @Config(category = Advanced, key = "jm.advanced.cache_animals", minValue = 1000, maxValue = 10000, defaultValue = 3100)
    public final AtomicInteger cacheAnimalsData = new AtomicInteger(3100);

    @Config(category = Advanced, key = "jm.advanced.cache_mobs", minValue = 1000, maxValue = 10000, defaultValue = 3000)
    public final AtomicInteger cacheMobsData = new AtomicInteger(3000);

    @Config(category = Advanced, key = "jm.advanced.cache_player", minValue = 500, maxValue = 2000, defaultValue = 1000)
    public final AtomicInteger cachePlayerData = new AtomicInteger(1000);

    @Config(category = Advanced, key = "jm.advanced.cache_players", minValue = 1000, maxValue = 10000, defaultValue = 2000)
    public final AtomicInteger cachePlayersData = new AtomicInteger(2000);

    @Config(category = Advanced, key = "jm.advanced.cache_villagers", minValue = 1000, maxValue = 10000, defaultValue = 2200)
    public final AtomicInteger cacheVillagersData = new AtomicInteger(2200);

    @Config(category = Advanced, key = "jm.advanced.announcemod", defaultBoolean = true)
    public final AtomicBoolean announceMod = new AtomicBoolean(true);

    @Config(category = Advanced, key = "jm.advanced.checkupdates", defaultBoolean = true)
    public final AtomicBoolean checkUpdates = new AtomicBoolean(true);

    @Config(category = Advanced, key = "jm.advanced.recordcachestats", defaultBoolean = false)
    public final AtomicBoolean recordCacheStats = new AtomicBoolean(false);

    @Config(category = Advanced, key = "jm.advanced.browserpoll", minValue = 1000, maxValue = 10000, defaultValue = 2000)
    public final AtomicInteger browserPoll = new AtomicInteger(2000);

    @Config(category = FullMap, key = "jm.common.ui_theme", stringListProvider = ThemeFileHandler.ThemeStringListProvider.class)
    public final AtomicReference<String> themeName = new AtomicReference<String>(new ThemeFileHandler.ThemeStringListProvider().getDefaultString());

    @Config(category = Cartography, key = "jm.common.map_style_caveignoreglass", defaultBoolean = true)
    public final AtomicBoolean caveIgnoreGlass = new AtomicBoolean(true);

    @Config(category = Cartography, key = "jm.common.map_style_bathymetry", defaultBoolean = false)
    public final AtomicBoolean mapBathymetry = new AtomicBoolean(false);

    @Config(category = Cartography, key = "jm.common.map_style_transparency", defaultBoolean = true)
    public final AtomicBoolean mapTransparency = new AtomicBoolean(true);

    @Config(category = Cartography, key = "jm.common.map_style_cavelighting", defaultBoolean = true)
    public final AtomicBoolean mapCaveLighting = new AtomicBoolean(true);

    @Config(category = Cartography, key = "jm.common.map_style_antialiasing", defaultBoolean = true)
    public final AtomicBoolean mapAntialiasing = new AtomicBoolean(true);

    @Config(category = Cartography, key = "jm.common.map_style_plantshadows", defaultBoolean = false)
    public final AtomicBoolean mapPlantShadows = new AtomicBoolean(false);

    @Config(category = Cartography, key = "jm.common.map_style_plants", defaultBoolean = true)
    public final AtomicBoolean mapPlants = new AtomicBoolean(true);

    @Config(category = Cartography, key = "jm.common.map_style_crops", defaultBoolean = true)
    public final AtomicBoolean mapCrops = new AtomicBoolean(true);

    @Config(category = Cartography, key = "jm.common.map_style_caveshowsurface", defaultBoolean = true)
    public final AtomicBoolean mapSurfaceAboveCaves = new AtomicBoolean(true);

    @Config(category = Advanced, key = "jm.common.radar_max_animals", minValue = 1, maxValue = 128, defaultValue = 32)
    public final AtomicInteger maxAnimalsData = new AtomicInteger(32);

    @Config(category = Advanced, key = "jm.common.radar_max_mobs", minValue = 1, maxValue = 128, defaultValue = 32)
    public final AtomicInteger maxMobsData = new AtomicInteger(32);

    @Config(category = Advanced, key = "jm.common.radar_max_players", minValue = 1, maxValue = 128, defaultValue = 32)
    public final AtomicInteger maxPlayersData = new AtomicInteger(32);

    @Config(category = Advanced, key = "jm.common.radar_max_villagers", minValue = 1, maxValue = 128, defaultValue = 32)
    public final AtomicInteger maxVillagersData = new AtomicInteger(32);

    @Config(category = Advanced, key = "jm.common.radar_hide_sneaking", defaultBoolean = true)
    public final AtomicBoolean hideSneakingEntities = new AtomicBoolean(true);

    @Config(category = Advanced, key = "jm.common.radar_lateral_distance", minValue = 16, maxValue = 512, defaultValue = 64)
    public final AtomicInteger radarLateralDistance = new AtomicInteger(64);

    @Config(category = Advanced, key = "jm.common.radar_vertical_distance", minValue = 8, maxValue = 256, defaultValue = 16)
    public final AtomicInteger radarVerticalDistance = new AtomicInteger(16);

    protected transient final String name = "core";

    public CoreProperties()
    {
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
        result = 31 * result + chunkPoll.hashCode();
        result = 31 * result + autoMapPoll.hashCode();
        result = 31 * result + cacheAnimalsData.hashCode();
        result = 31 * result + cacheMobsData.hashCode();
        result = 31 * result + cachePlayerData.hashCode();
        result = 31 * result + cachePlayersData.hashCode();
        result = 31 * result + cacheVillagersData.hashCode();
        result = 31 * result + announceMod.hashCode();
        result = 31 * result + checkUpdates.hashCode();
        result = 31 * result + recordCacheStats.hashCode();
        result = 31 * result + browserPoll.hashCode();
        result = 31 * result + themeName.hashCode();
        result = 31 * result + caveIgnoreGlass.hashCode();
        result = 31 * result + mapBathymetry.hashCode();
        result = 31 * result + mapTransparency.hashCode();
        result = 31 * result + mapCaveLighting.hashCode();
        result = 31 * result + mapAntialiasing.hashCode();
        result = 31 * result + mapPlantShadows.hashCode();
        result = 31 * result + mapPlants.hashCode();
        result = 31 * result + mapCrops.hashCode();
        result = 31 * result + mapSurfaceAboveCaves.hashCode();
        result = 31 * result + maxAnimalsData.hashCode();
        result = 31 * result + maxMobsData.hashCode();
        result = 31 * result + maxPlayersData.hashCode();
        result = 31 * result + maxVillagersData.hashCode();
        result = 31 * result + hideSneakingEntities.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return "CoreProperties{" +
                "logLevel=" + logLevel +
                ", chunkOffset=" + chunkOffset +
                ", chunkPoll=" + chunkPoll +
                ", autoMapPoll=" + autoMapPoll +
                ", cacheAnimalsData=" + cacheAnimalsData +
                ", cacheMobsData=" + cacheMobsData +
                ", cachePlayerData=" + cachePlayerData +
                ", cachePlayersData=" + cachePlayersData +
                ", cacheVillagersData=" + cacheVillagersData +
                ", announceMod=" + announceMod +
                ", checkUpdates=" + checkUpdates +
                ", recordCacheStats=" + recordCacheStats +
                ", browserPoll=" + browserPoll +
                ", themeName=" + themeName +
                ", caveIgnoreGlass=" + caveIgnoreGlass +
                ", mapBathymetry=" + mapBathymetry +
                ", mapTransparency=" + mapTransparency +
                ", mapCaveLighting=" + mapCaveLighting +
                ", mapAntialiasing=" + mapAntialiasing +
                ", mapPlantShadows=" + mapPlantShadows +
                ", mapPlants=" + mapPlants +
                ", mapCrops=" + mapCrops +
                ", mapSurfaceAboveCaves=" + mapSurfaceAboveCaves +
                ", maxAnimalsData=" + maxAnimalsData +
                ", maxMobsData=" + maxMobsData +
                ", maxPlayersData=" + maxPlayersData +
                ", maxVillagersData=" + maxVillagersData +
                ", hideSneakingEntities=" + hideSneakingEntities +
                '}';
    }
}
