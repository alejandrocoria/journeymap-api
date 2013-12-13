package net.techbrew.mcjm.render.overlay;

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
import net.techbrew.mcjm.model.EntityHelper;
import net.techbrew.mcjm.render.texture.TextureCache;
import net.techbrew.mcjm.render.texture.TextureImpl;

import org.lwjgl.opengl.GL11;

public abstract class BaseOverlayRenderer<K> {
	
	abstract public List<DrawStep> prepareSteps(List<K> data, GridRenderer grid);
	
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
     * @param text
     * @param x
     * @param z
     * @param height
     * @param zOffset
     * @param bgColor
     * @param color
     * @param alpha
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
	
	private static void drawQuad(TextureImpl texture, final int x, final int y, final int width, final int height, boolean flip) {
		drawQuad(texture,x,y,width,height,null,1f,flip);
	}
	
	private static void drawQuad(TextureImpl texture, final int x, final int y, final int width, final int height, Color color, float alpha, boolean flip) {

		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);		
		if(color!=null) {
			float[] c = color.getColorComponents(null);
			GL11.glColor4f(c[0], c[1], c[2], alpha);
		} else {
			GL11.glColor4f(alpha,alpha,alpha,alpha);
		}
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getGlTextureId());
		
		final int direction = flip ? -1 : 1;
		
		Tessellator tessellator = Tessellator.instance;		
		tessellator.startDrawingQuads();		
		tessellator.addVertexWithUV(x, height + y, 0.0D, 0, 1);
		tessellator.addVertexWithUV(x + width, height + y, 0.0D, direction, 1);
		tessellator.addVertexWithUV(x + width, y, 0.0D, direction, 0);
		tessellator.addVertexWithUV(x, y, 0.0D, 0, 0);
		tessellator.draw();

        GL11.glEnable(GL11.GL_ALPHA_TEST);
	}
	
	public static void drawRectangle(int x, int y, int width, int height, Color color, int alpha) {

		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
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
        GL11.glEnable(GL11.GL_ALPHA_TEST);
	}

	public static void drawImage(TextureImpl texture, int x, int y, boolean flip) {				
		drawQuad(texture, x, y, texture.width, texture.height, flip);		
	}
	
	public static void drawRotatedImage(TextureImpl texture, int x, int y, float heading) {
		
		// Start a new matrix for translation/rotation
		GL11.glPushMatrix();

		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		
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
		drawQuad(texture, texture.width/2, texture.height/2, texture.width, texture.height, false);

        // Re-enable alpha test
        GL11.glEnable(GL11.GL_ALPHA_TEST);

		// Drop out of the translated+rotated matrix
		GL11.glPopMatrix();	
	}
	
	private static void drawColoredImage(TextureImpl texture, int alpha, Color color, int x, int y) {
		
		drawQuad(texture, x, y, texture.width, texture.height, color, alpha, false);
		
	}

    /**
     * Draw the entity's location and heading on the overlay image
     * using the provided icon.
     * @param x
     * @param y
     * @param heading
     * @param flipInsteadOfRotate
     * @param texture
     * @param bottomMargin
     */
	private static void drawEntity(int x, int y, Double heading, boolean flipInsteadOfRotate, TextureImpl texture, int bottomMargin) {

		if(heading==null) {
			drawImage(texture, x, y, false);
		} else if(!flipInsteadOfRotate) {
			drawRotatedImage(texture, x, y, heading.floatValue());
		} else {
			boolean flip = heading<90;
			drawImage(texture, x-(texture.width/2), y-(texture.height/2)-bottomMargin, flip);			
		}			
	}
	
	/**
	 * Draw a list of steps
	 * @param drawStepList
	 * @param xOffset
	 * @param yOffset
	 */
	public static synchronized void draw(final List<DrawStep> drawStepList, int xOffset, int yOffset) {
		if(drawStepList==null || drawStepList.isEmpty()) return;
        draw(xOffset, yOffset, drawStepList.toArray(new DrawStep[drawStepList.size()]));
	}

    /**
     * Draw an array of steps
     * @param xOffset
     * @param yOffset
     * @param drawSteps
     */
    public static synchronized void draw( int xOffset, int yOffset, DrawStep... drawSteps) {

        //GL11.glDisable(GL11.GL_DEPTH_TEST);
        //GL11.glDepthMask(false);
        //GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        for(DrawStep drawStep : drawSteps) {
            drawStep.draw(xOffset, yOffset);
        }

        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    /**
     * Draw the player marker
     * @param gridRenderer
     * @param mc
     * @param xOffset
     * @param yOffset
     * @param fullSize
     */
    public static void drawPlayer(GridRenderer gridRenderer, Minecraft mc, int xOffset, int yOffset, boolean fullSize) {
        Point playerPixel = gridRenderer.getPixel((int) mc.thePlayer.posX, (int) mc.thePlayer.posZ);
        if(playerPixel!=null) {
            draw(xOffset, yOffset, new BaseOverlayRenderer.DrawEntityStep(
                    playerPixel, EntityHelper.getHeading(mc.thePlayer), false, TextureCache.instance().getPlayerLocator(), 8));
        }
    }
	
	/**
	 * Interface for something that needs to be drawn at a pixel coordinate.
	 * @author mwoodman
	 *
	 */
	public interface DrawStep {
		public void draw(int xOffset, int yOffset);
	}
	
	public static class DrawEntityStep implements DrawStep {
		final Point pixel;
		final Double heading;
		final boolean flip;
		final TextureImpl texture;
		final int bottomMargin;
		
		public DrawEntityStep(Point pixel, Double heading, boolean flip, TextureImpl texture, int bottomMargin) {
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
		final TextureImpl texture;
		final Color color;
		final int alpha;
		
		public DrawColoredImageStep(Point pixel, TextureImpl texture,
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
		final TextureImpl texture;
		final float heading;
		
		public DrawRotatedImageStep(Point pixel, TextureImpl texture, float heading) {
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
