/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */
package journeymap.client.properties;

import com.google.common.base.Objects;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.io.ThemeFileHandler;
import journeymap.client.log.JMLogger;
import journeymap.client.model.GridSpecs;
import journeymap.client.task.multi.RenderSpec;
import journeymap.common.properties.PropertiesSerializer;
import journeymap.common.properties.config.*;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import java.util.Arrays;

import static journeymap.common.properties.Category.*;

/**
 * Properties for basic mod configuration.
 */
public class CoreProperties extends ClientProperties implements Comparable<CoreProperties>
{
    public final StringField logLevel = new StringField(Advanced, "jm.advanced.loglevel", JMLogger.LogLevelStringProvider.class);
    public final IntegerField autoMapPoll = new IntegerField(Advanced, "jm.advanced.automappoll", 500, 10000, 2000);
    public final IntegerField cacheAnimalsData = new IntegerField(Advanced, "jm.advanced.cache_animals", 1000, 10000, 3100);
    public final IntegerField cacheMobsData = new IntegerField(Advanced, "jm.advanced.cache_mobs", 1000, 10000, 3000);
    public final IntegerField cachePlayerData = new IntegerField(Advanced, "jm.advanced.cache_player", 500, 2000, 1000);
    public final IntegerField cachePlayersData = new IntegerField(Advanced, "jm.advanced.cache_players", 1000, 10000, 2000);
    public final IntegerField cacheVillagersData = new IntegerField(Advanced, "jm.advanced.cache_villagers", 1000, 10000, 2200);
    public final BooleanField announceMod = new BooleanField(Advanced, "jm.advanced.announcemod", true);
    public final BooleanField checkUpdates = new BooleanField(Advanced, "jm.advanced.checkupdates", true);
    public final BooleanField recordCacheStats = new BooleanField(Advanced, "jm.advanced.recordcachestats", false);
    public final IntegerField browserPoll = new IntegerField(Advanced, "jm.advanced.browserpoll", 1000, 10000, 2000);
    public final StringField themeName = new StringField(FullMap, "jm.common.ui_theme", ThemeFileHandler.ThemeValuesProvider.class);
    public final BooleanField caveIgnoreGlass = new BooleanField(Cartography, "jm.common.map_style_caveignoreglass", true);
    public final BooleanField mapBathymetry = new BooleanField(Cartography, "jm.common.map_style_bathymetry", false);
    public final BooleanField mapTransparency = new BooleanField(Cartography, "jm.common.map_style_transparency", true);
    public final BooleanField mapCaveLighting = new BooleanField(Cartography, "jm.common.map_style_cavelighting", true);
    public final BooleanField mapAntialiasing = new BooleanField(Cartography, "jm.common.map_style_antialiasing", true);
    public final BooleanField mapPlantShadows = new BooleanField(Cartography, "jm.common.map_style_plantshadows", false);
    public final BooleanField mapPlants = new BooleanField(Cartography, "jm.common.map_style_plants", false);
    public final BooleanField mapCrops = new BooleanField(Cartography, "jm.common.map_style_crops", true);
    public final BooleanField mapSurfaceAboveCaves = new BooleanField(Cartography, "jm.common.map_style_caveshowsurface", true);
    public final IntegerField renderDistanceCaveMin = new IntegerField(Cartography, "jm.common.renderdistance_cave_min", 1, 32, 3, 101);
    public final IntegerField renderDistanceCaveMax = new IntegerField(Cartography, "jm.common.renderdistance_cave_max", 1, 32, 3, 102);
    public final IntegerField renderDistanceSurfaceMin = new IntegerField(Cartography, "jm.common.renderdistance_surface_min", 1, 32, 4, 103);
    public final IntegerField renderDistanceSurfaceMax = new IntegerField(Cartography, "jm.common.renderdistance_surface_max", 1, 32, 7, 104);
    public final IntegerField renderDelay = new IntegerField(Cartography, "jm.common.renderdelay", 0, 10, 2);
    public final EnumField<RenderSpec.RevealShape> revealShape = new EnumField<RenderSpec.RevealShape>(Cartography, "jm.common.revealshape", RenderSpec.RevealShape.Circle);
    public final BooleanField alwaysMapCaves = new BooleanField(Cartography, "jm.common.alwaysmapcaves", false);
    public final BooleanField alwaysMapSurface = new BooleanField(Cartography, "jm.common.alwaysmapsurface", false);
    public final BooleanField tileHighDisplayQuality = new BooleanField(Cartography, "jm.common.tile_display_quality", true);
    public final IntegerField maxAnimalsData = new IntegerField(Advanced, "jm.common.radar_max_animals", 1, 128, 32);
    public final IntegerField maxMobsData = new IntegerField(Advanced, "jm.common.radar_max_mobs", 1, 128, 32);
    public final IntegerField maxPlayersData = new IntegerField(Advanced, "jm.common.radar_max_players", 1, 128, 32);
    public final IntegerField maxVillagersData = new IntegerField(Advanced, "jm.common.radar_max_villagers", 1, 128, 32);
    public final BooleanField hideSneakingEntities = new BooleanField(Advanced, "jm.common.radar_hide_sneaking", true);
    public final IntegerField radarLateralDistance = new IntegerField(Advanced, "jm.common.radar_lateral_distance", 16, 512, 64);
    public final IntegerField radarVerticalDistance = new IntegerField(Advanced, "jm.common.radar_vertical_distance", 8, 256, 16);
    public final IntegerField tileRenderType = new IntegerField(Advanced, "jm.advanced.tile_render_type", 1, 4, 1);

