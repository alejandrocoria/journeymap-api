/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.render.draw;


import journeymap.client.api.model.ShapeProperties;
import journeymap.client.cartography.RGB;
import journeymap.client.render.texture.TextureImpl;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.geom.Point2D;
import java.util.List;

/**
 * Useful drawing routines that utilize the Minecraft Tessellator.
 */
public class DrawUtil
{
    /**
     * The constant zLevel.
     */
    public static double zLevel = 0;

    /**
     * The Tessellator.
     */
    static Tessellator tessellator = Tessellator.getInstance();
    /**
     * The Worldrenderer.
     */
    static VertexBuffer worldrenderer = tessellator.getBuffer();

    /**
     * Draw a text key, centered on x,z.  If bgColor not null,
     * a rectangle will be drawn behind the text.
     *
     * @param text      the text
     * @param x         the x
     * @param y         the y
     * @param bgColor   the bg color
     * @param bgAlpha   the bg alpha
     * @param color     the color
     * @param alpha     the alpha
     * @param fontScale the font scale
     */
    public static void drawCenteredLabel(final String text, double x, double y, Integer bgColor, float bgAlpha, Integer color, float alpha, double fontScale)
    {
        drawLabel(text, x, y, HAlign.Center, VAlign.Middle, bgColor, bgAlpha, color, alpha, fontScale, true, 0);
    }

    /**
     * Draw a text key, centered on x,z.  If bgColor not null,
     * a rectangle will be drawn behind the text.
     *
     * @param text       the text
     * @param x          the x
     * @param y          the y
     * @param bgColor    the bg color
     * @param bgAlpha    the bg alpha
     * @param color      the color
     * @param alpha      the alpha
     * @param fontScale  the font scale
     * @param fontShadow the font shadow
     */
    public static void drawCenteredLabel(final String text, double x, double y, Integer bgColor, float bgAlpha, Integer color, float alpha, double fontScale, boolean fontShadow)
    {
        drawLabel(text, x, y, HAlign.Center, VAlign.Middle, bgColor, bgAlpha, color, alpha, fontScale, fontShadow, 0);
    }

    /**
     * Draw a text key, centered on x,z.  If bgColor not null,
     * a rectangle will be drawn behind the text.
     *
     * @param text      the text
     * @param x         the x
     * @param y         the y
     * @param bgColor   the bg color
     * @param bgAlpha   the bg alpha
     * @param color     the color
     * @param alpha     the alpha
     * @param fontScale the font scale
     * @param rotation  the rotation
     */
    public static void drawCenteredLabel(final String text, double x, double y, Integer bgColor, float bgAlpha, Integer color, float alpha, double fontScale, double rotation)
    {
        drawLabel(text, x, y, HAlign.Center, VAlign.Middle, bgColor, bgAlpha, color, alpha, fontScale, true, rotation);
    }

    /**
     * Draw a text key, aligned on x,z.  If bgColor not null,
     * a rectangle will be drawn behind the text.
     *
     * @param text       the text
     * @param x          the x
     * @param y          the y
     * @param hAlign     the h align
     * @param vAlign     the v align
     * @param bgColor    the bg color
     * @param bgAlpha    the bg alpha
     * @param color      the color
     * @param alpha      the alpha
     * @param fontScale  the font scale
     * @param fontShadow the font shadow
     */
    public static void drawLabel(final String text, double x, double y, final HAlign hAlign, final VAlign vAlign, Integer bgColor, float bgAlpha, int color, float alpha, double fontScale, boolean fontShadow)
    {
        drawLabel(text, x, y, hAlign, vAlign, bgColor, bgAlpha, color, alpha, fontScale, fontShadow, 0);
    }

