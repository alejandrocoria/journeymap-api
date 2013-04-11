package net.techbrew.mcjm.render.overlay;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import net.minecraft.src.ChunkCoordIntPair;
import net.minecraft.src.Entity;
import net.minecraft.src.Tessellator;
import net.techbrew.mcjm.data.EntityKey;
import net.techbrew.mcjm.model.Waypoint;

public abstract class BaseOverlayRenderer<K> {
	
	public static AlphaComposite SLIGHTLYOPAQUE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1F);
	public static AlphaComposite OPAQUE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1F);
	public static AlphaComposite CLEAR = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0F);
	public static AlphaComposite SEMICLEAR = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5F);
	public static AlphaComposite SLIGHTLYCLEAR = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8F);
	
	final int startChunkX;
	final int startChunkZ;
	final int endChunkX;
	final int endChunkZ;
	final int entityChunkSize;
	final int entityBlockSize;
	final int canvasWidth;
	final int canvasHeight;
	final int widthCutoff;
	final int heightCutoff;
	
	/**
	 * Constructor.
	 * @param startCoords		Chunk coords for upper left of screen
	 * @param entityChunkSize	Size of a chunk in pixels, scaled for the entity layer
	 * @param canvasWidth		Width of the map canvas
	 * @param canvasHeight		Height of the map canvas
	 * @param widthCutoff		Right-hand margin of canvas that is off-screen
	 * @param heightCutoff		Bottom margin of canvas that is off-screen
	 */
	public BaseOverlayRenderer(final ChunkCoordIntPair startCoords, final ChunkCoordIntPair endCoords, final int entityChunkSize, final int canvasWidth, final int canvasHeight, final int widthCutoff, final int heightCutoff) {
		this.startChunkX = startCoords.chunkXPos;
		this.startChunkZ = startCoords.chunkZPos;
		this.endChunkX = endCoords.chunkXPos;
		this.endChunkZ = endCoords.chunkZPos;
		this.entityChunkSize = entityChunkSize;
		this.entityBlockSize = entityChunkSize/16;
		this.canvasWidth = canvasWidth;
		this.canvasHeight = canvasHeight;
		this.widthCutoff = widthCutoff;
		this.heightCutoff = heightCutoff;		
	}
	
	/**
	 * Renders data
	 * @param data
	 * @param g2D
	 */
	abstract public void render(K data, Graphics2D g2D) throws Exception;

	/**
	 * Get the scaled screen position value for world chunk X, world position X.
	 * @param chunkZ
	 * @param posZ
	 * @return
	 */
	int getScaledEntityX(int chunkX, double posX) {
		return getScaledEntityPos(startChunkX, chunkX, posX);
	}
	
	/**
	 * Get the scaled screen position value for world position X.
	 * @param chunkZ
	 * @param posZ
	 * @return
	 */
	int getScaledEntityX(double posX) {
		return getScaledEntityPos(startChunkX, (int) posX>>4, posX);
	}

	/**
	 * Get the scaled screen position value for world chunk Z, world position Z.
	 * @param chunkZ
	 * @param posZ
	 * @return
	 */
	int getScaledEntityZ(int chunkZ, double posZ) {
		return getScaledEntityPos(startChunkZ, chunkZ, posZ);
	}

	/**
	 * Get the scaled screen position value for world position Z.
	 * @param posZ
	 * @return
	 */
	int getScaledEntityZ(double posZ) {
		return getScaledEntityPos(startChunkZ, (int) posZ>>4, posZ);
	}
	
	/**
	 * Get the scaled screen position value.
	 * @param startChunkPos
	 * @param chunkPos
	 * @param blockPos
	 * @return
	 */
	int getScaledEntityPos(int startChunkPos, int chunkPos, double blockPos) {
		int delta = chunkPos - startChunkPos;
		if(chunkPos<0) {
			delta++;
		}
		int scaledChunkPos = (delta * entityChunkSize);		
		int scaledBlockPos = (int) (Math.floor(blockPos) % 16) * entityBlockSize;
		return (scaledChunkPos + scaledBlockPos - (entityBlockSize/2));
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

	public static void drawImage(int bufferedImage, float transparency, int startX, int startY, int srcWidth, int srcHeight) {
		Tessellator tessellator = Tessellator.instance;
		GL11.glDisable(2929 /*GL_DEPTH_TEST*/);
		GL11.glDepthMask(false);
		GL11.glBlendFunc(770, 771);
		GL11.glColor4f(transparency, transparency, transparency, transparency);
		GL11.glDisable(3008 /*GL_ALPHA_TEST*/);
		GL11.glBindTexture(3553 /*GL_TEXTURE_2D*/, bufferedImage);

		tessellator.startDrawingQuads();

		tessellator.addVertexWithUV(startX, srcHeight + startY, 0.0D, 0, 1);
		tessellator.addVertexWithUV(startX + srcWidth, srcHeight + startY, 0.0D, 1, 1);
		tessellator.addVertexWithUV(startX + srcWidth, startY, 0.0D, 1, 0);
		tessellator.addVertexWithUV(startX, startY, 0.0D, 0, 0);
		tessellator.draw();
	}
	
	/**
	 * Draw the entity's location and heading on the overlay image
	 * using the provided icon.
	 * @param entity
	 * @param entityIcon
	 * @param overlayImg
	 */
	protected void drawEntity(int chunkX, double posX, int chunkZ, double posZ, Double heading, boolean flipNotRotate, BufferedImage entityIcon, Graphics2D g2D) {
		int radius = entityIcon.getWidth()/2;
		int size = entityIcon.getWidth();
		
		int offset = 0;
		int x = getScaledEntityX(chunkX, posX) + offset;
		int y = getScaledEntityZ(chunkZ, posZ) + offset;
		
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
		gCopy.drawImage(entityIcon, 0, 0, size, size, null);
		gCopy.dispose();		
	}
	
	public boolean inBounds(Entity entity) {
		int chunkX = entity.chunkCoordX;
		int chunkZ = entity.chunkCoordZ;
		return (chunkX>=startChunkX && chunkX<=endChunkX && 
				chunkZ>=startChunkZ && chunkZ<=endChunkZ);
	}
	
	public boolean inBounds(Map entityMap) {
		try {
			int chunkX = (Integer) entityMap.get(EntityKey.chunkCoordX);
			int chunkZ = (Integer) entityMap.get(EntityKey.chunkCoordZ);
			return (chunkX>=startChunkX && chunkX<=endChunkX && 
					chunkZ>=startChunkZ && chunkZ<=endChunkZ);
		} catch(NullPointerException e) {
			return false;
		}
	}
	
}
