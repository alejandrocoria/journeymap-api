package net.techbrew.mcjm.io;

import java.io.File;

import net.minecraft.src.Chunk;
import net.techbrew.mcjm.Constants;

public class RegionCoord {
	
	public final File worldDir;
	public final int regionX;
	public final int regionZ;
	public final Integer vSlice;
	public final Constants.CoordType cType;
	static final int SIZE = 5;
	private static final int chunkSqRt = (int) Math.pow(2,SIZE);
	
//	public static RegionCoord fromChunk(File worldDir, Chunk chunk, Integer vSlice, Constants.CoordType cType) {
//		return RegionCoord.fromChunkPos(worldDir, chunk.xPosition, vSlice, chunk.zPosition, cType);
//	}
	
	public static RegionCoord fromChunkPos(File worldDir, int chunkX, Integer vSlice, int chunkZ, Constants.CoordType cType) {
		return new RegionCoord(worldDir, getRegionPos(chunkX), vSlice, getRegionPos(chunkZ), cType);
	}
		
	public RegionCoord(File worldDir, int regionX, Integer vSlice, int regionZ, Constants.CoordType cType) {
		this.worldDir = worldDir;
		this.regionX = regionX;
		this.regionZ = regionZ;
		this.vSlice = vSlice;
		this.cType = cType;
	}
	
	public int getXOffsetDay(int chunkX) {
		if(chunkX>>SIZE!=regionX) {
			throw new IllegalArgumentException("chunkX " + chunkX + " out of bounds for regionX " + regionX); //$NON-NLS-1$ //$NON-NLS-2$
		}
		int offset = ((chunkX % chunkSqRt)*16);
		if(offset<0) {
			offset = (chunkSqRt*16) + offset;
		}
		return offset;
	}
	
	public int getXOffsetNight(int chunkX) {
		return getXOffsetDay(chunkX) + (chunkSqRt*16);
	}
	
	public int getZOffsetDay(int chunkZ) {
		if(getRegionPos(chunkZ)!=regionZ) {
			throw new IllegalArgumentException("chunkZ " + chunkZ + " out of bounds for regionZ " + regionZ); //$NON-NLS-1$ //$NON-NLS-2$
		}
		int offset = ((chunkZ % chunkSqRt)*16);
		if(offset<0) {
			offset = (chunkSqRt*16) + offset;
		}
		return offset;
	}
	
	public int getZOffsetNight(int chunkZ) {
		return getZOffsetDay(chunkZ);
	}
	
	public int getMinChunkX() {
		return getMinChunkX(regionX);
	}
	
	public int getMaxChunkX() {
		return getMaxChunkX(regionX);
	}
	
	public static int getMinChunkX(int rX) {
		return rX << SIZE;
	}
	
	public static int getMaxChunkX(int rX) {
		return getMinChunkX(rX) + (int) Math.pow(2,SIZE) -1;
	}
	
	public int getMinChunkZ() {
		return getMinChunkZ(regionZ);
	}
	
	public int getMaxChunkZ() {
		return getMaxChunkZ(regionZ);
	}
	
	public static int getMinChunkZ(int rZ) {
		return rZ << SIZE;
	}
	
	public static int getMaxChunkZ(int rZ) {
		return getMinChunkZ(rZ) + (int) Math.pow(2,SIZE) -1;
	}
	
	public static int getRegionPos(int chunkPos) {
		return chunkPos >> SIZE;
	}
	
	public Boolean isUnderground() {
		return !cType.equals(Constants.CoordType.Normal);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RegionCoord ["); //$NON-NLS-1$
		builder.append(regionX);
		builder.append(","); //$NON-NLS-1$
		builder.append(regionZ);
		builder.append("]"); //$NON-NLS-1$
		return builder.toString();
	}
	
	public Integer getVerticalSlice() {
		if(vSlice==null) {
			return -1;
		} else {
			return vSlice;
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getVerticalSlice();
		result = prime * result + regionX;
		result = prime * result + regionZ;
		result = prime * result
				+ ((worldDir == null) ? 0 : worldDir.hashCode());
		result = prime * result + cType.hashCode();
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
		RegionCoord other = (RegionCoord) obj;
		if (other.getVerticalSlice() != getVerticalSlice())
			return false;
		if (regionX != other.regionX)
			return false;
		if (regionZ != other.regionZ)
			return false;
		if (worldDir == null) {
			if (other.worldDir != null)
				return false;
		} else if (!worldDir.equals(other.worldDir))
			return false;
		if (!cType.equals(other.cType))
			return false;
		return true;
	}	
}