    /**
     * Draw labels.
     *
     * @param lines      the lines
     * @param x          the x
     * @param y          the y
     * @param hAlign     the h align
     * @param vAlign     the v align
     * @param bgColor    the bg color
     * @param bgAlpha    the bg alpha
     * @param color      the color
     * @param alpha      the alpha
     * @param fontScale  the font scale
     * @param fontShadow the font shadow
     * @param rotation   the rotation
     */
    public static void drawLabels(final String[] lines, double x, double y, final HAlign hAlign, final VAlign vAlign, Integer bgColor, float bgAlpha, Integer color, float alpha, double fontScale, boolean fontShadow, double rotation)
    {
        if (lines.length == 0)
        {
            return;
        }
        else if (lines.length == 1)
        {
            drawLabel(lines[0], x, y, hAlign, vAlign, bgColor, bgAlpha, color, alpha, fontScale, fontShadow, rotation);
            return;
        }

        final FontRenderer fontRenderer = FMLClientHandler.instance().getClient().fontRenderer;

        final double vpad = fontRenderer.getUnicodeFlag() ? 0 : fontShadow ? 6 : 4;
        final double lineHeight = fontRenderer.FONT_HEIGHT * fontScale;
        double bgHeight = (lineHeight * lines.length) + vpad;
        double bgWidth = 0;
        if (bgColor != null && bgAlpha > 0)
        {
            for (String line : lines)
            {
                bgWidth = Math.max(bgWidth, fontRenderer.getStringWidth(line) * fontScale);
            }

            if ((bgWidth % 2) == 0)
            {
                bgWidth++;
            }
        }

        if (lines.length > 1)
        {
            switch (vAlign)
            {
                case Above:
                {
                    y = y - (lineHeight * lines.length);
                    bgHeight += (vpad / 2);
                    break;
                }
                case Middle:
                {
                    y = y - (bgHeight / 2);
                    break;
                }
                case Below:
                {
                    break;
                }
            }
        }

        for (String line : lines)
        {
            drawLabel(line, x, y, hAlign, vAlign, bgColor, bgAlpha, bgWidth, bgHeight, color, alpha, fontScale, fontShadow, rotation);
            bgColor = null;
            y += (lineHeight);
        }
    }

    /**
     * Draw label.
     *
     * @param text       the text
     * @param x          the x
     * @param y          the y
     * @param hAlign     the h align
     * @param vAlign     the v align
     * @param bgColor    the bg color
     * @param bgAlpha    the bg alpha
     * @param color      the color
     * @param alpha      the alpha
     * @param fontScale  the font scale
     * @param fontShadow the font shadow
     * @param rotation   the rotation
     */
    public static void drawLabel(final String text, double x, double y, final HAlign hAlign, final VAlign vAlign, Integer bgColor, float bgAlpha, Integer color, float alpha, double fontScale, boolean fontShadow, double rotation)
    {
        double bgWidth = 0;
        double bgHeight = 0;
        if (bgColor != null && bgAlpha > 0)
        {
            final FontRenderer fontRenderer = FMLClientHandler.instance().getClient().fontRenderer;
            bgWidth = fontRenderer.getStringWidth(text);
            bgHeight = getLabelHeight(fontRenderer, fontShadow);
        }

        drawLabel(text, x, y, hAlign, vAlign, bgColor, bgAlpha, bgWidth, bgHeight, color, alpha, fontScale, fontShadow, rotation);
    }

