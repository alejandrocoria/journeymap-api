package net.techbrew.journeymap.model;

import java.io.File;

public class ChunkCoord {

	public final File worldDir;
	public final int chunkX;
	public final int chunkZ;
	public final Integer vSlice;
	public final int dimension;
	
	private RegionCoord rCoord = null;
	
	public static ChunkCoord fromChunkMD(File worldDir, ChunkMD chunkMd, Integer vSlice, int dimension) {
		return ChunkCoord.fromChunkPos(worldDir, chunkMd.stub.xPosition, vSlice, chunkMd.stub.zPosition, dimension);
	}
	
	public static ChunkCoord fromChunkPos(File worldDir, int xPosition, Integer vSlice, int zPosition, int dimension) {
		return new ChunkCoord(worldDir, xPosition, vSlice, zPosition, dimension);
	}
		
	private ChunkCoord(File worldDir, int chunkX, Integer vSlice, int chunkZ, int dimension) {
		this.worldDir = worldDir;
		this.chunkX = chunkX;
		if(vSlice!=null && vSlice > 16) {
			throw new IllegalArgumentException("Need the vSlice, not a y"); //$NON-NLS-1$
		}
		this.vSlice = vSlice;
		this.chunkZ = chunkZ;
		this.dimension = dimension;
	}
	
	public RegionCoord getRegionCoord() {
		if(rCoord==null) {
			rCoord = RegionCoord.fromChunkPos(worldDir, chunkX, vSlice, chunkZ, dimension);
		}
		return rCoord;
	}
	
	public Boolean isUnderground() {
		return vSlice!=null ? vSlice!=-1 : false;
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
		result = prime * result + dimension;
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
		if (dimension!=other.dimension)
			return false;
		if (chunkX != other.chunkX)
			return false;
		if (chunkZ != other.chunkZ)
			return false;
		if (other.getVerticalSlice() != getVerticalSlice())
			return false;
		if (worldDir == null) {
			if (other.worldDir != null)
				return false;
		} else if (!worldDir.equals(other.worldDir))
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
