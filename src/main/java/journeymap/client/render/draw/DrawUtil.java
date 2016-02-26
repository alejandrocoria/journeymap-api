/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.render.draw;


import journeymap.client.api.model.ShapeProperties;
import journeymap.client.cartography.RGB;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.forge.helper.IRenderHelper;
import journeymap.client.render.texture.TextureImpl;
import net.minecraft.client.gui.FontRenderer;
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
    private static IRenderHelper renderHelper = ForgeHelper.INSTANCE.getRenderHelper();

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
     * @param rotation
     */
    public static void drawLabel(final String text, double x, double y, final HAlign hAlign, final VAlign vAlign, Integer bgColor, float bgAlpha, Integer color, float alpha, double fontScale, boolean fontShadow, double rotation)
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
        final int width = fontRenderer.getStringWidth(text);
        int height = drawRect ? getLabelHeight(fontRenderer, fontShadow) : fontRenderer.FONT_HEIGHT;

        if (!drawRect && fontRenderer.getUnicodeFlag())
        {
            height--;
        }

        GL11.glPushMatrix();

        try
        {
            if (fontScale != 1)
            {
                x = x / fontScale;
                y = y / fontScale;
                renderHelper.glScaled(fontScale, fontScale, 0);
            }

            double textX = x;
            double textY = y;
            double rectX = x;
            double rectY = y;

            switch (hAlign)
            {
                case Left:
                {
                    textX = x - width;
                    break;
                }
                case Center:
                {
                    textX = x - (width / 2) + (fontScale > 1 ? .5 : 0);
                    break;
                }
                case Right:
                {
                    textX = x;
                    break;
                }
            }

            double vpad = drawRect ? (height - fontRenderer.FONT_HEIGHT) / 2.0 : 0;

            switch (vAlign)
            {
                case Above:
                {
                    rectY = y - height;
                    textY = rectY + vpad + (fontRenderer.getUnicodeFlag() ? 0 : 1);
                    break;
                }
                case Middle:
                {
                    rectY = y - (height / 2) + (fontScale > 1 ? .5 : 0);
                    textY = rectY + vpad;
                    break;
                }
                case Below:
                {
                    rectY = y;
                    textY = rectY + vpad;
                    break;
                }
            }

            if (rotation != 0)
            {
                // Move origin to x,y
                GL11.glTranslated(x, y, 0);

                // Rotatate around origin
                GL11.glRotated(-rotation, 0, 0, 1.0f);

                // Offset the radius
                GL11.glTranslated(-x, -y, 0);
            }

            // Draw background
            if (bgColor != null && bgAlpha > 0)
            {
                final int hpad = 2;
                final double rectHeight = getLabelHeight(fontRenderer, fontShadow);
                drawRectangle(textX - hpad - .5, rectY, width + (2 * hpad), rectHeight, bgColor, bgAlpha);
            }

            // String positioning uses ints
            int intTextX = (int) Math.floor(textX);
            int intTextY = (int) Math.floor(textY);
            double dTextX = textX - intTextX;
            double dTextY = textY - intTextY;

            // Use translation for the double precision
            GL11.glTranslated(dTextX, dTextY, 0);

            if (fontShadow)
            {
                fontRenderer.drawStringWithShadow(text, intTextX, intTextY, color);
            }
            else
            {
                fontRenderer.drawString(text, intTextX, intTextY, color);
            }

        }
        finally
        {
            GL11.glPopMatrix();
        }
    }

    public static int getLabelHeight(FontRenderer fr, boolean fontShadow)
    {
        final int vpad = fr.getUnicodeFlag() ? 0 : fontShadow ? 3 : 2;
        return fr.FONT_HEIGHT + (2 * vpad);
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
        GL11.glPushMatrix();

        try
        {
            if (blend)
            {
                renderHelper.glEnableBlend();
                renderHelper.glBlendFunc(glBlendSfactor, glBlendDFactor, 1, 0);
            }

            renderHelper.glEnableTexture2D();
            renderHelper.glBindTexture(texture.getGlTextureId());

            if(alpha>1)
            {
                // TODO: There shouldn't be any more cases of this, but a breakpoint here is prudent until I'm sure.
                alpha = alpha/255f;
            }

            if (blend)
            {
                float[] c = RGB.floats(color);

                renderHelper.glColor4f(c[0], c[1], c[2], alpha);
            }
            else
            {
                renderHelper.glColor4f(1, 1, 1, alpha);
            }

            renderHelper.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            renderHelper.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

            int texEdgeBehavior = clampTexture ? GL12.GL_CLAMP_TO_EDGE : GL11.GL_REPEAT;
            renderHelper.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, texEdgeBehavior);
            renderHelper.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, texEdgeBehavior);

            if (rotation != 0)
            {
                double transX = x + (width / 2);
                double transY = y + (height / 2);

                // Move origin to center of texture
                GL11.glTranslated(transX, transY, 0);

                // Rotatate around origin
                GL11.glRotated(rotation, 0, 0, 1.0f);

                // Return origin
                GL11.glTranslated(-transX, -transY, 0);
            }

            final double direction = flip ? -maxU : maxU;

            renderHelper.startDrawingQuads(false);
            renderHelper.addVertexWithUV(x, height + y, zLevel, minU, maxV);
            renderHelper.addVertexWithUV(x + width, height + y, zLevel, direction, maxV);
            renderHelper.addVertexWithUV(x + width, y, zLevel, direction, minV);
            renderHelper.addVertexWithUV(x, y, zLevel, minU, minV);
            renderHelper.draw();

            // Ensure normal alpha blending afterward, just in case
            if (blend)
            {
                renderHelper.glColor4f(1, 1, 1, 1);
                if (glBlendSfactor != GL11.GL_SRC_ALPHA || glBlendDFactor != GL11.GL_ONE_MINUS_SRC_ALPHA)
                {
                    renderHelper.glEnableBlend();
                    renderHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
                }
            }
        }
        finally
        {
            GL11.glPopMatrix();
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
        renderHelper.glEnableBlend();
        renderHelper.glDisableTexture2D();
        renderHelper.glDisableAlpha();
        renderHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        // Draw
        int[] rgba = RGB.ints(color, alpha);
        renderHelper.startDrawingQuads(true);
        renderHelper.addVertex(x, height + y, zLevel, rgba);
        renderHelper.addVertex(x + width, height + y, zLevel, rgba);
        renderHelper.addVertex(x + width, y, zLevel, rgba);
        renderHelper.addVertex(x, y, zLevel, rgba);
        renderHelper.draw();

        // Clean up
        renderHelper.glColor4f(1, 1, 1, 1);
        renderHelper.glEnableTexture2D();
        renderHelper.glEnableAlpha();
        renderHelper.glDisableBlend();
    }

    public static void drawPolygon(double xOffset, double yOffset, List<Point2D.Double> screenPoints, ShapeProperties shapeProperties)
    {
        // Prep
        renderHelper.glEnableBlend();
        renderHelper.glDisableTexture2D();
        renderHelper.glEnableAlpha();
        renderHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        // Draw Fill
        if (shapeProperties.getFillOpacity() >= 0.01F)
        {
            float[] rgba = RGB.floats(shapeProperties.getFillColor(), shapeProperties.getFillOpacity());
            renderHelper.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);

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
            renderHelper.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
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
        renderHelper.glColor4f(1, 1, 1, 1);
        renderHelper.glEnableTexture2D();
        renderHelper.glEnableAlpha();
        renderHelper.glDisableBlend();
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

        renderHelper.glDisableTexture2D();
        renderHelper.glEnableBlend();
        renderHelper.glDisableAlpha();
        renderHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        renderHelper.glShadeModel(GL11.GL_SMOOTH);

        renderHelper.startDrawingQuads(true);
        renderHelper.addVertexWithUV(x, height + y, zLevel, 0, 1, rgbaEnd);
        renderHelper.addVertexWithUV(x + width, height + y, zLevel, 1, 1, rgbaEnd);
        renderHelper.addVertexWithUV(x + width, y, zLevel, 1, 0, rgbaStart);
        renderHelper.addVertexWithUV(x, y, zLevel, 0, 0, rgbaStart);
        renderHelper.draw();

        renderHelper.glShadeModel(GL11.GL_FLAT);

        renderHelper.glEnableTexture2D();
        renderHelper.glEnableAlpha();
        renderHelper.glEnableBlend();
    }

    public static void drawBoundTexture(double startU, double startV, double startX, double startY, double z, double endU, double endV, double endX, double endY)
    {
        renderHelper.startDrawingQuads(false);
        renderHelper.addVertexWithUV(startX, endY, z, startU, endV);
        renderHelper.addVertexWithUV(endX, endY, z, endU, endV);
        renderHelper.addVertexWithUV(endX, startY, z, endU, startV);
        renderHelper.addVertexWithUV(startX, startY, z, startU, startV);
        renderHelper.draw();
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

    public static void sizeDisplay(double width, double height)
    {
        renderHelper.sizeDisplay(width, height);
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
