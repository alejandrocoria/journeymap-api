/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */
package journeymap.client.properties;

import journeymap.client.cartography.RGB;
import journeymap.client.io.ThemeFileHandler;
import journeymap.client.log.JMLogger;
import journeymap.client.model.GridSpecs;
import journeymap.client.task.multi.RenderSpec;
import journeymap.common.properties.Category;
import journeymap.common.properties.PropertiesBase;
import journeymap.common.properties.config.BooleanField;
import journeymap.common.properties.config.EnumField;
import journeymap.common.properties.config.IntegerField;
import journeymap.common.properties.config.StringField;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.util.Arrays;
import java.util.HashMap;

import static journeymap.client.properties.ClientCategory.*;

/**
 * Properties for basic mod configuration.
 */
public class CoreProperties extends ClientPropertiesBase implements Comparable<CoreProperties>
{
    public final static String PATTERN_COLOR = "^#[a-f0-9]{6}$";

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
    public final BooleanField mapTopography = new BooleanField(Cartography, "jm.common.map_style_topography", true);
    public final BooleanField mapTransparency = new BooleanField(Cartography, "jm.common.map_style_transparency", true);
    public final BooleanField mapCaveLighting = new BooleanField(Cartography, "jm.common.map_style_cavelighting", true);
    public final BooleanField mapAntialiasing = new BooleanField(Cartography, "jm.common.map_style_antialiasing", true);
    public final BooleanField mapPlantShadows = new BooleanField(Cartography, "jm.common.map_style_plantshadows", false);
    public final BooleanField mapPlants = new BooleanField(Cartography, "jm.common.map_style_plants", false);
    public final BooleanField mapCrops = new BooleanField(Cartography, "jm.common.map_style_crops", true);
    public final BooleanField mapSurfaceAboveCaves = new BooleanField(Cartography, "jm.common.map_style_caveshowsurface", true);
    //public final IntegerField renderDistanceCaveMin = new IntegerField(Cartography, "jm.common.renderdistance_cave_min", 1, 32, 3, 101);
    public final IntegerField renderDistanceCaveMax = new IntegerField(Cartography, "jm.common.renderdistance_cave_max", 1, 32, 3, 102);
    //public final IntegerField renderDistanceSurfaceMin = new IntegerField(Cartography, "jm.common.renderdistance_surface_min", 1, 32, 4, 103);
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
    public final BooleanField mappingEnabled = new BooleanField(Category.Hidden, "", true);
    public final EnumField<RenderGameOverlayEvent.ElementType> renderOverlayEventTypeName = new EnumField<RenderGameOverlayEvent.ElementType>(Category.Hidden, "", RenderGameOverlayEvent.ElementType.ALL);
    public final BooleanField renderOverlayPreEvent = new BooleanField(Category.Hidden, "", true);
    public final StringField optionsManagerViewed = new StringField(Category.Hidden, "", null);
    public final StringField splashViewed = new StringField(Category.Hidden, "", null);
    public final GridSpecs gridSpecs = new GridSpecs();
    public final StringField colorPassive = new StringField(Category.Hidden, "jm.common.radar_color_passive", null, "#bbbbbb").pattern(PATTERN_COLOR);
    public final StringField colorHostile = new StringField(Category.Hidden, "jm.common.radar_color_hostile", null, "#ff0000").pattern(PATTERN_COLOR);
    public final StringField colorPet = new StringField(Category.Hidden, "jm.common.radar_color_pet", null, "#0077ff").pattern(PATTERN_COLOR);
    public final StringField colorVillager = new StringField(Category.Hidden, "jm.common.radar_color_villager", null, "#88e188").pattern(PATTERN_COLOR);
    public final StringField colorPlayer = new StringField(Category.Hidden, "jm.common.radar_color_player", null, "#ffffff").pattern(PATTERN_COLOR);
    public final StringField colorSelf = new StringField(Category.Hidden, "jm.common.radar_color_self", null, "#0000ff").pattern(PATTERN_COLOR);

    private transient HashMap<StringField, Integer> mobColors = new HashMap<>(6);

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

    @Override
    public <T extends PropertiesBase> void updateFrom(T otherInstance)
    {
        super.updateFrom(otherInstance);
        if (otherInstance instanceof CoreProperties)
        {
            this.gridSpecs.updateFrom(((CoreProperties) otherInstance).gridSpecs);
        }
        mobColors.clear();
    }

    @Override
    public boolean isValid(boolean fix)
    {
        boolean valid = super.isValid(fix);

        if (FMLClientHandler.instance().getClient() != null)
        {
            int gameRenderDistance = FMLClientHandler.instance().getClient().gameSettings.renderDistanceChunks;
            //for (IntegerField prop : Arrays.asList(renderDistanceCaveMin, renderDistanceCaveMax, renderDistanceSurfaceMin, renderDistanceSurfaceMax))
            for (IntegerField prop : Arrays.asList(renderDistanceCaveMax, renderDistanceSurfaceMax))
            {
                if (prop.get() > gameRenderDistance)
                {
                    warn(String.format("Render distance %s is less than %s", gameRenderDistance, prop.getDeclaredField()));
                    if (fix)
                    {
                        prop.set(gameRenderDistance);
                    }
                    else
                    {
                        valid = false;
                    }
                }
            }
        }
        return valid;
    }

//    public boolean hasValidCaveRenderDistances()
//    {
//        return renderDistanceCaveMax.get() >= renderDistanceCaveMin.get();
//    }
//
//    public boolean hasValidSurfaceRenderDistances()
//    {
//        return renderDistanceSurfaceMax.get() >= renderDistanceSurfaceMin.get();
//    }

    public int getColor(StringField colorField)
    {
        Integer color = mobColors.get(colorField);
        if (color == null)
        {
            color = RGB.hexToInt(colorField.get());
            mobColors.put(colorField, color);
        }
        return color;
    }
}
