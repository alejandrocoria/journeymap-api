package net.techbrew.journeymap.render.map;

import net.minecraft.client.renderer.Tessellator;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.model.RegionCoord;
import net.techbrew.journeymap.model.RegionImageCache;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.render.texture.DelayedTexture;
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.render.texture.TextureImpl;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.Future;

/**
 * Created by Mark on 12/10/2014.
 */
public class TileDrawStep
{
    private final Logger logger = JourneyMap.getLogger();
    private final boolean debug = logger.isTraceEnabled();
    RegionCoord regionCoord;
    Integer vSlice;
    int dimension;
    Boolean useCache;
    boolean allowNullImage;
    boolean showGrid;
    Constants.MapType mapType;
    int dx1, dy1, dx2, dy2;
    int sx1, sy1, sx2, sy2;
    long lastImageTime = 0;
    Future<DelayedTexture> futureTex;
    TextureImpl textureImpl;

    public TileDrawStep(RegionCoord regionCoord)
    {
        this.regionCoord = regionCoord;
    }

    public void setContext(Integer vSlice, final int dimension, final Boolean useCache, final boolean allowNullImage, boolean showGrid)
    {
        this.vSlice = vSlice;
        this.dimension = dimension;
        this.useCache = useCache;
        this.allowNullImage = allowNullImage;
        this.showGrid = showGrid;
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
        this.sy2 = sy2;
    }

    void draw(final TilePos pos, final double offsetX, final double offsetZ, float alpha, int textureFilter, int textureWrap)
    {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.getTexture().getGlTextureId());

        GL11.glColor4f(1, 1, 1, alpha);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, textureFilter);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, textureFilter);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, textureWrap);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, textureWrap);

        final double startX = offsetX + pos.startX;
        final double startY = offsetZ + pos.startZ;
        final double endX = offsetX + pos.endX;
        final double endY = offsetZ + pos.endZ;
        final double z = 0.0D;

        final double startU = sx1 / 512.0;
        final double startV = (sy2 - (sx2 - sx1)) / 512.0;
        final double endU = sx2 / 512.0;
        final double endV = sy2 / 512.0;

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

//            int pad = 3;
//            DrawUtil.drawLabel(String.format("TL %.0f, %.0f", startX, startZ), startX + pad, startZ + pad, DrawUtil.HAlign.Right, DrawUtil.VAlign.Below, Color.WHITE, 255, color, 255, 1.0, false);
//            DrawUtil.drawLabel(String.format("BR %.0f, %.0f", endX, endZ), endX - pad, endZ - pad, DrawUtil.HAlign.Left, DrawUtil.VAlign.Above, Color.WHITE, 255, color, 255, 1.0, false);

            DrawUtil.drawRectangle(startX, startY, 2, endV * 512, Color.white, 200);
            DrawUtil.drawRectangle(startX, startY, endU * 512, 2, Color.red, 200);

        }
    }

    public boolean updateTexture(final Constants.MapType mapType, final Integer vSlice, boolean forceReset)
    {
        if (futureTex != null)
        {
            if (forceReset)
            {
                futureTex.cancel(true);
                futureTex = null;
            }
            else
            {
                return false;
            }
        }

        this.mapType = mapType;
        this.vSlice = vSlice;

        Integer glId = null;
        BufferedImage image = RegionImageCache.getInstance().getGuaranteedImage(regionCoord, mapType);
        if (textureImpl != null)
        {
            // Reuse existing buffered image and glId
            glId = textureImpl.getGlTextureId();
        }

        futureTex = TextureCache.instance().prepareImage(glId, image);

        return true;
    }

    public boolean hasTexture()
    {
        if (textureImpl != null)
        {
            return true;
        }
        if (futureTex != null && futureTex.isDone())
        {
            try
            {
                if (futureTex.get() == null)
                {
                    futureTex = null;
                    lastImageTime = 0;
                    return false;
                }
                else
                {
                    return true;
                }
            }
            catch (Throwable e)
            {
                logger.error(LogFormatter.toString(e));
            }
        }
        return false;
    }

    public TextureImpl getTexture()
    {

        if (futureTex != null && futureTex.isDone())
        {
            try
            {
                DelayedTexture dt = futureTex.get();
                if (dt != null)
                {
                    TextureImpl texture = dt.bindTexture();
                    if (textureImpl == null)
                    { // new
                        textureImpl = texture;
                    }
                }
                futureTex = null;
            }
            catch (Throwable e)
            {
                logger.error(LogFormatter.toString(e));
            }
        }

        return textureImpl;
    }

    public void clear()
    {
        if (textureImpl != null)
        {
            textureImpl.deleteTexture();
            textureImpl = null;
        }
    }
}
