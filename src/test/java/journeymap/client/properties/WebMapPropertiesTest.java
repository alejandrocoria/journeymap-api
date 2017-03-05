/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.properties;

/**
 * WebMapProperties tests.
 */
public class WebMapPropertiesTest extends ClientPropertiesBaseTest<WebMapProperties>
{
    @Override
    protected WebMapProperties createDefaultInstance()
    {
        return new WebMapProperties();
    }

    @Override
    protected WebMapProperties createRandomizedInstance()
    {
        WebMapProperties p = new WebMapProperties();
        randomizeMapProperties(p);

        randomize(p.enabled);
        randomize(p.port);
        randomize(p.googleMapApiDomain);

        return p;
    }
}
