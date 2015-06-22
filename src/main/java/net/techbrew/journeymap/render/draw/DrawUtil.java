/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.render.draw;


import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.techbrew.journeymap.render.texture.TextureImpl;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.*;

/**
 * Useful drawing routines that utilize the Minecraft Tessellator.
 */
public class DrawUtil
{
    public static double zLevel = 0;

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
    public static void drawCenteredLabel(final String text, double x, double y, Color bgColor, int bgAlpha, Color color, int alpha, double fontScale)
    {
        drawLabel(text, x, y, HAlign.Center, VAlign.Middle, bgColor, bgAlpha, color, alpha, fontScale, true, 0);
    }

    /**
     * Draw a text key, centered on x,z.  If bgColor not null,
     * a rectangle will be drawn behind the text.
     */
    public static void drawCenteredLabel(final String text, double x, double y, Color bgColor, int bgAlpha, Color color, int alpha, double fontScale, boolean fontShadow)
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
    public static void drawCenteredLabel(final String text, double x, double y, Color bgColor, int bgAlpha, Color color, int alpha, double fontScale, double rotation)
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
    public static void drawLabel(final String text, double x, double y, final HAlign hAlign, final VAlign vAlign, Color bgColor, int bgAlpha, Color color, int alpha, double fontScale, boolean fontShadow)
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
    public static void drawLabel(final String text, double x, double y, final HAlign hAlign, final VAlign vAlign, Color bgColor, int bgAlpha, Color color, int alpha, double fontScale, boolean fontShadow, double rotation)
    {
        if (text == null || text.length() == 0)
        {
            return;
        }

        Minecraft mc = FMLClientHandler.instance().getClient();
        final FontRenderer fontRenderer = mc.fontRenderer;
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
                GL11.glScaled(fontScale, fontScale, 0);
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

            // Draw the string
            if (color.getTransparency() != alpha)
            {
                color = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
            }

            if (fontShadow)
            {
                fontRenderer.drawStringWithShadow(text, intTextX, intTextY, color.getRGB());
            }
            else
            {
                fontRenderer.drawString(text, intTextX, intTextY, color.getRGB());
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

    private static void drawQuad(TextureImpl texture, float alpha, final double x, final double y, final double width, final double height, boolean flip, double rotation)
    {
        drawQuad(texture, x, y, width, height, rotation, null, alpha, flip, true, GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, false);
    }

    private static void drawQuad(TextureImpl texture, final double x, final double y, final double width, final double height, boolean flip, double rotation)
    {
        drawQuad(texture, x, y, width, height, rotation, null, 1f, flip, true, GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, false);
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
    public static void drawQuad(TextureImpl texture, final double x, final double y, final double width, final double height, double rotation, Color color, float alpha, boolean flip, boolean blend, int glBlendSfactor, int glBlendDFactor, boolean clampTexture)
    {
        GL11.glPushMatrix();

        if (blend)
        {
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(glBlendSfactor, glBlendDFactor, 1, 0);
        }

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getGlTextureId());

        if (blend)
        {
            if (color != null)
            {
                float[] c = color.getColorComponents(null);
                GL11.glColor4f(c[0], c[1], c[2], alpha);
            }
            else
            {
                GL11.glColor4f(1, 1, 1, alpha);
            }
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
            GL11.glTranslated(transX, transY, 0);

            // Rotatate around origin
            GL11.glRotated(rotation, 0, 0, 1.0f);

            // Return origin
            GL11.glTranslated(-transX, -transY, 0);
        }

        final int direction = flip ? -1 : 1;

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, height + y, zLevel, 0, 1);
        tessellator.addVertexWithUV(x + width, height + y, zLevel, direction, 1);
        tessellator.addVertexWithUV(x + width, y, zLevel, direction, 0);
        tessellator.addVertexWithUV(x, y, zLevel, 0, 0);
        tessellator.draw();

        // Ensure normal alpha blending afterward, just in case
        if (blend)
        {
            if (glBlendSfactor != GL11.GL_SRC_ALPHA || glBlendDFactor != GL11.GL_ONE_MINUS_SRC_ALPHA)
            {
                GL11.glEnable(GL11.GL_BLEND);
                OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
            }
        }

        GL11.glPopMatrix();
    }

    public static void drawRectangle(double x, double y, double width, double height, Color color, int alpha)
    {
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA(color.getRed(), color.getGreen(), color.getBlue(), alpha);
        tessellator.addVertexWithUV(x, height + y, zLevel, 0, 1);
        tessellator.addVertexWithUV(x + width, height + y, zLevel, 1, 1);
        tessellator.addVertexWithUV(x + width, y, zLevel, 1, 0);
        tessellator.addVertexWithUV(x, y, zLevel, 0, 0);
        tessellator.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_BLEND);
    }

