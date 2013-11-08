package net.techbrew.mcjm.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.src.ChunkCoordIntPair;
import net.techbrew.mcjm.Constants.MapType;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.io.RegionImageHandler;
import net.techbrew.mcjm.render.overlay.MapTexture;
import net.techbrew.mcjm.ui.Tiles.TilePos;

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
	MapTexture mapTexture;
	
	private final Logger logger = JourneyMap.getLogger();
	private final boolean debug = logger.isLoggable(Level.FINE);

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
	
	public boolean updateTexture(final TilePos pos, final MapType mapType, final Integer vSlice) {
		boolean changed = (mapTexture==null) || RegionImageHandler.hasImageChanged(worldDir, topLeft, bottomRight, mapType, vSlice, dimension, lastImageTime);
		if(changed) {
			clear();
			
			BufferedImage image = RegionImageHandler.getMergedChunks(worldDir, topLeft, bottomRight, mapType, vSlice, dimension, true, Tiles.TILESIZE, Tiles.TILESIZE);
			lastMapType = mapType;
			lastImageTime = new Date().getTime();

			if(debug) {		
				Graphics2D g = RegionImageHandler.initRenderingHints(image.createGraphics());				
				g.setPaint(Color.WHITE);
				g.setStroke(new BasicStroke(3));
				g.drawRect(0, 0, image.getWidth(), image.getHeight());
				final Font labelFont = new Font("Arial", Font.BOLD, 16);
				g.setFont(labelFont); //$NON-NLS-1$
				g.drawString("DEBUG " + pos.toString(), 32, 32);
				g.dispose();
			}
			
			mapTexture = new MapTexture(image);
			if(debug) logger.fine("Updated texture for " + this);
		}
		return changed;
	}
	
	public boolean hasTexture() {
		return mapTexture!=null;
	}
	
	public MapTexture getTexture() {	
		return mapTexture;
	}
	
	public void clear() {
		if(mapTexture!=null) {
			mapTexture.clear();
		}
	}

	@Override
	public String toString() {
		return "Tile [ " + tileX + "," + tileZ + " (zoom " + zoom + ") ]";
	}

	@Override
	public int hashCode() {
		return toHashCode(tileX, tileZ, zoom, dimension);
	}
	
	public static int blockPosToTile(int b, int zoom) {
		int tile = b >> (9-zoom);  // (2 pow 9 = 512)
		return tile;
	}
	
	public static int blockPosToTileOffset(int b, int zoom) {
		double scale = Math.pow(2,9-zoom);
		double pos = new Double(b) / scale;
		double dec = pos - ((int) pos);
		int offset = (int) (dec * scale);
		if(b<0) offset = ((int)scale)+offset-1; // magic!
		return offset;
	}
	
	public static double blockPosToPixelOffset(int blockPos, int zoom) {
		final double pixelPerBlock = Math.pow(2, zoom);
		
		// (center of tile) - (blocks offset within tile * pixel size) + (center of block)
		return (Tiles.TILESIZE/2) -(Tile.blockPosToTileOffset(blockPos, zoom) * pixelPerBlock) + (32-pixelPerBlock)/2;
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
