package net.techbrew.journeymap.render.map;

import com.google.common.base.Objects;
import net.minecraft.client.renderer.Tessellator;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.io.RegionImageHandler;
import net.techbrew.journeymap.log.StatTimer;
import net.techbrew.journeymap.model.GridSpec;
import net.techbrew.journeymap.model.RegionCoord;
import net.techbrew.journeymap.model.RegionImageCache;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.render.texture.DelayedTexture;
import net.techbrew.journeymap.render.texture.TextureCache;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Created by Mark on 12/10/2014.
 */
public class TileDrawStep
{
    private final Logger logger = JourneyMap.getLogger();
    private final boolean debug = logger.isTraceEnabled();
    private int sx1, sy1, sx2, sy2;
    private int size;
    private DelayedTexture scaledTexture;
    private Future<DelayedTexture> pendingScaledTexture;
    private RegionCoord regionCoord;
    private Constants.MapType mapType;
    private Integer zoom;
    private Constants.MapTileQuality quality;
    private StatTimer drawTimer;

    public TileDrawStep(RegionCoord regionCoord, final Constants.MapType mapType, Integer zoom, int sx1, int sy1, int sx2, int sy2)
    {
        this.mapType = mapType;
        this.regionCoord = regionCoord;
        this.zoom = zoom;
        this.sx1 = sx1;
        this.sx2 = sx2;
        this.sy1 = sy1;
        this.sy2 = sy2;
        this.size = sx2 - sx1;
        updateTexture();
    }

    public static int toHashCode(RegionCoord regionCoord, final Constants.MapType mapType, Integer zoom, int sx1, int sy1, int sx2, int sy2)
    {
        return Objects.hashCode(regionCoord, mapType, zoom, sx1, sy1, sx2, sy2);
    }

    void updateTexture()
    {
        Constants.MapTileQuality newQuality = JourneyMap.getCoreProperties().mapTileQuality.get();
        if (newQuality != this.quality)
        {
            this.quality = newQuality;
            drawTimer = (this.quality == Constants.MapTileQuality.High) ? StatTimer.get("TileDrawStep.draw(high)") : StatTimer.get("TileDrawStep.draw(low)");
            //updateTextureTimer = (this.quality==Constants.MapTileQuality.High) ? StatTimer.get("TileDrawStep.updateTexture(high)") : StatTimer.get("TileDrawStep.updateTexture(low)");
        }

        //updateTextureTimer.start();

        // Cancel pending textures if any
        if (pendingScaledTexture != null && !pendingScaledTexture.isDone())
        {
            pendingScaledTexture.cancel(false);
            pendingScaledTexture = null;
        }

        RegionImageCache.instance().getRegionTexture(regionCoord, mapType);

        try
        {
            if (quality == Constants.MapTileQuality.High && (zoom != 0)) // todo change when zoom can be < zero or 0 no longer 1:1
            {
                pendingScaledTexture = TextureCache.instance().scheduleTextureTask(createDelayedScaledTexture());
            }
        }
        catch (Exception e)
        {
            JourneyMap.getLogger().warn(String.format("Con't get sync texture for %s : %s", this, e));
        }
    }

    void bindPendingTextures()
    {
        if (pendingScaledTexture != null && pendingScaledTexture.isDone())
        {
            try
            {
                this.scaledTexture = pendingScaledTexture.get();
                if (this.scaledTexture != null)
                {
                    this.scaledTexture.bindTexture();
                }
                else
                {
                    JourneyMap.getLogger().warn(String.format("TileDrawStep couldn't get async scaled texture for %s : %s", this, null));
                }
            }
            catch (Throwable t)
            {
                JourneyMap.getLogger().warn(String.format("TileDrawStep couldn't bind async scaled texture for %s : %s", this, t));
            }
            this.pendingScaledTexture = null;
        }
    }

