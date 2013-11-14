package net.techbrew.mcjm.render.overlay;

import java.awt.Point;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.src.Minecraft;
import net.minecraft.src.Tessellator;
import net.techbrew.mcjm.Constants.MapType;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.model.BlockCoordIntPair;

import org.lwjgl.opengl.GL11;

/**
 * Contains a set of 9 tiles organized along compass points. 
 * Has basic logic to center on a tile and arrange neighboring tiles around it.
 * 
 * @author mwoodman
 *
 */
public class CoreRenderer {
			
	private final Logger logger = JourneyMap.getLogger();
	private final boolean debug = logger.isLoggable(Level.INFO);
	
	private final TreeMap<TilePos, Tile> grid = new TreeMap<TilePos, Tile>();
	private final int gridSize = 5; // 2560px.  Must be an odd number so as to have a center tile.
	
	private int lastMcDisplayHeight=-1;
	private int lastMcDisplayWidth=-1;
	
	private int centerTileHash;
	private int zoom;	
	private final BlockCoordIntPair ulBlock = new BlockCoordIntPair();
	private final BlockCoordIntPair centerBlock = new BlockCoordIntPair();
	private final BlockCoordIntPair lrBlock = new BlockCoordIntPair();
	
	private final Point centerPixelOffset = new Point();
	
	//private MapTexture crosshairs;
	
	private final int dimension;
	private final File worldDir;		

	public CoreRenderer(final File worldDir, final int dimension) {
		this.worldDir = worldDir;
		this.dimension = dimension;
	}
	
	private void populateGrid(Tile centerTile, Set<Tile> existingTiles) {
				
		final int endRow = (gridSize-1)/2;
		final int endCol = (gridSize-1)/2;
		final int startRow = -endRow;
		final int startCol = -endCol;
		
		ulBlock.setLocation(Tile.tileToBlock(startCol, zoom), Tile.tileToBlock(startRow, zoom));
		lrBlock.setLocation(Tile.tileToBlock(endCol, zoom), Tile.tileToBlock(endRow, zoom));
		
		Set<Tile> removedTiles = new HashSet<Tile>(grid.size());
		
		for(int z = startRow;z<=endRow;z++) {
			for(int x = startCol;x<=endCol;x++) {			
				TilePos pos = new TilePos(x,z);
				Tile tile = findNeighbor(centerTile, pos, existingTiles);
				Tile oldTile = grid.put(pos, tile);
				if(oldTile!=null) removedTiles.add(oldTile);
				//if(debug) logger.info("Grid pos added: " + pos);				
			}
		}
		
		for(Tile oldTile : removedTiles) {
			if(!grid.containsValue(oldTile)) {
				oldTile.clear();
				//if(debug) logger.info("Obsolete tile cleared: " + oldTile);
			}
		}
		removedTiles.clear();

		//if(debug) logger.info("Grid cen done for cols " + startCol + " to " + endCol + " and rows " + startRow + " to " + endRow);
	}
	
	public void move(final int deltaBlockX, final int deltaBlockZ) {
		center(centerBlock.x + deltaBlockX, centerBlock.z + deltaBlockZ, zoom);
	}
	
	public boolean center() {
		return center(0,0,zoom);
	}
	
	public boolean center(final int blockX, final int blockZ, final int zoom) {
		
		centerBlock.setLocation(blockX, blockZ);
		this.zoom = zoom;
		
		// Get zoomed tile coords
		final int tileX = Tile.blockPosToTile(centerBlock.x, this.zoom);
		final int tileZ = Tile.blockPosToTile(centerBlock.z, this.zoom);			
		
		// Chech hash of tile coords
		final int newCenterHash = Tile.toHashCode(tileX, tileZ, zoom, dimension);
		final boolean centerTileChanged = newCenterHash!=centerTileHash;
		centerTileHash = newCenterHash;

		if(centerTileChanged || grid.isEmpty()) {	
			// Pull current tiles for reuse
			final Set<Tile> currentTiles = new HashSet<Tile>(grid.values());		
			
			// Center on tile
			Tile newCenterTile = findTile(tileX, tileZ, currentTiles, true);
			populateGrid(newCenterTile, currentTiles);
			
//			if(debug) logger.info("Centered on " + newCenterTile + " with pixel offsets of " + centerPixelOffset.x + "," + centerPixelOffset.y);
//			
//			if(debug) {
//				Minecraft mc = Minecraft.getMinecraft();
//				BufferedImage tmp = new BufferedImage(mc.displayWidth, mc.displayHeight, BufferedImage.TYPE_INT_ARGB);
//				Graphics2D g = tmp.createGraphics();
//				g.setStroke(new BasicStroke(1));
//				g.setColor(Color.GREEN);
//				g.drawLine(mc.displayWidth/2, 0, mc.displayWidth/2, mc.displayHeight);
//				g.drawLine(0, mc.displayHeight/2, mc.displayWidth, mc.displayHeight/2);
//				if(crosshairs!=null) crosshairs.clear();
//				crosshairs = new MapTexture(tmp);
//			}
			
			return true;
		} else {
			return false;
		}
	}
	
