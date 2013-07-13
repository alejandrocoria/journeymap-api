package net.techbrew.mcjm.render.overlay;

import net.minecraft.src.Tessellator;
import net.minecraft.src.ChunkCoordIntPair;

import org.lwjgl.opengl.GL11;

public class OverlayMapRenderer2 {
	
	public static final int TEXTURE_SIZE = 1024;
	
	final ChunkCoordIntPair startCoords;
	final ChunkCoordIntPair endCoords;
	final int viewportWidth;
	final int viewportHeight;
	
	Tessellator tessellator = Tessellator.instance;
	Integer textureId;
	
	public OverlayMapRenderer2(ChunkCoordIntPair startCoords, ChunkCoordIntPair endCoords, int viewportWidth, int viewportHeight) {
		this.startCoords = startCoords;
		this.endCoords = endCoords;
		this.viewportWidth = viewportWidth;
		this.viewportHeight = viewportHeight;
	}
	
	public void render() {
		
		if(textureId!=null) {
			double scale = 1f * Math.min(viewportWidth, viewportHeight) / TEXTURE_SIZE;
			double scaledSize = scale * TEXTURE_SIZE;
			double startX = (viewportWidth - scaledSize) * .5; 
			double startY = (viewportHeight - scaledSize) * .5; 
			
			GL11.glPushMatrix();				
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glDepthMask(false);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glColor4f(1f, 1f, 1f, 1f);
			GL11.glDisable(GL11.GL_ALPHA_TEST_FUNC);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

			GL11.glScaled(scale, scale, scale);
			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(startX, TEXTURE_SIZE + startY, 0.0D, 0, 1);
			tessellator.addVertexWithUV(startX + TEXTURE_SIZE, TEXTURE_SIZE + startY, 0.0D, 1, 1);
			tessellator.addVertexWithUV(startX + TEXTURE_SIZE, startY, 0.0D, 1, 0);
			tessellator.addVertexWithUV(startX, startY, 0.0D, 0, 0);
			tessellator.draw();			
			GL11.glPopMatrix();
		}
		
	}

}
