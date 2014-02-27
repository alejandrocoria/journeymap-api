package net.techbrew.journeymap.io.nbt;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.chunk.storage.RegionFileCache;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.model.ChunkMD;

import java.io.DataInputStream;
import java.io.File;
import java.util.logging.Logger;

public class ChunkLoader {
	
	private static Logger logger = JourneyMap.getLogger();
	
	public static ChunkMD getChunkStubFromDisk(int chunkX, int chunkZ, File worldDir, World world) {
		
		Chunk chunk = getChunkFromDisk(chunkX, chunkZ, worldDir, world);
		if(chunk==null) {
			return null;
		}	
		return new ChunkMD(chunk, true, world, /* do error checks */ true);
		
	}
	
	public static ChunkMD getChunkStubFromMemory(int chunkX, int chunkZ, World world) {
		Chunk chunk = getChunkFromMemory(chunkX, chunkZ, world);
		if(chunk!=null) {
			return new ChunkMD(chunk, true, world);
		}
		return null;
	}

    public static ChunkMD refreshChunkStubFromMemory(ChunkMD chunkMD, World world) {
        Chunk chunk = getChunkFromMemory(chunkMD.coord.chunkXPos, chunkMD.coord.chunkZPos, world);
        if(chunk!=null) {
            chunkMD.stub.updateFrom(chunk);
            return chunkMD;
        } else {
            return null;
        }
    }

	public static ChunkMD getChunkStubFromMemory(int chunkX, int chunkZ, Minecraft minecraft) {
		Chunk chunk = getChunkFromMemory(chunkX, chunkZ, minecraft.theWorld);
		if(chunk!=null) return new ChunkMD(chunk, true, minecraft.theWorld);
		return null;
	}
	
	public static Chunk getChunkFromMemory(int chunkX, int chunkZ, World world) {
		Chunk result  = null;
		if(world.getChunkProvider().chunkExists(chunkX, chunkZ)) {
			Chunk theChunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
			if(!(theChunk instanceof EmptyChunk)) {
				if(theChunk.isChunkLoaded && !theChunk.isEmpty()) {
					result = theChunk;
				}
			}			
		}
		return result;
	}
	
	public static Chunk getChunkFromDisk(int chunkX, int chunkZ, File worldDir, World world) {
		
		Chunk chunk = null; // Utils.getChunkIfAvailable(world, coord.chunkXPos, coord.chunkZPos);
		if(chunk==null) {
			try {
				DataInputStream dis = RegionFileCache.getChunkInputStream(worldDir, chunkX, chunkZ);
				if(dis!=null) {
					NBTTagCompound chunkNBT = CompressedStreamTools.read(dis);
					if(chunkNBT!=null) {
						chunk = checkedReadChunkFromNBT(world, chunkX, chunkZ, chunkNBT);
						if(chunk!=null) {
							chunk.generateHeightMap();
							chunk.generateSkylightMap();
						}
					}
				}
			} catch (Throwable t) {
				logger.severe("Error getting chunk from RegionFile: " + LogFormatter.toString(t));
			}
		}
		return chunk;
	}

	/**
     * Wraps readChunkFromNBT. Checks the coordinates and several NBT tags.
     */
    protected static Chunk checkedReadChunkFromNBT(World world, int chunkX, int chunkZ, NBTTagCompound par4NBTTagCompound)
    {
    	final int par2 = chunkX;
    	final int par3 = chunkZ;
    	
        if (!par4NBTTagCompound.hasKey("Level"))
        {
            logger.severe("Chunk file at " + par2 + "," + par3 + " is missing level data, skipping");
            return null;
        }
        else if (!par4NBTTagCompound.getCompoundTag("Level").hasKey("Sections"))
        {
        	logger.severe("Chunk file at " + par2 + "," + par3 + " is missing block data, skipping");
            return null;
        }
        else
        {
            Chunk var5 = readChunkFromNBT(world, par4NBTTagCompound.getCompoundTag("Level"));

            if (!var5.isAtLocation(par2, par3))
            {
            	logger.severe("Chunk file at " + par2 + "," + par3 + " is in the wrong location; relocating. (Expected " + par2 + ", " + par3 + ", got " + var5.xPosition + ", " + var5.zPosition + ")");
                par4NBTTagCompound.setInteger("xPos", par2);
                par4NBTTagCompound.setInteger("zPos", par3);
                var5 = readChunkFromNBT(world, par4NBTTagCompound.getCompoundTag("Level"));
            }

            return var5;
        }
    }
    
    /**
     * Reads the data stored in the passed NBTTagCompound and creates a Chunk with that data in the passed World.
     * Returns the created Chunk.
     */
    private static Chunk readChunkFromNBT(World par1World, NBTTagCompound par2NBTTagCompound)
    {
        int i = par2NBTTagCompound.getInteger("xPos");
        int j = par2NBTTagCompound.getInteger("zPos");
        Chunk chunk = new Chunk(par1World, i, j);
        chunk.heightMap = par2NBTTagCompound.getIntArray("HeightMap");
        chunk.isTerrainPopulated = par2NBTTagCompound.getBoolean("TerrainPopulated");
        chunk.isLightPopulated = par2NBTTagCompound.getBoolean("LightPopulated");
        chunk.inhabitedTime = par2NBTTagCompound.getLong("InhabitedTime");
        NBTTagList nbttaglist = par2NBTTagCompound.getTagList("Sections", 10);
        byte b0 = 16;
        ExtendedBlockStorage[] aextendedblockstorage = new ExtendedBlockStorage[b0];
        boolean flag = !par1World.provider.hasNoSky;

        for (int k = 0; k < nbttaglist.tagCount(); ++k)
        {
            NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(k);
            byte b1 = nbttagcompound1.getByte("Y");
            ExtendedBlockStorage extendedblockstorage = new ExtendedBlockStorage(b1 << 4, flag);
            extendedblockstorage.setBlockLSBArray(nbttagcompound1.getByteArray("Blocks"));

            if (nbttagcompound1.hasKey("Add", 7))
            {
                extendedblockstorage.setBlockMSBArray(new NibbleArray(nbttagcompound1.getByteArray("Add"), 4));
            }

            extendedblockstorage.setBlockMetadataArray(new NibbleArray(nbttagcompound1.getByteArray("Data"), 4));
            extendedblockstorage.setBlocklightArray(new NibbleArray(nbttagcompound1.getByteArray("BlockLight"), 4));

            if (flag)
            {
                extendedblockstorage.setSkylightArray(new NibbleArray(nbttagcompound1.getByteArray("SkyLight"), 4));
            }

            extendedblockstorage.removeInvalidBlocks();
            aextendedblockstorage[b1] = extendedblockstorage;
        }

        chunk.setStorageArrays(aextendedblockstorage);

        if (par2NBTTagCompound.hasKey("Biomes", 7))
        {
            chunk.setBiomeArray(par2NBTTagCompound.getByteArray("Biomes"));
        }

        return chunk;
    }

}
