package net.techbrew.mcjm.model;

import java.util.Arrays;

import net.minecraft.src.BiomeGenBase;
import net.minecraft.src.Block;
import net.minecraft.src.Chunk;
import net.minecraft.src.ChunkCoordIntPair;
import net.minecraft.src.EnumSkyBlock;
import net.minecraft.src.ExtendedBlockStorage;
import net.minecraft.src.Material;
import net.minecraft.src.World;
import net.minecraft.src.WorldChunkManager;

public class ChunkStub {
	
	public final int heightMap[];
	public final byte[] blockBiomeArray;
	public final int xPosition;
	public final int zPosition;
	public final World worldObj;
    public int[] precipitationHeightMap;
    public final long lastSaveTime;
	public final ExtendedBlockStorageStub storageArrays[];
	public boolean isModified;			
	
	ChunkStub(Chunk chunk, boolean doErrorChecks) {
		
		this.heightMap = Arrays.copyOf(chunk.heightMap, chunk.heightMap.length);
		this.blockBiomeArray = Arrays.copyOf(chunk.getBiomeArray(), chunk.getBiomeArray().length);
		this.xPosition = chunk.xPosition;
		this.zPosition = chunk.zPosition;
		this.isModified = chunk.isModified;
		this.lastSaveTime = chunk.lastSaveTime;
		this.worldObj = chunk.worldObj;
		this.precipitationHeightMap = Arrays.copyOf(chunk.precipitationHeightMap, chunk.precipitationHeightMap.length);
		this.storageArrays = new ExtendedBlockStorageStub[chunk.getBlockStorageArray().length];
		for(int i=0;i<storageArrays.length;i++) {
			ExtendedBlockStorage ebs = chunk.getBlockStorageArray()[i];
			if(ebs!=null) {
				if(doErrorChecks) {
					ebs.removeInvalidBlocks();
				}
				this.storageArrays[i] = new ExtendedBlockStorageStub(ebs);
			}
		}
	}
	
	/**
     * Checks whether the chunk is at the X/Z location specified
     */
    public boolean isAtLocation(int par1, int par2)
    {
        return par1 == this.xPosition && par2 == this.zPosition;
    }

    /**
     * Returns the value in the height map at this x, z coordinate in the chunk
     */
    public int getHeightValue(int par1, int par2)
    {
        return this.heightMap[par2 << 4 | par1];
    }
    
    


    /**
     * Returns the topmost ExtendedBlockStorage instance for this Chunk that actually contains a block.
     */
    public int getTopFilledSegment()
    {
        for (int var1 = this.storageArrays.length - 1; var1 >= 0; --var1)
        {
            if (this.storageArrays[var1] != null)
            {
                return this.storageArrays[var1].getYLocation();
            }
        }

        return 0;
    }

    /**
     * Returns the ExtendedBlockStorage array for this Chunk.
     */
    public ExtendedBlockStorageStub[] getBlockStorageArray()
    {
        return this.storageArrays;
    }

    /**
     * Generates the height map for a chunk from scratch
     */
    public void generateHeightMap()
    {
        int var1 = this.getTopFilledSegment();

        for (int var2 = 0; var2 < 16; ++var2)
        {
            int var3 = 0;

            while (var3 < 16)
            {
                this.precipitationHeightMap[var2 + (var3 << 4)] = -999;
                int var4 = var1 + 16 - 1;

                while (true)
                {
                    if (var4 > 0)
                    {
                        int var5 = this.getBlockID(var2, var4 - 1, var3);

                        if (Block.lightOpacity[var5] == 0)
                        {
                            --var4;
                            continue;
                        }

                        this.heightMap[var3 << 4 | var2] = var4;
                    }

                    ++var3;
                    break;
                }
            }
        }

        this.isModified = true;
    }


    public int getBlockLightOpacity(int par1, int par2, int par3)
    {
        return Block.lightOpacity[this.getBlockID(par1, par2, par3)];
    }

    /**
     * Return the ID of a block in the chunk.
     */
    public int getBlockID(int par1, int par2, int par3)
    {
        if (par2 >> 4 >= this.storageArrays.length)
        {
            return 0;
        }
        else
        {
            ExtendedBlockStorageStub var4 = this.storageArrays[par2 >> 4];
            return var4 != null ? var4.getExtBlockID(par1, par2 & 15, par3) : 0;
        }
    }

    /**
     * Return the metadata corresponding to the given coordinates inside a chunk.
     */
    public int getBlockMetadata(int par1, int par2, int par3)
    {
        if (par2 >> 4 >= this.storageArrays.length)
        {
            return 0;
        }
        else
        {
        	ExtendedBlockStorageStub var4 = this.storageArrays[par2 >> 4];
            return var4 != null ? var4.getExtBlockMetadata(par1, par2 & 15, par3) : 0;
        }
    }

