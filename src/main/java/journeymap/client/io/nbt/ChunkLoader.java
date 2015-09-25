/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
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
import net.minecraft.world.chunk.storage.RegionFile;
import net.minecraft.world.chunk.storage.RegionFileCache;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class ChunkLoader
{

    private static Logger logger = Journeymap.getLogger();

    public static AnvilChunkLoader getAnvilChunkLoader(Minecraft mc)
    {
        if (mc.isSingleplayer())
        {
            try
            {
                File savesDir = new File(mc.mcDataDir, "saves");
                File worldSaveDir = new File(savesDir, mc.getIntegratedServer().getFolderName());
                File file2;
                if (mc.theWorld.provider.getSaveFolder() != null)
                {
                    file2 = new File(worldSaveDir, mc.theWorld.provider.getSaveFolder());
                    file2.mkdirs();
                    return new AnvilChunkLoader(file2);
                }
                else
                {
                    return new AnvilChunkLoader(worldSaveDir);
                }
            }
            catch (Throwable t)
            {
                logger.error("Couldn't get chunk loader: %s", t);
            }
        }
        return null;
    }

    public static ChunkMD getChunkMD(AnvilChunkLoader loader, Minecraft mc, ChunkCoordIntPair coord, boolean forceRetain)
    {
        try
        {
            // Check for the region file on disk first so the loader doesn't create empty region files
            File regionDir = new File(loader.chunkSaveLocation, "region");
            File regionFile = new File(regionDir, "r." + (coord.chunkXPos >> 5) + "." + (coord.chunkZPos >> 5) + ".mca");
            if (regionFile.exists())
            {
                RegionFile rf = RegionFileCache.createOrLoadRegionFile(loader.chunkSaveLocation, (coord.chunkXPos >> 5), (coord.chunkZPos >> 5));
                if (rf.chunkExists(coord.chunkXPos, coord.chunkZPos))
                {
                    Chunk chunk = loader.loadChunk(mc.theWorld, coord.chunkXPos, coord.chunkZPos);
//                    if (chunk!=null && !(chunk instanceof EmptyChunk))

                    // 1.8 TODO:  Can this be safely left commented out?
                    //chunk.generateHeightMap();
                    chunk.generateSkylightMap();
                    return new ChunkMD(chunk, forceRetain);
                }
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
