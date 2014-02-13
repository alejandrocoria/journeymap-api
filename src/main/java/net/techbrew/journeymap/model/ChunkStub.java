package net.techbrew.journeymap.model;

import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import java.util.Arrays;

public class ChunkStub extends Chunk {

	ChunkStub(Chunk chunk) {

        super(chunk.worldObj, chunk.xPosition, chunk.zPosition);

        // private ExtendedBlockStorage[] storageArrays;
        ReflectionHelper.setPrivateValue(Chunk.class, (Chunk) this, chunk.getBlockStorageArray(), 1);

        // private byte[] blockBiomeArray;
        ReflectionHelper.setPrivateValue(Chunk.class, (Chunk) this, chunk.getBiomeArray(), 2);

        // public int[] precipitationHeightMap;
        this.precipitationHeightMap = Arrays.copyOf(chunk.precipitationHeightMap, chunk.precipitationHeightMap.length);

        // public boolean[] updateSkylightColumns;
        this.updateSkylightColumns = Arrays.copyOf(chunk.updateSkylightColumns, chunk.updateSkylightColumns.length);

        // public boolean isChunkLoaded;
        this.isChunkLoaded = chunk.isChunkLoaded;

        // public World worldObj;
        // Set in constructor

        // public int[] heightMap;
        this.heightMap = Arrays.copyOf(chunk.heightMap, chunk.heightMap.length);

        // public final int xPosition;
        // Set in constructor

        // public final int zPosition;
        // Set in constructor

        // private boolean isGapLightingUpdated;
        // Ignore

        // public Map field_150816_i;
        // Map<TileEntity, ChunkPosition>
        // this.field_150816_i.putAll(chunk.field_150816_i);
        // Ignore

        // public List[] entityLists;
        // Ignore

        // public boolean isTerrainPopulated;
        this.isTerrainPopulated = chunk.isTerrainPopulated;

        // public boolean isLightPopulated;
        //this.isGapLightingUpdated = chunk.isLightPopulated;

        // public boolean field_150815_m;
        //this.field_150815_m = chunk.field_150815_m;

        // public boolean isModified;
        this.isModified = chunk.isModified;

        // public boolean hasEntities;
        this.hasEntities = chunk.hasEntities;

        // public long lastSaveTime;
        this.lastSaveTime = chunk.lastSaveTime;

        // public boolean sendUpdates;
        this.sendUpdates = chunk.sendUpdates;

        // public int heightMapMinimum;
        this.heightMapMinimum = chunk.heightMapMinimum;

        // public long inhabitedTime;
        this.inhabitedTime = chunk.inhabitedTime;

        // private int queuedLightChecks;
        // ignored

	}

    public void updateFrom(Chunk chunk) {
        if(this.xPosition!=chunk.xPosition || this.zPosition!=chunk.zPosition) {
            throw new IllegalArgumentException("ChunkStub can't be populated from a chunk in a different position");
        }

        // private ExtendedBlockStorage[] storageArrays;
        ReflectionHelper.setPrivateValue(Chunk.class, (Chunk) this, chunk.getBlockStorageArray(), 1);

        // public boolean[] updateSkylightColumns;
        this.updateSkylightColumns = Arrays.copyOf(chunk.updateSkylightColumns, chunk.updateSkylightColumns.length);

        // public int[] heightMap;
        this.heightMap = Arrays.copyOf(chunk.heightMap, chunk.heightMap.length);

        // public boolean isTerrainPopulated;
        this.isTerrainPopulated = chunk.isTerrainPopulated;

        // public boolean isLightPopulated;
        //this.isLightPopulated = chunk.isLightPopulated;

        // public boolean field_150815_m;
        //this.field_150815_m = chunk.field_150815_m;

        // public boolean isModified;
        this.isModified = chunk.isModified;

        // public boolean hasEntities;
        this.hasEntities = chunk.hasEntities;

        // public long lastSaveTime;
        this.lastSaveTime = chunk.lastSaveTime;

        // public int heightMapMinimum;
        this.heightMapMinimum = chunk.heightMapMinimum;

        // public long inhabitedTime;
        this.inhabitedTime = chunk.inhabitedTime;
    }

    /**
     * Compare storage arrays of another chunkStub at the same position.
     * @param other
     * @return
     */
    public boolean equalTo(Chunk other) {
        if(lastSaveTime!=other.lastSaveTime) {
            return false;
        }

        if(!Arrays.equals(this.heightMap,other.heightMap)) {
            return false;
        }

        ExtendedBlockStorage[] myEbsArray = getBlockStorageArray();
        ExtendedBlockStorage[] otherEbsArray = other.getBlockStorageArray();

        if(myEbsArray.length!=otherEbsArray.length) {
            return false;
        }

        for(int i=0;i<myEbsArray.length;i++) {
            ExtendedBlockStorage ebs = myEbsArray[i];
            ExtendedBlockStorage otherEbs = otherEbsArray[i];
            if(!areEqual(ebs, otherEbs)) {
                return false;
            }
        }
        return true;
    }

    boolean areEqual(ExtendedBlockStorage a, ExtendedBlockStorage obj) {
        if (a == obj)
            return true;
        if (a!=null && obj == null)
            return false;
        if (a==null && obj !=null)
            return false;

//        if (a.blockRefCount != other.blockRefCount) {
//            return false;
//        }

        if (a.getMetadataArray() == null) {
            if (obj.getMetadataArray() != null) {
                return false;
            }
        } else if (!Arrays.equals(a.getMetadataArray().data,obj.getMetadataArray().data)) {
            return false;
        }

        if (a.getBlocklightArray() == null) {
            if (obj.getBlocklightArray() != null) {
                return false;
            }
        } else if (!Arrays.equals(a.getBlocklightArray().data, obj.getBlocklightArray().data)) {
            return false;
        }

        return true;
    }

}
