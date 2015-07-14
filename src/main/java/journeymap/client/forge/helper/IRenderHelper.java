/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.forge.helper;

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
}
