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