    // Hidden (not shown in Options Manager
    public final BooleanField mappingEnabled = new BooleanField(Hidden, "", true);
    public final EnumField<RenderGameOverlayEvent.ElementType> renderOverlayEventTypeName = new EnumField<RenderGameOverlayEvent.ElementType>(Hidden, "", RenderGameOverlayEvent.ElementType.ALL);
    public final BooleanField renderOverlayPreEvent = new BooleanField(Hidden, "", true);
    public final StringField optionsManagerViewed = new StringField(Hidden, "", null);
    public final StringField splashViewed = new StringField(Hidden, "", null);
    public final GridSpecs gridSpecs = new GridSpecs();

    public CoreProperties()
    {
    }

    @Override
    public String getName()
    {
        return "core";
    }

    @Override
    public int compareTo(CoreProperties other)
    {
        return Integer.valueOf(this.hashCode()).compareTo(other.hashCode());
    }

    /**
     * Should return true if save needed after validation.
     *
     * @return
     */
    @Override
    protected boolean validate()
    {
        boolean saveNeeded = super.validate();
        if (renderDistanceCaveMax.get() < renderDistanceCaveMin.get())
        {
            renderDistanceCaveMax.set(renderDistanceCaveMin.get());
            saveNeeded = true;
        }
        if (renderDistanceSurfaceMax.get() < renderDistanceSurfaceMin.get())
        {
            renderDistanceSurfaceMax.set(renderDistanceSurfaceMin.get());
            saveNeeded = true;
        }
        int gameRenderDistance = ForgeHelper.INSTANCE.getClient().gameSettings.renderDistanceChunks;
        for (IntegerField prop : Arrays.asList(renderDistanceCaveMin, renderDistanceCaveMax, renderDistanceSurfaceMin, renderDistanceSurfaceMax))
        {
            if (prop.get() > gameRenderDistance)
            {
                prop.set(gameRenderDistance);
                saveNeeded = true;
            }
        }
        return saveNeeded;
    }

    public boolean hasValidCaveRenderDistances()
    {
        return renderDistanceCaveMax.get() >= renderDistanceCaveMin.get();
    }

