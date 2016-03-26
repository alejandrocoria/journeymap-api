/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */
package journeymap.client.properties;

import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.io.ThemeFileHandler;
import journeymap.client.log.JMLogger;
import journeymap.client.model.GridSpecs;
import journeymap.client.task.multi.RenderSpec;
import journeymap.common.properties.config.BooleanField;
import journeymap.common.properties.config.EnumField;
import journeymap.common.properties.config.IntegerField;
import journeymap.common.properties.config.StringField;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import java.util.Arrays;

import static journeymap.client.properties.ClientCategory.*;

/**
 * Properties for basic mod configuration.
 */
public class CoreProperties extends ClientPropertiesBase implements Comparable<CoreProperties>
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

    public static void main(String[] args)
    {
        CoreProperties c1 = new CoreProperties();
        c1.getConfigFields();
        String json = c1.toJsonString(false);
        System.out.println(json);

//        CoreProperties c2 = gson.fromJson(json, CoreProperties.class);
//        System.out.println("Equal? " + c1.equals(c2));
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

    @Override
    protected boolean validate()
    {
        boolean valid = super.validate();
        if (renderDistanceCaveMax.get() < renderDistanceCaveMin.get())
        {
            renderDistanceCaveMax.set(renderDistanceCaveMin.get());
            valid = false;
        }
        if (renderDistanceSurfaceMax.get() < renderDistanceSurfaceMin.get())
        {
            renderDistanceSurfaceMax.set(renderDistanceSurfaceMin.get());
            valid = false;
        }
        int gameRenderDistance = ForgeHelper.INSTANCE.getClient().gameSettings.renderDistanceChunks;
        for (IntegerField prop : Arrays.asList(renderDistanceCaveMin, renderDistanceCaveMax, renderDistanceSurfaceMin, renderDistanceSurfaceMax))
        {
            if (prop.get() > gameRenderDistance)
            {
                prop.set(gameRenderDistance);
                valid = false;
            }
        }
        return valid;
    }

    public boolean hasValidCaveRenderDistances()
    {
        return renderDistanceCaveMax.get() >= renderDistanceCaveMin.get();
    }

    public boolean hasValidSurfaceRenderDistances()
    {
        return renderDistanceSurfaceMax.get() >= renderDistanceSurfaceMin.get();
    }
}
