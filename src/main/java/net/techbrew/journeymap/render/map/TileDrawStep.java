package net.techbrew.journeymap.render.map;

import com.google.common.base.Objects;
import net.minecraft.client.renderer.Tessellator;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.io.RegionImageHandler;
import net.techbrew.journeymap.log.StatTimer;
import net.techbrew.journeymap.model.*;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.render.texture.TextureImpl;
import net.techbrew.journeymap.task.main.ExpireTextureTask;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 */
public class TileDrawStep
{
    private static final Color bgColor = new Color(0x22, 0x22, 0x22);

    private final Logger logger = JourneyMap.getLogger();
    private final boolean debug = logger.isTraceEnabled();
    private final RegionCoord regionCoord;
    private final Constants.MapType mapType;
    private final Integer zoom;
    private final boolean highQuality;
    private final StatTimer drawTimer;
    private final RegionImageSet regionImageSet;
    private int sx1, sy1, sx2, sy2;
    private TextureImpl scaledTexture;
    private ImageHolder regionTextureHolder;

    public TileDrawStep(RegionCoord regionCoord, final Constants.MapType mapType, Integer zoom, boolean highQuality, int sx1, int sy1, int sx2, int sy2)
    {
        this.mapType = mapType;
        this.regionCoord = regionCoord;
        this.zoom = zoom;
        this.sx1 = sx1;
        this.sx2 = sx2;
        this.sy1 = sy1;
        this.sy2 = sy2;
        this.highQuality = highQuality && zoom != 0; // todo change when zoom can be < zero or 0 no longer 1:1
        this.drawTimer = (this.highQuality) ? StatTimer.get("TileDrawStep.draw(high)") : StatTimer.get("TileDrawStep.draw(low)");

        this.regionImageSet = RegionImageCache.instance().getRegionImageSet(regionCoord);
        this.regionTextureHolder = regionImageSet.getHolder(mapType, regionCoord.vSlice);

        if (highQuality)
        {
            updateScaledTexture();
        }
    }

    public static int toHashCode(RegionCoord regionCoord, final Constants.MapType mapType, Integer zoom, boolean highQuality, int sx1, int sy1, int sx2, int sy2)
    {
        return Objects.hashCode(regionCoord, mapType, zoom, highQuality, sx1, sy1, sx2, sy2);
    }

    void draw(final TilePos pos, final double offsetX, final double offsetZ, float alpha, int textureFilter, int textureWrap, GridSpec gridSpec)
    {
        // Bind pending textures if needed
        TextureImpl regionTexture = regionTextureHolder.getTexture();
        boolean regionBindNeeded = regionTexture.isBindNeeded();
        if (regionBindNeeded)
        {
            regionTexture.bindTexture();
        }
        else if (highQuality)
        {
            if (scaledTexture == null || scaledTexture.getLastBound() < regionTexture.getLastBound())
            {
                updateScaledTexture();
            }
        }

        boolean useScaled = !regionBindNeeded && highQuality;

        Integer textureId = useScaled ? scaledTexture.getGlTextureId() : regionTexture.getGlTextureId();

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

        final double startU = useScaled ? 0D : sx1 / size;
        final double startV = useScaled ? 0D : sy1 / size;
        final double endU = useScaled ? 1D : sx2 / size;
        final double endV = useScaled ? 1D : sy2 / size;

        // Background
        DrawUtil.drawRectangle(startX, startY, endX - startX, endY - startY, bgColor, 200);

        // Tile
        if (textureId != null && textureId != -1)
        {
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

            GL11.glColor4f(1, 1, 1, alpha);

            // http://gregs-blog.com/2008/01/17/opengl-texture-filter-parameters-explained/
            if (!useScaled)
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
            System.out.println("Missing texture: " + this);
        }

        // Grid
        if (gridSpec != null)
        {
            gridSpec.beginTexture(textureWrap, alpha);
            drawBoundTexture(sx1 / size, sy1 / size, startX, startY, z, sx2 / size, sy2 / size, endX, endY);
            gridSpec.finishTexture();
        }

        if (debug)
        {
            int debugX = (int) startX;
            int debugY = (int) startY;
            DrawUtil.drawRectangle(debugX, debugY, 3, endV * 512, Color.green, 200);
            DrawUtil.drawRectangle(debugX, debugY, endU * 512, 3, Color.red, 200);
            DrawUtil.drawLabel(this.toString(), debugX + 5, debugY + 10, DrawUtil.HAlign.Right, DrawUtil.VAlign.Below, Color.WHITE, 255, Color.BLUE, 255, 1.0, false);
            DrawUtil.drawLabel(String.format("Full size: %s", useScaled), debugX + 5, debugY + 20, DrawUtil.HAlign.Right, DrawUtil.VAlign.Below, Color.WHITE, 255, Color.BLUE, 255, 1.0, false);
            long imageTimestamp = useScaled ? scaledTexture.getLastBound() : regionTexture.getLastBound();
            DrawUtil.drawLabel(new SimpleDateFormat("HH:mm:ss.SSS").format(new Date(imageTimestamp)), debugX + 5, debugY + 30, DrawUtil.HAlign.Right, DrawUtil.VAlign.Below, Color.WHITE, 255, Color.BLUE, 255, 1.0, false);
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

    public void clearTexture()
    {
        ExpireTextureTask.queue(scaledTexture);
        scaledTexture = null;
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
        return toHashCode(regionCoord, mapType, zoom, highQuality, sx1, sy1, sx2, sy2);
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

    private void updateScaledTexture()
    {
        BufferedImage image = RegionImageHandler.getScaledRegionArea(regionCoord, mapType, zoom, highQuality, sx1, sy1);
        if (image == null)
        {
            return;
        }
        if (scaledTexture == null)
        {
            scaledTexture = new TextureImpl(null, image, false, false);
            scaledTexture.setDescription("scaled for " + this);
            scaledTexture.bindTexture();
        }
        else
        {
            scaledTexture.updateAndBind(image, false);
        }

    }
}