    void draw(final TilePos pos, final double offsetX, final double offsetZ, float alpha, int textureFilter, int textureWrap, GridSpec gridSpec)
    {
        // Bind pending textures if needed
        bindPendingTextures();

        Integer textureId = null;
        boolean fullSize = false;
        long imageTimestamp = 0;

        if (scaledTexture != null)
        {
            textureId = scaledTexture.getGlTextureId();
            imageTimestamp = scaledTexture.getLastUpdated();
            fullSize = true;
        }
        else
        {
            textureId = RegionImageCache.instance().getBoundRegionTextureId(regionCoord, mapType);
        }

        // Draw already!
        drawTimer.start();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        final double startX = offsetX + pos.startX;
        final double startY = offsetZ + pos.startZ;
        final double endX = offsetX + pos.endX;
        final double endY = offsetZ + pos.endZ;
        final double z = 0D;

        final double size = Tile.TILESIZE;

        final double startU = fullSize ? 0D : sx1 / size;
        final double startV = fullSize ? 0D : sy1 / size;
        final double endU = fullSize ? 1D : sx2 / size;
        final double endV = fullSize ? 1D : sy2 / size;

        if (textureId != null && textureId != -1)
        {
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

            GL11.glColor4f(1, 1, 1, alpha);

            // http://gregs-blog.com/2008/01/17/opengl-texture-filter-parameters-explained/
            if (!fullSize && zoom > 0)
            {
                textureFilter = GL11.GL_NEAREST;
            }

            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, textureFilter); // GL11.GL_LINEAR_MIPMAP_NEAREST
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, textureFilter); // GL11.GL_NEAREST
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, textureWrap);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, textureWrap);
            drawBoundTexture(startU, startV, startX, startY, z, endU, endV, endX, endY);
        }

        if (gridSpec != null)
        {
            gridSpec.bindTexture(textureWrap);
            drawBoundTexture(startU, startV, startX, startY, z, endU, endV, endX, endY);
        }

        if (debug) // todo
        {
            DrawUtil.drawRectangle(startX, startY, 2, endV * 512, Color.green, 200);
            DrawUtil.drawRectangle(startX, startY, endU * 512, 2, Color.red, 200);
            DrawUtil.drawLabel(this.toString(), startX + 5, startY + 10, DrawUtil.HAlign.Right, DrawUtil.VAlign.Below, Color.WHITE, 255, Color.BLUE, 255, 1.0, false);
            DrawUtil.drawLabel(String.format("Full size: %s", fullSize), startX + 5, startY + 20, DrawUtil.HAlign.Right, DrawUtil.VAlign.Below, Color.WHITE, 255, Color.BLUE, 255, 1.0, false);
            if (hasTexture())
            {
                DrawUtil.drawLabel(new SimpleDateFormat("HH:mm:ss.SSS").format(new Date(imageTimestamp)), startX + 5, startY + 30, DrawUtil.HAlign.Right, DrawUtil.VAlign.Below, Color.WHITE, 255, Color.BLUE, 255, 1.0, false);
            }
        }

        drawTimer.stop();

        int glErr = GL11.glGetError();
        if (glErr != GL11.GL_NO_ERROR)
        {
            JourneyMap.getLogger().warn("GL Error in TileDrawStep: " + glErr);
            clearTexture();
        }
    }

    private void drawBoundTexture(double startU, double startV, double startX, double startY, double z, double endU, double endV, double endX, double endY)
    {
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(startX, endY, z, startU, endV);
        tessellator.addVertexWithUV(endX, endY, z, endU, endV);
        tessellator.addVertexWithUV(endX, startY, z, endU, startV);
        tessellator.addVertexWithUV(startX, startY, z, startU, startV);
        tessellator.draw();
    }

    public boolean hasTexture()
    {
        return scaledTexture != null || RegionImageCache.instance().getBoundRegionTextureId(regionCoord, mapType) != null;
    }

//    public boolean isDirtySince(long time)
//    {
//        return RegionImageCache.instance().isDirtySince(regionCoord, mapType, time);
//    }

    public boolean clearTexture()
    {
        pendingScaledTexture = null;
        if (scaledTexture != null)
        {
            return scaledTexture.deleteTexture();
        }
        else
        {
            return false;
        }
    }

    public void refreshIfDirty()
    {
        if (RegionImageCache.instance().textureNeedsUpdate(regionCoord, mapType)
                || (quality == Constants.MapTileQuality.High && scaledTexture == null)
                || (scaledTexture != null && RegionImageCache.instance().isDirtySince(regionCoord, mapType, scaledTexture.getLastUpdated())))
        {
            updateTexture();
        }
    }

    public int hashCode()
    {
        return toHashCode(regionCoord, mapType, zoom, sx1, sy1, sx2, sy2);
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("rc", regionCoord)
                .add("type", mapType)
                .add("q", quality)
                .add("zoom", zoom)
                .add("sx1", sx1)
                .add("sy1", sy1)
                .toString();
    }


    private Callable<DelayedTexture> createDelayedScaledTexture()
    {
        if (quality == Constants.MapTileQuality.Low || zoom == 0) // todo change when zoom can be < zero or 0 no longer 1:1
        {
            return null;
        }

        return new Callable<DelayedTexture>()
        {
            @Override
            public DelayedTexture call() throws Exception
            {
                BufferedImage image = RegionImageHandler.getScaledRegionArea(regionCoord, mapType, zoom, quality, sx1, sy1);
                if (image == null)
                {
                    return null;
                }
                else
                {
                    return new DelayedTexture(null, image);
                }
            }
        };
    }
}
