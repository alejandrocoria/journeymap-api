package net.techbrew.mcjm.render.overlay;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Point;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.imageio.ImageIO;

import net.minecraft.src.DynamicTexture;
import net.minecraft.src.FontRenderer;
import net.minecraft.src.Minecraft;
import net.minecraft.src.ResourceLocation;
import net.minecraft.src.Tessellator;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.log.LogFormatter;

import org.lwjgl.opengl.GL11;

public abstract class BaseOverlayRenderer<K> {
	
	public static AlphaComposite SLIGHTLYOPAQUE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1F);
	public static AlphaComposite OPAQUE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1F);
	public static AlphaComposite CLEAR = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0F);
	public static AlphaComposite SEMICLEAR = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5F);
	public static AlphaComposite SLIGHTLYCLEAR = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8F);
	
	abstract public List<DrawStep> prepareSteps(List<K> data, CoreRenderer core);
	
	abstract public void clear();

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
	public static void drawCenteredLabel(final String text, int x, int z, int height, int zOffset, Color bgColor, Color color, int alpha) {

		if(text==null || text.length()==0) {
			return;
		}
		
		final int width = Minecraft.getMinecraft().fontRenderer.getStringWidth(text) + 6;

		// Draw background
		if(bgColor!=null) {
			final float[] rgb = bgColor.getColorComponents(null);
			drawRectangle(x-width/2, z-height/2 + zOffset, width, height, bgColor, alpha);
		}

		// Draw text
		drawCenteredString(text, x, z-height/2 + zOffset + 3, color.getRGB());		
	}
	
	private static void drawCenteredString(String text, int x, int y, int color)
    {
		FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        fontRenderer.drawStringWithShadow(text, x - fontRenderer.getStringWidth(text) / 2, y, color);
    }
	
	private static void drawQuad(final int x, final int y, final int width, final int height, boolean flip) {
		drawQuad(x,y,width,height,null,1f,flip);
	}
	
	private static void drawQuad(final int x, final int y, final int width, final int height, Color color, float alpha, boolean flip) {
				
		if(color!=null) {
			float[] c = color.getColorComponents(null);
			GL11.glColor4f(c[0], c[1], c[2], alpha);
		} else {
			GL11.glColor4f(alpha,alpha,alpha,alpha);
		}
		
		final int direction = flip ? -1 : 1;
		
		Tessellator tessellator = Tessellator.instance;		
		tessellator.startDrawingQuads();		
		tessellator.addVertexWithUV(x, height + y, 0.0D, 0, 1);
		tessellator.addVertexWithUV(x + width, height + y, 0.0D, direction, 1);
		tessellator.addVertexWithUV(x + width, y, 0.0D, direction, 0);
		tessellator.addVertexWithUV(x, y, 0.0D, 0, 0);
		tessellator.draw();
	}
	
	public static void drawRectangle(int x, int y, int width, int height, Color color, int alpha) {
		
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDepthMask(false);
		GL11.glBlendFunc(770, 771);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glDisable(GL11.GL_TEXTURE_2D);		
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.setColorRGBA(color.getRed(),color.getGreen(),color.getBlue(),alpha);	
		tessellator.addVertexWithUV(x, height + y, 0.0D, 0, 1);
		tessellator.addVertexWithUV(x + width, height + y, 0.0D, 1, 1);
		tessellator.addVertexWithUV(x + width, y, 0.0D, 1, 0);
		tessellator.addVertexWithUV(x, y, 0.0D, 0, 0);
		tessellator.draw();	
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	public static void drawImage(MapTexture texture, int x, int y, boolean flip) {		
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDepthMask(false);
		GL11.glBlendFunc(770, 771);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getGlTextureId());
		drawQuad(x, y, texture.width, texture.height, flip);		
	}
	
	public static void drawRotatedImage(MapTexture texture, int x, int y, float heading) {
			
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDepthMask(false);
		GL11.glBlendFunc(770, 771);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		
		// Start a new matrix for translation/rotation
		GL11.glPushMatrix();
		

		
		// Bind texture
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getGlTextureId());
		
		// Smooth the texture interpolation that will come with rotation
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		
        // Move origin to x,y
		GL11.glTranslated(x, y, 0);
		
		// Rotatate around origin
		GL11.glRotatef(heading, 0, 0, 1.0f);
		
		// Offset the radius
		GL11.glTranslated(-texture.width, -texture.height, 0);
		
		// Draw texture in rotated position
		drawQuad(texture.width/2, texture.height/2, texture.width, texture.height, false);		
		
		// Drop out of the translated+rotated matrix
		GL11.glPopMatrix();	
	}
	
	private static void drawColoredImage(MapTexture texture, int alpha, Color color, int x, int y) {
		
		GL11.glBindTexture(3553 /*GL_TEXTURE_2D*/, texture.getGlTextureId());
		drawQuad(x, y, texture.width, texture.height, color, alpha, false);
		
	}
	
	/**
	 * Draw the entity's location and heading on the overlay image
	 * using the provided icon.
	 * @param entity
	 * @param entityIcon
	 * @param overlayImg
	 */
	private static void drawEntity(int x, int y, Double heading, boolean flipInsteadOfRotate, MapTexture texture, int bottomMargin) {

		if(heading==null) {
			drawImage(texture, x, y, false);
		} else if(!flipInsteadOfRotate) {
			drawRotatedImage(texture, x, y, heading.floatValue());
		} else {
			boolean flip = heading<90;
			drawImage(texture, x-(texture.width/2), y-(texture.height/2)-bottomMargin, flip);			
		}			
	}
	
	public static void draw(List<DrawStep> drawStepList, int xOffset, int yOffset) {
		
		for(DrawStep drawStep : drawStepList) {
			drawStep.draw(xOffset, yOffset);
		}
	}
	
	public interface DrawStep {
		public void draw(int xOffset, int yOffset);
	}
	
	public static class DrawEntityStep implements DrawStep {
		final Point pixel;
		final Double heading;
		final boolean flip;
		final MapTexture texture;
		final int bottomMargin;
		
		public DrawEntityStep(Point pixel, Double heading, boolean flip, MapTexture texture, int bottomMargin) {
			super();
			this.pixel = pixel;
			this.heading = heading;
			this.flip = flip;
			this.texture = texture;
			this.bottomMargin = bottomMargin;
		}

		@Override
		public void draw(int xOffset, int yOffset) {
			drawEntity(pixel.x + xOffset, pixel.y + yOffset, heading, flip, texture, bottomMargin);
		}		
	}
	
	class DrawColoredImageStep implements DrawStep {
		
		final Point pixel;
		final MapTexture texture;
		final Color color;
		final int alpha;
		
		public DrawColoredImageStep(Point pixel, MapTexture texture,
				Color color, int alpha) {
			super();
			this.pixel = pixel;
			this.texture = texture;
			this.color = color;
			this.alpha = alpha;
		}

		@Override
		public void draw(int xOffset, int yOffset) {
			drawColoredImage(texture, alpha, color, pixel.x + xOffset - (texture.width/2), pixel.y + yOffset- (texture.height/2));
		}		
	}
	
	class DrawRotatedImageStep implements DrawStep {
		
		final Point pixel;
		final MapTexture texture;
		final float heading;
		
		public DrawRotatedImageStep(Point pixel, MapTexture texture, float heading) {
			super();
			this.pixel = pixel;
			this.texture = texture;
			this.heading = heading;
		}

		@Override
		public void draw(int xOffset, int yOffset) {
			drawRotatedImage(texture, pixel.x + xOffset, pixel.y + yOffset, heading);
		}		
	}
	
	class DrawCenteredLabelStep implements DrawStep {
		final Point pixel;
		final String text;
		final int height;
		final int heightOffset;
		final Color bgColor;
		final Color fgColor;
		
		public DrawCenteredLabelStep(Point pixel, String text, int height,
				int heightOffset, Color bgColor, Color fgColor) {
			super();
			this.pixel = pixel;
			this.text = text;
			this.height = height;
			this.heightOffset = heightOffset;
			this.bgColor = bgColor;
			this.fgColor = fgColor;
		}

		@Override
		public void draw(int xOffset, int yOffset) {
			drawCenteredLabel(text, pixel.x + xOffset, pixel.y + yOffset, height, heightOffset, bgColor, fgColor, 205);
		}		
	}
	
}
