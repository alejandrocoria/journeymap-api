/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.server.properties;

import journeymap.common.properties.PropertiesBaseTest;

import java.util.UUID;

/**
 * GlobalProperties tests.
 */
public class GlobalPropertiesTest extends PropertiesBaseTest<GlobalProperties>
{
    // Reuse the same worldId per testcase
    String worldId = UUID.randomUUID().toString();

    @Override
    protected GlobalProperties createDefaultInstance()
    {
        GlobalProperties p = new GlobalProperties();
        return p;
    }

    @Override
    protected GlobalProperties createRandomizedInstance()
    {
        GlobalProperties p = new GlobalProperties();

//        p.useWorldID.set(rand.nextBoolean());
//        p.worldID.set(worldId);
//
//        // Don't randomize; setting to true requires a running server
//        p.saveInWorldFolder.set(false);
//
//        p.opCaveMapping.set(rand.nextBoolean());
//        p.playerCaveMapping.set(rand.nextBoolean());
//        p.whiteListCaveMapping.set(String.format("user%s, user%s", rand.nextInt(1000), rand.nextInt(1000)));
//        p.opRadar.set(rand.nextBoolean());
//        p.playerRadar.set(rand.nextBoolean());
//        p.whiteListRadar.set(String.format("user%s, user%s", rand.nextInt(1000), rand.nextInt(1000)));

        return p;
    }
}