    /**
     * Draw a text key, aligned on x,z.  If bgColor not null,
     * a rectangle will be drawn behind the text.
     *
     * @param text       the text
     * @param x          the x
     * @param y          the y
     * @param hAlign     the h align
     * @param vAlign     the v align
     * @param bgColor    the bg color
     * @param bgAlpha    the bg alpha
     * @param bgWidth    the bg width
     * @param bgHeight   the bg height
     * @param color      the color
     * @param alpha      the alpha
     * @param fontScale  the font scale
     * @param fontShadow the font shadow
     * @param rotation   the rotation
     */
    public static void drawLabel(final String text, double x, double y, final HAlign hAlign, final VAlign vAlign, Integer bgColor, float bgAlpha, double bgWidth, double bgHeight, Integer color, float alpha, double fontScale, boolean fontShadow, double rotation)
    {
        if (text == null || text.length() == 0)
        {
            return;
        }

        if (alpha > 1)
        {
            // TODO: There shouldn't be any more cases of this, but a breakpoint here is prudent until I'm sure.
            alpha = alpha / 255f;
        }

        final FontRenderer fontRenderer = FMLClientHandler.instance().getClient().fontRenderer;
        final boolean drawRect = (bgColor != null && alpha > 0);
        final double width = fontRenderer.getStringWidth(text);
        int height = drawRect ? getLabelHeight(fontRenderer, fontShadow) : fontRenderer.FONT_HEIGHT;

        if (!drawRect && fontRenderer.getUnicodeFlag())
        {
            height--;
        }

        GlStateManager.pushMatrix();

        try
        {
            if (fontScale != 1)
            {
                x = x / fontScale;
                y = y / fontScale;
                GlStateManager.scale(fontScale, fontScale, 0);
            }

            float textX = (float) x;
            float textY = (float) y;
            double rectX = x;
            double rectY = y;

            switch (hAlign)
            {
                case Left:
                {
                    textX = (float) (x - width);
                    rectX = textX;
                    break;
                }
                case Center:
                {
                    textX = (float) (x - (width / 2) + (fontScale > 1 ? .5 : 0));
                    rectX = (float) (x - (Math.max(1, bgWidth) / 2) + (fontScale > 1 ? .5 : 0));
                    break;
                }
                case Right:
                {
                    textX = (float) x;
                    rectX = (float) x;
                    break;
                }
            }

            double vpad = drawRect ? (height - fontRenderer.FONT_HEIGHT) / 2.0 : 0;

            switch (vAlign)
            {
                case Above:
                {
                    rectY = y - height;
                    textY = (float) (rectY + vpad + (fontRenderer.getUnicodeFlag() ? 0 : 1));
                    break;
                }
                case Middle:
                {
                    rectY = y - (height / 2) + (fontScale > 1 ? .5 : 0);
                    textY = (float) (rectY + vpad);
                    break;
                }
                case Below:
                {
                    rectY = y;
                    textY = (float) (rectY + vpad);
                    break;
                }
            }

            if (rotation != 0)
            {
                // Move origin to x,y
                GlStateManager.translate(x, y, 0);

                // Rotatate around origin
                GlStateManager.rotate((float) -rotation, 0, 0, 1.0f);

                // Offset the radius
                GlStateManager.translate(-x, -y, 0);
            }

            // Draw background
            if (bgColor != null && bgAlpha > 0)
            {
                final int hpad = 2;
                drawRectangle(rectX - hpad - .5, rectY, bgWidth + (2 * hpad), bgHeight, bgColor, bgAlpha);
            }

            // String positioning uses ints
//            int intTextX = (int) Math.floor(textX);
//            int intTextY = (int) Math.floor(textY);
//            double dTextX = textX - intTextX;
//            double dTextY = textY - intTextY;

            // Use translation for the double precision
            GlStateManager.translate(textX - Math.floor(textX), textY - Math.floor(textY), 0);

            fontRenderer.drawString(text, textX, textY, color, fontShadow);

        }
        finally
        {
            GlStateManager.popMatrix();
        }
    }

    /**
     * Gets label height.
     *
     * @param fr         the fr
     * @param fontShadow the font shadow
     * @return the label height
     */
    public static int getLabelHeight(FontRenderer fr, boolean fontShadow)
    {
        final int vpad = fr.getUnicodeFlag() ? 0 : fontShadow ? 6 : 4;
        return fr.FONT_HEIGHT + vpad;
    }

    /**
     * Draw image.
     *
     * @param texture  the texture
     * @param x        the x
     * @param y        the y
     * @param flip     the flip
     * @param scale    the scale
     * @param rotation the rotation
     */
    public static void drawImage(TextureImpl texture, double x, double y, boolean flip, float scale, double rotation)
    {
        drawQuad(texture, x, y, (texture.getWidth() * scale), (texture.getHeight() * scale), flip, rotation);
    }

    /**
     * Draw image.
     *
     * @param texture  the texture
     * @param alpha    the alpha
     * @param x        the x
     * @param y        the y
     * @param flip     the flip
     * @param scale    the scale
     * @param rotation the rotation
     */
    public static void drawImage(TextureImpl texture, float alpha, double x, double y, boolean flip, float scale, double rotation)
    {
        drawQuad(texture, 0xffffff, alpha, x, y, (texture.getWidth() * scale), (texture.getHeight() * scale), false, rotation);
    }

    /**
     * Draw clamped image.
     *
     * @param texture  the texture
     * @param x        the x
     * @param y        the y
     * @param scale    the scale
     * @param rotation the rotation
     */
    public static void drawClampedImage(TextureImpl texture, double x, double y, float scale, double rotation)
    {
        drawClampedImage(texture, 0xffffff, 1f, x, y, scale, rotation);
    }

