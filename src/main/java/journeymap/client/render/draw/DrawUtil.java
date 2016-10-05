/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.render.draw;


import journeymap.client.api.model.ShapeProperties;
import journeymap.client.cartography.RGB;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.render.texture.TextureImpl;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.geom.Point2D;
import java.util.List;

/**
 * Useful drawing routines that utilize the Minecraft Tessellator.
 */
public class DrawUtil
{
    public static double zLevel = 0;

    static Tessellator tessellator = Tessellator.getInstance();
    static VertexBuffer worldrenderer = tessellator.getBuffer();

    /**
     * Draw a text key, centered on x,z.  If bgColor not null,
     * a rectangle will be drawn behind the text.
     *
     * @param text
     * @param x
     * @param y
     * @param bgColor
     * @param color
     * @param bgAlpha
     */
    public static void drawCenteredLabel(final String text, double x, double y, Integer bgColor, float bgAlpha, Integer color, float alpha, double fontScale)
    {
        drawLabel(text, x, y, HAlign.Center, VAlign.Middle, bgColor, bgAlpha, color, alpha, fontScale, true, 0);
    }

    /**
     * Draw a text key, centered on x,z.  If bgColor not null,
     * a rectangle will be drawn behind the text.
     */
    public static void drawCenteredLabel(final String text, double x, double y, Integer bgColor, float bgAlpha, Integer color, float alpha, double fontScale, boolean fontShadow)
    {
        drawLabel(text, x, y, HAlign.Center, VAlign.Middle, bgColor, bgAlpha, color, alpha, fontScale, fontShadow, 0);
    }

    /**
     * Draw a text key, centered on x,z.  If bgColor not null,
     * a rectangle will be drawn behind the text.
     *
     * @param text
     * @param x
     * @param y
     * @param bgColor
     * @param color
     * @param bgAlpha
     * @param rotation
     */
    public static void drawCenteredLabel(final String text, double x, double y, Integer bgColor, float bgAlpha, Integer color, float alpha, double fontScale, double rotation)
    {
        drawLabel(text, x, y, HAlign.Center, VAlign.Middle, bgColor, bgAlpha, color, alpha, fontScale, true, rotation);
    }

    /**
     * Draw a text key, aligned on x,z.  If bgColor not null,
     * a rectangle will be drawn behind the text.
     *
     * @param text
     * @param x
     * @param y
     * @param hAlign
     * @param vAlign
     * @param bgColor
     * @param bgAlpha
     * @param color
     * @param alpha
     * @param fontScale
     * @param fontShadow
     */
    public static void drawLabel(final String text, double x, double y, final HAlign hAlign, final VAlign vAlign, Integer bgColor, float bgAlpha, int color, float alpha, double fontScale, boolean fontShadow)
    {
        drawLabel(text, x, y, hAlign, vAlign, bgColor, bgAlpha, color, alpha, fontScale, fontShadow, 0);
    }

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

        final FontRenderer fontRenderer = ForgeHelper.INSTANCE.getFontRenderer();

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

    public static void drawLabel(final String text, double x, double y, final HAlign hAlign, final VAlign vAlign, Integer bgColor, float bgAlpha, Integer color, float alpha, double fontScale, boolean fontShadow, double rotation)
    {
        double bgWidth = 0;
        double bgHeight = 0;
        if (bgColor != null && bgAlpha > 0)
        {
            final FontRenderer fontRenderer = ForgeHelper.INSTANCE.getFontRenderer();
            bgWidth = fontRenderer.getStringWidth(text);
            bgHeight = getLabelHeight(fontRenderer, fontShadow);
        }

        drawLabel(text, x, y, hAlign, vAlign, bgColor, bgAlpha, bgWidth, bgHeight, color, alpha, fontScale, fontShadow, rotation);
    }

