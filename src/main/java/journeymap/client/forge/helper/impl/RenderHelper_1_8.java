/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.forge.helper.impl;

import journeymap.client.cartography.RGB;
import journeymap.client.forge.helper.IRenderHelper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;

import java.awt.*;

/**
 * Encapsulates setting up vertices for a Tesselator
 */
public class RenderHelper_1_8 implements IRenderHelper
{

    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer worldrenderer = tessellator.getWorldRenderer();

    @Override
    public void startDrawingQuads()
    {
        // 1.7
        // tessellator.startDrawingQuads();

        // 1.8
        worldrenderer.startDrawingQuads();
    }

    @Override
    public void addVertex(double x, double y, double z)
    {
        // 1.7
        // tessellator.addVertex(x,y,z);

        // 1.8
        worldrenderer.addVertex(x,y,z);
    }

    @Override
    public void addVertexWithUV(double x, double y, double z, double u, double v)
    {
        // 1.7
        // tessellator.addVertexWithUV(x,y,z,u,v);

        // 1.8
        worldrenderer.addVertexWithUV(x,y,z,u,v);
    }

    @Override
    public void setColorRGBA_F(float r, float g, float b, float a)
    {
        // 1.7
        // tessellator.setColorRGBA_F(x,y,z);

        // 1.8
        worldrenderer.setColorRGBA_F(r,g,b,a);
    }

    @Override
    public void setColorRGBA(int r, int g, int b, int a)
    {
        // 1.7
        // tessellator.setColorRGBA_F(x,y,z);

        // 1.8
        worldrenderer.setColorRGBA_F(r,g,b,a);
    }

    @Override
    public void setColorRGBA_I(int rgb, int a)
    {
        // 1.7
        // tessellator.setColorRGBA_I(rgb, a);

        // 1.8
        worldrenderer.setColorRGBA_I(rgb, a);
    }

    @Override
    public void draw()
    {
        tessellator.draw();
    }

    @Override
    public void glEnableBlend()
    {
        // 1.7
        // GL11.glEnable(GL11.GL_BLEND);

        // 1.8
        GlStateManager.enableBlend();
    }

    @Override
    public void glDisableBlend()
    {
        // 1.7
        // GL11.glDisable(GL11.GL_BLEND);

        // 1.8
        GlStateManager.disableBlend();
    }

    @Override
    public void glEnableTexture2D()
    {
        // 1.7
        // GL11.glEnable(GL11.GL_TEXTURE_2D);

        // 1.8
        GlStateManager.enableTexture2D();
    }

    @Override
    public void glDisableTexture2D()
    {
        // 1.7
        // GL11.glDisable(GL11.GL_TEXTURE_2D);

        // 1.8
        GlStateManager.disableTexture2D();
    }

    @Override
    public void glEnableAlpha()
    {
        // 1.7
        // GL11.glEnable(GL11.GL_ALPHA_TEST);

        // 1.8
        GlStateManager.enableAlpha();
    }

    @Override
    public void glDisableAlpha()
    {
        // 1.7
        // GL11.glDisable(GL11.GL_ALPHA_TEST);

        // 1.8
        GlStateManager.disableAlpha();
    }

    @Override
    public void glBlendFunc(int sfactorRGB, int dfactorRGB, int sfactorAlpha, int dfactorAlpha)
    {
        // 1.7
        // OpenGlHelper.glBlendFunc(sfactorRGB, dfactorRGB, sfactorAlpha, dfactorAlpha);

        // 1.8
        GlStateManager.tryBlendFuncSeparate(sfactorRGB, dfactorRGB, sfactorAlpha, dfactorAlpha);
    }

    @Override
    public void glColor(Color color, int alpha)
    {
        float[] rgb = RGB.floats(color.getRGB());

        // 1.7
        // GL11.glColor4f(rgb[0], rgb[1], rgb[2], alpha/255f);

        // 1.8
        GlStateManager.color(rgb[0], rgb[1], rgb[2], alpha/255f);
    }

    @Override
    public void glColor4f(float r, float g, float b, float a)
    {
        // 1.7
        // GL11.glColor4f(r,g,b,a);

        // 1.8
        GlStateManager.color(r,g,b,a);
    }

    @Override
    public void glShadeModel(int model)
    {
        // 1.7
        // GL11.glShadeModel(model);

        // 1.8
        GlStateManager.shadeModel(model);
    }

    @Override
    public void glBindTexture(int glid)
    {
        // 1.7
        // GL11.glBindTexture(GL11.GL_TEXTURE_2D, glid);

        // 1.8
        GlStateManager.bindTexture(glid);
    }
}