	public boolean updateTextures(MapType mapType, Integer vSlice) {
		
		// Update screen dimensions
		Minecraft mc = Minecraft.getMinecraft();
		lastMcDisplayWidth = mc.displayWidth;
		lastMcDisplayHeight = mc.displayHeight;
		
		// Update pixel offsets for center
		final double srcSize = gridSize*Tile.TILESIZE;
		final int magic = (2*Tile.TILESIZE); // TODO:  Understand why "2" as it relates to gridSize.  If gridSize is 3, this has to be "1".
		final double displayOffsetX = magic-((srcSize - lastMcDisplayWidth)/2);
		final double displayOffsetY = magic-((srcSize - lastMcDisplayHeight)/2);
		
		// Get center tile
		Tile centerTile = grid.get(new TilePos(0,0));	
		if(centerTile==null) {
			return false;
		}
		
		Point blockPixelOffset = centerTile.blockPixelOffsetInTile(centerBlock.x, centerBlock.z);
		centerPixelOffset.setLocation(displayOffsetX + blockPixelOffset.x, displayOffsetY + blockPixelOffset.y);
				
		// Update textures only if on-screen
		boolean updated = false;
		TilePos pos;
		Tile tile;
		for(Map.Entry<TilePos,Tile> entry : grid.entrySet()) {
			pos = entry.getKey();
			tile = entry.getValue();
			if(isOnScreen(pos)) {
				if(tile.updateTexture(pos, mapType, vSlice)) {
					updated=true;
				}
			} else {
				//if(debug) logger.info("Skipped updating offscreen tile: " + pos);
			}
		}
		return updated;
	}
	
	public Point getBlockPixelInGrid(int x, int z) {
		
		int localBlockX = x - centerBlock.x;
		int localBlockZ = z - centerBlock.z;
		
		int blockSize = (int) Math.pow(2,zoom);

		int pixelOffsetX = lastMcDisplayWidth/2 + (localBlockX*blockSize) ;
		int pixelOffsetZ = lastMcDisplayHeight/2 +(localBlockZ*blockSize) ;
		
		return new Point(pixelOffsetX, pixelOffsetZ);
	}				
	
	public void draw(final float opacity, final double offsetX, final double offsetZ) {		
		if(!grid.isEmpty()) {	
								
			double centerX = offsetX + centerPixelOffset.x;
			double centerZ = offsetZ + centerPixelOffset.y;		
					
			GL11.glDisable(2929 /*GL_DEPTH_TEST*/);
			GL11.glDepthMask(false);
			GL11.glBlendFunc(770, 771);
			GL11.glColor4f(opacity, opacity, opacity, opacity);
			GL11.glDisable(3008 /*GL_ALPHA_TEST*/);
			
			for(Map.Entry<TilePos,Tile> entry : grid.entrySet()) {
				//if(entry.getKey().deltaX!=0 || entry.getKey().deltaZ!=0) continue;
				drawTile(entry.getKey(), entry.getValue(), centerX, centerZ);
			}
			
//			if(debug && crosshairs!=null) {
//				Minecraft mc = Minecraft.getMinecraft();
//				GL11.glBindTexture(3553 /*GL_TEXTURE_2D*/, crosshairs.getGlTextureId());	
//				Tessellator tessellator = Tessellator.instance;
//				tessellator.startDrawingQuads();			
//				tessellator.addVertexWithUV(0, mc.displayHeight, 0.0D, 0, 1);
//				tessellator.addVertexWithUV(mc.displayWidth, mc.displayHeight, 0.0D, 1, 1);
//				tessellator.addVertexWithUV(mc.displayWidth, 0, 0.0D, 1, 0);
//				tessellator.addVertexWithUV(0, 0, 0.0D, 0, 0);
//				tessellator.draw();
//			}
		}		
	}
	
