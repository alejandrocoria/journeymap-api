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
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;

import java.awt.*;

/**
 * Encapsulates setting up vertices for a Tesselator
 */
public class RenderHelper_1_7_10 implements IRenderHelper
{

    Tessellator tessellator = Tessellator.instance;

    @Override
    public void startDrawingQuads()
    {
        // 1.7
        tessellator.startDrawingQuads();
    }

    @Override
    public void addVertex(double x, double y, double z)
    {
        // 1.7
        tessellator.addVertex(x, y, z);
    }

    @Override
    public void addVertexWithUV(double x, double y, double z, double u, double v)
    {
        // 1.7
        tessellator.addVertexWithUV(x, y, z, u, v);
    }

    @Override
    public void setColorRGBA_F(float r, float g, float b, float a)
    {
        // 1.7
        tessellator.setColorRGBA_F(r, g, b, a);
    }

    @Override
    public void setColorRGBA(int r, int g, int b, int a)
    {
        // 1.7
        tessellator.setColorRGBA_F(r, g, b, a);
    }

    @Override
    public void setColorRGBA_I(int rgb, int a)
    {
        // 1.7
        tessellator.setColorRGBA_I(rgb, a);
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
        GL11.glEnable(GL11.GL_BLEND);
    }

    @Override
    public void glDisableBlend()
    {
        // 1.7
        GL11.glDisable(GL11.GL_BLEND);
    }

    @Override
    public void glEnableTexture2D()
    {
        // 1.7
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    @Override
    public void glDisableTexture2D()
    {
        // 1.7
        GL11.glDisable(GL11.GL_TEXTURE_2D);
    }

    @Override
    public void glEnableAlpha()
    {
        // 1.7
        GL11.glEnable(GL11.GL_ALPHA_TEST);
    }

    @Override
    public void glDisableAlpha()
    {
        // 1.7
        GL11.glDisable(GL11.GL_ALPHA_TEST);
    }

    @Override
    public void glBlendFunc(int sfactorRGB, int dfactorRGB, int sfactorAlpha, int dfactorAlpha)
    {
        // 1.7
        OpenGlHelper.glBlendFunc(sfactorRGB, dfactorRGB, sfactorAlpha, dfactorAlpha);
    }

    @Override
    public void glBlendFunc(int sfactorRGB, int dfactorRGB)
    {
        // 1.7
        GL11.glBlendFunc(sfactorRGB, dfactorRGB);
    }

    @Override
    public void glColor(Color color, int alpha)
    {
        float[] rgb = RGB.floats(color.getRGB());

        // 1.7
        GL11.glColor4f(rgb[0], rgb[1], rgb[2], alpha / 255f);
    }

    @Override
    public void glColor4f(float r, float g, float b, float a)
    {
        // 1.7
        GL11.glColor4f(r, g, b, a);
    }

    @Override
    public void glClearColor(float r, float g, float b, float a)
    {
        // 1.7
        GL11.glClearColor(r, g, b, a);
    }

    @Override
    public void glColorMask(boolean r, boolean g, boolean b, boolean a)
    {
        // 1.7
        GL11.glColorMask(r, g, b, a);
    }

    @Override
    public void glTexParameteri(int target, int pname, int param)
    {
        GL11.glTexParameteri(target, pname, param);
    }

    @Override
    public void glScaled(double x, double y, double z)
    {
        // 1.7
        GL11.glScaled(x, y, z);
    }

    @Override
    public void glDepthFunc(int func)
    {
        // 1.7
        GL11.glDepthFunc(func);
    }

    @Override
    public void glShadeModel(int model)
    {
        // 1.7
        GL11.glShadeModel(model);
    }

    @Override
    public void glBindTexture(int glid)
    {
        // 1.7
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, glid);
    }

    @Override
    public void glDisableDepth()
    {
        // 1.7
        GL11.glDisable(GL11.GL_DEPTH_TEST);
    }

    @Override
    public void glEnableDepth()
    {
        // 1.7
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    @Override
    public void glDepthMask(boolean enable)
    {
        // 1.7
        GL11.glDepthMask(enable);
    }

    @Override
    public void glEnableLighting()
    {
        // 1.7
        GL11.glEnable(GL11.GL_LIGHTING);
    }

    @Override
    public void glDisableLighting()
    {
        // 1.7
        GL11.glDisable(GL11.GL_LIGHTING);
    }

    @Override
    public void glEnableFog()
    {
        // 1.7
        GL11.glEnable(GL11.GL_FOG);
    }

    @Override
    public void glDisableFog()
    {
        // 1.7
        GL11.glDisable(GL11.GL_FOG);
    }

    @Override
    public void glEnableCull()
    {
        // 1.7
        GL11.glEnable(GL11.GL_CULL_FACE);
    }

    @Override
    public void glDisableCull()
    {
        // 1.7
        GL11.glDisable(GL11.GL_CULL_FACE);
    }

    @Override
    public void glDeleteTextures(int textureId)
    {
        // 1.7
        GL11.glDeleteTextures(textureId);
    }
}
