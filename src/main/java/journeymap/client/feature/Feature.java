/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.feature;

import java.util.EnumSet;

public enum Feature
{
    RadarPlayers,
    RadarAnimals,
    RadarMobs,
    RadarVillagers,
    MapCaves;

    public static EnumSet<Feature> radar()
    {
        return EnumSet.of(RadarPlayers, RadarAnimals, RadarMobs, RadarVillagers);
    }

    public static EnumSet<Feature> all()
    {
        return EnumSet.allOf(Feature.class);
    }
}