    /**
     * Draws a rectangle with a vertical gradient between the specified colors.
     * 0, top, this.width, this.height - top, -1072689136, -804253680
     */
    public static void drawGradientRect(double x, double y, double width, double height, Color startColor, int startAlpha, Color endColor, int endAlpha)
    {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        GL11.glShadeModel(GL11.GL_SMOOTH);

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_I(endColor.getRGB(), endAlpha);
        tessellator.addVertexWithUV(x, height + y, zLevel, 0, 1);
        tessellator.addVertexWithUV(x + width, height + y, zLevel, 1, 1);
        tessellator.setColorRGBA_I(startColor.getRGB(), startAlpha);
        tessellator.addVertexWithUV(x + width, y, zLevel, 1, 0);
        tessellator.addVertexWithUV(x, y, zLevel, 0, 0);
        tessellator.draw();

        GL11.glShadeModel(GL11.GL_FLAT);

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_BLEND);
    }

    public static void drawBoundTexture(double startU, double startV, double startX, double startY, double z, double endU, double endV, double endX, double endY)
    {
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(startX, endY, z, startU, endV);
        tessellator.addVertexWithUV(endX, endY, z, endU, endV);
        tessellator.addVertexWithUV(endX, startY, z, endU, startV);
        tessellator.addVertexWithUV(startX, startY, z, startU, startV);
        tessellator.draw();
    }

    public static void drawImage(TextureImpl texture, double x, double y, boolean flip, float alpha, float scale, double rotation)
    {
        drawQuad(texture, alpha, x, y, (texture.getWidth() * scale), (texture.getHeight() * scale), flip, rotation);
    }

    public static void drawImage(TextureImpl texture, double x, double y, boolean flip, float scale, double rotation)
    {
        drawQuad(texture, x, y, (texture.getWidth() * scale), (texture.getHeight() * scale), flip, rotation);
    }

    public static void drawClampedImage(TextureImpl texture, double x, double y, float scale, double rotation)
    {
        drawClampedImage(texture, null, x, y, scale, 1f, rotation);
    }

    public static void drawClampedImage(TextureImpl texture, Color color, double x, double y, float scale, float alpha, double rotation)
    {
        drawQuad(texture, x, y, (texture.getWidth() * scale), (texture.getHeight() * scale), rotation, color, alpha, false, true, GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, true);
    }

    public static void drawColoredImage(TextureImpl texture, int alpha, Color color, double x, double y, float scale, double rotation)
    {
        drawQuad(texture, x, y, (texture.getWidth() * scale), (texture.getHeight() * scale), rotation, color, alpha, false, true, GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, false);
    }

    public static void drawColoredImage(TextureImpl texture, int alpha, Color color, double x, double y, double rotation)
    {
        drawQuad(texture, x, y, texture.getWidth(), texture.getHeight(), rotation, color, alpha, false, true, GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, false);
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
            drawImage(texture, drawX, drawY, flip, alpha, scale, -rotation);
        }
        else
        {
            // Draw texture in rotated position
            drawImage(texture, drawX, drawY, false, alpha, scale, heading);
        }
    }

    public static void sizeDisplay(double width, double height)
    {
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0D, width, height, 0.0D, 1000.0D, 3000.0D);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        GL11.glTranslatef(0.0F, 0.0F, -2000.0F);
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
