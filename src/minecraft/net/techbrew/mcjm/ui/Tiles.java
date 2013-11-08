package net.techbrew.mcjm.ui;

import java.io.File;
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

import org.lwjgl.opengl.GL11;

/**
 * Contains a set of 9 tiles organized along compass points. 
 * Has basic logic to center on a tile and arrange neighboring tiles around it.
 * 
 * @author mwoodman
 *
 */
public class Tiles {
	
	final static int TILESIZE = 512;
			
	private final Logger logger = JourneyMap.getLogger();
	private final boolean debug = logger.isLoggable(Level.INFO);
	
	private final TreeMap<TilePos, Tile> grid = new TreeMap<TilePos, Tile>();
	
	private int lastMcDisplayHeight=-1;
	private int lastMcDisplayWidth=-1;
	
	private int displayRows=0;
	private int displayCols=0;
	
	private int centerBlockX;
	private int centerBlockZ;
	private int centerTileHash;
	private int zoom;	
	
	private double centerPixelOffsetX;
	private double centerPixelOffsetZ;
	
	private final int dimension;
	private final File worldDir;		

	public Tiles(final File worldDir, final int dimension) {
		this.worldDir = worldDir;
		this.dimension = dimension;
	}
	
	private int[] getLayout() {		
		Minecraft mc = Minecraft.getMinecraft();
		if(mc.displayHeight==lastMcDisplayHeight && mc.displayWidth==lastMcDisplayWidth) {
			if(displayCols>=3 && displayRows>=3) {
				//if(debug) logger.info("Screensize hasn't changed. Cols,Rows = " + displayCols + "," + displayRows);
				return new int[]{displayCols, displayRows};
			}
		}
		lastMcDisplayHeight = mc.displayHeight;
		lastMcDisplayWidth = mc.displayWidth;
		
		final int height = mc.displayHeight + 512;
		int rows = (int) Math.ceil(height/512);
		if(rows%2==0) rows++;
		if(rows<3) rows = 3;
		
		final int width = mc.displayWidth + 512;
		int cols = (int) Math.ceil(width/512);
		if(cols%2==0) cols++;
		if(cols<3) cols = 3;
		
		if(rows==displayRows && cols==displayCols) {	
			if(debug) logger.info("Layout hasn't changed after resize. Cols,Rows = " + displayCols + "," + displayRows);
			return new int[]{displayCols, displayRows};
		}
		
		if(debug) logger.info("Layout needs to change. Cols,Rows = " + cols + "," + rows);
		return new int[]{cols, rows};
	}
	
	private void populateGrid(Tile centerTile, Set<Tile> existingTiles) {
				
		final int endRow = (displayRows-1)/2;
		final int endCol = (displayCols-1)/2;
		final int startRow = -endRow;
		final int startCol = -endCol;				

		HashSet<Tile> oldGrid = new HashSet<Tile>(grid.values());
		grid.clear();
		
		for(int z = startRow;z<=endRow;z++) {
			for(int x = startCol;x<=endCol;x++) {			
				TilePos pos = new TilePos(x,z);
				Tile tile = findNeighbor(centerTile, pos, existingTiles);
				grid.put(pos, tile);
				oldGrid.remove(tile);
				//if(debug) logger.info("Grid pos added: " + pos);				
			}
		}
		
		for(Tile oldTile : oldGrid) {
			oldTile.clear();
			//if(debug) logger.info("Old tile cleared: " + oldTile);
		}
		
		//if(debug) logger.info("Layout done for cols " + startCol + " to " + endCol + " and rows " + startRow + " to " + endRow);
	}
	
	public boolean center(final int blockX, final int blockZ, final int zoom) {
		
		this.centerBlockX = blockX;
		this.centerBlockZ = blockZ;
		this.zoom = zoom;
		
		final int tileX = Tile.blockPosToTile(this.centerBlockX, this.zoom);
		final int tileZ = Tile.blockPosToTile(this.centerBlockZ, this.zoom);
			
		final int[] colsRows = getLayout();
		final boolean layoutChanged = colsRows[0]!=displayCols || colsRows[1]!=displayRows;
		displayCols = colsRows[0];
		displayRows = colsRows[1];
		
		final int newCenterHash = Tile.toHashCode(tileX, tileZ, zoom, dimension);
		final boolean centerChanged = newCenterHash!=centerTileHash;
		centerTileHash = newCenterHash;
		
		final double srcWidth = displayCols*TILESIZE;
		final double srcHeight = displayRows*TILESIZE;
		final double mysteryX = displayCols==5 ? 2 : displayCols==3 ? 1 : 0; // TODO figure out a formula
		final double mysteryZ = displayRows==5 ? 2 : displayRows==3 ? 1 : 0; // TODO figure out a formula
		final double displayOffsetX = (mysteryX*TILESIZE)-((srcWidth - lastMcDisplayWidth)/2);
		final double displayOffsetZ = (mysteryZ*TILESIZE)-((srcHeight - lastMcDisplayHeight)/2);
		centerPixelOffsetX = displayOffsetX + Tile.blockPosToPixelOffset(centerBlockX, zoom);
		centerPixelOffsetZ = displayOffsetZ + Tile.blockPosToPixelOffset(centerBlockZ, zoom);		
		
		if(!layoutChanged && !centerChanged && !grid.isEmpty()) {
			//if(debug) logger.info("No change based on center");
			return false;
		}
		
		// Pull current tiles for reuse
		final Set<Tile> currentTiles = new HashSet<Tile>(grid.values());		
		
		// Center on tile
		Tile newCenterTile = findTile(tileX, tileZ, currentTiles, true);
		populateGrid(newCenterTile, currentTiles);
		
		if(debug) logger.info("Centered on " + centerPixelOffsetX + "," + centerPixelOffsetZ + " using " + newCenterTile + " with " + displayCols + " cols and " + displayRows + " rows");
		return true;
	}
	
