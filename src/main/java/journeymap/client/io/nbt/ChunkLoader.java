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
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class ChunkLoader
{

    private static Logger logger = Journeymap.getLogger();

    public static File getWorldSaveDir(Minecraft mc)
    {
        if (mc.isSingleplayer())
        {
            try
            {
                File savesDir = new File(mc.mcDataDir, "saves");
                File worldSaveDir = new File(savesDir, mc.getIntegratedServer().getFolderName());
                if (mc.theWorld.provider.getSaveFolder() != null)
                {
                    File dir = new File(worldSaveDir, mc.theWorld.provider.getSaveFolder());
                    dir.mkdirs();
                    return dir;
                }
                else
                {
                    return worldSaveDir;
                }
            }
            catch (Throwable t)
            {
                logger.error("Error getting world save dir: %s", t);
            }
        }
        return null;
    }

    public static File getRegionFile(Minecraft minecraft, int chunkX, int chunkZ)
    {
        File regionDir = new File(getWorldSaveDir(minecraft), "region");
        File regionFile = new File(regionDir, String.format("r.%s.%s.mca", (chunkX >> 5), (chunkZ >> 5)));
        return regionFile;
    }

    public static ChunkMD getChunkMD(AnvilChunkLoader loader, Minecraft mc, ChunkCoordIntPair coord, boolean forceRetain)
    {
        try
        {
            // Check for the region file on disk first so the loader doesn't create empty region files
            if (getRegionFile(mc, coord.chunkXPos, coord.chunkZPos).exists())
            {
                if (loader.chunkExists(mc.theWorld, coord.chunkXPos, coord.chunkZPos))
                {
                    Chunk chunk = loader.loadChunk(mc.theWorld, coord.chunkXPos, coord.chunkZPos);
//                    if (chunk!=null && !(chunk instanceof EmptyChunk))

                    // 1.8 TODO:  Can this be safely left commented out?
                    //chunk.generateHeightMap();
                    chunk.generateSkylightMap();
                    return new ChunkMD(chunk, forceRetain);
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
