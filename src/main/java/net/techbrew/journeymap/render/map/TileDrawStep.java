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
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.render.texture.TextureImpl;
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
    private static final Color bgColor = new Color(0x22, 0x22, 0x22);

    private final Logger logger = JourneyMap.getLogger();
    private final boolean debug = logger.isTraceEnabled();
    private int sx1, sy1, sx2, sy2;
    private int size;
    private TextureImpl scaledTexture;
    private Future<TextureImpl> pendingScaledTexture;
    private RegionCoord regionCoord;
    private Constants.MapType mapType;
    private Integer zoom;
    private boolean highQuality;
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
        boolean newQuality = JourneyMap.getCoreProperties().tileHighDisplayQuality.get();
        if (newQuality != this.highQuality || drawTimer == null)
        {
            this.highQuality = newQuality;
            drawTimer = (this.highQuality) ? StatTimer.get("TileDrawStep.draw(high)") : StatTimer.get("TileDrawStep.draw(low)");
            //updateTextureTimer = (this.highQuality==Constants.MapTileQuality.High) ? StatTimer.get("TileDrawStep.updateTexture(high)") : StatTimer.get("TileDrawStep.updateTexture(low)");
        }

        //updateTextureTimer.start();

        // Cancel pending textures if any
        if (pendingScaledTexture != null && !pendingScaledTexture.isDone())
        {
            pendingScaledTexture.cancel(false);
            pendingScaledTexture = null;
        }

        RegionImageCache.instance().getRegionImageSet(regionCoord).getHolder(mapType).updateTexture();

        try
        {
            if (highQuality && (zoom != 0)) // todo change when zoom can be < zero or 0 no longer 1:1
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

        // Background
        DrawUtil.drawRectangle(startX, startY, endX - startX, endY - startY, bgColor, 200);

        // Tile
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
        else
        {
            //System.out.println("Missing texture: " + this);
        }

        // Grid
        if (gridSpec != null)
        {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, textureFilter); // GL11.GL_LINEAR_MIPMAP_NEAREST
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, textureFilter); // GL11.GL_NEAREST
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, textureWrap);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, textureWrap);
            gridSpec.bindTexture(textureWrap);
            drawBoundTexture(sx1 / size, sy1 / size, startX, startY, z, sx2 / size, sy2 / size, endX, endY);
        }

        if (debug)
        {
            int debugX = (int) startX;
            int debugY = (int) startY;
            DrawUtil.drawRectangle(debugX, debugY, 3, endV * 512, Color.green, 200);
            DrawUtil.drawRectangle(debugX, debugY, endU * 512, 3, Color.red, 200);
            DrawUtil.drawLabel(this.toString(), debugX + 5, debugY + 10, DrawUtil.HAlign.Right, DrawUtil.VAlign.Below, Color.WHITE, 255, Color.BLUE, 255, 1.0, false);
            DrawUtil.drawLabel(String.format("Full size: %s", fullSize), debugX + 5, debugY + 20, DrawUtil.HAlign.Right, DrawUtil.VAlign.Below, Color.WHITE, 255, Color.BLUE, 255, 1.0, false);
            if (hasTexture())
            {
                DrawUtil.drawLabel(new SimpleDateFormat("HH:mm:ss.SSS").format(new Date(imageTimestamp)), debugX + 5, debugY + 30, DrawUtil.HAlign.Right, DrawUtil.VAlign.Below, Color.WHITE, 255, Color.BLUE, 255, 1.0, false);
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

    public void clearTexture()
    {
        pendingScaledTexture = null;
        if (scaledTexture != null)
        {
            TextureCache.instance().expireTexture(scaledTexture);
            scaledTexture = null;
        }
        pendingScaledTexture = null;
    }

    public RegionCoord getRegionCoord()
    {
        return regionCoord;
    }

    public Constants.MapType getMapType()
    {
        return mapType;
    }

    public Integer getZoom()
    {
        return zoom;
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
                .add("q", highQuality)
                .add("zoom", zoom)
                .add("sx1", sx1)
                .add("sy1", sy1)
                .toString();
    }


    private Callable<TextureImpl> createDelayedScaledTexture()
    {
        if (highQuality || zoom == 0) // todo change when zoom can be < zero or 0 no longer 1:1
        {
            return null;
        }

        return new Callable<TextureImpl>()
        {
            @Override
            public TextureImpl call() throws Exception
            {
                BufferedImage image = RegionImageHandler.getScaledRegionArea(regionCoord, mapType, zoom, highQuality, sx1, sy1);
                if (image == null)
                {
                    return null;
                }
                else
                {
                    return new TextureImpl(null, image, false, false);
                }
            }
        };
    }
}