	public boolean updateTexture(MapType mapType, Integer vSlice) {		
		boolean updated = false;
		for(Map.Entry<TilePos,Tile> entry : grid.entrySet()) {
			if(entry.getValue().updateTexture(entry.getKey(), mapType, vSlice)) {
				updated=true;
			}
		}
		return updated;
	}
					
	public void drawImage() {
		draw(1f,0,0);
	}
	
	public void draw(final float opacity, final double offsetX, final double offsetZ) {		
		if(!grid.isEmpty()) {	
								
			double centerX = offsetX + centerPixelOffsetX;
			double centerZ = offsetZ + centerPixelOffsetZ;		
					
			GL11.glDisable(2929 /*GL_DEPTH_TEST*/);
			GL11.glDepthMask(false);
			GL11.glBlendFunc(770, 771);
			GL11.glColor4f(opacity, opacity, opacity, opacity);
			GL11.glDisable(3008 /*GL_ALPHA_TEST*/);
			
			for(Map.Entry<TilePos,Tile> entry : grid.entrySet()) {
				//if(entry.getKey().deltaX!=0 || entry.getKey().deltaZ!=0) continue;
				drawTile(entry.getKey(), entry.getValue(), centerX, centerZ);
			}
		}		
	}
	
	private void drawTile(final TilePos pos, final Tile tile, final double offsetX, final double offsetZ) {
		
		if(tile.hasTexture()) {
		
			final double startX = offsetX + (pos.deltaX*512);
			final double startZ = offsetZ + (pos.deltaZ*512);
								
			GL11.glBindTexture(3553 /*GL_TEXTURE_2D*/, tile.getTexture().getGlTextureId());

			Tessellator tessellator = Tessellator.instance;
			tessellator.startDrawingQuads();			
			tessellator.addVertexWithUV(startX, TILESIZE + startZ, 0.0D, 0, 1);
			tessellator.addVertexWithUV(startX + TILESIZE, TILESIZE + startZ, 0.0D, 1, 1);
			tessellator.addVertexWithUV(startX + TILESIZE, startZ, 0.0D, 1, 0);
			tessellator.addVertexWithUV(startX, startZ, 0.0D, 0, 0);
			tessellator.draw();
		}		
	}
	

	private Tile findNeighbor(Tile tile, TilePos pos, Set<Tile> tiles) {				
		if(pos.deltaX==0 && pos.deltaZ==0) return tile;
		return findTile(tile.tileX + pos.deltaX, tile.tileZ + pos.deltaZ, tiles, true);
	}
	
	private Tile findTile(final int tileX, final int tileZ, Set<Tile> tiles, final boolean createIfMissing) {	
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
			center(centerBlockX, centerBlockZ, zoom);
		}
	}

	public void clear() {
		for(Tile tile : grid.values()) {
			tile.clear();
		}
		grid.clear();
	}
	
	final class TilePos implements Comparable<TilePos> {		
		public final int deltaX;
		public final int deltaZ;
		
		TilePos(int deltaX, int deltaZ) {
			this.deltaX = deltaX;
			this.deltaZ = deltaZ;
		}

		@Override
		public int hashCode() {
			final int prime = 37;
			int result = 1;
			result = prime * result + deltaX;
			result = prime * result + deltaZ;
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
			TilePos other = (TilePos) obj;
			if (deltaX != other.deltaX)
				return false;
			if (deltaZ != other.deltaZ)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "TilePos [" + deltaX + "," + deltaZ + "]";
		}

		@Override
		public int compareTo(TilePos o) {
			int result = new Integer(deltaZ).compareTo(o.deltaZ);
			if(result==0) {
				result = new Integer(deltaX).compareTo(o.deltaX);
			}
			return result;
		}		
				
	}
}
