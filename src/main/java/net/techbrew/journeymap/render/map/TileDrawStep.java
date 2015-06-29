package net.techbrew.journeymap.render.map;

import com.google.common.base.Objects;
import net.minecraft.client.renderer.OpenGlHelper;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.io.RegionImageHandler;
import net.techbrew.journeymap.log.StatTimer;
import net.techbrew.journeymap.model.*;
import net.techbrew.journeymap.render.draw.DrawUtil;
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.render.texture.TextureImpl;
import net.techbrew.journeymap.task.main.ExpireTextureTask;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 *
 */
public class TileDrawStep
{
    private static final Color bgColor = new Color(0x22, 0x22, 0x22);

    private static final Logger logger = JourneyMap.getLogger();
    private final boolean debug = logger.isDebugEnabled();
    private final RegionCoord regionCoord;
    private final MapType mapType;
    private final Integer zoom;
    private final boolean highQuality;
    private final StatTimer drawTimer;
    private final RegionImageSet regionImageSet;
    private final StatTimer updateRegionTimer = StatTimer.get("TileDrawStep.updateRegionTexture", 5, 50);
    private final StatTimer updateScaledTimer = StatTimer.get("TileDrawStep.updateScaledTexture", 5, 50);
    private int sx1, sy1, sx2, sy2;
    private volatile TextureImpl scaledTexture;
    private volatile Future<TextureImpl> regionFuture;
    private volatile Future<TextureImpl> scaledFuture;
    private volatile ImageHolder regionTextureHolder;
    private int lastTextureFilter;
    private int lastTextureWrap;


    public TileDrawStep(RegionCoord regionCoord, final MapType mapType, Integer zoom, boolean highQuality, int sx1, int sy1, int sx2, int sy2)
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
        this.regionTextureHolder = regionImageSet.getHolder(mapType);

