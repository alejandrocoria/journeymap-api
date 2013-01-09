package net.techbrew.mcjm;

import java.util.Arrays;

import net.minecraft.src.BiomeGenBase;
import net.minecraft.src.Block;
import net.minecraft.src.Chunk;
import net.minecraft.src.EnumSkyBlock;
import net.minecraft.src.ExtendedBlockStorage;
import net.minecraft.src.Material;
import net.minecraft.src.NibbleArray;
import net.minecraft.src.World;
import net.minecraft.src.WorldChunkManager;

public class ChunkStub {

	public final int heightMap[];
	public final byte[] blockBiomeArray;
	public final int xPosition;
	public final int zPosition;
	public final String biomeName;
	public final Boolean hasNoSky;
	public final int worldType;
	public final long worldHash;
	public final World worldObj;
	public final ExtendedBlockStorageStub storageArrays[];
	//public final NibbleArray blocklightMap;
	//public final byte blocks[];
	//public final int worldXShift;
	//public final int worldHeightShift;
	public final int worldHeight;
	//public final int worldMaxY;
	//public final int grassColor;
	//public final int foliageColor;
	public Boolean doMap;
	
	
	public ChunkStub(ChunkStub original) {
		this.heightMap = Arrays.copyOf(original.heightMap, original.heightMap.length);
		this.blockBiomeArray = Arrays.copyOf(original.blockBiomeArray, original.blockBiomeArray.length);
		this.xPosition = original.xPosition;
		this.zPosition = original.zPosition;
		this.worldHash = original.worldHash;
		this.worldType = original.worldType;
		this.worldObj = original.worldObj;
		this.worldHeight = original.worldHeight;
		this.doMap = original.doMap;
		this.biomeName = original.biomeName;
		this.hasNoSky = original.hasNoSky;
		this.storageArrays = new ExtendedBlockStorageStub[original.storageArrays.length];
		for(int i=0;i<storageArrays.length;i++) {
			ExtendedBlockStorageStub ebs = original.storageArrays[i];
			if(ebs!=null) {
				this.storageArrays[i] = new ExtendedBlockStorageStub(ebs);
			}
		}
	}
	
	public ChunkStub(Chunk chunk, Boolean doMap, World worldObj, long worldHash) {
		this.heightMap = Arrays.copyOf(chunk.heightMap, chunk.heightMap.length);
		this.blockBiomeArray = Arrays.copyOf(chunk.getBiomeArray(), chunk.getBiomeArray().length);
		this.xPosition = chunk.xPosition;
		this.zPosition = chunk.zPosition;
		this.worldHash = worldHash;
		this.worldType = chunk.worldObj.provider.dimensionId;
		this.worldObj = worldObj;
		this.worldHeight = worldObj.getHeight();
		this.doMap = doMap;
		this.storageArrays = new ExtendedBlockStorageStub[chunk.getBlockStorageArray().length];
		for(int i=0;i<storageArrays.length;i++) {
			ExtendedBlockStorage ebs = chunk.getBlockStorageArray()[i];
			if(ebs!=null) {
				this.storageArrays[i] = new ExtendedBlockStorageStub(ebs);
			}
		}
		
		//this.blocklightMap = new NibbleArray(chunk.blocklightMap.data, chunk.worldObj.heightShift);
		//this.blocks = Arrays.copyOf(chunk.blocks, chunk.blocks.length);
		//this.worldXShift = chunk.worldObj.xShift;
		//this.worldHeightShift = chunk.worldObj.heightShift;
		//this.worldHeight = chunk.worldObj.worldHeight;
		
		if(chunk.isEmpty() || !chunk.isChunkLoaded) {
			doMap = false;
		}
		
		BiomeGenBase biome = null;
		if(doMap) {
			try {
				//biome = chunk.func_48490_a(chunk.xPosition>>4, chunk.zPosition>>4, chunk.worldObj.worldProvider.worldChunkMgr);
				biome = worldObj.getWorldChunkManager().getBiomeGenAt(xPosition * 16, zPosition * 16);
			} catch(Throwable t) {
				doMap = false;
			}
		}
		
		if(doMap && biome!=null) {
			this.biomeName = biome.biomeName;
		} else {
			this.biomeName = null;
		}
		
		//this.grassColor = biome.getGrassColorAtCoords(chunk.worldObj, chunk.xPosition, chunk.worldObj.getSeaLevel(), chunk.zPosition);
		//this.foliageColor = biome.getFoliageColorAtCoords(chunk.worldObj, chunk.xPosition,  chunk.worldObj.getSeaLevel(), chunk.zPosition);
		this.hasNoSky = chunk.worldObj.provider.hasNoSky;
	}
	
