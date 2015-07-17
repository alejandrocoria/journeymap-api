/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.forge.helper;

import java.awt.*;

/**
 * Encapsulates setting up vertices for a Tesselator.
 */
public interface IRenderHelper
{
    public void startDrawingQuads();
    public void addVertex(double x, double y, double z);
    public void addVertexWithUV(double x, double y, double z, double u, double v);
    public void setColorRGBA_F(float r, float g, float b, float a);
    public void setColorRGBA(int r, int g, int b, int a);
    public void setColorRGBA_I(int rgb, int a);
    public void draw();

    public void glEnableBlend();
    public void glDisableBlend();
    public void glEnableTexture2D();
    public void glDisableTexture2D();
    public void glEnableAlpha();
    public void glDisableAlpha();
    public void glBlendFunc(int sfactorRGB, int dfactorRGB, int sfactorAlpha, int dfactorAlpha);
    public void glBlendFunc(int sfactorRGB, int dfactorRGB);
    public void glColor(Color color, int alpha);
    public void glColor4f(float r, float g, float b, float a);
    public void glShadeModel(int model);
    public void glBindTexture(int glid);
    public void glDisableDepth();
    public void glEnableDepth();
    public void glDepthMask(boolean enable);
    public void glEnableLighting();
    public void glDisableLighting();
    public void glEnableFog();
    public void glDisableFog();
    public void glEnableCull();
    public void glDisableCull();
}
