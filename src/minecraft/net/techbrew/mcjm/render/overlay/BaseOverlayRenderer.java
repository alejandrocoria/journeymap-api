package net.techbrew.mcjm.render.overlay;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.ResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.world.ChunkCoordIntPair;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.data.EntityKey;
import net.techbrew.mcjm.log.LogFormatter;

import org.lwjgl.opengl.GL11;

public abstract class BaseOverlayRenderer<K> {
	
	public static AlphaComposite SLIGHTLYOPAQUE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1F);
	public static AlphaComposite OPAQUE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1F);
	public static AlphaComposite CLEAR = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0F);
	public static AlphaComposite SEMICLEAR = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5F);
	public static AlphaComposite SLIGHTLYCLEAR = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8F);
	
	public static int MAX_TEXTURE_SIZE = 1024;
	
	final ChunkCoordIntPair startCoords;
	final ChunkCoordIntPair endCoords;

	final int canvasWidth;
	final int canvasHeight;
	
	int layerWidth;
	int layerHeight;
	
	Double blockSize;

	
	/**
	 * Constructor.
	 * @param startCoords		Chunk coords for upper left of screen
	 * @param canvasWidth		Width of the map canvas
	 * @param canvasHeight		Height of the map canvas
	 * @param widthCutoff		Right-hand margin of canvas that is off-screen
	 * @param heightCutoff		Bottom margin of canvas that is off-screen
	 */
	public BaseOverlayRenderer(final ChunkCoordIntPair startCoords, final ChunkCoordIntPair endCoords, final int canvasWidth, final int canvasHeight, final int layerWidth, final int layerHeight) {
		this.startCoords = startCoords;
		this.endCoords = endCoords;

		this.canvasWidth = canvasWidth;
		this.canvasHeight = canvasHeight;
		
		this.layerWidth = layerWidth;
		this.layerHeight = layerHeight;
	
	}
	
	/**
	 * Renders data
	 * @param data
	 * @param g2D
	 */
	abstract public void render(K data, Graphics2D g2D) throws Exception;


	/**
	 * Get a DynamicTexture for a path
	 * @param path
	 * @return
	 */
	public static DynamicTexture getTexture(String path) {
		ResourceLocation loc = new ResourceLocation(path);
		DynamicTexture texture = null;
		InputStream is = null;
		try {
			is = JourneyMap.class.getResourceAsStream(path);
        	texture = new DynamicTexture(ImageIO.read(is));
        } catch(Exception e) {
        	JourneyMap.getLogger().severe("Can't get icon for " + loc + ": " + LogFormatter.toString(e));     
        	if(is!=null) {
        		try {
					is.close();
				} catch (IOException e1) {
				}
        	}
        }
		return texture;
	}
	
	/**
	 * Draw a text label, centered on x,z.  If bgColor not null,
	 * a rectangle will be drawn behind the text.
	 * 
	 * @param label
	 * @param x
	 * @param z
	 * @param height
	 * @param zOffset
	 * @param g2D
	 * @param fm
	 * @param bgColor
	 * @param color
	 */
	public static void drawCenteredLabel(final String label, int x, int z, int height, int zOffset, final Graphics2D g2D, final FontMetrics fm, Color bgColor, Color color) {

		if(label==null || label.length()==0) {
			return;
		}
		final int margin = 3;
		final int labelWidth = fm.stringWidth(label);
		final int lx = x - (labelWidth/2);
		final int ly = z + zOffset;
		
		// Draw background
		if(bgColor!=null) {
			g2D.setComposite(SLIGHTLYCLEAR);
			g2D.setPaint(bgColor);
			g2D.fillRect(lx - margin, ly - margin, labelWidth + margin + margin, height + margin + margin);
		}

		// Draw text
		g2D.setComposite(OPAQUE);
		g2D.setPaint(color);
		g2D.drawString(label, lx, ly + height - margin);
		
	}
	
	public static void drawRectangle(int x, int y, int width, int height, int red, int green, int blue, int alpha) {
		Tessellator tessellator = Tessellator.instance;
		
		GL11.glDisable(3553 /*GL_TEXTURE_2D*/);
		GL11.glDisable(2929 /*GL_DEPTH_TEST*/);
		GL11.glDepthMask(false);
		GL11.glBlendFunc(770, 771);
		GL11.glDisable(3008 /*GL_ALPHA_TEST*/);

		tessellator.startDrawingQuads();
		tessellator.setColorRGBA(red,green,blue,alpha);

		tessellator.addVertexWithUV(x, height + y, 0.0D, 0, 1);
		tessellator.addVertexWithUV(x + width, height + y, 0.0D, 1, 1);
		tessellator.addVertexWithUV(x + width, y, 0.0D, 1, 0);
		tessellator.addVertexWithUV(x, y, 0.0D, 0, 0);
		tessellator.draw();
		
		GL11.glEnable(3553 /*GL_TEXTURE_2D*/);
	}

	public static void drawImage(DynamicTexture texture, float transparency, double startX, double startY, double srcWidth, double srcHeight) {
		Tessellator tessellator = Tessellator.instance;
		GL11.glDisable(2929 /*GL_DEPTH_TEST*/);
		GL11.glDepthMask(false);
		GL11.glBlendFunc(770, 771);
		GL11.glColor4f(transparency, transparency, transparency, transparency);
		GL11.glDisable(3008 /*GL_ALPHA_TEST*/);
		GL11.glBindTexture(3553 /*GL_TEXTURE_2D*/, texture.func_110552_b());

		tessellator.startDrawingQuads();

		tessellator.addVertexWithUV(startX, srcHeight + startY, 0.0D, 0, 1);
		tessellator.addVertexWithUV(startX + srcWidth, srcHeight + startY, 0.0D, 1, 1);
		tessellator.addVertexWithUV(startX + srcWidth, startY, 0.0D, 1, 0);
		tessellator.addVertexWithUV(startX, startY, 0.0D, 0, 0);
		tessellator.draw();
	}
	
	public boolean inBounds(Entity entity) {
		int chunkX = entity.chunkCoordX;
		int chunkZ = entity.chunkCoordZ;
		return (chunkX>=startCoords.chunkXPos && chunkX<=endCoords.chunkXPos && 
				chunkZ>=startCoords.chunkZPos && chunkZ<=endCoords.chunkZPos);
	}
	
	public boolean inBounds(Map entityMap) {
		try {
			int chunkX = (Integer) entityMap.get(EntityKey.chunkCoordX);
			int chunkZ = (Integer) entityMap.get(EntityKey.chunkCoordZ);
			return (chunkX>=startCoords.chunkXPos && chunkX<=endCoords.chunkXPos && 
					chunkZ>=startCoords.chunkZPos && chunkZ<=endCoords.chunkZPos);
		} catch(NullPointerException e) {
			return false;
		}
	}
	
	static abstract class BaseEntityOverlayRenderer<K> extends BaseOverlayRenderer<K> {

		final int widthCutoff;
		final int heightCutoff;
		
		public BaseEntityOverlayRenderer(ChunkCoordIntPair startCoords,
				ChunkCoordIntPair endCoords, int canvasWidth, int canvasHeight,
				int layerWidth, int layerHeight,
				int widthCutoff, int heightCutoff) {
			super(startCoords, endCoords, canvasWidth, canvasHeight, layerWidth, layerHeight);
			this.widthCutoff = widthCutoff;
			this.heightCutoff = heightCutoff;	
		}
		
		/**
		 * Get the scaled screen position value for world chunk X, world position X.
		 * @param chunkZ
		 * @param posZ
		 * @return
		 */
		double getScaledEntityX(int chunkX, double posX) {
			return getScaledEntityPos(startCoords.chunkXPos, chunkX, posX);
		}
		
		/**
		 * Get the scaled screen position value for world position X.
		 * @param chunkZ
		 * @param posZ
		 * @return
		 */
		double getScaledEntityX(double posX) {
			return getScaledEntityPos(startCoords.chunkXPos, (int) posX>>4, posX);
		}

		/**
		 * Get the scaled screen position value for world chunk Z, world position Z.
		 * @param chunkZ
		 * @param posZ
		 * @return
		 */
		double getScaledEntityZ(int chunkZ, double posZ) {
			return getScaledEntityPos(startCoords.chunkZPos, chunkZ, posZ);
		}

		/**
		 * Get the scaled screen position value for world position Z.
		 * @param posZ
		 * @return
		 */
		double getScaledEntityZ(double posZ) {
			return getScaledEntityPos(startCoords.chunkZPos, (int) posZ>>4, posZ);
		}
		
		/**
		 * Get the scaled screen position value.
		 * @param startChunkPos
		 * @param chunkPos
		 * @param blockPos
		 * @return
		 */
		double getScaledEntityPos(int startChunkPos, int chunkPos, double blockPos) {
			int delta = chunkPos - startChunkPos;
			if(chunkPos<0) {
				delta++;
			}
			double scaledChunkPos = (delta * blockSize * 16);		
			double scaledBlockPos = ((blockPos) % 16) * blockSize;
			double adjusted = (scaledChunkPos + scaledBlockPos - (blockSize/2));
			return adjusted;
		}
		
		/**
		 * Draw the entity's location and heading on the overlay image
		 * using the provided icon.
		 * @param entity
		 * @param entityIcon
		 * @param overlayImg
		 */
		public void drawEntity(int chunkX, double posX, int chunkZ, double posZ, Double heading, boolean flipNotRotate, BufferedImage entityIcon, Graphics2D g2D) {
			
			int radius = entityIcon.getWidth()/2;			
			
			int offset = 0;
			double x = getScaledEntityX(chunkX, posX) + offset;
			double y = getScaledEntityZ(chunkZ, posZ) + offset;
			
			final Graphics2D gCopy = (Graphics2D) g2D.create();
			
			gCopy.translate(x, y);
			if(heading!=null) {
				if(flipNotRotate) {
					if(heading>Math.PI) {
						gCopy.scale(-1, 1);				
					} 
				} else {
					gCopy.rotate(heading);				
				}
			}
			gCopy.translate(-radius, -radius);
			gCopy.drawImage(entityIcon, 0, 0, radius*2, radius*2, null);
			gCopy.dispose();		
		}
		
	}
	
}
