package net.techbrew.mcjm.render.draw;

import net.minecraft.src.FontRenderer;
import net.minecraft.src.Minecraft;
import net.minecraft.src.Tessellator;
import net.techbrew.mcjm.render.texture.TextureImpl;
import org.lwjgl.opengl.GL11;

import java.awt.*;

/**
 * Created by mwoodman on 12/26/13.
 */
public class DrawUtil {
    /**
     * Draw a text label, centered on x,z.  If bgColor not null,
     * a rectangle will be drawn behind the text.
     *
     * @param text
     * @param x
     * @param y
     * @param bgColor
     * @param color
     * @param alpha
     */
    public static void drawCenteredLabel(final String text, double x, double y, Color bgColor, Color color, int alpha, double fontScale) {

        if (text == null || text.length() == 0) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        final FontRenderer fontRenderer = mc.fontRenderer;
        final int width = fontRenderer.getStringWidth(text);

        if (fontScale != 1) {
            GL11.glPushMatrix();

            x = x / fontScale;
            y = y / fontScale;
            GL11.glScaled(fontScale, fontScale, 0);
        }

        // Draw background
        if (bgColor != null) {
            final float[] rgb = bgColor.getColorComponents(null);
            final int rectWidth = width + fontRenderer.getCharWidth(' ');
            final int vMargin = 2;
            drawRectangle(x - (rectWidth / 2), y - 2, rectWidth, fontRenderer.FONT_HEIGHT + 3, bgColor, alpha);
        }

        // Draw text
        fontRenderer.drawStringWithShadow(text, (int) x - (width / 2), (int) y, color.getRGB());

        if (fontScale != 1) {
            GL11.glPopMatrix();
        }
    }

    private static void drawQuad(TextureImpl texture, final double x, final double y, final int width, final int height, boolean flip) {
        drawQuad(texture, x, y, width, height, null, 1f, flip, true, GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    private static void drawQuad(TextureImpl texture, final double x, final double y, final int width, final int height, boolean flip, int glBlendSfactor, int glBlendDFactor) {
        drawQuad(texture, x, y, width, height, null, 1f, flip, true, glBlendSfactor, glBlendDFactor);
    }

    /**
     * @param texture
     * @param x
     * @param y
     * @param width
     * @param height
     * @param color
     * @param alpha
     * @param flip
     * @param glBlendSfactor For normal alpha blending: GL11.GL_SRC_ALPHA
     * @param glBlendDFactor For normal alpha blending: GL11.GL_ONE_MINUS_SRC_ALPHA
     */
    public static void drawQuad(TextureImpl texture, final double x, final double y, final int width, final int height, Color color, float alpha, boolean flip, boolean blend, int glBlendSfactor, int glBlendDFactor) {

        if(blend) {
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(glBlendSfactor, glBlendDFactor); // normal alpha blending: GL11.GL_ONE_MINUS_SRC_ALPHA
            if (color != null) {
                float[] c = color.getColorComponents(null);
                GL11.glColor4f(c[0], c[1], c[2], alpha);
            } else {
                GL11.glColor4f(alpha, alpha, alpha, alpha);
            }
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
        if(blend) {
            if (glBlendSfactor != GL11.GL_SRC_ALPHA || glBlendDFactor != GL11.GL_ONE_MINUS_SRC_ALPHA) {
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            }
        }
    }

    public static void drawRectangle(double x, double y, int width, int height, Color color, int alpha) {

        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA(color.getRed(), color.getGreen(), color.getBlue(), alpha);
        tessellator.addVertexWithUV(x, height + y, 0.0D, 0, 1);
        tessellator.addVertexWithUV(x + width, height + y, 0.0D, 1, 1);
        tessellator.addVertexWithUV(x + width, y, 0.0D, 1, 0);
        tessellator.addVertexWithUV(x, y, 0.0D, 0, 0);
        tessellator.draw();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
    }

    public static void drawImage(TextureImpl texture, double x, double y, boolean flip) {
        drawQuad(texture, x, y, texture.width, texture.height, flip);
    }

    public static void drawImage(TextureImpl texture, double x, double y, boolean flip, int glBlendSfactor, int glBlendDfactor) {
        drawQuad(texture, x, y, texture.width, texture.height, flip, glBlendSfactor, glBlendDfactor);
    }

    public static void drawRotatedImage(TextureImpl texture, double x, double y, float heading) {

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
        drawQuad(texture, texture.width / 2, texture.height / 2, texture.width, texture.height, false);

        GL11.glDisable(GL11.GL_BLEND);

        // Drop out of the translated+rotated matrix
        GL11.glPopMatrix();
    }

    public static void drawColoredImage(TextureImpl texture, int alpha, Color color, double x, double y) {

        drawQuad(texture, x, y, texture.width, texture.height, color, alpha, false, true, GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

    }

    public static void drawColoredImage(TextureImpl texture, int alpha, Color color, double x, double y, boolean blend) {

        drawQuad(texture, x, y, texture.width, texture.height, color, alpha, false, false, 0, 0);

    }

    /**
     * Draw the entity's location and heading on the overlay image
     * using the provided icon.
     *
     * @param x
     * @param y
     * @param heading
     * @param flipInsteadOfRotate
     * @param texture
     * @param bottomMargin
     */
    public static void drawEntity(double x, double y, Double heading, boolean flipInsteadOfRotate, TextureImpl texture, int bottomMargin) {

        if (heading == null) {
            drawImage(texture, x, y, false);
        } else if (!flipInsteadOfRotate) {
            drawRotatedImage(texture, x, y, heading.floatValue());
        } else {
            boolean flip = heading < 90;
            drawImage(texture, x - (texture.width / 2), y - (texture.height / 2) - bottomMargin, flip);
        }
    }
}
