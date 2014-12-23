package net.techbrew.journeymap.render.map;

import net.minecraft.client.renderer.Tessellator;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.StatTimer;
import net.techbrew.journeymap.model.RegionCoord;
import net.techbrew.journeymap.model.RegionImageCache;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.render.texture.TextureImpl;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Objects;

/**
 * Created by Mark on 12/10/2014.
 */
public class TileDrawStep
{
    private final Logger logger = JourneyMap.getLogger();
    private final boolean debug = logger.isTraceEnabled();
    RegionCoord regionCoord;
    Constants.MapType mapType;

    int dx1, dy1, dx2, dy2;
    int sx1, sy1, sx2, sy2;
    TextureImpl textureImpl;

    public TileDrawStep()
    {
    }

    public void setContext(final Constants.MapType mapType, RegionCoord regionCoord, boolean forceRefresh)
    {
        if (!forceRefresh)
        {
            forceRefresh = (textureImpl == null) || (mapType != this.mapType) || !Objects.equals(regionCoord, this.regionCoord);
        }

        this.mapType = mapType;
        this.regionCoord = regionCoord;

        if (forceRefresh)
        {
            forceRefreshTexture();
        }
    }

    public void setCoordinates(int dx1, int dy1, int dx2, int dy2,
                               int sx1, int sy1, int sx2, int sy2)
    {
        this.dx1 = dx1;
        this.dx2 = dx2;
        this.dy1 = dy1;
        this.dy2 = dy2;
        this.sx1 = sx1;
        this.sx2 = sx2;
        this.sy1 = sy1;
        this.sy2 = sy2;
    }

    void forceRefreshTexture()
    {
        StatTimer timer = StatTimer.get("TileDrawStep.forceRefreshTexture").start();
        textureImpl = RegionImageCache.getInstance().getRegionTexture(regionCoord, mapType, true);
        timer.stop();
    }

    void draw(final TilePos pos, final double offsetX, final double offsetZ, float alpha, int textureFilter, int textureWrap, boolean showGrid)
    {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        if (textureImpl != null)
        {
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureImpl.getGlTextureId());

            GL11.glColor4f(1, 1, 1, alpha);

            //http://gregs-blog.com/2008/01/17/opengl-texture-filter-parameters-explained/
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, textureWrap);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, textureWrap);

            final double startX = offsetX + pos.startX;
            final double startY = offsetZ + pos.startZ;
            final double endX = offsetX + pos.endX;
            final double endY = offsetZ + pos.endZ;
            final double z = 0D;

            final double startU = sx1 / 512D;
            final double startV = sy1 / 512D;
            final double endU = sx2 / 512D;
            final double endV = sy2 / 512D;

            Tessellator tessellator = Tessellator.instance;
            tessellator.startDrawingQuads();
            tessellator.addVertexWithUV(startX, endY, z, startU, endV);
            tessellator.addVertexWithUV(endX, endY, z, endU, endV);
            tessellator.addVertexWithUV(endX, startY, z, endU, startV);
            tessellator.addVertexWithUV(startX, startY, z, startU, startV);
            tessellator.draw();

            if (debug)
            {
                DrawUtil.drawLabel("DrawStep " + pos.toString(), startX, startY, DrawUtil.HAlign.Right, DrawUtil.VAlign.Below, Color.WHITE, 255, Color.BLUE, 255, 1.0, false);
                DrawUtil.drawRectangle(startX, startY, 2, endV * 512, Color.white, 200);
                DrawUtil.drawRectangle(startX, startY, endU * 512, 2, Color.red, 200);
            }
        }

        if (showGrid)
        {
            // TODO
        }
    }

    public boolean hasTexture()
    {
        return textureImpl != null;
    }

    public void clear()
    {
        textureImpl = null;
    }

//    public boolean isDirtySince(long time)
//    {
//        return RegionImageCache.getInstance().isDirtySince(regionCoord, mapType, time);
//    }

    public void refreshIfDirty(long since)
    {
        if (RegionImageCache.getInstance().isDirtySince(regionCoord, mapType, since))
        {
            forceRefreshTexture();
        }
    }
}
