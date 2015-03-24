package net.techbrew.journeymap.model;

import com.google.common.base.Objects;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.io.RegionImageHandler;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.log.StatTimer;
import net.techbrew.journeymap.render.texture.TextureImpl;
import net.techbrew.journeymap.task.main.ExpireTextureTask;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Formerly ImageSet.Wrapper
 */
public class ImageHolder
{
    final static Logger logger = JourneyMap.getLogger();
    final Constants.MapType mapType;
    final Integer vSlice;
    final ReentrantLock writeLock = new ReentrantLock();
    final Path imagePath;
    final int imageSize;
    boolean dirty = true;
    boolean partialUpdate;
    StatTimer writeToDiskTimer = StatTimer.get("ImageHolder.writeToDisk", 2, 1000);
    private volatile TextureImpl texture;

    ImageHolder(Constants.MapType mapType, Integer vSlice, File imageFile, int imageSize)
    {
        this.mapType = mapType;
        this.vSlice = vSlice;
        this.imagePath = imageFile.toPath();
        this.imageSize = imageSize;
        getTexture();
    }

    public static Graphics2D initRenderingHints(Graphics2D g)
    {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        return g;
    }

    File getFile()
    {
        return imagePath.toFile();
    }

    Constants.MapType getMapType()
    {
        return mapType;
    }

    Integer getvSlice()
    {
        return vSlice;
    }

    BufferedImage getImage()
    {
        return texture.getImage();
    }

    void setImage(BufferedImage image)
    {
        texture.setImage(image, true);
        setDirty();
    }

    void partialImageUpdate(BufferedImage imagePart, int x, int y)
    {
        writeLock.lock();
        try
        {
            BufferedImage textureImage = texture.getImage();
            Graphics2D g2D = initRenderingHints(textureImage.createGraphics());
            g2D.drawImage(imagePart, x, y, null);
            g2D.dispose();
            partialUpdate = true;
        }
        finally
        {
            writeLock.unlock();
        }
    }

    void finishPartialImageUpdates()
    {
        writeLock.lock();
        try
        {
            if (partialUpdate)
            {
                BufferedImage textureImage = texture.getImage();
                texture.setImage(textureImage, true);
                setDirty();
                partialUpdate = false;
            }
        }
        finally
        {
            writeLock.unlock();
        }
    }

    public TextureImpl getTexture()
    {
        if (texture == null || texture.isDefunct())
        {
            BufferedImage image = RegionImageHandler.readRegionImage(imagePath.toFile(), false);
            if (image == null || image.getWidth() != imageSize || image.getHeight() != imageSize)
            {
                image = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);
            }
            this.texture = new TextureImpl(null, image, true, false);
            texture.setDescription(imagePath.toString());
        }
        return texture;
    }

    private void setDirty()
    {
        dirty = true;
    }

    boolean isDirty()
    {
        return dirty;
    }

    protected void writeToDisk()
    {
        writeToDiskTimer.start();
        if (writeLock.tryLock())
        {
            try
            {
                File imageFile = imagePath.toFile();
                if (!imageFile.exists())
                {
                    imageFile.getParentFile().mkdirs();
                }

                BufferedOutputStream imageOutputStream = new BufferedOutputStream(new FileOutputStream(imageFile));
                ImageIO.write(texture.getImage(), "PNG", imageOutputStream);
                imageOutputStream.close();

                if (logger.isEnabled(Level.DEBUG))
                {
                    logger.debug("Wrote to disk: " + imageFile); //$NON-NLS-1$
                }
                dirty = false;
            }
            catch (Throwable e)
            {
                String error = "Unexpected error writing to disk: " + this + ": " + LogFormatter.toString(e);
                logger.error(error);
                //throw new RuntimeException(e);
            }
            finally
            {
                writeLock.unlock();
            }
        }
        else
        {
            logger.warn("Couldn't get write lock to write to disk: " + this);
        }

        writeToDiskTimer.stop();
//        if (writeToDiskTimer.hasReachedElapsedLimit() && writeToDiskTimer.getElapsedLimitWarningsRemaining() > 0)
//        {
//            logger.warn("Image that took too long: " + this);
//        }
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("mapType", mapType)
                .add("vSlice", vSlice)
                .add("textureId", texture == null ? null : texture.isBound() ? texture.getGlTextureId(false) : -1)
                .add("dirty", dirty)
                .add("imagePath", imagePath)
                .toString();
    }

    public void clear()
    {
        writeLock.lock();
        ExpireTextureTask.queue(texture);
        texture = null;
        writeLock.unlock();
    }

    public void finalize()
    {
        if (texture != null)
        {
            // This shouldn't have happened
            clear();
        }
    }

    public long getImageTimestamp()
    {
        return texture.getLastImageUpdate();
    }
}
