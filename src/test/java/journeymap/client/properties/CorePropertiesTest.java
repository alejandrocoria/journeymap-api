package journeymap.client.properties;

import journeymap.common.properties.PropertiesBaseTest;

import java.util.UUID;

/**
 * CoreProperties tests
 */
public class CorePropertiesTest extends PropertiesBaseTest<CoreProperties>
{
    @Override
    protected CoreProperties createDefaultInstance()
    {
        return new CoreProperties();
    }

    @Override
    protected CoreProperties createRandomizedInstance()
    {
        CoreProperties p = new CoreProperties();
        randomize(p.logLevel);
        randomize(p.autoMapPoll);
        randomize(p.cacheAnimalsData);
        randomize(p.cacheMobsData);
        randomize(p.cachePlayerData);
        randomize(p.cachePlayersData);
        randomize(p.cacheVillagersData);
        randomize(p.announceMod);
        randomize(p.checkUpdates);
        randomize(p.recordCacheStats);
        randomize(p.browserPoll);
        randomize(p.themeName);
        randomize(p.caveIgnoreGlass);
        randomize(p.mapBathymetry);
        randomize(p.mapTransparency);
        randomize(p.mapCaveLighting);
        randomize(p.mapAntialiasing);
        randomize(p.mapPlantShadows);
        randomize(p.mapPlants);
        randomize(p.mapCrops);
        randomize(p.mapSurfaceAboveCaves);
        //randomize(p.renderDistanceCaveMin);
        randomize(p.renderDistanceCaveMax);
        //randomize(p.renderDistanceSurfaceMin);
        randomize(p.renderDistanceSurfaceMax);
        randomize(p.renderDelay);
        randomize(p.revealShape);
        randomize(p.alwaysMapCaves);
        randomize(p.alwaysMapSurface);
        randomize(p.tileHighDisplayQuality);
        randomize(p.maxAnimalsData);
        randomize(p.maxMobsData);
        randomize(p.maxPlayersData);
        randomize(p.maxVillagersData);
        randomize(p.hideSneakingEntities);
        randomize(p.radarLateralDistance);
        randomize(p.radarVerticalDistance);
        randomize(p.tileRenderType);
        randomize(p.mappingEnabled);
        randomize(p.renderOverlayEventTypeName);
        randomize(p.renderOverlayPreEvent);
        p.optionsManagerViewed.set(UUID.randomUUID().toString());
        p.splashViewed.set(UUID.randomUUID().toString());
        return p;
    }


}
