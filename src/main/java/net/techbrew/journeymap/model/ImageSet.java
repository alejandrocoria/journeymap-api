/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.model;

import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.Constants.MapType;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.log.StatTimer;
import org.apache.logging.log4j.Level;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.*;

/**
 * An ImageSet contains one or more Wrappers of image, file, and maptype.
 *
 * @author mwoodman
 */
public abstract class ImageSet
{
    protected final Map<Constants.MapType, Wrapper> imageWrappers;
    protected StatTimer writeToDiskTimer = StatTimer.get("ImageSet.writeToDisk", 2, 500);

    public ImageSet()
    {
        imageWrappers = Collections.synchronizedMap(new HashMap<Constants.MapType, Wrapper>(3));
    }

    protected abstract Wrapper getWrapper(Constants.MapType mapType);

    public BufferedImage getImage(Constants.MapType mapType)
    {
        return getWrapper(mapType).getImage();
    }

    public void writeToDisk(boolean force)
    {
        for (Wrapper wrapper : imageWrappers.values())
        {
            if (force || wrapper.isDirty())
            {
                wrapper.writeToDisk();
            }
        }
    }

    public boolean updatedSince(MapType mapType, long time)
    {
        for (Wrapper wrapper : imageWrappers.values())
        {
            if (mapType != null)
            {
                if (wrapper.getMapType() == mapType && wrapper.getTimestamp() > time)
                {
                    return true;
                }
            }
            else if (wrapper.getTimestamp() > time)
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName()).append("[ ");
        Iterator<Wrapper> iter = imageWrappers.values().iterator();
        while (iter.hasNext())
        {
            sb.append(iter.next().toString());
            if (iter.hasNext())
            {
                sb.append(", ");
            }
        }
        return sb.append(" ]").toString();
    }

    public void clear()
    {
        for (ImageSet.Wrapper wrapper : imageWrappers.values())
        {
            wrapper.clear();
        }
        imageWrappers.clear();
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    /**
     * ************************
     */

    protected abstract Wrapper addWrapper(Constants.MapType mapType, BufferedImage image);

    protected Wrapper addWrapper(Constants.MapType mapType, File imageFile, BufferedImage image)
    {
        return addWrapper(new Wrapper(mapType, imageFile, image));
    }

    protected Wrapper addWrapper(Wrapper wrapper)
    {
        imageWrappers.put(wrapper.mapType, wrapper);
        return wrapper;
    }

    class Wrapper
    {
        final static String delim = " : ";
        final Constants.MapType mapType;
        Path imagePath;
        BufferedImage _image = null;
        boolean _dirty = true;
        long timestamp = System.currentTimeMillis();

        Wrapper(Constants.MapType mapType, File imageFile, BufferedImage image)
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
            return _image;
        }

        void setImage(BufferedImage image)
        {
            if (image != _image)
            {
                setDirty();
            }
            _image = image;

        }

        void setDirty()
        {
            _dirty = true;
            timestamp = new Date().getTime();
        }

        long getTimestamp()
        {
            return timestamp;
        }

        boolean isDirty()
        {
            return _dirty;
        }

        protected void writeToDisk()
        {
            writeToDiskTimer.start();
            try
            {
                if (_image == null)
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
                    ImageIO.write(_image, "PNG", imageOutputStream);
                    imageOutputStream.close();

                    if (JourneyMap.getLogger().isEnabled(Level.DEBUG))
                    {
                        JourneyMap.getLogger().debug("Wrote to disk: " + imageFile); //$NON-NLS-1$
                    }
                    _dirty = false;
                }
            }
            catch (Throwable e)
            {
                String error = "Unexpected error writing to disk: " + this + ": " + LogFormatter.toString(e);
                JourneyMap.getLogger().error(error);
                //throw new RuntimeException(e);
            }
            writeToDiskTimer.stop();
            if (writeToDiskTimer.hasReachedElapsedLimit() && writeToDiskTimer.getElapsedLimitWarningsRemaining() > 0)
            {
                JourneyMap.getLogger().warn("Image that took too long: " + this);
            }
        }

        @Override
        public String toString()
        {
            File imageFile = getFile();
            return mapType.name() + delim + (imageFile != null ? imageFile.getPath() : "") + delim + "image=" + (_image == null ? "null" : "ok") + ", dirty=" + _dirty;
        }

        public void clear()
        {
            imagePath = null;
            _image = null;
        }
    }
}