    /**
     * Draw a text key, aligned on x,z.  If bgColor not null,
     * a rectangle will be drawn behind the text.
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

        final FontRenderer fontRenderer = ForgeHelper.INSTANCE.getFontRenderer();
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

    public static int getLabelHeight(FontRenderer fr, boolean fontShadow)
    {
        final int vpad = fr.getUnicodeFlag() ? 0 : fontShadow ? 6 : 4;
        return fr.FONT_HEIGHT + vpad;
    }

    public static void drawImage(TextureImpl texture, double x, double y, boolean flip, float scale, double rotation)
    {
        drawQuad(texture, x, y, (texture.getWidth() * scale), (texture.getHeight() * scale), flip, rotation);
    }

    public static void drawImage(TextureImpl texture, float alpha, double x, double y, boolean flip, float scale, double rotation)
    {
        drawQuad(texture, 0xffffff, alpha, x, y, (texture.getWidth() * scale), (texture.getHeight() * scale), false, rotation);
    }

    public static void drawClampedImage(TextureImpl texture, double x, double y, float scale, double rotation)
    {
        drawClampedImage(texture, 0xffffff, 1f, x, y, scale, rotation);
    }

    public static void drawClampedImage(TextureImpl texture, int color, float alpha, double x, double y, float scale, double rotation)
    {
        drawQuad(texture, color, alpha, x, y, (texture.getWidth() * scale), (texture.getHeight() * scale), false, rotation);
    }

    public static void drawColoredImage(TextureImpl texture, int color, float alpha, double x, double y, float scale, double rotation)
    {
        drawQuad(texture, color, alpha, x, y, (texture.getWidth() * scale), (texture.getHeight() * scale), false, rotation);
    }

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

    public static void drawColoredImage(TextureImpl texture, int color, float alpha, double x, double y, double rotation)
    {
        drawQuad(texture, color, alpha, x, y, texture.getWidth(), texture.getHeight(), false, rotation);
    }

    public static void drawColoredImage(TextureImpl texture, int color, float alpha, double x, double y, int width, int height, double rotation)
    {
        drawQuad(texture, color, alpha, x, y, width, height, false, rotation);
    }

    public static void drawQuad(TextureImpl texture, final double x, final double y, final double width, final double height, boolean flip, double rotation)
    {
        drawQuad(texture, 0xffffff, 1f, x, y, width, height, 0, 0, 1, 1, rotation, flip, true, GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, false);
    }

    public static void drawQuad(TextureImpl texture, int color, float alpha, final double x, final double y, final double width, final double height, boolean flip, double rotation)
    {
        drawQuad(texture, color, alpha, x, y, width, height, 0, 0, 1, 1, rotation, flip, true, GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, false);
    }


    /**
     * @param texture
     * @param x
     * @param y
     * @param minU
     * @param minV
     * @param maxU
     * @param maxV
     * @param width
     * @param height
     * @param color
     * @param alpha
     * @param flip
     * @param glBlendSfactor For normal alpha blending: GL11.GL_SRC_ALPHA
     * @param glBlendDFactor For normal alpha blending: GL11.GL_ONE_MINUS_SRC_ALPHA
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

                // Move origin to center of texture
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
     * @param x
     * @param y
     * @param heading
     * @param flipInsteadOfRotate
     * @param texture
     */
    public static void drawEntity(double x, double y, double heading, boolean flipInsteadOfRotate, TextureImpl texture, float scale, double rotation)
    {
        drawEntity(x, y, heading, flipInsteadOfRotate, texture, 1f, scale, rotation);
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
     */
    public static void drawEntity(double x, double y, double heading, boolean flipInsteadOfRotate, TextureImpl texture, float alpha, float scale, double rotation)
    {
        // Adjust to scale
        double width = (texture.getWidth() * scale);
        double height = (texture.getHeight() * scale);
        double drawX = x - (width / 2);
        double drawY = y - (height / 2);

        if (flipInsteadOfRotate)
        {
            boolean flip = (heading % 180) < 90;
            drawImage(texture, alpha, drawX, drawY, flip, scale, -rotation);
        }
        else
        {
            // Draw texture in rotated position
            drawImage(texture, alpha, drawX, drawY, false, scale, heading);
        }
    }

    /**
     * Draw the entity's location and heading on the overlay image
     * using the provided icon.
     */
    public static void drawColoredEntity(double x, double y, TextureImpl texture, int color, float alpha, float scale, double rotation)
    {
        // Adjust to scale
        double width = (texture.getWidth() * scale);
        double height = (texture.getHeight() * scale);
        double drawX = x - (width / 2);
        double drawY = y - (height / 2);

        // Draw texture in rotated position
        drawColoredImage(texture, color, alpha, drawX, drawY, scale, rotation);
    }

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

    public static void draw()
    {
        tessellator.draw();
    }

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

    public static void addVertexWithUV(double x, double y, double z, double u, double v)
    {
        // 1.7.10
        // tessellator.addVertexWithUV(x,y,z,u,v);

        // 1.8
        // worldrenderer.addVertexWithUV(x, y, z, u, v);

        // 1.8.8
        worldrenderer.pos(x, y, z).tex(u, v).endVertex();
    }

    public static void addVertex(double x, double y, double z, int[] rgba)
    {
        // 1.7 and 1.8
        // tessellator.setColorRGBA_I(rgb, a);
        // worldrenderer.addVertexWithUV(x, y, z, u, v);

        // 1.8.8
        worldrenderer.pos(x, y, z).tex(1, 1).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
    }

    public static void addVertexWithUV(double x, double y, double z, double u, double v, int[] rgba)
    {
        // 1.7 and 1.8
        // tessellator.setColorRGBA_I(rgb, a);
        // worldrenderer.addVertexWithUV(x, y, z, u, v);

        // 1.8.8
        worldrenderer.pos(x, y, z).tex(u, v).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
    }

    public enum HAlign
    {
        Left, Center, Right
    }

    public enum VAlign
    {
        Above, Middle, Below
    }
}
