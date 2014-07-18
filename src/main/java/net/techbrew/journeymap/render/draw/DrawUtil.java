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

import java.awt.*;

/**
 * Useful drawing routines that utilize the Minecraft Tessellator.
 */
public class DrawUtil
{
    private static final float lightmapS = (float) (15728880 % 65536) / 1f;
    private static final float lightmapT = (float) (15728880 / 65536) / 1f;

    public static void resetLightMap()
    {
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightmapS, lightmapT);
    }

    /**
     * Draw a text label, centered on x,z.  If bgColor not null,
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
        drawLabel(text, x, y, HAlign.Center, VAlign.Middle, bgColor, bgAlpha, color, alpha, fontScale, true);
    }

    /**
     * Draw a text label, aligned on x,z.  If bgColor not null,
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

        if (text == null || text.length() == 0)
        {
            return;
        }

        Minecraft mc = FMLClientHandler.instance().getClient();
        final FontRenderer fontRenderer = mc.fontRenderer;
        final boolean drawRect = (bgColor != null && alpha > 0);
        final int width = fontRenderer.getStringWidth(text);
        final int height = drawRect ? getLabelHeight(fontRenderer, fontShadow) : fontRenderer.FONT_HEIGHT;

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
                    textX = x - (width / 2);
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
                    rectY = y - (height / 2);
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

    private static void drawQuad(TextureImpl texture, final double x, final double y, final int width, final int height, boolean flip)
    {
        drawQuad(texture, x, y, width, height, null, 1f, flip, true, GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    private static void drawQuad(TextureImpl texture, final double x, final double y, final int width, final int height, boolean flip, int glBlendSfactor, int glBlendDFactor)
    {
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
    public static void drawQuad(TextureImpl texture, final double x, final double y, final int width, final int height, Color color, float alpha, boolean flip, boolean blend, int glBlendSfactor, int glBlendDFactor)
    {

        if (blend)
        {
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(glBlendSfactor, glBlendDFactor); // normal alpha blending: GL11.GL_ONE_MINUS_SRC_ALPHA
            if (color != null)
            {
                float[] c = color.getColorComponents(null);
                GL11.glColor4f(c[0], c[1], c[2], alpha);
            }
            else
            {
                GL11.glColor4f(alpha, alpha, alpha, alpha);
            }
        }

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getGlTextureId());

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        final int direction = flip ? -1 : 1;

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, height + y, 0.0D, 0, 1);
        tessellator.addVertexWithUV(x + width, height + y, 0.0D, direction, 1);
        tessellator.addVertexWithUV(x + width, y, 0.0D, direction, 0);
        tessellator.addVertexWithUV(x, y, 0.0D, 0, 0);
        tessellator.draw();

        // Ensure normal alpha blending afterward, just in case
        if (blend)
        {
            if (glBlendSfactor != GL11.GL_SRC_ALPHA || glBlendDFactor != GL11.GL_ONE_MINUS_SRC_ALPHA)
            {
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            }
        }
    }

    public static void drawRectangle(double x, double y, double width, double height, Color color, int alpha)
    {

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
        GL11.glDisable(GL11.GL_BLEND);
    }

    public static void drawImage(TextureImpl texture, double x, double y, boolean flip, float scale)
    {
        drawQuad(texture, x, y, (int) (texture.width * scale), (int) (texture.height * scale), flip);
    }

    public static void drawImage(TextureImpl texture, double x, double y, boolean flip, int glBlendSfactor, int glBlendDfactor)
    {
        drawQuad(texture, x, y, texture.width, texture.height, flip, glBlendSfactor, glBlendDfactor);
    }

    public static void drawRotatedImage(TextureImpl texture, double x, double y, float heading, float scale)
    {

        // Start a new matrix for translation/rotation
        GL11.glPushMatrix();

        // Move origin to x,y
        GL11.glTranslated(x, y, 0);

        // Rotatate around origin
        GL11.glRotatef(heading, 0, 0, 1.0f);

        // Adjust to scale
        int width = (int) (texture.width * scale);
        int height = (int) (texture.height * scale);

        // Offset the radius
        GL11.glTranslated(-width, -height, 0);

        // Draw texture in rotated position
        drawQuad(texture, width / 2, height / 2, width, height, false);

        GL11.glDisable(GL11.GL_BLEND);

        // Drop out of the translated+rotated matrix
        GL11.glPopMatrix();
    }

    public static void drawColoredImage(TextureImpl texture, int alpha, Color color, double x, double y)
    {

        drawQuad(texture, x, y, texture.width, texture.height, color, alpha, false, true, GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

    }

    public static void drawColoredImage(TextureImpl texture, int alpha, Color color, double x, double y, boolean blend)
    {

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
    public static void drawEntity(double x, double y, Double heading, boolean flipInsteadOfRotate, TextureImpl texture, int bottomMargin, float scale)
    {

        if (heading == null)
        {
            drawImage(texture, x, y, false, scale);
        }
        else if (!flipInsteadOfRotate)
        {
            drawRotatedImage(texture, x, y, heading.floatValue(), scale);
        }
        else
        {
            boolean flip = heading < 90;
            int width = (int) (texture.width * scale);
            int height = (int) (texture.height * scale);
            drawImage(texture, x - (width / 2), y - (height / 2) - bottomMargin, flip, scale);
        }
    }

    public static boolean startUnicode(FontRenderer fr, boolean force)
    {
        if (!force)
        {
            return false;
        }

        boolean isUnicode = fr.getUnicodeFlag();
        if (!isUnicode)
        {
            fr.setUnicodeFlag(true);
            return true;
        }
        return false;
    }

    public static void stopUnicode(FontRenderer fr)
    {
        fr.setUnicodeFlag(false);
    }

    public enum HAlign
    {
        Left, Center, Right;
    }

    public enum VAlign
    {
        Above, Middle, Below;
    }

}