    /**
     * Draw clamped image.
     *
     * @param texture  the texture
     * @param color    the color
     * @param alpha    the alpha
     * @param x        the x
     * @param y        the y
     * @param scale    the scale
     * @param rotation the rotation
     */
    public static void drawClampedImage(TextureImpl texture, int color, float alpha, double x, double y, float scale, double rotation)
    {
        drawQuad(texture, color, alpha, x, y, (texture.getWidth() * scale), (texture.getHeight() * scale), false, rotation);
    }

    /**
     * Draw colored image.
     *
     * @param texture  the texture
     * @param color    the color
     * @param alpha    the alpha
     * @param x        the x
     * @param y        the y
     * @param scale    the scale
     * @param rotation the rotation
     */
    public static void drawColoredImage(TextureImpl texture, int color, float alpha, double x, double y, float scale, double rotation)
    {
        drawQuad(texture, color, alpha, x, y, (texture.getWidth() * scale), (texture.getHeight() * scale), false, rotation);
    }

    /**
     * Draw colored sprite.
     *
     * @param texture       the texture
     * @param displayWidth  the display width
     * @param displayHeight the display height
     * @param spriteX       the sprite x
     * @param spriteY       the sprite y
     * @param spriteWidth   the sprite width
     * @param spriteHeight  the sprite height
     * @param color         the color
     * @param alpha         the alpha
     * @param x             the x
     * @param y             the y
     * @param scale         the scale
     * @param rotation      the rotation
     */
    public static void drawColoredSprite(final TextureImpl texture, final double displayWidth, final double displayHeight, final double spriteX, final double spriteY, final double spriteWidth, final double spriteHeight, final Integer color, final float alpha, final double x, final double y, final float scale, final double rotation)
    {
        final double texWidth = texture.getWidth();
        final double texHeight = texture.getHeight();
        final double minU = Math.max(0, spriteX / texWidth);
        final double minV = Math.max(0, spriteY / texHeight);
        final double maxU = Math.min(1, (spriteX + spriteWidth) / texWidth);
        final double maxV = Math.min(1, (spriteY + spriteHeight) / texHeight);
        drawQuad(texture, color, alpha, x, y, displayWidth * scale, displayHeight * scale, minU, minV, maxU, maxV, rotation, false, true, GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, false);
    }

    /**
     * Draw colored image.
     *
     * @param texture  the texture
     * @param color    the color
     * @param alpha    the alpha
     * @param x        the x
     * @param y        the y
     * @param rotation the rotation
     */
    public static void drawColoredImage(TextureImpl texture, int color, float alpha, double x, double y, double rotation)
    {
        drawQuad(texture, color, alpha, x, y, texture.getWidth(), texture.getHeight(), false, rotation);
    }

    /**
     * Draw colored image.
     *
     * @param texture  the texture
     * @param color    the color
     * @param alpha    the alpha
     * @param x        the x
     * @param y        the y
     * @param width    the width
     * @param height   the height
     * @param rotation the rotation
     */
    public static void drawColoredImage(TextureImpl texture, int color, float alpha, double x, double y, int width, int height, double rotation)
    {
        drawQuad(texture, color, alpha, x, y, width, height, false, rotation);
    }

    /**
     * Draw quad.
     *
     * @param texture  the texture
     * @param x        the x
     * @param y        the y
     * @param width    the width
     * @param height   the height
     * @param flip     the flip
     * @param rotation the rotation
     */
    public static void drawQuad(TextureImpl texture, final double x, final double y, final double width, final double height, boolean flip, double rotation)
    {
        drawQuad(texture, 0xffffff, 1f, x, y, width, height, 0, 0, 1, 1, rotation, flip, true, GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, false);
    }

    /**
     * Draw quad.
     *
     * @param texture  the texture
     * @param color    the color
     * @param alpha    the alpha
     * @param x        the x
     * @param y        the y
     * @param width    the width
     * @param height   the height
     * @param flip     the flip
     * @param rotation the rotation
     */
    public static void drawQuad(TextureImpl texture, int color, float alpha, final double x, final double y, final double width, final double height, boolean flip, double rotation)
    {
        drawQuad(texture, color, alpha, x, y, width, height, 0, 0, 1, 1, rotation, flip, true, GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, false);
    }