        updateRegionTexture();
        if (highQuality)
        {
            updateScaledTexture();
        }
    }

    public static int toHashCode(RegionCoord regionCoord, final MapType mapType, Integer zoom, boolean highQuality, int sx1, int sy1, int sx2, int sy2)
    {
        return Objects.hashCode(regionCoord, mapType, zoom, highQuality, sx1, sy1, sx2, sy2);
    }

    boolean draw(final TilePos pos, final double offsetX, final double offsetZ, float alpha, int textureFilter, int textureWrap, GridSpec gridSpec)
    {
        boolean regionUpdatePending = updateRegionTexture();
        if (highQuality)
        {
            updateScaledTexture();
        }

        //boolean scaledUpdatePending = !regionUpdatePending && highQuality && updateScaledTexture();
        Integer textureId = -1;
        boolean useScaled = false;

        if (highQuality && scaledTexture != null)
        {
            textureId = scaledTexture.getGlTextureId();
            useScaled = true;
        }
        else if (!regionUpdatePending)
        {
            textureId = regionTextureHolder.getTexture().getGlTextureId();
        }
        else
        {
            textureId = -1;
        }

        if (textureFilter != lastTextureFilter)
        {
            lastTextureFilter = textureFilter;
        }

        if (textureWrap != lastTextureWrap)
        {
            lastTextureWrap = textureWrap;
        }

        // Draw already!
        drawTimer.start();

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

        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        // Tile
        if (textureId != -1)
        {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
            GL11.glColor4f(1, 1, 1, alpha);

            // http://gregs-blog.com/2008/01/17/opengl-texture-filter-parameters-explained/
            if (!useScaled)
            {
                // TODO: Does this help?
                //textureFilter = GL11.GL_NEAREST;
            }

            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, textureFilter); // GL11.GL_LINEAR_MIPMAP_NEAREST
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, textureFilter); // GL11.GL_NEAREST
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, textureWrap);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, textureWrap);
            DrawUtil.drawBoundTexture(startU, startV, startX, startY, z, endU, endV, endX, endY);
        }

        // Grid
        if (gridSpec != null)
        {
            gridSpec.beginTexture(textureWrap, alpha);
            DrawUtil.drawBoundTexture(sx1 / size, sy1 / size, startX, startY, z, sx2 / size, sy2 / size, endX, endY);
            gridSpec.finishTexture();
        }

        if (debug)
        {
            int debugX = (int) startX;
            int debugY = (int) startY;
            DrawUtil.drawRectangle(debugX, debugY, 3, endV * 512, Color.green, 200);
            DrawUtil.drawRectangle(debugX, debugY, endU * 512, 3, Color.red, 200);
            DrawUtil.drawLabel(this.toString(), debugX + 5, debugY + 10, DrawUtil.HAlign.Right, DrawUtil.VAlign.Below, Color.WHITE, 255, Color.BLUE, 255, 1.0, false);
            DrawUtil.drawLabel(String.format("Tile Render Type: %s, Scaled: %s", Tile.debugGlSettings, useScaled), debugX + 5, debugY + 20, DrawUtil.HAlign.Right, DrawUtil.VAlign.Below, Color.WHITE, 255, Color.BLUE, 255, 1.0, false);
            long imageTimestamp = useScaled ? scaledTexture.getLastImageUpdate() : regionTextureHolder.getImageTimestamp();
            long age = (System.currentTimeMillis() - imageTimestamp) / 1000;
            DrawUtil.drawLabel(mapType + " tile age: " + age + " seconds old", debugX + 5, debugY + 30, DrawUtil.HAlign.Right, DrawUtil.VAlign.Below, Color.WHITE, 255, Color.BLUE, 255, 1.0, false);
        }

        GL11.glColor4f(1, 1, 1, 1);
        GL11.glClearColor(1, 1, 1, 1f); // defensive against shaders

        drawTimer.stop();

        int glErr = GL11.glGetError();
        if (glErr != GL11.GL_NO_ERROR)
        {
            JourneyMap.getLogger().warn("GL Error in TileDrawStep: " + glErr);
            clearTexture();
        }

        return textureId != 1;
    }


    public void clearTexture()
    {
        ExpireTextureTask.queue(scaledTexture);
        scaledTexture = null;
        if (scaledFuture != null && !scaledFuture.isDone())
        {
            scaledFuture.cancel(true);
        }
        scaledFuture = null;
        if (regionFuture != null && !regionFuture.isDone())
        {
            regionFuture.cancel(true);
        }
        regionFuture = null;
    }

    public MapType getMapType()
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
                .add("high", highQuality)
                .add("zoom", zoom)
                .add("sx1", sx1)
                .add("sy1", sy1)
                .toString();
    }

    boolean hasTexture(MapType mapType)
    {
        if (!Objects.equal(this.mapType, mapType))
        {
            return false;
        }
        if (highQuality)
        {
            return scaledTexture != null && scaledTexture.isBound();
        }
        else
        {
            return regionTextureHolder.getTexture().isBound();
        }
    }

    private boolean updateRegionTexture()
    {
        updateRegionTimer.start();

        if (regionFuture != null)
        {
            if (!regionFuture.isDone())
            {
                updateRegionTimer.stop();
                return true;
            }
            regionFuture = null;
        }

        if (regionTextureHolder.hasTexture())
        {
            if (regionTextureHolder.getTexture().isBindNeeded())
            {
                regionTextureHolder.getTexture().bindTexture();
            }
            updateRegionTimer.stop();
            return false;
        }

        regionFuture = TextureCache.instance().scheduleTextureTask(new Callable<TextureImpl>()
        {
            @Override
            public TextureImpl call() throws Exception
            {
                return regionTextureHolder.getTexture();
            }
        });

        updateRegionTimer.stop();

        return true;
    }

    private boolean updateScaledTexture()
    {
        updateScaledTimer.start();

        if (scaledFuture != null)
        {
            if (!scaledFuture.isDone())
            {
                updateScaledTimer.stop();
                return true;
            }
            try
            {
                scaledTexture = scaledFuture.get();
                scaledTexture.bindTexture();
            }
            catch (Throwable e)
            {
                logger.error(e);
            }
            scaledFuture = null;
            updateScaledTimer.stop();
            return false;
        }

        if (scaledTexture == null)
        {
            scaledFuture = TextureCache.instance().scheduleTextureTask(new Callable<TextureImpl>()
            {
                @Override
                public TextureImpl call() throws Exception
                {
                    TextureImpl temp = new TextureImpl(null, getScaledRegionArea(), false, false);
                    temp.setDescription("scaled for " + TileDrawStep.this);
                    return temp;
                }
            });
        }
        else if (scaledTexture.getLastImageUpdate() < regionTextureHolder.getImageTimestamp())
        {
            final TextureImpl temp = scaledTexture;
            scaledFuture = TextureCache.instance().scheduleTextureTask(new Callable<TextureImpl>()
            {
                @Override
                public TextureImpl call() throws Exception
                {
                    temp.setImage(getScaledRegionArea(), false);
                    return temp;
                }
            });
        }
        updateScaledTimer.stop();
        return true;
    }

    public BufferedImage getScaledRegionArea()
    {
        int scale = (int) Math.pow(2, zoom);
        int scaledSize = Tile.TILESIZE / scale;

        try
        {
            BufferedImage subImage = this.regionTextureHolder.getTexture().getImage().getSubimage(sx1, sy1, scaledSize, scaledSize);
            BufferedImage scaledImage = new BufferedImage(Tile.TILESIZE, Tile.TILESIZE, BufferedImage.TYPE_INT_ARGB);
            final Graphics2D g = RegionImageHandler.initRenderingHints(scaledImage.createGraphics());
            g.drawImage(subImage, 0, 0, Tile.TILESIZE, Tile.TILESIZE, null);
            g.dispose();
            return scaledImage;
        }
        catch (Throwable e)
        {
            logger.error(e);
            return null;
        }


        /**
         BufferedImage image = new BufferedImage(regionImage.getWidth(), regionImage.getHeight(), regionImage.getType());
         final Graphics2D g2D = initRenderingHints(image.createGraphics());


         g2D.drawImage(regionImage, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
         //g2D.drawImage(regionImage, 0, 0, 512, 512, sx1, sy1, sx2, sy2, Color.yellow, null);
         g2D.drawImage(regionImage, sx1, sy1, sx2-sx1, sy2-sy1,null);
         g2D.dispose();

         return image;
         */
    }
}