    public boolean hasValidSurfaceRenderDistances()
    {
        return renderDistanceSurfaceMax.get() >= renderDistanceSurfaceMin.get();
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
        return Objects.hashCode(announceMod, autoMapPoll, browserPoll, cacheAnimalsData, cacheMobsData, cachePlayerData,
                cachePlayersData, cacheVillagersData, caveIgnoreGlass, checkUpdates, renderDelay, hideSneakingEntities,
                logLevel, mapAntialiasing, mapBathymetry, mapCaveLighting, mapCrops, mapPlants, mapPlantShadows,
                mapSurfaceAboveCaves, mapTransparency, maxAnimalsData, maxMobsData, maxPlayersData, maxVillagersData,
                getName(), radarLateralDistance, radarVerticalDistance, recordCacheStats, renderOverlayEventTypeName,
                renderOverlayPreEvent, renderDistanceCaveMin, renderDistanceCaveMax, renderDistanceSurfaceMin,
                renderDistanceSurfaceMax, revealShape, themeName, gridSpecs);
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("announceMod", announceMod)
                .add("autoMapPoll", autoMapPoll)
                .add("browserPoll", browserPoll)
                .add("cacheAnimalsData", cacheAnimalsData)
                .add("cacheMobsData", cacheMobsData)
                .add("cachePlayerData", cachePlayerData)
                .add("cachePlayersData", cachePlayersData)
                .add("cacheVillagersData", cacheVillagersData)
                .add("caveIgnoreGlass", caveIgnoreGlass)
                .add("isUpdateCheckEnabled", checkUpdates)
                .add("renderDelay", renderDelay)
                .add("hideSneakingEntities", hideSneakingEntities)
                .add("logLevel", logLevel)
                .add("mapAntialiasing", mapAntialiasing)
                .add("mapBathymetry", mapBathymetry)
                .add("mapCaveLighting", mapCaveLighting)
                .add("mapCrops", mapCrops)
                .add("mapPlants", mapPlants)
                .add("mapPlantShadows", mapPlantShadows)
                .add("mapSurfaceAboveCaves", mapSurfaceAboveCaves)
                .add("tileHighDisplayQuality", tileHighDisplayQuality)
                .add("mapTransparency", mapTransparency)
                .add("maxAnimalsData", maxAnimalsData)
                .add("maxMobsData", maxMobsData)
                .add("maxPlayersData", maxPlayersData)
                .add("maxVillagersData", maxVillagersData)
                .add("optionsManagerViewed", optionsManagerViewed)
                .add("radarLateralDistance", radarLateralDistance)
                .add("radarVerticalDistance", radarVerticalDistance)
                .add("recordCacheStats", recordCacheStats)
                .add("renderOverlayEventTypeName", renderOverlayEventTypeName)
                .add("renderOverlayPreEvent", renderOverlayPreEvent)
                .add("renderDistanceCaveMin", renderDistanceCaveMin)
                .add("renderDistanceCaveMax", renderDistanceCaveMax)
                .add("renderDistanceSurfaceMin", renderDistanceSurfaceMin)
                .add("renderDistanceSurfaceMax", renderDistanceSurfaceMax)
                .add("revealShape", revealShape)
                .add("themeName", themeName)
                .add("tileRenderType", tileRenderType)
                .toString();
    }

    public static void main(String[] args)
    {
        boolean verbose = true;
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                        .registerTypeAdapter(BooleanField.class, new PropertiesSerializer.BooleanFieldSerializer(verbose))
                        .registerTypeAdapter(IntegerField.class, new PropertiesSerializer.IntegerFieldSerializer(verbose))
                        .registerTypeAdapter(StringField.class, new PropertiesSerializer.StringFieldSerializer(verbose))
                        .registerTypeAdapter(EnumField.class, new PropertiesSerializer.EnumFieldSerializer(verbose))
                .create();

        CoreProperties c1 = new CoreProperties();
        String json = gson.toJson(c1);
        System.out.println(json);

        CoreProperties c2 = gson.fromJson(json, CoreProperties.class);
        System.out.println("Equal? " + c1.equals(c2));
    }
}
