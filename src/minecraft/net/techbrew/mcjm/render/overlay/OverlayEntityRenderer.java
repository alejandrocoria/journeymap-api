package net.techbrew.mcjm.render.overlay;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.src.ChunkCoordIntPair;
import net.minecraft.src.Tessellator;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.Utils;
import net.techbrew.mcjm.data.AnimalsData;
import net.techbrew.mcjm.data.DataCache;
import net.techbrew.mcjm.data.EntityKey;
import net.techbrew.mcjm.data.MobsData;
import net.techbrew.mcjm.data.PlayersData;
import net.techbrew.mcjm.data.VillagersData;
import net.techbrew.mcjm.io.RegionFileHandler;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.model.EntityHelper;
import net.techbrew.mcjm.model.MapOverlayState;
import net.techbrew.mcjm.model.Waypoint;
import net.techbrew.mcjm.render.MapBlocks;

/**
 * Renders an entity image in the MapOverlay.
 * 
 * @author mwoodman
 *
 */
public class OverlayEntityRenderer extends BaseOverlayRenderer<MapOverlayState> {
	
	private BufferedImage entityImage;
	private Graphics2D g2D;
	private Integer textureIndex;
	private Double maxImgDim;	

	int layerWidth;
	int layerHeight;

	/**
	 * Constructor.
	 * @param startCoords
	 * @param entityChunkSize
	 * @param canvasWidth
	 * @param canvasHeight
	 */
	public OverlayEntityRenderer(final ChunkCoordIntPair startCoords, final ChunkCoordIntPair endCoords, final int canvasWidth, final int canvasHeight, int layerWidth, int layerHeight) {
		super(startCoords, endCoords, canvasWidth, canvasHeight);
		this.layerWidth = layerWidth;
		this.layerHeight = layerHeight;
		init();
	}
	
	private void init() {
		int textureSize = getTextureSize();
		maxImgDim = new Double(textureSize);
		entityImage = new BufferedImage(textureSize, textureSize, BufferedImage.TYPE_INT_ARGB);
		g2D = entityImage.createGraphics();			
		g2D.setFont(new Font("Arial", Font.BOLD, 16)); //$NON-NLS-1$
		g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	
		int maxBlocks = Utils.upperDistanceInBlocks(startCoords, endCoords);
		blockSize = maxImgDim/maxBlocks; 		
	}
	
	public Graphics2D getGraphics() {
		return g2D;
	}

	/**
	 * Render list of entities.
	 */
	@Override
	public void render(MapOverlayState state, Graphics2D unused) {

		try {
			
			if(textureIndex==null && entityImage!=null) {
								
//				int maxScreenSize = Math.max(canvasWidth, canvasHeight);
//				double scale = maxImgDim/maxScreenSize;
//				int texSize = getTextureSize();
//				
//				BufferedImage after = new BufferedImage(entityImage.getWidth(), entityImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
//				AffineTransform at = new AffineTransform();
//				//at.scale(scale, scale);
//				AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
//				after = scaleOp.filter(entityImage, after);
//				
//				entityImage = after;
				
				// Allocate the new map image as a texture
				textureIndex = Minecraft.getMinecraft().renderEngine.allocateAndSetupTexture((BufferedImage) entityImage);				
			}
			
			// Draw to screen
			draw(1f, 0, 0);
			
		} catch(Throwable t) {
			JourneyMap.getLogger().severe("Error during render: " + LogFormatter.toString(t));
		}
	}
	
	public void draw(float opacity, double xOffset, double zOffset) {
		if(textureIndex!=null && maxImgDim!=null) {
			
			int maxScreenSize = Math.max(canvasWidth, canvasHeight);
			double scale = maxScreenSize/maxImgDim;
			
			GL11.glPushMatrix();
			GL11.glScaled(scale, scale, scale);
			drawImage(textureIndex, opacity, xOffset, zOffset, entityImage.getWidth(), entityImage.getHeight());
			GL11.glPopMatrix();
		}
	}
	
	public void eraseCachedImg() {
		entityImage = null;
		if(textureIndex!=null) {
			try {
				Minecraft.getMinecraft().renderEngine.deleteTexture(textureIndex);
				textureIndex = null;
			} catch(Throwable t) {
				JourneyMap.getLogger().warning("Map image texture not deleted: " + t.getMessage());
			}
		}
	}
	
	public int getTextureSize() {	
		
		return Utils.upperPowerOfTwo(Math.max(canvasHeight*2, canvasWidth*2), 2048);
		//return MAX_TEXTURE_SIZE*2;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		eraseCachedImg();
	}
	

}
