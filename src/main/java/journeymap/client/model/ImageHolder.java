/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import journeymap.client.io.RegionImageHandler;
import journeymap.client.log.StatTimer;
import journeymap.client.render.texture.RegionTextureImpl;
import journeymap.client.task.main.ExpireTextureTask;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.IThreadedFileIO;
import net.minecraft.world.storage.ThreadedFileIOBase;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Formerly ImageSet.Wrapper
 */
public class ImageHolder implements IThreadedFileIO
{
    final static Logger logger = Journeymap.getLogger();
    final MapType mapType;
    final Path imagePath;
    final int imageSize;
    boolean blank = true;
    boolean dirty = true;
    boolean partialUpdate;
    private volatile ReentrantLock writeLock = new ReentrantLock();
    private volatile RegionTextureImpl texture;
    private boolean debug;
    private HashSet<ChunkPos> updatedChunks = new HashSet<>();

    ImageHolder(MapType mapType, File imageFile, int imageSize)
    {
        this.mapType = mapType;
        this.imagePath = imageFile.toPath();
        this.imageSize = imageSize;
        this.debug = logger.isEnabled(Level.DEBUG);
        getTexture();
    }

    File getFile()
    {
        return imagePath.toFile();
    }

    MapType getMapType()
    {
        return mapType;
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
        final StatTimer timer = StatTimer.get("ImageHolder.partialImageUpdate", 5, 500);
        timer.start();
        try
        {
            if (texture != null)
            {
                blank = false;
                int width = imagePart.getWidth();
                int height = imagePart.getHeight();

                int[] updatedPixels = new int[width * height];
                imagePart.getRGB(0, 0, width, height, updatedPixels, 0, width);

                texture.getImage().setRGB(x, y, width, height, updatedPixels, 0, width);
                partialUpdate = true;
                updatedChunks.add(new ChunkPos(x, y));
            }
            else
            {
                logger.warn(this + " can't partialImageUpdate without a texture.");
            }
        }
        finally
        {
            timer.stop();
            writeLock.unlock();
        }
    }

    void finishPartialImageUpdates()
    {
        writeLock.lock();
        try
        {
            if (partialUpdate && !updatedChunks.isEmpty())
            {
                BufferedImage textureImage = texture.getImage();
                texture.setImage(textureImage, true, updatedChunks);
                setDirty();
                partialUpdate = false;
                updatedChunks.clear();
                //System.out.println("Finished image updates on " + this);
            }
        }
        finally
        {
            writeLock.unlock();
        }
    }

    public boolean hasTexture()
    {
        return texture != null && !texture.isDefunct();
    }

    public RegionTextureImpl getTexture()
    {
        if (!hasTexture())
        {
            if (!imagePath.toFile().exists())
            {
                // check for .new that didn't finish renaming
                File temp = new File(imagePath.toString() + ".new");
                if (temp.exists())
                {
                    Journeymap.getLogger().warn("Recovered image file: " + temp);
                    temp.renameTo(imagePath.toFile());
                }
            }

            BufferedImage image = RegionImageHandler.readRegionImage(imagePath.toFile(), false);
            if (image == null || image.getWidth() != imageSize || image.getHeight() != imageSize)
            {
                image = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);
                blank = true;
                dirty = false;
            }
            else
            {
                blank = false;
            }
            this.texture = new RegionTextureImpl(image);
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

    /**
     * Update the image file on disk.
     *
     * @param async Whether to queue the write with Minecraft's IO thread and immediately return
     * @return true if the file was actually updated and async=false
     */
    protected boolean writeToDisk(boolean async)
    {
        if (blank || texture == null || !texture.hasImage())
        {
            return false;
        }
        else
        {
            //MapPlayerTask.addTempDebugMessage("ImageHolder" + imagePath.hashCode(), "Writing to disk: " + imagePath);

            if (async)
            {
                ThreadedFileIOBase.getThreadedIOInstance().queueIO(this);
                return true;
            }
            else
            {
                int tries = 0;
                boolean success = false;
                while (tries < 5)
                {
                    if (writeNextIO())
                    {
                        tries++;
                    }
                    else
                    {
                        success = true;
                        break;
                    }
                }
                if (!success)
                {
                    Journeymap.getLogger().warn("Couldn't write file after 5 tries: " + this);
                }
                return success;
            }
        }
    }

    /**
     * Update the image file on disk.
     * <p/>
     * Implements IThreaedFileIO, to be called by ThreadedFileIOBase.
     *
     * @return true if a retry is needed
     */
    public boolean writeNextIO()
    {
        if (texture == null || !texture.hasImage())
        {
            return false; // no retry
        }

        try
        {
            if (writeLock.tryLock(250, TimeUnit.MILLISECONDS))
            {
                writeImageToFile();
                writeLock.unlock();
                return false; // don't retry
            }
            else
            {
                logger.warn("Couldn't get write lock for file: " + writeLock + " for " + this);
                return false; // do retry
            }
        }
        catch (InterruptedException e)
        {
            logger.warn("Timeout waiting for write lock  " + writeLock + " for " + this);
            return false; // do retry
        }
    }

    private void writeImageToFile()
    {
        File imageFile = imagePath.toFile();

        try
        {
            BufferedImage image = texture.getImage();
            if (image != null)
            {
                if (!imageFile.exists())
                {
                    imageFile.getParentFile().mkdirs();
                }

                File temp = new File(imageFile.getParentFile(), imageFile.getName() + ".new");

                ImageIO.write(image, "PNG", temp);

                if (imageFile.exists())
                {
                    if (!imageFile.delete())
                    {
                        logger.warn("Couldn't delete old file " + imageFile.getName());
                    }
                }

                if (temp.renameTo(imageFile))
                {
                    dirty = false;
                }
                else
                {
                    logger.warn("Couldn't rename temp file to " + imageFile.getName());
                }

                if (debug)
                {
                    logger.debug("Wrote to disk: " + imageFile);
                }
            }
        }
        catch (IOException e)
        {
            if (imageFile.exists())
            {
                try
                {
                    logger.error("IOException updating file, will delete and retry: " + this + ": " + LogFormatter.toPartialString(e));
                    imageFile.delete();
                    writeImageToFile();
                }
                catch (Throwable e2)
                {
                    logger.error("Exception after delete/retry: " + this + ": " + LogFormatter.toPartialString(e));
                }
            }
            else
            {
                logger.error("IOException creating file: " + this + ": " + LogFormatter.toPartialString(e));
            }
        }
        catch (Throwable e)
        {
            logger.error("Exception writing to disk: " + this + ": " + LogFormatter.toPartialString(e));
        }
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(this)
                .add("mapType", mapType)
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
        if (texture != null)
        {
            return texture.getLastImageUpdate();
        }
        return 0;
    }
}
