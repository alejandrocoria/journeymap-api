package net.techbrew.mcjm.render.overlay;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import net.minecraft.src.ChunkCoordIntPair;
import net.minecraft.src.DynamicTexture;
import net.minecraft.src.Minecraft;
import net.techbrew.mcjm.Constants.MapType;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.Utils;
import net.techbrew.mcjm.io.RegionImageHandler;
import net.techbrew.mcjm.log.LogFormatter;
import net.techbrew.mcjm.model.MapOverlayState;

import org.lwjgl.opengl.GL11;

/**
 * Renders an entity image in the MapOverlay.
 * 
 * @author mwoodman
 *
 */
public class OverlayMapRenderer extends BaseOverlayRenderer<MapOverlayState> {
	
	private BufferedImage mapImg;
	private DynamicTexture mapTexture;
	private Double imgScale;

	/**
	 * Constructor.
	 * @param startCoords
	 * @param entityChunkSize
	 * @param canvasWidth
	 * @param canvasHeight
	 */
	public OverlayMapRenderer(final ChunkCoordIntPair startCoords, final ChunkCoordIntPair endCoords, final int canvasWidth, final int canvasHeight, int layerWidth, int layerHeight) {
		super(startCoords, endCoords, canvasWidth, canvasHeight, layerWidth, layerHeight);
	}

	/**
	 * Render map tiles
	 */
	@Override
	public void render(MapOverlayState state, Graphics2D unused) {

		try {
			
			if(mapImg==null || mapTexture==null) {
			
				Minecraft mc = Minecraft.getMinecraft();
				
				// Get the map image		
	
				final int dimension = mc.thePlayer.dimension;
	
				final int imgWidth = (endCoords.chunkXPos - startCoords.chunkXPos+1) * 16;
				final int imgHeight = (endCoords.chunkZPos - startCoords.chunkZPos+1) * 16;		
								
				double maxScreenSize = Math.max(state.getCanvasWidth(), state.getCanvasHeight());
				double textureSize = Math.max(imgWidth, imgHeight);
				imgScale = maxScreenSize / textureSize;
				
				//width = (new Double(Math.ceil(width *imgScale)).intValue() >> 4) * 16;
				//height = (new Double(Math.ceil(height *imgScale)).intValue() >> 4) * 16;
				
				Integer vSlice = (state.getMapType()==MapType.underground) ? mc.thePlayer.chunkCoordY : null;
				BufferedImage tmpMapImg = RegionImageHandler.getMergedChunks(state.getWorldDir(), 
						startCoords, endCoords, 
						state.getMapType(), vSlice, dimension, true, 
						imgWidth, 
						imgHeight);
				
				ImageIO.write(tmpMapImg, "png", new File("G:\\tmp\\map.png"));
				
				eraseCachedImg();
				mapImg = tmpMapImg;
				mapTexture = new DynamicTexture(mapImg);

			}
			
			// Draw to screen
			draw(1f, state.getBlockXOffset(), state.getBlockZOffset());
			
		} catch(Throwable t) {
			JourneyMap.getLogger().severe("Error during render: " + LogFormatter.toString(t));
		}
	}
	
	public void draw(float opacity, double xOffset, double zOffset) {
		if(mapTexture!=null && imgScale!=null) {
			drawImage(mapTexture, opacity, xOffset, zOffset, mapImg.getWidth(), mapImg.getHeight());
		}
	}
	
	public void eraseCachedImg() {
		mapImg = null;
		if(mapTexture!=null) {
			try {
				GL11.glDeleteTextures(mapTexture.getGlTextureId());		
			} catch(Throwable t) {
				JourneyMap.getLogger().warning("Map image texture not deleted: " + t.getMessage());
				t.printStackTrace();
			}
			mapTexture = null;
		}
	}
	
	public int getTextureSize() {
		int width = Math.max(16, (endCoords.chunkXPos - startCoords.chunkXPos) * 16);
		int height = Math.max(16, (endCoords.chunkZPos - startCoords.chunkZPos) * 16);
		return Utils.upperPowerOfTwo(Math.max(width, height), MAX_TEXTURE_SIZE);
		//return MAX_TEXTURE_SIZE;
	}

	public void setLayerDimensions(int layerWidth, int layerHeight) {
		this.layerWidth = layerWidth;
		this.layerHeight = layerHeight;		
	}


}
