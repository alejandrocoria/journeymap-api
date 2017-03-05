/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
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
    /**
     * The constant PATTERN_COLOR.
     */
    public final static String PATTERN_COLOR = "^#[a-f0-9]{6}$";

    /**
     * The Log level.
     */
    public final StringField logLevel = new StringField(Advanced, "jm.advanced.loglevel", JMLogger.LogLevelStringProvider.class);
    /**
     * The Auto map poll.
     */
    public final IntegerField autoMapPoll = new IntegerField(Advanced, "jm.advanced.automappoll", 500, 10000, 2000);
    /**
     * The Cache animals data.
     */
    public final IntegerField cacheAnimalsData = new IntegerField(Advanced, "jm.advanced.cache_animals", 1000, 10000, 3100);
    /**
     * The Cache mobs data.
     */
    public final IntegerField cacheMobsData = new IntegerField(Advanced, "jm.advanced.cache_mobs", 1000, 10000, 3000);
    /**
     * The Cache player data.
     */
    public final IntegerField cachePlayerData = new IntegerField(Advanced, "jm.advanced.cache_player", 500, 2000, 1000);
    /**
     * The Cache players data.
     */
    public final IntegerField cachePlayersData = new IntegerField(Advanced, "jm.advanced.cache_players", 1000, 10000, 2000);
    /**
     * The Cache villagers data.
     */
    public final IntegerField cacheVillagersData = new IntegerField(Advanced, "jm.advanced.cache_villagers", 1000, 10000, 2200);
    /**
     * The Announce mod.
     */
    public final BooleanField announceMod = new BooleanField(Advanced, "jm.advanced.announcemod", true);
    /**
     * The Check updates.
     */
    public final BooleanField checkUpdates = new BooleanField(Advanced, "jm.advanced.checkupdates", true);
    /**
     * The Record cache stats.
     */
    public final BooleanField recordCacheStats = new BooleanField(Advanced, "jm.advanced.recordcachestats", false);
    /**
     * The Browser poll.
     */
    public final IntegerField browserPoll = new IntegerField(Advanced, "jm.advanced.browserpoll", 1000, 10000, 2000);
    /**
     * The Theme name.
     */
    public final StringField themeName = new StringField(FullMap, "jm.common.ui_theme", ThemeFileHandler.ThemeValuesProvider.class);
    /**
     * The Cave ignore glass.
     */
    public final BooleanField caveIgnoreGlass = new BooleanField(Cartography, "jm.common.map_style_caveignoreglass", true);
    /**
     * The Map bathymetry.
     */
    public final BooleanField mapBathymetry = new BooleanField(Cartography, "jm.common.map_style_bathymetry", false);
    /**
     * The Map topography.
     */
    public final BooleanField mapTopography = new BooleanField(Cartography, "jm.common.map_style_topography", true);
    /**
     * The Map transparency.
     */
    public final BooleanField mapTransparency = new BooleanField(Cartography, "jm.common.map_style_transparency", true);
    /**
     * The Map cave lighting.
     */
    public final BooleanField mapCaveLighting = new BooleanField(Cartography, "jm.common.map_style_cavelighting", true);
    /**
     * The Map antialiasing.
     */
    public final BooleanField mapAntialiasing = new BooleanField(Cartography, "jm.common.map_style_antialiasing", true);
    /**
     * The Map plant shadows.
     */
    public final BooleanField mapPlantShadows = new BooleanField(Cartography, "jm.common.map_style_plantshadows", false);
    /**
     * The Map plants.
     */
    public final BooleanField mapPlants = new BooleanField(Cartography, "jm.common.map_style_plants", false);
    /**
     * The Map crops.
     */
    public final BooleanField mapCrops = new BooleanField(Cartography, "jm.common.map_style_crops", true);
    /**
     * The Map surface above caves.
     */
    public final BooleanField mapSurfaceAboveCaves = new BooleanField(Cartography, "jm.common.map_style_caveshowsurface", true);
    /**
     * The Render distance cave max.
     */
//public final IntegerField renderDistanceCaveMin = new IntegerField(Cartography, "jm.common.renderdistance_cave_min", 1, 32, 3, 101);
    public final IntegerField renderDistanceCaveMax = new IntegerField(Cartography, "jm.common.renderdistance_cave_max", 1, 32, 3, 102);
    /**
     * The Render distance surface max.
     */
