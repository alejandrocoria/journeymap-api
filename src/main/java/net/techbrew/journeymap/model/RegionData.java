/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.model;

import net.minecraft.world.ChunkCoordIntPair;

import java.util.HashMap;

/**
 * Created by mwoodman on 6/4/2014.
 */
public class RegionData
{
    RegionCoord region;

    HashMap<ChunkCoordIntPair, ChunkData> chunks;

    static class ChunkData
    {
        ChunkCoordIntPair chunk;
        int[] heightMap;
        byte[] blockBiomeArray;
    }
}
