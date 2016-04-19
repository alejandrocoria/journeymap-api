/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.io.nbt;


import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.model.ChunkMD;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class ChunkLoader
{
    private static Logger logger = Journeymap.getLogger();

    /**
     * Gets the chunk from the region file on disk.  Only works in SinglePlayer, and assumes the current dimension
     * is the intended dimension.
     */
    public static ChunkMD getChunkMD(AnvilChunkLoader loader, Minecraft mc, ChunkCoordIntPair coord, boolean forceRetain)
    {
        try
        {
            // Check for the region file on disk first so the loader doesn't create empty region files
            if (RegionLoader.getRegionFile(mc, coord.chunkXPos, coord.chunkZPos).exists())
            {
                if (loader.chunkExists(mc.theWorld, coord.chunkXPos, coord.chunkZPos))
                {
                    Chunk chunk = loader.loadChunk(mc.theWorld, coord.chunkXPos, coord.chunkZPos);
                    if (chunk != null)
                    {
                        if (!chunk.isLoaded())
                        {
                            chunk.setChunkLoaded(true);
                            //chunk.generateSkylightMap();
                        }
                        return new ChunkMD(chunk, forceRetain);
                    }
                    else
                    {
                        logger.warn("AnvilChunkLoader returned null for chunk: " + coord);
                    }
                }
                else
                {
                    //logger.warn("AnvilChunkLoader didn't find data for for chunk: " + coord);
                }
            }
            else
            {
                logger.warn("Region doesn't exist for chunk: " + coord);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static ChunkMD getChunkMdFromMemory(World world, int chunkX, int chunkZ)
    {
        if (world.getChunkProvider().chunkExists(chunkX, chunkZ))
        {
            Chunk theChunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
            if (ForgeHelper.INSTANCE.hasChunkData(theChunk))
            {
                return new ChunkMD(theChunk);
            }
        }
        return null;
    }
}
