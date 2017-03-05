/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.feature;

import java.util.EnumSet;

/**
 * The enum Feature.
 */
public enum Feature
{
    /**
     * Radar players feature.
     */
    RadarPlayers,
    /**
     * Radar animals feature.
     */
    RadarAnimals,
    /**
     * Radar mobs feature.
     */
    RadarMobs,
    /**
     * Radar villagers feature.
     */
    RadarVillagers,
    /**
     * Map caves feature.
     */
    MapCaves;

    /**
     * Radar enum set.
     *
     * @return the enum set
     */
    public static EnumSet<Feature> radar()
    {
        return EnumSet.of(RadarPlayers, RadarAnimals, RadarMobs, RadarVillagers);
    }

    /**
     * All enum set.
     *
     * @return the enum set
     */
    public static EnumSet<Feature> all()
    {
        return EnumSet.allOf(Feature.class);
    }
}