    /**
     * Draw quad.
     *
     * @param texture        the texture
     * @param color          the color
     * @param alpha          the alpha
     * @param x              the x
     * @param y              the y
     * @param width          the width
     * @param height         the height
     * @param minU           the min u
     * @param minV           the min v
     * @param maxU           the max u
     * @param maxV           the max v
     * @param rotation       the rotation
     * @param flip           the flip
     * @param blend          the blend
     * @param glBlendSfactor For normal alpha blending: GL11.GL_SRC_ALPHA
     * @param glBlendDFactor For normal alpha blending: GL11.GL_ONE_MINUS_SRC_ALPHA
     * @param clampTexture   the clamp texture
     */
    public static void drawQuad(TextureImpl texture, int color, float alpha, final double x, final double y, final double width, final double height, final double minU, final double minV, final double maxU, final double maxV, double rotation, boolean flip, boolean blend, int glBlendSfactor, int glBlendDFactor, boolean clampTexture)
    {
        GlStateManager.pushMatrix();

        try
        {
            if (blend)
            {
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(glBlendSfactor, glBlendDFactor, 1, 0);
            }

            GlStateManager.enableTexture2D();
            GlStateManager.bindTexture(texture.getGlTextureId());

            if (alpha > 1)
            {
                // TODO: There shouldn't be any more cases of this, but a breakpoint here is prudent until I'm sure.
                alpha = alpha / 255f;
            }

            if (blend)
            {
                float[] c = RGB.floats(color);

                GlStateManager.color(c[0], c[1], c[2], alpha);
            }
            else
            {
                GlStateManager.color(1, 1, 1, alpha);
            }

            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

            int texEdgeBehavior = clampTexture ? GL12.GL_CLAMP_TO_EDGE : GL11.GL_REPEAT;
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, texEdgeBehavior);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, texEdgeBehavior);

            if (rotation != 0)
            {
                double transX = x + (width / 2);
                double transY = y + (height / 2);

                // Move origin to center of upperTexture
                GlStateManager.translate(transX, transY, 0);

                // Rotatate around origin
                GlStateManager.rotate((float) rotation, 0, 0, 1.0f);

                // Return origin
                GlStateManager.translate(-transX, -transY, 0);
            }

            final double direction = flip ? -maxU : maxU;

            startDrawingQuads(false);
            addVertexWithUV(x, height + y, zLevel, minU, maxV);
            addVertexWithUV(x + width, height + y, zLevel, direction, maxV);
            addVertexWithUV(x + width, y, zLevel, direction, minV);
            addVertexWithUV(x, y, zLevel, minU, minV);
            draw();

