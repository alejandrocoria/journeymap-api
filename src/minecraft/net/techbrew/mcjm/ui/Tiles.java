package net.techbrew.mcjm.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import net.minecraft.src.DynamicTexture;
import net.minecraft.src.Minecraft;
import net.minecraft.src.Tessellator;
import net.techbrew.mcjm.Constants.MapType;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.io.RegionImageHandler;

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
	
	public enum Compass {
		NorthWest(-1,-1),  North(0,-1),  NorthEast(1,-1), 
		West(-1,0),  Central(0,0),  East(1,0), 
		SouthWest(-1,1),  South(0,1),  SouthEast(1,1);		
		public final int deltaX;
		public final int deltaZ;
		private Compass(int dX, int dZ) {
			deltaX = dX;
			deltaZ = dZ;
		}		
	}	
			
	private final Logger logger = JourneyMap.getLogger();
	private final boolean debug = logger.isLoggable(Level.INFO);
	
	private final Map<Compass, Tile> tileSet = new HashMap<Compass, Tile>(13);
	
	private int centerBlockX;
	private int centerBlockZ;
	private int centerHash;
	private int zoom;	
	
	private final int dimension;
	private final File worldDir;
	
	DynamicTexture mapTexture;
	private BufferedImage allImage;

	public Tiles(final File worldDir, final int dimension) {
		this.worldDir = worldDir;
		this.dimension = dimension;
	}
	
	public boolean center(final int blockX, final int blockZ, final int zoom) {
		this.centerBlockX = blockX;
		this.centerBlockZ = blockZ;
		this.zoom = zoom;
		
		final int tileX = Tile.blockPosToTile(this.centerBlockX, this.zoom);
		final int tileZ = Tile.blockPosToTile(this.centerBlockZ, this.zoom);
		
		final int newCenterHash = Tile.toHashCode(tileX, tileZ, zoom, dimension);	
		if(newCenterHash==centerHash) {
			return false;
		} else {		
			centerHash = newCenterHash;
			Tile newCenterTile = findTile(tileX, tileZ, true);
			Map<Compass, Tile> tempTileSet = new HashMap<Compass, Tile>(13);
			for(Compass compass : Compass.values()) {
				tempTileSet.put(compass, findNeighbor(newCenterTile, compass));
			}		
			tileSet.putAll(tempTileSet);
			if(debug) logger.info("Centered on " + newCenterTile);
			return true;
		}
	}
	
	public boolean hasChanged(MapType mapType, Integer vSlice) {
		boolean changed = (allImage==null);
		if(allImage!=null) {
			for(Tile tile : tileSet.values()) {
				if(tile.markObsolete(mapType, vSlice)) {
					changed = true;
				}
			}
		}
		return changed;
	}
	
	public BufferedImage getImage(MapType mapType, Integer vSlice) {
		
		allImage = new BufferedImage(TILESIZE*3,TILESIZE*3, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = RegionImageHandler.initRenderingHints(allImage.createGraphics());
		
		int col = 0;
		int row = 0;
		final Font labelFont = new Font("Arial", Font.BOLD, 24);
		g.setFont(labelFont); //$NON-NLS-1$
		g.setPaint(Color.WHITE);

		for(Compass compass : Compass.values()) {
			Tile tile = tileSet.get(compass);
			BufferedImage tileImg = tile.getImage(mapType, vSlice, false);
			int x = col*TILESIZE;
			int y = row*TILESIZE;			
			g.drawImage(tileImg, x, y, null);
			g.drawRect(x, y, TILESIZE, TILESIZE);
			g.drawString(tile.toString(), x, y + 40);
			col++;
			if(col>2) {
				col=0;
				row++;
			}
		}
		g.dispose();
		
		try {
			clearTexture();
			mapTexture = new DynamicTexture(allImage);			
			ImageIO.write(allImage, "png", new File("G:\\tmp\\allImage.png"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return allImage;		
	}
	
	public void drawImage() {
		if(mapTexture!=null && allImage!=null) {
			draw(1f,0,0);
		}
	}
	
	public void draw(float opacity, double offsetX, double offsetZ) {
		final int SIZE = TILESIZE*3;
		
		Minecraft mc = Minecraft.getMinecraft();
		int displayOffsetX = -(SIZE - mc.displayWidth)/2;
		int displayOffsetZ = -(SIZE - mc.displayHeight)/2;
		
		int scale = (int) Math.pow(2, 5-zoom);
		int rx1 = centerBlockX % scale;
		int rx2 = centerBlockZ % scale;
		
		double pixelPerBlock = Math.pow(2, zoom);

		double adjustX = (256/pixelPerBlock) - ((centerBlockX % 512)  );
		double adjustZ = (256/pixelPerBlock) - ((centerBlockZ % 512)  );
	
		
		double startX = displayOffsetX + adjustX ;
		double startZ = displayOffsetZ + adjustZ ;
		
		System.out.println("rx " + rx1 + "," + rx2 + " // tile " + Tile.blockPosToTile(centerBlockX, zoom) +"," + Tile.blockPosToTile(centerBlockZ, zoom) + " center " + centerBlockX + "," + centerBlockZ + " at ppb " + pixelPerBlock + " zoom " + zoom);
		
		if(mapTexture!=null && allImage!=null) {			
			Tessellator tessellator = Tessellator.instance;					
			
			GL11.glDisable(2929 /*GL_DEPTH_TEST*/);
			GL11.glDepthMask(false);
			GL11.glBlendFunc(770, 771);
			GL11.glColor4f(opacity, opacity, opacity, opacity);
			GL11.glDisable(3008 /*GL_ALPHA_TEST*/);
			GL11.glBindTexture(3553 /*GL_TEXTURE_2D*/, mapTexture.getGlTextureId());

			tessellator.startDrawingQuads();

			tessellator.addVertexWithUV(startX, SIZE + startZ, 0.0D, 0, 1);
			tessellator.addVertexWithUV(startX + SIZE, SIZE + startZ, 0.0D, 1, 1);
			tessellator.addVertexWithUV(startX + SIZE, startZ, 0.0D, 1, 0);
			tessellator.addVertexWithUV(startX, startZ, 0.0D, 0, 0);
			tessellator.draw();
		}		
	}

	private Tile findNeighbor(Tile tile, Compass compass) {				
		if(compass==Compass.Central) return tile;
		return findTile(tile.tileX + compass.deltaX, tile.tileZ + compass.deltaZ, true);
	}
	
	private Tile findTile(final int tileX, final int tileZ, final boolean createIfMissing) {	
		final int hash = Tile.toHashCode(tileX, tileZ, zoom, dimension);
		for(Tile tile : tileSet.values()) {
			if(tile.hashCode()==hash) {
				if(debug) logger.info("Got existing " + tile);
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
		this.allImage = null;
		clearTexture();
		tileSet.clear();
	}
	
	public void clearTexture() {
		if(mapTexture!=null) {
			try {
				GL11.glDeleteTextures(mapTexture.getGlTextureId());		
			} catch(Throwable t) {
				logger.warning("Map image texture not deleted: " + t.getMessage());
			}
			mapTexture = null;
		}
	}
}
