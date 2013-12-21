package net.techbrew.mcjm.render.overlay;

import com.google.common.cache.Cache;
import net.minecraft.src.Minecraft;
import net.minecraft.src.Tessellator;
import net.techbrew.mcjm.Constants.MapType;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.model.BlockCoordIntPair;
import net.techbrew.mcjm.render.texture.TextureImpl;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contains a set of 9 tiles organized along compass points. 
 * Has basic logic to center on a tile and arrange neighboring tiles around it.
 * 
 * @author mwoodman
 *
 */
public class GridRenderer {
			
	private final Logger logger = JourneyMap.getLogger();
	private final boolean debug = logger.isLoggable(Level.FINE);
	
	private final TreeMap<TilePos, Integer> grid = new TreeMap<TilePos, Integer>();
	private final int gridSize; // 5 = 2560px.
	
	private int lastHeight =-1;
	private int lastWidth =-1;
	
	private int centerTileHash;
	private int zoom;
	private final BlockCoordIntPair centerBlock = new BlockCoordIntPair();
    private final Color bgColor = new Color(0x22, 0x22, 0x22);
	
	private final Point centerPixelOffset = new Point();
	
	private TextureImpl crosshairs;
	
	private Integer dimension;
	private File worldDir;

	public GridRenderer(final int gridSize) {
        this.gridSize = gridSize;  // Must be an odd number so as to have a center tile.
	}
	
	private void populateGrid(Tile centerTile) {
				
		final int endRow = (gridSize-1)/2;
		final int endCol = (gridSize-1)/2;
		final int startRow = -endRow;
		final int startCol = -endCol;
        Cache<Integer, Tile> tc = TileCache.instance();

		for(int z = startRow;z<=endRow;z++) {
			for(int x = startCol;x<=endCol;x++) {			
				TilePos pos = new TilePos(x,z);
                Tile tile = findNeighbor(centerTile, pos);
                grid.put(pos, tile.hashCode());
			}
		}

		//if(debug) logger.info("Grid cen done for cols " + startCol + " to " + endCol + " and rows " + startRow + " to " + endRow);
	}
	
	public void move(final int deltaBlockX, final int deltaBlockZ) {
		center(centerBlock.x + deltaBlockX, centerBlock.z + deltaBlockZ, zoom);
	}

	public boolean center() {
		return center(centerBlock.x, centerBlock.z, zoom);
	}

    public boolean hasTile(Tile tile) {
        return grid.containsValue(tile);
    }

