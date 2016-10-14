package journeymap.client.properties;

import journeymap.common.properties.PropertiesBase;
import journeymap.common.properties.PropertiesBaseTest;

/**
 * Base class for MapProperties tests.
 */
public abstract class ClientPropertiesBaseTest<P extends PropertiesBase> extends PropertiesBaseTest<P>
{
    protected void randomizeMapProperties(MapProperties p)
    {
        randomize(p.showMobs);
        randomize(p.showAnimals);
        randomize(p.showVillagers);
        randomize(p.showPets);
        randomize(p.showPlayers);
        randomize(p.showWaypoints);
        randomize(p.showSelf);
        randomize(p.showGrid);
        randomize(p.preferredMapType);
        randomize(p.zoomLevel);
    }

    protected void randomizeInGameMapProperties(InGameMapProperties p)
    {
        randomizeMapProperties(p);
        randomize(p.showCaves);
        randomize(p.fontScale);
        randomize(p.mobDisplay);
        randomize(p.playerDisplay);
        randomize(p.showWaypointLabels);
        randomize(p.locationFormatVerbose);
        randomize(p.locationFormat);
        randomize(p.showMobHeading);
        randomize(p.showPlayerHeading);
    }
}
