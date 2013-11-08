package net.techbrew.mcjm.render.overlay;

import java.awt.image.BufferedImage;

import net.minecraft.src.DynamicTexture;

import org.lwjgl.opengl.GL11;

public class MapTexture extends DynamicTexture {

	public final int mapWidth;
	public final int mapHeight;
	
	public MapTexture(BufferedImage mapImage) {
		super(mapImage);
		mapWidth = mapImage.getWidth();
		mapHeight = mapImage.getHeight();
	}
	
	public void clear() {
		GL11.glDeleteTextures(this.getGlTextureId());	
	}
}