	public boolean center(final int blockX, final int blockZ, final int zoom) {

        if(blockX==centerBlock.x && blockZ==centerBlock.z && zoom==this.zoom && !grid.isEmpty()){
            return false;
        }

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

			// Center on tile
			Tile newCenterTile = findTile(tileX, tileZ);
			populateGrid(newCenterTile);
			
			if(debug) logger.fine("Centered on " + newCenterTile + " with pixel offsets of " + centerPixelOffset.x + "," + centerPixelOffset.y);

			if(debug) {
				Minecraft mc = Minecraft.getMinecraft();
				BufferedImage tmp = new BufferedImage(mc.displayWidth, mc.displayHeight, BufferedImage.TYPE_INT_ARGB);
				Graphics2D g = tmp.createGraphics();
				g.setStroke(new BasicStroke(1));
				g.setColor(Color.GREEN);
				g.drawLine(mc.displayWidth/2, 0, mc.displayWidth/2, mc.displayHeight);
				g.drawLine(0, mc.displayHeight/2, mc.displayWidth, mc.displayHeight/2);
				if(crosshairs!=null) crosshairs.deleteTexture();
				crosshairs = new TextureImpl(tmp);
			}
		}
        return true;
	}

	public boolean updateTextures(MapType mapType, Integer vSlice, int width, int height, boolean fullUpdate, double xOffset, double yOffset) {

		// Update screen dimensions
		lastWidth = width;
		lastHeight = height;
		
		// Update pixel offsets for center
		final double srcSize = gridSize*Tile.TILESIZE;

		final int magic = (gridSize==5? 2 : 1) * Tile.TILESIZE; // TODO:  Understand why "2" as it relates to gridSize.  If gridSize is 3, this has to be "1".
		final double displayOffsetX = xOffset + magic-((srcSize - lastWidth)/2);
		final double displayOffsetY = yOffset + magic-((srcSize - lastHeight)/2);

        final Cache<Integer,Tile> tc = TileCache.instance();
		
		// Get center tile
        TilePos centerPos = new TilePos(0,0);
        Integer centerHash = grid.get(centerPos);
        if(centerHash==null){
            return false;
        }
		Tile centerTile = tc.getIfPresent(centerHash);
		if(centerTile==null) {
			return false;
		}
		
		Point blockPixelOffset = centerTile.blockPixelOffsetInTile(centerBlock.x, centerBlock.z);
		centerPixelOffset.setLocation(displayOffsetX + blockPixelOffset.x, displayOffsetY + blockPixelOffset.y);

        if(!fullUpdate) return false;

		boolean updated = false;

        TilePos pos;
        Tile tile;
        Integer hashCode;
        // Get tiles

        for(Map.Entry<TilePos,Integer> entry : grid.entrySet()) {
            pos = entry.getKey();
            hashCode = entry.getValue();
            tile = tc.getIfPresent(hashCode);

            // Update texture only if on-screen
            if(tile!=null) {
                if(fullUpdate) {
                    if(isOnScreen(pos)) {
                        if(tile.updateTexture(pos, mapType, vSlice)) {
                            updated=true;
                        }
                    }
                }
            }
        }


		return updated;
	}

    public Point getCenterPixelOffset() {
        return centerPixelOffset;
    }
	
	public Point getBlockPixelInGrid(int x, int z) {
		
		int localBlockX = x - centerBlock.x;
		int localBlockZ = z - centerBlock.z;
		
		int blockSize = (int) Math.pow(2,zoom);

		int pixelOffsetX = lastWidth /2 + (localBlockX*blockSize) ;
		int pixelOffsetZ = lastHeight /2 +(localBlockZ*blockSize) ;
		
		return new Point(pixelOffsetX, pixelOffsetZ);
	}

    /**
     * Draw a list of steps
     * @param drawStepList
     * @param xOffset
     * @param yOffset
     */
    public void draw(final List<BaseOverlayRenderer.DrawStep> drawStepList, int xOffset, int yOffset) {
        if(drawStepList==null || drawStepList.isEmpty()) return;
        draw(xOffset, yOffset, drawStepList.toArray(new BaseOverlayRenderer.DrawStep[drawStepList.size()]));
    }

    /**
     * Draw an array of steps
     * @param xOffset
     * @param yOffset
     * @param drawSteps
     */
    public void draw(int xOffset, int yOffset, BaseOverlayRenderer.DrawStep... drawSteps) {

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        for(BaseOverlayRenderer.DrawStep drawStep : drawSteps) {
            drawStep.draw(xOffset, yOffset, this);
        }

        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }
	
	public void draw(final float opacity, final double offsetX, final double offsetZ) {		
		if(!grid.isEmpty()) {	
								
			double centerX = offsetX + centerPixelOffset.x;
			double centerZ = offsetZ + centerPixelOffset.y;
            final Cache<Integer,Tile> tc = TileCache.instance();
					
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glDepthMask(false);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glColor4f(opacity, opacity, opacity, opacity);
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			
			for(Map.Entry<TilePos,Integer> entry : grid.entrySet()) {
				//if(entry.getKey().deltaX!=0 || entry.getKey().deltaZ!=0) continue;
                Tile tile = tc.getIfPresent(entry.getValue());
                if(tile!=null) {
				    drawTile(entry.getKey(), tile, centerX, centerZ);
                } else {
                    System.out.println("Grid tile missing at " + entry.getKey());
                }
			}

            if(debug && crosshairs!=null) {
                Minecraft mc = Minecraft.getMinecraft();
                GL11.glBindTexture(3553 /*GL_TEXTURE_2D*/, crosshairs.getGlTextureId());
                Tessellator tessellator = Tessellator.instance;
                tessellator.startDrawingQuads();
                tessellator.addVertexWithUV(0, mc.displayHeight, 0.0D, 0, 1);
                tessellator.addVertexWithUV(mc.displayWidth, mc.displayHeight, 0.0D, 1, 1);
                tessellator.addVertexWithUV(mc.displayWidth, 0, 0.0D, 1, 0);
                tessellator.addVertexWithUV(0, 0, 0.0D, 0, 0);
                tessellator.draw();
            }

            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glDepthMask(true);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
			

		}		
	}
	
	private void drawTile(final TilePos pos, final Tile tile, final double offsetX, final double offsetZ) {

        final double startX = offsetX + pos.startX;
        final double startZ = offsetZ + pos.startZ;
        final double endX = offsetX + pos.endX;
        final double endZ = offsetZ + pos.endZ;

        BaseOverlayRenderer.drawRectangle(startX, startZ, Tile.TILESIZE, Tile.TILESIZE, bgColor, 255);

		if(tile.hasTexture()) {
            // TODO: get this check working again
			//if(isOnScreen(startX, startZ, endX, endZ)) {
                GL11.glEnable(GL11.GL_TEXTURE_2D);
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, tile.getTexture().getGlTextureId());
				Tessellator tessellator = Tessellator.instance;
				tessellator.startDrawingQuads();			
				tessellator.addVertexWithUV(startX, endZ, 0.0D, 0, 1);
				tessellator.addVertexWithUV(endX, endZ, 0.0D, 1, 1);
				tessellator.addVertexWithUV(endX, startZ, 0.0D, 1, 0);
				tessellator.addVertexWithUV(startX, startZ, 0.0D, 0, 0);
				tessellator.draw();
			//}
		} else {
           if(debug) logger.fine("Tile has no texture: " + tile);
        }
	}
	
	private boolean isOnScreen(TilePos pos) {
		return isOnScreen(pos.startX + centerPixelOffset.x, pos.startZ + centerPixelOffset.y, pos.endX + centerPixelOffset.x, pos.endZ + centerPixelOffset.y);
	}

    /**
     * Returns a pixel point if on screen, null if not.
     * @param blockX pos x
     * @param blockZ pos z
     * @return  pixel
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
     * @param blockX x
     * @param blockZ z
     * @return  pixel
     */
	public Point getClosestOnscreenBlock(int blockX, int blockZ) {
		Point pixel = getBlockPixelInGrid(blockX, blockZ);
		if(pixel.x<0) {
			pixel.setLocation(0, pixel.y);
		} else if(pixel.x> lastWidth) {
			pixel.setLocation(lastWidth, pixel.y);
		}
		if(pixel.y<0) {
			pixel.setLocation(pixel.x, 0);
		} else if(pixel.y> lastHeight) {
			pixel.setLocation(pixel.x, lastHeight);
		}
		return pixel;
	}

    /**
     * Ensures a point is going to be visible.
     */
    public void ensureOnScreen(Point pixel) {
        if(pixel.x<0) {
            pixel.setLocation(0, pixel.y);
        } else if(pixel.x> lastWidth) {
            pixel.setLocation(lastWidth, pixel.y);
        }
        if(pixel.y<0) {
            pixel.setLocation(pixel.x, 0);
        } else if(pixel.y> lastHeight) {
            pixel.setLocation(pixel.x, lastHeight);
        }
    }
	
	/**
	 * This is a pixel check, not a location check
	 * @param point pixel
	 * @return true if on screen
	 */
	public boolean isOnScreen(Point point) {
		return point.x>0 && point.x< lastWidth && point.y>0 && point.y< lastHeight;
	}

    /**
     * This is a pixel check, not a location check
     * @param x screen x
     * @param y screen y
     * @return true if on screen
     */
	public boolean isOnScreen(double x, double y) {
		return x>0 && x< lastWidth && y>0 && y< lastHeight;
	}

    /**
     * This is a pixel check, not a location check
     * @param startX upper pixel x
     * @param startY upper pixel y
     * @param endX lower pixel x
     * @param endY lower pixel y
     * @return true if on screen
     */
	public boolean isOnScreen(double startX, double startY, double endX, double endY) {
		return endX>0 && startX< lastWidth && endY>0 && startY< lastHeight;
	}	

	private Tile findNeighbor(Tile tile, TilePos pos) {
		if(pos.deltaX==0 && pos.deltaZ==0) return tile;
		return findTile(tile.tileX + pos.deltaX, tile.tileZ + pos.deltaZ);
	}
	
	private Tile findTile(final int tileX, final int tileZ) {
		final int hash = Tile.toHashCode(tileX, tileZ, zoom, dimension);

        // Check cache first
        Tile tile = TileCache.instance().getIfPresent(hash);
        if(tile==null) {
            tile = new Tile(worldDir, tileX, tileZ, zoom, dimension);
            TileCache.instance().put(hash, tile);
            //logger.info("Created for cache:" + tile);
        } else {
            //logger.info("Reused from cache:" + tile);
        }
        return tile;
	}
	
	public void setContext(File worldDir, int dimension) {
        this.worldDir = worldDir;
        this.dimension = dimension;
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

	public boolean setZoom(int zoom) {
		return center(centerBlock.x, centerBlock.z, zoom);
	}

    public int getRenderSize() {
        return this.gridSize * Tile.TILESIZE;
    }

	public void clear() {
		grid.clear();
	}
}