    /**
     * Gets the amount of light saved in this block (doesn't adjust for daylight)
     */
    int getSavedLightValue(EnumSkyBlock par1EnumSkyBlock, int par2, int par3, int par4)
    {
    	ExtendedBlockStorageStub var5 = this.storageArrays[par3 >> 4];
        return var5 == null ? (this.canBlockSeeTheSky(par2, par3, par4) ? par1EnumSkyBlock.defaultLightValue : 0) : (par1EnumSkyBlock == EnumSkyBlock.Sky ? (this.worldObj.provider.hasNoSky ? 0 : var5.getExtSkylightValue(par2, par3 & 15, par4)) : (par1EnumSkyBlock == EnumSkyBlock.Block ? var5.getExtBlocklightValue(par2, par3 & 15, par4) : par1EnumSkyBlock.defaultLightValue));
    }
    
    /**
     * Gets the amount of light on a block taking into account sunlight
     */
    public int getBlockLightValue(int par1, int par2, int par3, int par4)
    {
        ExtendedBlockStorageStub var5 = this.storageArrays[par2 >> 4];

        if (var5 != null)
        {
            int var6 = this.worldObj.provider.hasNoSky ? 0 : var5.getExtSkylightValue(par1, par2 & 15, par3);

            if (var6 > 0)
            {
                //isLit = true;
            }

            var6 -= par4;
            int var7 = var5.getExtBlocklightValue(par1, par2 & 15, par3);

            if (var7 > var6)
            {
                var6 = var7;
            }

            return var6;
        }
        else
        {
            return !this.worldObj.provider.hasNoSky && par4 < EnumSkyBlock.Sky.defaultLightValue ? EnumSkyBlock.Sky.defaultLightValue - par4 : 0;
        }
    }


    /**
     * Returns whether is not a block above this one blocking sight to the sky (done via checking against the heightmap)
     */
    public boolean canBlockSeeTheSky(int par1, int par2, int par3)
    {
        return par2 >= this.heightMap[par3 << 4 | par1];
    }


    /**
     * Gets the height to which rain/snow will fall. Calculates it if not already stored.
     */
    public int getPrecipitationHeight(int par1, int par2)
    {
        int var3 = par1 | par2 << 4;
        int var4 = this.precipitationHeightMap[var3];

        if (var4 == -999)
        {
            int var5 = this.getTopFilledSegment() + 15;
            var4 = -1;

            while (var5 > 0 && var4 == -1)
            {
                int var6 = this.getBlockID(par1, var5, par2);
                Material var7 = var6 == 0 ? Material.air : Block.blocksList[var6].blockMaterial;

                if (!var7.blocksMovement() && !var7.isLiquid())
                {
                    --var5;
                }
                else
                {
                    var4 = var5 + 1;
                }
            }

            this.precipitationHeightMap[var3] = var4;
        }

        return var4;
    }

    /**
     * Gets a ChunkCoordIntPair representing the Chunk's position.
     */
    public ChunkCoordIntPair getChunkCoordIntPair()
    {
        return new ChunkCoordIntPair(this.xPosition, this.zPosition);
    }

    /**
     * Returns whether the ExtendedBlockStorages containing levels (in blocks) from arg 1 to arg 2 are fully empty
     * (true) or not (false).
     */
    public boolean getAreLevelsEmpty(int par1, int par2)
    {
        if (par1 < 0)
        {
            par1 = 0;
        }

        if (par2 >= 256)
        {
            par2 = 255;
        }

        for (int var3 = par1; var3 <= par2; var3 += 16)
        {
        	ExtendedBlockStorageStub var4 = this.storageArrays[var3 >> 4];

            if (var4 != null && !var4.isEmpty())
            {
                return false;
            }
        }

        return true;
    }
    
    /**
     * This method retrieves the biome at a set of coordinates
     */
    public BiomeGenBase getBiomeGenForWorldCoords(int par1, int par2, WorldChunkManager par3WorldChunkManager)
    {
        int var4 = this.blockBiomeArray[par2 << 4 | par1] & 255;

        if (var4 == 255)
        {
//            BiomeGenBase var5 = par3WorldChunkManager.getBiomeGenAt((this.xPosition << 4) + par1, (this.zPosition << 4) + par2);
//            var4 = var5.biomeID;
//            this.blockBiomeArray[par2 << 4 | par1] = (byte)(var4 & 255);
        	var4 = BiomeGenBase.taiga.biomeID; // JourneyMap override
        }

        return BiomeGenBase.biomeList[var4] == null ? BiomeGenBase.plains : BiomeGenBase.biomeList[var4];
    }

    /**
     * Returns an array containing a 16x16 mapping on the X/Z of block positions in this Chunk to biome IDs.
     */
    public byte[] getBiomeArray()
    {
        return this.blockBiomeArray;
    }
    
	
	
	
	
}