	private void drawTile(final TilePos pos, final Tile tile, final double offsetX, final double offsetZ) {
				
		if(tile.hasTexture()) {
		
			final double startX = offsetX + pos.startX;
			final double startZ = offsetZ + pos.startZ;		
			final double endX = offsetX + pos.endX;
			final double endZ = offsetZ + pos.endZ;	
			
			if(isOnScreen(startX, startZ, endX, endZ)) {								
				GL11.glBindTexture(3553 /*GL_TEXTURE_2D*/, tile.getTexture().getGlTextureId());	
				Tessellator tessellator = Tessellator.instance;
				tessellator.startDrawingQuads();			
				tessellator.addVertexWithUV(startX, endZ, 0.0D, 0, 1);
				tessellator.addVertexWithUV(endX, endZ, 0.0D, 1, 1);
				tessellator.addVertexWithUV(endX, startZ, 0.0D, 1, 0);
				tessellator.addVertexWithUV(startX, startZ, 0.0D, 0, 0);
				tessellator.draw();
			} else {
				//if(debug) logger.info("Skipped offscreen tile: " + pos);
			}
		} else {
			//if(debug) logger.info("Skipped tile with no texture: " + pos);
		}		
	}
	
	private boolean isOnScreen(TilePos pos) {
		return isOnScreen(pos.startX + centerPixelOffset.x, pos.startZ + centerPixelOffset.y, pos.endX + centerPixelOffset.x, pos.endZ + centerPixelOffset.y);
	}
	
	/**
	 * Returns a pixel point if on screen, null if not.
	 * @param blockX
	 * @param blockZ
	 * @param radius
	 * @return
	 */
	public Point getPixel(int blockX, int blockZ) {
		Point pixel = getBlockPixelInGrid(blockX, blockZ);
		if(isOnScreen(pixel)) {
			return pixel;
		} else {
			return null;
		}
	}
	
	/**
	 * Returns a pixel point if on screen, the closest one there is if not.
	 * @param blockX
	 * @param blockZ
	 * @param radius
	 * @return
	 */
	public Point getClosestOnscreenPixel(int blockX, int blockZ) {
		Point pixel = getBlockPixelInGrid(blockX, blockZ);
		if(pixel.x<0) {
			pixel.setLocation(0, pixel.y);
		} else if(pixel.x>lastMcDisplayWidth) {
			pixel.setLocation(lastMcDisplayWidth, pixel.y);
		}
		if(pixel.y<0) {
			pixel.setLocation(pixel.x, 0);
		} else if(pixel.y>lastMcDisplayHeight) {
			pixel.setLocation(pixel.x, lastMcDisplayHeight);
		}
		return pixel;
	}
	
	/**
	 * This is a pixel check, not a location check
	 * @param point
	 * @return
	 */
	public boolean isOnScreen(Point point) {
		return point.x>0 && point.x<lastMcDisplayWidth && point.y>0 && point.y<lastMcDisplayHeight;
	}
	
	/**
	 * This is a pixel check, not a location check
	 * @param startX
	 * @param startZ
	 * @param endX
	 * @param endZ
	 * @return
	 */
	public boolean isOnScreen(double x, double z) {
		return x>0 && x<lastMcDisplayWidth && z>0 && z<lastMcDisplayHeight;
	}
	
	/**
	 * This is a pixel check, not a location check
	 * @param startX
	 * @param startZ
	 * @param endX
	 * @param endZ
	 * @return
	 */
	public boolean isOnScreen(double startX, double startZ, double endX, double endZ) {
		return endX>0 && startX<lastMcDisplayWidth && endZ>0 && startZ<lastMcDisplayHeight;
	}	

	private Tile findNeighbor(Tile tile, TilePos pos, Set<Tile> tiles) {				
		if(pos.deltaX==0 && pos.deltaZ==0) return tile;
		return findTile(tile.tileX + pos.deltaX, tile.tileZ + pos.deltaZ, tiles, true);
	}
	
	private Tile findTile(final int tileX, final int tileZ, Collection<Tile> tiles, final boolean createIfMissing) {	
		final int hash = Tile.toHashCode(tileX, tileZ, zoom, dimension);
		for(Tile tile : tiles) {
			if(tile!=null && tile.hashCode()==hash) {
				//if(debug) logger.info("Got existing " + tile);
				return tile;
			}
		}
		if(createIfMissing) {
			Tile tile = new Tile(worldDir, tileX, tileZ, zoom, dimension);
			//if(debug) logger.info("Created " + tile);
			return tile;
		} else {
			return null;
		}
	}
	
	public boolean isUsing(File worldDir, int dimension) {
		return this.dimension == dimension && this.worldDir.equals(worldDir);
	}
	
	public File getWorldDir() {
		return worldDir;
	}
	
	public int getDimension() {
		return dimension;
	}
	
	public int getZoom() {
		return zoom;
	}

	public void setZoom(int zoom) {
		if(zoom!=this.zoom) {
			center(centerBlock.x, centerBlock.z, zoom);
		}
	}

	public void clear() {
		for(Tile tile : grid.values()) {
			tile.clear();
		}
		grid.clear();
	}
}
