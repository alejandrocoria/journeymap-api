package net.techbrew.mcjm.render.overlay;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.List;

import net.minecraft.src.ChunkCoordIntPair;
import net.techbrew.mcjm.model.Waypoint;

public abstract class BaseOverlayRenderer<K> {
	
	public static AlphaComposite SLIGHTLYOPAQUE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1F);
	public static AlphaComposite OPAQUE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1F);
	public static AlphaComposite CLEAR = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0F);
	public static AlphaComposite SEMICLEAR = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5F);
	public static AlphaComposite SLIGHTLYCLEAR = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8F);
	
	final int startChunkX;
	final int startChunkZ;
	final int entityChunkSize;
	final int entityBlockSize;
	final int canvasWidth;
	final int canvasHeight;
	final int widthCutoff;
	final int heightCutoff;
	
	public BaseOverlayRenderer(final ChunkCoordIntPair startCoords, final int entityChunkSize, final int canvasWidth, final int canvasHeight, final int widthCutoff, final int heightCutoff) {
		this.startChunkX = startCoords.chunkXPos;
		this.startChunkZ = startCoords.chunkZPos;
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
	abstract public void render(K data, Graphics2D g2D);

	int getScaledEntityX(int chunkX, double posX) {
		return getScaledEntityPos(startChunkX, chunkX, posX);
	}
	
	int getScaledEntityX(double posX) {
		return getScaledEntityPos(startChunkX, (int) posX>>4, posX);
	}

	int getScaledEntityZ(int chunkZ, double posZ) {
		return getScaledEntityPos(startChunkZ, chunkZ, posZ);
	}

	int getScaledEntityZ(double posZ) {
		return getScaledEntityPos(startChunkZ, (int) posZ>>4, posZ);
	}
	
	int getScaledEntityPos(int startChunkPos, int chunkPos, double blockPos) {
		int delta = chunkPos - startChunkPos;
		if(chunkPos<0) {
			delta++;
		}
		int scaledChunkPos = (delta * entityChunkSize);		
		int scaledBlockPos = (int) (Math.floor(blockPos) % 16) * entityBlockSize;
		return (scaledChunkPos + scaledBlockPos);
	}
	
	void drawCenteredLabel(final String label, int x, int z, int height, int zOffset, final Graphics2D g2D, final FontMetrics fm, Color bgColor, Color color) {

		final int margin = 3;
		final int labelWidth = fm.stringWidth(label);
		final int lx = x - (labelWidth/2);
		final int ly = z + zOffset;
		
		// Draw background
		g2D.setComposite(SLIGHTLYCLEAR);
		g2D.setPaint(bgColor);
		g2D.fillRect(lx - margin, ly - margin, labelWidth + margin + margin, height + margin);

		// Draw text
		g2D.setComposite(OPAQUE);
		g2D.setPaint(color);
		g2D.drawString(label, lx, ly + height - margin);
		
	}
	
}
