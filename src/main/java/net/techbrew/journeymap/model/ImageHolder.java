package net.techbrew.journeymap.model;

import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.log.StatTimer;
import net.techbrew.journeymap.render.texture.TextureCache;
import net.techbrew.journeymap.render.texture.TextureImpl;
import org.apache.logging.log4j.Level;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;

/**
 * Formerly ImageSet.Wrapper
 */
public class ImageHolder
{
    final static String delim = " : ";
    final Constants.MapType mapType;
    final Object writeLock = new Object();
    Path imagePath;
    BufferedImage image = null;
    TextureImpl texture;
    boolean dirty = true;
    long imageTimestamp = System.currentTimeMillis();
    StatTimer writeToDiskTimer = StatTimer.get("ImageHolder.writeToDisk", 2, 1000);

    ImageHolder(Constants.MapType mapType, File imageFile, BufferedImage image)
    {
        this.mapType = mapType;
        if (imageFile != null)
        {
            this.imagePath = imageFile.toPath();
        }
        setImage(image);
    }

    File getFile()
    {
        return imagePath == null ? null : imagePath.toFile();
    }

    Constants.MapType getMapType()
    {
        return mapType;
    }

    BufferedImage getImage()
    {
        return image;
    }

    void setImage(BufferedImage image)
    {
        if (image != this.image)
        {
            synchronized (writeLock)
            {
                this.image = image;
            }
            setDirty();
            updateTexture();
        }

    }

    TextureImpl getTexture()
    {
        if (texture == null)
        {
            texture = new TextureImpl(null, image, false, false);
        }
        return texture;
    }

    public boolean updateTexture()
    {
        if (image != null && texture != null)
        {
            if (texture.getLastUpdated() < imageTimestamp)
            {
                texture.setImage(image, true);
                texture.setLastUpdated(imageTimestamp);
                if (imagePath != null)
                {
                    texture.setDescription(imagePath.toString());
                }
                return true;
            }
        }
        return false;
    }

    void setDirty()
    {
        dirty = true;
        imageTimestamp = System.currentTimeMillis();
    }

    long getImageTimestamp()
    {
        return imageTimestamp;
    }

    boolean isDirty()
    {
        return dirty;
    }

    protected void writeToDisk()
    {
        writeToDiskTimer.start();
        try
        {
            synchronized (writeLock)
            {
                if (image == null)
                {
                    JourneyMap.getLogger().warn("Null image for " + this);
                }
                else if (imagePath == null)
                {
                    JourneyMap.getLogger().warn("Null path for " + this);
                }
                else
                {
                    File imageFile = imagePath.toFile();
                    if (!imageFile.exists())
                    {
                        imageFile.getParentFile().mkdirs();
                    }

                    BufferedOutputStream imageOutputStream = new BufferedOutputStream(new FileOutputStream(imageFile));
                    ImageIO.write(image, "PNG", imageOutputStream);
                    imageOutputStream.close();

                    if (JourneyMap.getLogger().isEnabled(Level.DEBUG))
                    {
                        JourneyMap.getLogger().debug("Wrote to disk: " + imageFile); //$NON-NLS-1$
                    }
                    dirty = false;
                }
            }
        }
        catch (Throwable e)
        {
            String error = "Unexpected error writing to disk: " + this + ": " + LogFormatter.toString(e);
            JourneyMap.getLogger().error(error);
            //throw new RuntimeException(e);
        }
        writeToDiskTimer.stop();
//        if (writeToDiskTimer.hasReachedElapsedLimit() && writeToDiskTimer.getElapsedLimitWarningsRemaining() > 0)
//        {
//            JourneyMap.getLogger().warn("Image that took too long: " + this);
//        }
    }

    @Override
    public String toString()
    {
        File imageFile = getFile();
        return mapType.name() + delim + (imageFile != null ? imageFile.getPath() : "") + delim + "image=" + (image == null ? "null" : "ok") + ", dirty=" + dirty;
    }

    public void clear()
    {
        synchronized (writeLock)
        {
            imagePath = null;
            image = null;
            if (texture != null)
            {
                TextureCache.instance().expireTexture(texture);
                texture = null;
            }
        }
    }
}
