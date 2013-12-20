package net.techbrew.mcjm.render.overlay;

import java.awt.*;
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

    final int fontHeight = 16;
    final Font labelFont = new Font("Arial", Font.BOLD, fontHeight);

	abstract public List<DrawStep> prepareSteps(List<K> data, GridRenderer grid, double fontScale);

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
     * @param y
     * @param bgColor
     * @param color
     * @param alpha
     */
	public static void drawCenteredLabel(final String text, double x, double y, Color bgColor, Color color, int alpha, double fontScale) {

		if(text==null || text.length()==0) {
			return;
		}

        Minecraft mc = Minecraft.getMinecraft();
        final FontRenderer fontRenderer = mc.fontRenderer;
		final int width = fontRenderer.getStringWidth(text);

        if(fontScale!=1) {
            GL11.glPushMatrix();

            x = x/fontScale;
            y = y/fontScale;
            GL11.glScaled(fontScale,fontScale,0);
        }

        // Draw background
        if(bgColor!=null) {
            final float[] rgb = bgColor.getColorComponents(null);
            final int rectWidth = width + fontRenderer.getCharWidth(' ');
            final int vMargin = 2;
            drawRectangle(x-(rectWidth/2), y-2, rectWidth, fontRenderer.FONT_HEIGHT+3, bgColor, alpha);
        }

        // Draw text
        fontRenderer.drawStringWithShadow(text, (int) x - (width/2), (int) y, color.getRGB());

        if(fontScale!=1) {
            GL11.glPopMatrix();
        }
	}
	
	private static void drawQuad(TextureImpl texture, final int x, final int y, final int width, final int height, boolean flip) {
		drawQuad(texture,x,y,width,height,null,1f,flip, GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	}

    private static void drawQuad(TextureImpl texture, final int x, final int y, final int width, final int height, boolean flip, int glBlendSfactor, int glBlendDFactor) {
        drawQuad(texture,x,y,width,height,null,1f,flip, glBlendSfactor, glBlendDFactor);
    }

    /**
     *
     * @param texture
     * @param x
     * @param y
     * @param width
     * @param height
     * @param color
     * @param alpha
     * @param flip
     * @param glBlendSfactor  For normal alpha blending: GL11.GL_SRC_ALPHA
     * @param glBlendDFactor  For normal alpha blending: GL11.GL_ONE_MINUS_SRC_ALPHA
     */
    public static void drawQuad(TextureImpl texture, final int x, final int y, final int width, final int height, Color color, float alpha, boolean flip, int glBlendSfactor, int glBlendDFactor) {

        GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(glBlendSfactor, glBlendDFactor); // normal alpha blending: GL11.GL_ONE_MINUS_SRC_ALPHA
		if(color!=null) {
			float[] c = color.getColorComponents(null);
			GL11.glColor4f(c[0], c[1], c[2], alpha);
		} else {
			GL11.glColor4f(alpha,alpha,alpha,alpha);
		}

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

        // Ensure normal alpha blending afterward, just in case
        if(glBlendSfactor!=GL11.GL_SRC_ALPHA || glBlendDFactor!=GL11.GL_ONE_MINUS_SRC_ALPHA){
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        }
	}
	
	public static void drawRectangle(double x, double y, int width, int height, Color color, int alpha) {

		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_BLEND);
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

    public static void drawImage(TextureImpl texture, int x, int y, boolean flip, int glBlendSfactor, int glBlendDfactor) {
        drawQuad(texture, x, y, texture.width, texture.height, flip, glBlendSfactor, glBlendDfactor);
    }
	
	public static void drawRotatedImage(TextureImpl texture, int x, int y, float heading) {
		
		// Start a new matrix for translation/rotation
		GL11.glPushMatrix();

        GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
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

        GL11.glDisable(GL11.GL_BLEND);

		// Drop out of the translated+rotated matrix
		GL11.glPopMatrix();	
	}
	
	private static void drawColoredImage(TextureImpl texture, int alpha, Color color, int x, int y) {
		
		drawQuad(texture, x, y, texture.width, texture.height, color, alpha, false, GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
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
	 * Interface for something that needs to be drawn at a pixel coordinate.
	 * @author mwoodman
	 *
	 */
	public interface DrawStep {
		public void draw(int xOffset, int yOffset, GridRenderer gridRenderer);
	}
	
	public static class DrawEntityStep implements DrawStep {
		final int posX;
        final int posZ;
		final Double heading;
		final boolean flip;
		final TextureImpl texture;
		final int bottomMargin;
		
		public DrawEntityStep(int posX, int posZ, Double heading, boolean flip, TextureImpl texture, int bottomMargin) {
			super();
			this.posX = posX;
            this.posZ = posZ;
			this.heading = heading;
			this.flip = flip;
			this.texture = texture;
			this.bottomMargin = bottomMargin;
		}

		@Override
		public void draw(int xOffset, int yOffset, GridRenderer gridRenderer) {
            Point pixel = gridRenderer.getPixel(posX, posZ);
            if(pixel!=null) {
                drawEntity(pixel.x + xOffset, pixel.y + yOffset, heading, flip, texture, bottomMargin);
            }
		}		
	}
	
	class DrawWayPointStep implements DrawStep {

        final int posX;
        final int posZ;
		final TextureImpl texture;
        final TextureImpl offScreenTexture;
        final String label;
		final Color color;
        final Color fontColor;
		final int alpha;
        final double fontScale;
		
		public DrawWayPointStep(int posX, int posZ, TextureImpl texture, TextureImpl offScreenTexture, String label,
                                Color color, Color fontColor, int alpha, double fontScale) {
			super();
            this.posX = posX;
            this.posZ = posZ;
			this.texture = texture;
            this.offScreenTexture = offScreenTexture;
            this.label = label;
			this.color = color;
            this.fontColor = fontColor;
			this.alpha = alpha;
            this.fontScale = fontScale;
		}

		@Override
		public void draw(int xOffset, int yOffset, GridRenderer gridRenderer) {
            Point pixel = gridRenderer.getBlockPixelInGrid(posX, posZ);
            if(gridRenderer.isOnScreen(pixel.x, pixel.y)) {
                drawColoredImage(texture, alpha, color, pixel.x + xOffset - (texture.width/2), pixel.y + yOffset- (texture.height/2));
                drawCenteredLabel(label, pixel.x, pixel.y-texture.height, Color.black, fontColor, alpha, fontScale);
            } else {
                gridRenderer.ensureOnScreen(pixel);
                drawColoredImage(offScreenTexture, alpha, color, pixel.x + xOffset - (offScreenTexture.width / 2), pixel.y + yOffset - (offScreenTexture.height / 2));
            }
		}		
	}
	
	class DrawRotatedImageStep implements DrawStep {

        final int posX;
        final int posZ;
		final TextureImpl texture;
		final float heading;
		
		public DrawRotatedImageStep(int posX, int posZ, TextureImpl texture, float heading) {
			super();
            this.posX = posX;
            this.posZ = posZ;
			this.texture = texture;
			this.heading = heading;
		}

		@Override
		public void draw(int xOffset, int yOffset, GridRenderer gridRenderer) {
            Point pixel = gridRenderer.getPixel(posX, posZ);
            if(pixel!=null) {
			    drawRotatedImage(texture, pixel.x + xOffset, pixel.y + yOffset, heading);
            }
		}		
	}
	
	class DrawCenteredLabelStep implements DrawStep {

        final int posX;
        final int posZ;
		final String text;
		final int labelYOffset;
		final Color bgColor;
		final Color fgColor;
        final double fontScale;
		
		public DrawCenteredLabelStep(int posX, int posZ, String text, int labelYOffset, Color bgColor, Color fgColor, double fontScale) {
            this.posX = posX;
            this.posZ = posZ;
			this.text = text;
			this.labelYOffset = labelYOffset;
			this.bgColor = bgColor;
			this.fgColor = fgColor;
            this.fontScale = fontScale;
		}

		@Override
		public void draw(int xOffset, int yOffset, GridRenderer gridRenderer) {
            Point pixel = gridRenderer.getPixel(posX, posZ);
            if(pixel!=null) {
			    drawCenteredLabel(text, pixel.x + xOffset, pixel.y + yOffset + labelYOffset, bgColor, fgColor, 205, fontScale);
            }
		}		
	}
	
}
