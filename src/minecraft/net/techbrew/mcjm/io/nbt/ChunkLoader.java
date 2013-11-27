package net.techbrew.mcjm.io.nbt;

import java.io.DataInputStream;
import java.io.File;
import java.util.logging.Logger;

import net.minecraft.src.Chunk;
import net.minecraft.src.CompressedStreamTools;
import net.minecraft.src.EmptyChunk;
import net.minecraft.src.ExtendedBlockStorage;
import net.minecraft.src.Minecraft;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.NibbleArray;
import net.minecraft.src.RegionFileCache;
import net.minecraft.src.World;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.model.ChunkMD;

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
				if(theChunk.isChunkLoaded) {
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
        int var3 = par2NBTTagCompound.getInteger("xPos");
        int var4 = par2NBTTagCompound.getInteger("zPos");
        Chunk var5 = new Chunk(par1World, var3, var4);
        var5.heightMap = par2NBTTagCompound.getIntArray("HeightMap");
        var5.isTerrainPopulated = par2NBTTagCompound.getBoolean("TerrainPopulated");
        var5.inhabitedTime = par2NBTTagCompound.getLong("InhabitedTime");
        NBTTagList var6 = par2NBTTagCompound.getTagList("Sections");
        byte var7 = 16;
        ExtendedBlockStorage[] var8 = new ExtendedBlockStorage[var7];
        boolean var9 = !par1World.provider.hasNoSky;

        for (int var10 = 0; var10 < var6.tagCount(); ++var10)
        {
            NBTTagCompound var11 = (NBTTagCompound)var6.tagAt(var10);
            byte var12 = var11.getByte("Y");
            ExtendedBlockStorage var13 = new ExtendedBlockStorage(var12 << 4, var9);
            var13.setBlockLSBArray(var11.getByteArray("Blocks"));

            if (var11.hasKey("Add"))
            {
                var13.setBlockMSBArray(new NibbleArray(var11.getByteArray("Add"), 4));
            }

            var13.setBlockMetadataArray(new NibbleArray(var11.getByteArray("Data"), 4));
            var13.setBlocklightArray(new NibbleArray(var11.getByteArray("BlockLight"), 4));

            if (var9)
            {
                var13.setSkylightArray(new NibbleArray(var11.getByteArray("SkyLight"), 4));
            }

            var13.removeInvalidBlocks();
            var8[var12] = var13;
        }

        var5.setStorageArrays(var8);

        if (par2NBTTagCompound.hasKey("Biomes"))
        {
            var5.setBiomeArray(par2NBTTagCompound.getByteArray("Biomes"));
        }

        return var5;
    }

}
