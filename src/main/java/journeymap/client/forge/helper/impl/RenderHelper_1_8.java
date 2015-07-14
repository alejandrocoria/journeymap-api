/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.forge.helper.impl;

import journeymap.client.forge.helper.IRenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;

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
}
