package net.techbrew.mcjm.render.overlay;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import net.minecraft.src.Minecraft;
import net.minecraft.src.DynamicTexture;
import net.minecraft.src.ChunkCoordIntPair;
import net.techbrew.mcjm.Constants;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.Utils;
import net.techbrew.mcjm.io.RegionFileHandler;
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
	private Double maxImgDim;

	/**
	 * Constructor.
	 * @param startCoords
	 * @param entityChunkSize
	 * @param canvasWidth
	 * @param canvasHeight
	 */
	public OverlayMapRenderer(final ChunkCoordIntPair startCoords, final ChunkCoordIntPair endCoords, final int canvasWidth, final int canvasHeight, int layerWidth, int layerHeight) {
		super(startCoords, endCoords, canvasWidth, canvasHeight, layerHeight, layerHeight);
	}

	/**
	 * Render list of entities.
	 */
	@Override
	public void render(MapOverlayState state, Graphics2D unused) {

		try {
			
			if(mapImg==null || mapTexture==null) {
			
				Minecraft mc = Minecraft.getMinecraft();
				
				// Get the map image		
	
				final Constants.CoordType cType = Constants.CoordType.convert(state.getMapType(), mc.thePlayer.dimension);
	
				int size = getTextureSize();				
				BufferedImage tmpMapImg = RegionFileHandler.getMergedChunks(state.getWorldDir(), 
						startCoords.chunkXPos, startCoords.chunkZPos, 
						endCoords.chunkXPos, endCoords.chunkZPos, 
						state.getMapType(), mc.thePlayer.chunkCoordY, cType, true, state.getCurrentZoom(),
						size, size);
				
				eraseCachedImg();
				mapImg = tmpMapImg;
				mapTexture = new DynamicTexture(mapImg);

				int maxScreenSize = Math.max(state.getCanvasWidth(), state.getCanvasHeight());
				int textureSize = mapImg.getWidth();
				int maxBlocks = Utils.upperDistanceInBlocks(startCoords, endCoords);
				double pct = new Double(textureSize) / maxBlocks;
				maxImgDim = maxScreenSize * pct;
			}
			
			// Draw to screen
			draw(1f, state.getBlockXOffset(), state.getBlockZOffset());
			
		} catch(Throwable t) {
			JourneyMap.getLogger().severe("Error during render: " + LogFormatter.toString(t));
		}
	}
	
	public void draw(float opacity, double xOffset, double zOffset) {
		if(mapTexture!=null && maxImgDim!=null) {
			drawImage(mapTexture, opacity, xOffset, zOffset, maxImgDim, maxImgDim);
		}
	}
	
	public void eraseCachedImg() {
		mapImg = null;
		if(mapTexture!=null) {
			try {
				GL11.glDeleteTextures(mapTexture.func_110552_b());		
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
