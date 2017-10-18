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
     * Whether players can be seen on radar.
     */
    RadarPlayers,
    /**
     * Whether animals can be seen on radar.
     */
    RadarAnimals,
    /**
     * Whether mobs can be seen on radar.
     */
    RadarMobs,
    /**
     * Whether villagers can be seen on radar.
     */
    RadarVillagers,
    /**
     * Whether topo mapping is allowed.
     */
    MapTopo,
    /**
     * Whether cave mapping is allowed.
     */
    MapSurface,
    /**
     * Whether cave mapping is allowed.
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