            // Ensure normal alpha blending afterward, just in case
            if (blend)
            {
                GlStateManager.color(1, 1, 1, 1);
                if (glBlendSfactor != GL11.GL_SRC_ALPHA || glBlendDFactor != GL11.GL_ONE_MINUS_SRC_ALPHA)
                {
                    GlStateManager.enableBlend();
                    GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
                }
            }
        }
        finally
        {
            GlStateManager.popMatrix();
        }
    }

    /**
     * Draw rectangle.
     *
     * @param x      the x
     * @param y      the y
     * @param width  the width
     * @param height the height
     * @param color  the color
     * @param alpha  the alpha
     */
    public static void drawRectangle(double x, double y, double width, double height, int color, float alpha)
    {
        if (alpha > 1)
        {
            // TODO: There shouldn't be any more cases of this, but a breakpoint here is prudent until I'm sure.
            alpha = alpha / 255f;
        }
        // Prep
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        // Draw
        int[] rgba = RGB.ints(color, alpha);
        startDrawingQuads(true);
        addVertex(x, height + y, zLevel, rgba);
        addVertex(x + width, height + y, zLevel, rgba);
        addVertex(x + width, y, zLevel, rgba);
        addVertex(x, y, zLevel, rgba);
        draw();

        // Clean up
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
    }

    /**
     * Draw polygon.
     *
     * @param xOffset         the x offset
     * @param yOffset         the y offset
     * @param screenPoints    the screen points
     * @param shapeProperties the shape properties
     */
    public static void drawPolygon(double xOffset, double yOffset, List<Point2D.Double> screenPoints, ShapeProperties shapeProperties)
    {
        // Prep
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.enableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        // Draw Fill
        if (shapeProperties.getFillOpacity() >= 0.01F)
        {
            float[] rgba = RGB.floats(shapeProperties.getFillColor(), shapeProperties.getFillOpacity());
            GlStateManager.color(rgba[0], rgba[1], rgba[2], rgba[3]);

            int lastIndex = screenPoints.size() - 1;
            Point2D.Double first, second;
            int j;
            GL11.glBegin(GL11.GL_POLYGON);
            for (int i = 0; i <= lastIndex; i++)
            {
                j = (i < lastIndex) ? i + 1 : 0;
                first = screenPoints.get(i);
                second = screenPoints.get(j);

                GL11.glVertex2d(first.getX() + xOffset, first.getY() + yOffset);
                GL11.glVertex2d(second.getX() + xOffset, second.getY() + yOffset);

            }
            GL11.glEnd();
        }

        // Draw Outline
        if (shapeProperties.getStrokeOpacity() >= 0.01F && shapeProperties.getStrokeWidth() > 0)
        {
            float[] rgba = RGB.floats(shapeProperties.getStrokeColor(), shapeProperties.getFillOpacity());
            GlStateManager.color(rgba[0], rgba[1], rgba[2], rgba[3]);
            float stroke = shapeProperties.getStrokeWidth();
            GL11.glLineWidth(stroke);

            int lastIndex = screenPoints.size() - 1;
            Point2D.Double first, second;
            int j;

            GL11.glBegin(GL11.GL_LINE_STRIP);
            for (int i = 0; i <= lastIndex; i++)
            {
                j = (i < lastIndex) ? i + 1 : 0;
                first = screenPoints.get(i);
                second = screenPoints.get(j);

                GL11.glVertex2d(first.getX() + xOffset, first.getY() + yOffset);
                GL11.glVertex2d(second.getX() + xOffset, second.getY() + yOffset);

            }
            GL11.glEnd();
        }

        // Clean up
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
    }

    /**
     * Draws a rectangle with a vertical gradient between the specified colors.
     * 0, top, this.width, this.height - top, -1072689136, -804253680
     *
     * @param x          the x
     * @param y          the y
     * @param width      the width
     * @param height     the height
     * @param startColor the start color
     * @param startAlpha the start alpha
     * @param endColor   the end color
     * @param endAlpha   the end alpha
     */
    public static void drawGradientRect(double x, double y, double width, double height, int startColor, float startAlpha, int endColor, float endAlpha)
    {
        if (startAlpha > 1)
        {
            // TODO: There shouldn't be any more cases of this, but a breakpoint here is prudent until I'm sure.
            startAlpha = startAlpha / 255f;
        }

        if (endAlpha > 1)
        {
            // TODO: There shouldn't be any more cases of this, but a breakpoint here is prudent until I'm sure.
            endAlpha = endAlpha / 255f;
        }

        int[] rgbaStart = RGB.ints(startColor, startAlpha);
        int[] rgbaEnd = RGB.ints(endColor, endAlpha);

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        startDrawingQuads(true);
        addVertexWithUV(x, height + y, zLevel, 0, 1, rgbaEnd);
        addVertexWithUV(x + width, height + y, zLevel, 1, 1, rgbaEnd);
        addVertexWithUV(x + width, y, zLevel, 1, 0, rgbaStart);
        addVertexWithUV(x, y, zLevel, 0, 0, rgbaStart);
        draw();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
    }

    /**
     * Draw bound texture.
     *
     * @param startU the start u
     * @param startV the start v
     * @param startX the start x
     * @param startY the start y
     * @param z      the z
     * @param endU   the end u
     * @param endV   the end v
     * @param endX   the end x
     * @param endY   the end y
     */
    public static void drawBoundTexture(double startU, double startV, double startX, double startY, double z, double endU, double endV, double endX, double endY)
    {
        startDrawingQuads(false);
        addVertexWithUV(startX, endY, z, startU, endV);
        addVertexWithUV(endX, endY, z, endU, endV);
        addVertexWithUV(endX, startY, z, endU, startV);
        addVertexWithUV(startX, startY, z, startU, startV);
        draw();
    }


    /**
     * Draw the entity's location and heading on the overlay image
     * using the provided icon.
     *
     * @param x        the x
     * @param y        the y
     * @param heading  the heading
     * @param texture  the texture
     * @param scale    the scale
     * @param rotation the rotation
     */
    public static void drawEntity(double x, double y, double heading, TextureImpl texture, float scale, double rotation)
    {
        drawEntity(x, y, heading, texture, 1f, scale, rotation);
    }

    /**
     * Draw the entity's location and heading on the overlay image
     * using the provided icon.
     *
     * @param x        the x
     * @param y        the y
     * @param heading  the heading
     * @param texture  the texture
     * @param alpha    the alpha
     * @param scale    the scale
     * @param rotation the rotation
     */
    public static void drawEntity(double x, double y, double heading, TextureImpl texture, float alpha, float scale, double rotation)
    {
        // Adjust to scale
        double width = (texture.getWidth() * scale);
        double height = (texture.getHeight() * scale);
        double drawX = x - (width / 2);
        double drawY = y - (height / 2);

        // Draw upperTexture in rotated position
        drawImage(texture, alpha, drawX, drawY, false, scale, heading);
    }

    /**
     * Draw the entity's location and heading on the overlay image
     * using the provided icon.
     *
     * @param x        the x
     * @param y        the y
     * @param texture  the texture
     * @param color    the color
     * @param alpha    the alpha
     * @param scale    the scale
     * @param rotation the rotation
     */
    public static void drawColoredEntity(double x, double y, TextureImpl texture, int color, float alpha, float scale, double rotation)
    {
        // Adjust to scale
        double width = (texture.getWidth() * scale);
        double height = (texture.getHeight() * scale);
        double drawX = x - (width / 2);
        double drawY = y - (height / 2);

        // Draw upperTexture in rotated position
        drawColoredImage(texture, color, alpha, drawX, drawY, scale, rotation);
    }

    /**
     * Size display.
     *
     * @param width  the width
     * @param height the height
     */
    public static void sizeDisplay(double width, double height)
    {
        GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, width, height, 0.0D, 100.0D, 3000.0D);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0F, 0.0F, -2000.0F);
    }

    /**
     * Draw.
     */
    public static void draw()
    {
        tessellator.draw();
    }

    /**
     * Start drawing quads.
     *
     * @param useColor the use color
     */
    public static void startDrawingQuads(boolean useColor)
    {
        // 1.7.10
        // tessellator.startDrawingQuads();

        // 1.8
        // worldrenderer.startDrawingQuads();

        // 1.8.8
        if (useColor)
        {
            // (floats) x,y,z + (floats) uv + (ints) r,g,b,a
            worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        }
        else
        {
            // (floats) x,y,z + (floats) u,v
            worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        }
    }

    /**
     * Add vertex with uv.
     *
     * @param x the x
     * @param y the y
     * @param z the z
     * @param u the u
     * @param v the v
     */
    public static void addVertexWithUV(double x, double y, double z, double u, double v)
    {
        // 1.7.10
        // tessellator.addVertexWithUV(x,y,z,u,v);

        // 1.8
        // worldrenderer.addVertexWithUV(x, y, z, u, v);

        // 1.8.8
        worldrenderer.pos(x, y, z).tex(u, v).endVertex();
    }

    /**
     * Add vertex.
     *
     * @param x    the x
     * @param y    the y
     * @param z    the z
     * @param rgba the rgba
     */
    public static void addVertex(double x, double y, double z, int[] rgba)
    {
        // 1.7 and 1.8
        // tessellator.setColorRGBA_I(rgb, a);
        // worldrenderer.addVertexWithUV(x, y, z, u, v);

        // 1.8.8
        worldrenderer.pos(x, y, z).tex(1, 1).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
    }

    /**
     * Add vertex with uv.
     *
     * @param x    the x
     * @param y    the y
     * @param z    the z
     * @param u    the u
     * @param v    the v
     * @param rgba the rgba
     */
    public static void addVertexWithUV(double x, double y, double z, double u, double v, int[] rgba)
    {
        // 1.7 and 1.8
        // tessellator.setColorRGBA_I(rgb, a);
        // worldrenderer.addVertexWithUV(x, y, z, u, v);

        // 1.8.8
        worldrenderer.pos(x, y, z).tex(u, v).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
    }

    /**
     * The enum H align.
     */
    public enum HAlign
    {
        /**
         * Left h align.
         */
        Left, /**
     * Center h align.
     */
    Center, /**
     * Right h align.
     */
    Right
    }

    /**
     * The enum V align.
     */
    public enum VAlign
    {
        /**
         * Above v align.
         */
        Above, /**
     * Middle v align.
     */
    Middle, /**
     * Below v align.
     */
    Below
    }
}
