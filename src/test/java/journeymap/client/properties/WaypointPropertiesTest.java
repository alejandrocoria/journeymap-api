/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.properties;

import journeymap.common.properties.PropertiesBaseTest;

/**
 * WaypointProperties tests.
 */
public class WaypointPropertiesTest extends PropertiesBaseTest<WaypointProperties>
{
    @Override
    protected WaypointProperties createDefaultInstance()
    {
        return new WaypointProperties();
    }

    @Override
    protected WaypointProperties createRandomizedInstance()
    {
        WaypointProperties p = new WaypointProperties();
        randomize(p.managerEnabled);
        randomize(p.beaconEnabled);
        randomize(p.showTexture);
        randomize(p.showStaticBeam);
        randomize(p.showRotatingBeam);
        randomize(p.showName);
        randomize(p.showDistance);
        randomize(p.autoHideLabel);
        randomize(p.boldLabel);
        randomize(p.fontScale);
        randomize(p.textureSmall);
        randomize(p.maxDistance);
        randomize(p.createDeathpoints);
        return p;
    }
}
