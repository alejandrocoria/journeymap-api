/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package net.techbrew.journeymap.io.nbt;


import net.minecraft.client.Minecraft;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.forge.helper.ForgeHelper;
import net.techbrew.journeymap.model.ChunkMD;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class ChunkLoader
{

    private static Logger logger = JourneyMap.getLogger();

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

    public static ChunkMD getChunkMD(AnvilChunkLoader loader, Minecraft mc, ChunkCoordIntPair coord)
    {
        ChunkMD chunkMD = null;
        try
        {
            Chunk chunk = loader.loadChunk(mc.theWorld, coord.chunkXPos, coord.chunkZPos);
            if (chunk != null)
            {
                // 1.8 TODO:  Can this be safely left commented out?
                // chunk.generateHeightMap();
                chunk.generateSkylightMap();
                chunkMD = new ChunkMD(chunk);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return chunkMD;
    }

//    public static ChunkMD getChunkMdFromDisk(File anvilFile, int chunkX, int chunkZ, File worldDir, World world)
//    {
//
//        Chunk chunk = getChunkFromDisk(anvilFile, chunkX, chunkZ, worldDir, world);
//        if (chunk == null)
//        {
//            return null;
//        }
//        return new ChunkMD(chunk);
//
//    }

    public static ChunkMD getChunkMdFromMemory(World world, int chunkX, int chunkZ)
    {
        Chunk chunk = getChunkFromMemory(world, chunkX, chunkZ);
        if (chunk != null && chunk.isEmpty())
        {
            return null;
        }
        if (chunk != null && !chunk.isEmpty())
        {
            return new ChunkMD(chunk);
        }
        return null;
    }

//    public static ChunkMD refreshChunkMdFromMemory(ChunkMD chunkMD)
//    {
//        if (chunkMD == null)
//        {
//            return null;
//        }
//
//        Chunk chunk = getChunkFromMemory(chunkMD.coord.chunkXPos, chunkMD.coord.chunkZPos, chunkMD.worldObj);
//        if (chunk != null)
//        {
//            chunkMD.stub.updateFrom(chunk);
//            chunkMD.sliceSlopes.clear();
//            return chunkMD;
//        }
//        else
//        {
//            return null;
//        }
//    }

    private static Chunk getChunkFromMemory(World world, int chunkX, int chunkZ)
    {
        Chunk result = null;
        if (world.getChunkProvider().chunkExists(chunkX, chunkZ))
        {
            Chunk theChunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
            if (!(theChunk instanceof EmptyChunk))
            {
                if (ForgeHelper.INSTANCE.hasChunkData(theChunk))
                {
                    result = theChunk;
                }
            }
        }
        return result;
    }

//    private static Chunk getChunkFromDisk(File anvilFile, int chunkX, int chunkZ, File worldDir, World world)
//    {
//        Chunk chunk = null;
//        try
//        {
//            //AnvilChunkLoader acl = new AnvilChunkLoader(anvilFile);
//            chunk = acl.loadChunk(world, chunkX, chunkZ);
//            if(chunk!=null)
//            {
//                logger.info(String.format("Chunk found via AnvilChunkLoader: %s,%s", chunkX, chunkZ));
//                chunk.generateHeightMap();
//                chunk.generateSkylightMap();
//                return chunk;
//            }
//        }
//        catch (Throwable t)
//        {
//            logger.info(String.format("Could not use chunkProviderServer: %s,%s", chunkX, chunkZ));
//        }
//
//        try
//        {
//            DataInputStream dis = RegionFileCache.getChunkInputStream(worldDir, chunkX, chunkZ);
//            if (dis != null)
//            {
//                NBTTagCompound chunkNBT = CompressedStreamTools.read(dis);
//                if (chunkNBT != null)
//                {
//                    chunk = checkedReadChunkFromNBT(world, chunkX, chunkZ, chunkNBT);
//                    if(chunk != null)
//                    {
//                        chunk.generateHeightMap();
//                        chunk.generateSkylightMap();
//                        return chunk;
//                    }
//                }
//            }
//        }
//        catch (Throwable t)
//        {
//            logger.error(String.format("Error getting chunk from RegionFile: %s,%s : %s", chunkX, t));
//        }
//
//        return chunk;
//    }
//
//    /**
//     * Wraps readChunkFromNBT. Checks the coordinates and several NBT tags.
//     */
//    protected static Chunk checkedReadChunkFromNBT(World world, int chunkX, int chunkZ, NBTTagCompound par4NBTTagCompound)
//    {
//        final int par2 = chunkX;
//        final int par3 = chunkZ;
//
//        if (!par4NBTTagCompound.hasKey("Level"))
//        {
//            logger.error("Chunk file at " + par2 + "," + par3 + " is missing level data, skipping");
//            return null;
//        }
//        else if (!par4NBTTagCompound.getCompoundTag("Level").hasKey("Sections"))
//        {
//            logger.error("Chunk file at " + par2 + "," + par3 + " is missing block data, skipping");
//            return null;
//        }
//        else
//        {
//            Chunk var5 = readChunkFromNBT(world, par4NBTTagCompound.getCompoundTag("Level"));
//
//            if (!var5.isAtLocation(par2, par3))
//            {
//                logger.error("Chunk file at " + par2 + "," + par3 + " is in the wrong location; relocating. (Expected " + par2 + ", " + par3 + ", got " + var5.xPosition + ", " + var5.zPosition + ")");
//                par4NBTTagCompound.setInteger("xPos", par2);
//                par4NBTTagCompound.setInteger("zPos", par3);
//                var5 = readChunkFromNBT(world, par4NBTTagCompound.getCompoundTag("Level"));
//            }
//
//            return var5;
//        }
//    }
//
//    /**
//     * Reads the data stored in the passed NBTTagCompound and creates a Chunk with that data in the passed World.
//     * Returns the created Chunk.
//     */
//    private static Chunk readChunkFromNBT(World par1World, NBTTagCompound par2NBTTagCompound)
//    {
//        int i = par2NBTTagCompound.getInteger("xPos");
//        int j = par2NBTTagCompound.getInteger("zPos");
//        Chunk chunk = new Chunk(par1World, i, j);
//        chunk.heightMap = par2NBTTagCompound.getIntArray("HeightMap");
//        chunk.isTerrainPopulated = par2NBTTagCompound.getBoolean("TerrainPopulated");
//        chunk.isLightPopulated = par2NBTTagCompound.getBoolean("LightPopulated");
//        chunk.inhabitedTime = par2NBTTagCompound.getLong("InhabitedTime");
//        NBTTagList nbttaglist = par2NBTTagCompound.getTagList("Sections", 10);
//        byte b0 = 16;
//        ExtendedBlockStorage[] aextendedblockstorage = new ExtendedBlockStorage[b0];
//        boolean flag = !par1World.provider.hasNoSky;
//
//        for (int k = 0; k < nbttaglist.tagCount(); ++k)
//        {
//            NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(k);
//            byte b1 = nbttagcompound1.getByte("Y");
//            ExtendedBlockStorage extendedblockstorage = new ExtendedBlockStorage(b1 << 4, flag);
//            extendedblockstorage.setBlockLSBArray(nbttagcompound1.getByteArray("Blocks"));
//
//            if (nbttagcompound1.hasKey("Add", 7))
//            {
//                extendedblockstorage.setBlockMSBArray(new NibbleArray(nbttagcompound1.getByteArray("Add"), 4));
//            }
//
//            extendedblockstorage.setBlockMetadataArray(new NibbleArray(nbttagcompound1.getByteArray("Data"), 4));
//            extendedblockstorage.setBlocklightArray(new NibbleArray(nbttagcompound1.getByteArray("BlockLight"), 4));
//
//            if (flag)
//            {
//                extendedblockstorage.setSkylightArray(new NibbleArray(nbttagcompound1.getByteArray("SkyLight"), 4));
//            }
//
//            extendedblockstorage.removeInvalidBlocks();
//            aextendedblockstorage[b1] = extendedblockstorage;
//        }
//
//        chunk.setStorageArrays(aextendedblockstorage);
//
//        if (par2NBTTagCompound.hasKey("Biomes", 7))
//        {
//            chunk.setBiomeArray(par2NBTTagCompound.getByteArray("Biomes"));
//        }
//
//        return chunk;
//    }

}
