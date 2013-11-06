package net.techbrew.mcjm.ui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.src.ChunkCoordIntPair;
import net.techbrew.mcjm.Constants.MapType;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.io.RegionImageHandler;

public class Tile {
		
	final int dimension;
	final int zoom;
	final int tileX; 
	final int tileZ;
	final File worldDir;
	final ChunkCoordIntPair topLeft;
	final ChunkCoordIntPair bottomRight;
	
	long lastImageTime = 0;
	MapType lastMapType;
	BufferedImage lastImage = null;
	
	private final Logger logger = JourneyMap.getLogger();
	private final boolean debug = logger.isLoggable(Level.INFO);

	public Tile(final File worldDir, final int tileX, final int tileZ, final int zoom, final int dimension) {
		this.worldDir = worldDir;
		this.tileX = tileX;
		this.tileZ = tileZ;
		this.zoom = zoom;
		this.dimension = dimension;
		final int distance = 32 / (int) Math.pow(2, zoom);
		topLeft = new ChunkCoordIntPair(tileX * distance, tileZ * distance);
		bottomRight = new ChunkCoordIntPair(topLeft.chunkXPos + distance - 1, topLeft.chunkZPos + distance - 1);
	}
	
	public boolean markObsolete(final MapType mapType, final Integer vSlice) {
		if(lastImage!=null) {
			boolean changed = RegionImageHandler.hasImageChanged(worldDir, topLeft, bottomRight, mapType, vSlice, dimension, lastImageTime);
			if(changed) lastImage=null;
			return changed;
		} else {
			return true;
		}
	}
	
	public BufferedImage getImage(final MapType mapType, final Integer vSlice, boolean refresh) {	
		if(lastImage==null || refresh) {		
			lastImage = RegionImageHandler.getMergedChunks(worldDir, topLeft, bottomRight, mapType, vSlice, dimension, true, Tiles.TILESIZE, Tiles.TILESIZE);
			lastMapType = mapType;
			lastImageTime = new Date().getTime();	
			if(debug) logger.info("Updated image for " + this);
		}
		return lastImage;
	}

	@Override
	public String toString() {
		return "Tile [ " + tileX + "," + tileZ + " (zoom " + zoom + ") ]";
	}

	@Override
	public int hashCode() {
		return toHashCode(tileX, tileZ, zoom, dimension);
	}
	
	public static int tilePosToChunk(int t, int zoom) {
		return tilePosToBlock(t, zoom) >> 4;  // TODO: Probably wrong
	}
	
	public static int tilePosToBlock(int t, int zoom) {
		return t * pixelsPerBlock(zoom); // TODO: Probably wrong
	}
	
	public static int blockPosToTile(int b, int zoom) {
		int tile = b >> (9-zoom);  // (2 pow 9 = 512)
		return tile;
	}
	
	public static int pixelsPerBlock(final int zoom) {
		return 32 / (int) Math.pow(2, zoom);
	}
	
	public static int toHashCode(final int tileX, final int tileZ, final int zoom, final int dimension) {
		final int prime = 31;
		int result = 1;
		result = prime * result + tileX;
		result = prime * result + tileZ;
		result = prime * result + zoom;
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
		Tile other = (Tile) obj;
		if (tileX != other.tileX)
			return false;
		if (tileZ != other.tileZ)
			return false;
		if (zoom != other.zoom)
			return false;
		if (dimension != other.dimension)
			return false;
		if (!worldDir.equals(other.worldDir))
			return false;
		return true;
	}

}
