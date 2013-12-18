package net.techbrew.mcjm.render.overlay;

import net.minecraft.src.ChunkCoordIntPair;
import net.techbrew.mcjm.Constants.MapType;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.io.RegionImageHandler;
import net.techbrew.mcjm.log.StatTimer;
import net.techbrew.mcjm.render.texture.TextureCache;
import net.techbrew.mcjm.render.texture.TextureImpl;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Tile {
		
	public final static int TILESIZE = 512;
	
	final int dimension;
	final int zoom;
	final int tileX; 
	final int tileZ;
	final File worldDir;
	final ChunkCoordIntPair ulChunk;
	final ChunkCoordIntPair lrChunk;
	final Point ulBlock;
	final Point lrBlock;
	
	long lastImageTime = 0;
	
	Integer lastVSlice;
	MapType lastMapType;
	TextureImpl textureImpl;
	
	private final Logger logger = JourneyMap.getLogger();
	private final boolean debug = logger.isLoggable(Level.FINE);

	public Tile(final File worldDir, final int tileX, final int tileZ, final int zoom, final int dimension) {
		this.worldDir = worldDir;
		this.tileX = tileX;
		this.tileZ = tileZ;
		this.zoom = zoom;
		this.dimension = dimension;
		final int distance = 32 / (int) Math.pow(2, zoom);
		ulChunk = new ChunkCoordIntPair(tileX * distance, tileZ * distance);
		lrChunk = new ChunkCoordIntPair(ulChunk.chunkXPos + distance - 1, ulChunk.chunkZPos + distance - 1);
		ulBlock = new Point(ulChunk.chunkXPos*16, ulChunk.chunkZPos*16);
		lrBlock = new Point((lrChunk.chunkXPos*16)+15, (lrChunk.chunkZPos*16)+15);
	}
	
	public boolean updateTexture(final TilePos pos, final MapType mapType, final Integer vSlice) {
		boolean changed = (textureImpl==null || mapType!=lastMapType || vSlice!=lastVSlice);
		if(!changed) changed = RegionImageHandler.hasImageChanged(worldDir, ulChunk, lrChunk, mapType, vSlice, dimension, lastImageTime);
		
		if(changed) {
            StatTimer timer = StatTimer.get("Tile.updateTexture").start();
			BufferedImage image = RegionImageHandler.getMergedChunks(worldDir, ulChunk, lrChunk, mapType, vSlice, dimension, true, TILESIZE, TILESIZE);
			lastMapType = mapType;
			lastVSlice = vSlice;
			lastImageTime = new Date().getTime();

			if(debug) {		
				Graphics2D g = RegionImageHandler.initRenderingHints(image.createGraphics());				
				g.setPaint(Color.WHITE);
				g.setStroke(new BasicStroke(3));
				g.drawRect(0, 0, image.getWidth(), image.getHeight());
				final Font labelFont = new Font("Arial", Font.BOLD, 16);
				g.setFont(labelFont); //$NON-NLS-1$
				g.drawString(pos.toString() + " " + toString(), 16, 16);
				g.drawString(blockBounds(), 16, 32);
				g.dispose();
			}
			if(textureImpl==null) {
				textureImpl = TextureCache.newUnmanagedTexture(image);
			} else {
				textureImpl.updateTexture(image);
			}
            timer.stop();
			//if(debug) logger.info("Updated texture for " + this + " at " + mapType + ", vSlice " + vSlice);
		}
		return changed;
	}
	
	public boolean hasTexture() {
		return textureImpl!=null;
	}
	
	public TextureImpl getTexture() {	
		return textureImpl;
	}
	
	public void clear() {
		if(textureImpl!=null) {
			textureImpl.deleteTexture();
            textureImpl = null;
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
	
	private String blockBounds() {
		return ulBlock.x + "," + ulBlock.y + " - " + lrBlock.x + "," + lrBlock.y;
	}
	
	private int tileToBlock(int t) {
		return t << (9-zoom);
	}
	
	public static int tileToBlock(int t, int zoom) {
		return t << (9-zoom);
	}
	

	public Point blockPixelOffsetInTile(int x, int z) {
		
		if(x<ulBlock.x || x>lrBlock.x || z<ulBlock.y || z>lrBlock.y) {
			throw new RuntimeException("Block " + x + "," + z + " isn't in " + this);
		}
		
		int localBlockX = ulBlock.x - x;
		if(x<0) localBlockX++;
		
		int localBlockZ = ulBlock.y - z;
		if(z<0) localBlockZ++;
		
//		int tileCenterBlockX = lrBlock.x-ulBlock.x;
//		int tileCenterBlockZ = lrBlock.y-ulBlock.y;
		
		int blockSize = (int) Math.pow(2,zoom);
		int pixelOffsetX = (TILESIZE/2) + (localBlockX*blockSize) - (blockSize/2);
		int pixelOffsetZ = (TILESIZE/2) + (localBlockZ*blockSize) - (blockSize/2);
		
		return new Point(pixelOffsetX, pixelOffsetZ);
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

    @Override
    protected void finalize()  {

        try {
            if(JourneyMap.getInstance().isMapping()) {
                if(textureImpl!=null) {
                    logger.warning("Tile wasn't cleared before finalize() called: " + this);
                    clear();
                }
            }
        } catch(NullPointerException e){
            // Forced shutdown, JM instance already GC'd
        } catch(Throwable t){
            logger.severe(t.getMessage());
        }
    }
}
