package net.techbrew.mcjm.io;

import java.io.File;

import net.minecraft.src.Chunk;
import net.techbrew.mcjm.ChunkStub;
import net.techbrew.mcjm.Constants;

public class ChunkCoord {

	public final File worldDir;
	public final int chunkX;
	public final int chunkZ;
	public final Integer vSlice;
	public final Constants.CoordType cType;
	
	private RegionCoord rCoord = null;
	
	public static ChunkCoord fromChunkStub(File worldDir, ChunkStub chunkStub, Integer vSlice, final Constants.CoordType cType) {
		return ChunkCoord.fromChunkPos(worldDir, chunkStub.xPosition, vSlice, chunkStub.zPosition, cType);
	}
	
	public static ChunkCoord fromChunkPos(File worldDir, int xPosition, Integer vSlice, int zPosition, final Constants.CoordType cType) {
		return new ChunkCoord(worldDir, xPosition, vSlice, zPosition, cType);
	}
		
	private ChunkCoord(File worldDir, int chunkX, Integer vSlice, int chunkZ, final Constants.CoordType cType) {
		this.worldDir = worldDir;
		this.chunkX = chunkX;
		if(vSlice > 16) {
			throw new IllegalArgumentException("Need the vSlice, not a y"); //$NON-NLS-1$
		}
		this.vSlice = vSlice;
		this.chunkZ = chunkZ;
		this.cType = cType;
	}
	
	public RegionCoord getRegionCoord() {
		if(rCoord==null) {
			rCoord = RegionCoord.fromChunkPos(worldDir, chunkX, vSlice, chunkZ, cType);
		}
		return rCoord;
	}
	
	public int getXOffsetDay() {
		return getRegionCoord().getXOffsetDay(chunkX);
	}
	
	public int getZOffsetDay() {
		return getRegionCoord().getZOffsetDay(chunkZ);
	}
	
	public int getXOffsetNight() {
		return getRegionCoord().getXOffsetNight(chunkX);
	}
	
	public int getZOffsetNight() {
		return getRegionCoord().getZOffsetNight(chunkZ);
	}
	
	public int getXOffsetUnderground() {		
		return getRegionCoord().getXOffsetDay(chunkX);
	}
	
	public int getZOffsetUnderground() {
		return getRegionCoord().getZOffsetDay(chunkZ);
	}
	
	public Boolean isUnderground() {
		return !cType.equals(Constants.CoordType.Normal);
	}
	
	public int getVerticalSlice() {
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
		result = prime * result + chunkX;
		result = prime * result + chunkZ;
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
		ChunkCoord other = (ChunkCoord) obj;
		if (other.getVerticalSlice() != getVerticalSlice())
			return false;
		if (chunkX != other.chunkX)
			return false;
		if (chunkZ != other.chunkZ)
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
//		builder.append("ChunkCoord [worldDir=");
//		builder.append(worldDir.getName());
//		builder.append(", chunkX=");
//		builder.append(chunkX);
//		builder.append(", chunkZ=");
//		builder.append(chunkZ);
//		builder.append(", vSlice=");
//		builder.append(vSlice);
//		builder.append(", worldProviderType=");
//		builder.append(worldProviderType);
//		builder.append("]");
		builder.append("ChunkCoord ["); //$NON-NLS-1$
		builder.append(chunkX);
		builder.append(","); //$NON-NLS-1$
		builder.append(chunkZ);
		builder.append("]"); //$NON-NLS-1$
		return builder.toString();
	}
	
}