//public final IntegerField renderDistanceSurfaceMin = new IntegerField(Cartography, "jm.common.renderdistance_surface_min", 1, 32, 4, 103);
    public final IntegerField renderDistanceSurfaceMax = new IntegerField(Cartography, "jm.common.renderdistance_surface_max", 1, 32, 7, 104);
    /**
     * The Render delay.
     */
    public final IntegerField renderDelay = new IntegerField(Cartography, "jm.common.renderdelay", 0, 10, 2);
    /**
     * The Reveal shape.
     */
    public final EnumField<RenderSpec.RevealShape> revealShape = new EnumField<RenderSpec.RevealShape>(Cartography, "jm.common.revealshape", RenderSpec.RevealShape.Circle);
    /**
     * The Always map caves.
     */
    public final BooleanField alwaysMapCaves = new BooleanField(Cartography, "jm.common.alwaysmapcaves", false);
    /**
     * The Always map surface.
     */
    public final BooleanField alwaysMapSurface = new BooleanField(Cartography, "jm.common.alwaysmapsurface", false);
    /**
     * The Tile high display quality.
     */
    public final BooleanField tileHighDisplayQuality = new BooleanField(Cartography, "jm.common.tile_display_quality", true);
    /**
     * The Max animals data.
     */
    public final IntegerField maxAnimalsData = new IntegerField(Advanced, "jm.common.radar_max_animals", 1, 128, 32);
    /**
     * The Max mobs data.
     */
    public final IntegerField maxMobsData = new IntegerField(Advanced, "jm.common.radar_max_mobs", 1, 128, 32);
    /**
     * The Max players data.
     */
    public final IntegerField maxPlayersData = new IntegerField(Advanced, "jm.common.radar_max_players", 1, 128, 32);
    /**
     * The Max villagers data.
     */
    public final IntegerField maxVillagersData = new IntegerField(Advanced, "jm.common.radar_max_villagers", 1, 128, 32);
    /**
     * The Hide sneaking entities.
     */
    public final BooleanField hideSneakingEntities = new BooleanField(Advanced, "jm.common.radar_hide_sneaking", true);
    /**
     * The Radar lateral distance.
     */
    public final IntegerField radarLateralDistance = new IntegerField(Advanced, "jm.common.radar_lateral_distance", 16, 512, 64);
    /**
     * The Radar vertical distance.
     */
    public final IntegerField radarVerticalDistance = new IntegerField(Advanced, "jm.common.radar_vertical_distance", 8, 256, 16);
    /**
     * The Tile render type.
     */
    public final IntegerField tileRenderType = new IntegerField(Advanced, "jm.advanced.tile_render_type", 1, 4, 1);

    /**
     * The Mapping enabled.
     */
// Hidden (not shown in Options Manager
    public final BooleanField mappingEnabled = new BooleanField(Category.Hidden, "", true);
    /**
     * The Render overlay event type name.
     */
    public final EnumField<RenderGameOverlayEvent.ElementType> renderOverlayEventTypeName = new EnumField<RenderGameOverlayEvent.ElementType>(Category.Hidden, "", RenderGameOverlayEvent.ElementType.ALL);
    /**
     * The Render overlay pre event.
     */
    public final BooleanField renderOverlayPreEvent = new BooleanField(Category.Hidden, "", true);
    /**
     * The Options manager viewed.
     */
    public final StringField optionsManagerViewed = new StringField(Category.Hidden, "", null);
    /**
     * The Splash viewed.
     */
    public final StringField splashViewed = new StringField(Category.Hidden, "", null);
    /**
     * The Grid specs.
     */
    public final GridSpecs gridSpecs = new GridSpecs();
    /**
     * The Color passive.
     */
    public final StringField colorPassive = new StringField(Category.Hidden, "jm.common.radar_color_passive", null, "#bbbbbb").pattern(PATTERN_COLOR);
    /**
     * The Color hostile.
     */
    public final StringField colorHostile = new StringField(Category.Hidden, "jm.common.radar_color_hostile", null, "#ff0000").pattern(PATTERN_COLOR);
    /**
     * The Color pet.
     */
    public final StringField colorPet = new StringField(Category.Hidden, "jm.common.radar_color_pet", null, "#0077ff").pattern(PATTERN_COLOR);
    /**
     * The Color villager.
     */
    public final StringField colorVillager = new StringField(Category.Hidden, "jm.common.radar_color_villager", null, "#88e188").pattern(PATTERN_COLOR);
    /**
     * The Color player.
     */
    public final StringField colorPlayer = new StringField(Category.Hidden, "jm.common.radar_color_player", null, "#ffffff").pattern(PATTERN_COLOR);
    /**
     * The Color self.
     */
    public final StringField colorSelf = new StringField(Category.Hidden, "jm.common.radar_color_self", null, "#0000ff").pattern(PATTERN_COLOR);

    private transient HashMap<StringField, Integer> mobColors = new HashMap<>(6);

    /**
     * Instantiates a new Core properties.
     */
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

    /**
     * Gets color.
     *
     * @param colorField the color field
     * @return the color
     */
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