	/**
     * Return the ID of a block in the chunk.
     */
    public int getBlockID(int par1, int par2, int par3)
    {
        ExtendedBlockStorageStub extendedblockstorage = storageArrays[par2 >> 4];

        if (extendedblockstorage != null)
        {
            return extendedblockstorage.getExtBlockID(par1, par2 & 0xf, par3);
        }
        else
        {
            return 0;
        }
    }
    
    /**
     * This method retrieves the biome at a set of coordinates
     */
    public BiomeGenBase getBiomeGenForWorldCoords(int par1, int par2, WorldChunkManager par3WorldChunkManager)
    {
        int var4 = this.blockBiomeArray[par2 << 4 | par1] & 255;

        if (var4 == 255)
        {
            BiomeGenBase var5 = par3WorldChunkManager.getBiomeGenAt((this.xPosition << 4) + par1, (this.zPosition << 4) + par2);
            var4 = var5.biomeID;
            this.blockBiomeArray[par2 << 4 | par1] = (byte)(var4 & 255);
        }

        return BiomeGenBase.biomeList[var4] == null ? BiomeGenBase.plains : BiomeGenBase.biomeList[var4];
    }
	
	/**
     * Return the metadata corresponding to the given coordinates inside a chunk.
     */
    public int getBlockMetadata(int par1, int par2, int par3)
    {
    	ExtendedBlockStorageStub extendedblockstorage = storageArrays[par2 >> 4];

        if (extendedblockstorage != null)
        {
            return extendedblockstorage.getExtBlockMetadata(par1, par2 & 0xf, par3);
        }
        else
        {
            return 0;
        }
    }
    
    /**
     * Returns the value in the height map at this x, z coordinate in the chunk
     */
    public int getHeightValue(int par1, int par2)
    {
        return heightMap[par2 << 4 | par1];
    }
	
	public int getTopSolidOrLiquidBlock()
    {
		int i = xPosition;
		int j = zPosition;
        int k = worldHeight - 1;
        i &= 0xf;
        j &= 0xf;
        while (k > 0)
        {
            int l = getBlockID(i, k, j);
            if (l == 0 || !Block.blocksList[l].blockMaterial.isSolid() || Block.blocksList[l].blockMaterial == Material.leaves)
            {
                k--;
            }
            else
            {
                return k + 1;
            }
        }
        return -1;
    }
	
	 /**
     * Gets the amount of light saved in this block (doesn't adjust for daylight)
     */
    public int getSavedLightValue(EnumSkyBlock par1EnumSkyBlock, int par2, int par3, int par4)
    {
    	ExtendedBlockStorageStub extendedblockstorage = storageArrays[par3 >> 4];

        if (extendedblockstorage == null)
        {
            return par1EnumSkyBlock.defaultLightValue;
        }

        if (par1EnumSkyBlock == EnumSkyBlock.Sky)
        {
            return extendedblockstorage.getExtSkylightValue(par2, par3 & 0xf, par4);
        }

        if (par1EnumSkyBlock == EnumSkyBlock.Block)
        {
            return extendedblockstorage.getExtBlocklightValue(par2, par3 & 0xf, par4);
        }
        else
        {
            return par1EnumSkyBlock.defaultLightValue;
        }
    }
    
    /**
     * Gets biome for block in chunk
     * @param par1
     * @param par2
     * @param par3WorldChunkManager
     * @return
     */
    //public BiomeGenBase func_48490_a(int par1, int par2, WorldChunkManager par3WorldChunkManager)
    public BiomeGenBase getBlockBiome(int x, int z)
    {
        int i = blockBiomeArray[z << 4 | x] & 0xff;

        if (i == 255)
        {
            BiomeGenBase biomegenbase = worldObj.getWorldChunkManager().getBiomeGenAt((xPosition << 4) + x, (zPosition << 4) + z);
            i = biomegenbase.biomeID;
            blockBiomeArray[z << 4 | x] = (byte)(i & 0xff);
        }

        if (BiomeGenBase.biomeList[i] == null)
        {
            return BiomeGenBase.plains;
        }
        else
        {
            return BiomeGenBase.biomeList[i];
        }
    }

	@Override
	public int hashCode() {
		return toHashCode(xPosition, zPosition);
	}
	
	public static int toHashCode(final int x, final int z) {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + z;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChunkStub other = (ChunkStub) obj;
		if (xPosition != other.xPosition)
			return false;
		if (zPosition != other.zPosition)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ChunkStub [" + xPosition + ", " + zPosition //$NON-NLS-1$ //$NON-NLS-2$
				+ ", doMap=" + doMap + "]"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	
	
	
}
