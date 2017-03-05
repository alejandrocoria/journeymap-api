/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.properties;

/**
 * FullMapProperties tests.
 */
public class FullMapPropertiesTest extends ClientPropertiesBaseTest<FullMapProperties>
{
    @Override
    protected FullMapProperties createDefaultInstance()
    {
        return new FullMapProperties();
    }

    @Override
    protected FullMapProperties createRandomizedInstance()
    {
        FullMapProperties p = new FullMapProperties();
        randomizeInGameMapProperties(p);
        return p;
    }
}
