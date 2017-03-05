/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.model;

import com.google.common.base.Objects;
import journeymap.common.Journeymap;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An ImageSet contains one or more ImageHolders
 *
 * @author techbrew
 */
public abstract class ImageSet
{
    /**
     * The Image holders.
     */
    protected final Map<MapType, ImageHolder> imageHolders;

    /**
     * Instantiates a new Image set.
     */
    public ImageSet()
    {
        imageHolders = Collections.synchronizedMap(new HashMap<MapType, ImageHolder>(8));
    }

    /**
     * Gets holder.
     *
     * @param mapType the map type
     * @return the holder
     */
    protected abstract ImageHolder getHolder(MapType mapType);

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);


    /**
     * Gets image.
     *
     * @param mapType the map type
     * @return the image
     */
    public BufferedImage getImage(MapType mapType)
    {
        return getHolder(mapType).getImage();
    }

    /**
     * Returns the number of imageHolders that should be updated.
     *
     * @param force write even if image isn't flagged as dirty
     * @return the int
     */
    public int writeToDiskAsync(boolean force)
    {
        return writeToDisk(force, true);
    }

    /**
     * Returns the number of imageHolders actually written to disk.
     *
     * @param force write even if image isn't flagged as dirty
     * @return the int
     */
    public int writeToDisk(boolean force)
    {
        return writeToDisk(force, false);
    }

    /**
     * Returns the number of imageHolders actually written to disk.
     *
     * @param force write even if image isn't flagged as dirty
     * @return number of images that should be updated if async=true, or
     * number of images actually updated if async=false
     */
    private int writeToDisk(boolean force, boolean async)
    {
        long now = System.currentTimeMillis();
        int count = 0;
        try
        {
            synchronized (imageHolders)
            {
                for (ImageHolder imageHolder : imageHolders.values())
                {
                    if (imageHolder.isDirty())
                    {
                        if (force || (now - imageHolder.getImageTimestamp() > 10000))
                        {
                            imageHolder.writeToDisk(async);
                            count++;
                        }
                    }
                }
            }

        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error("Error writing ImageSet to disk: " + t);
        }
        return count;
    }

    /**
     * Updated since boolean.
     *
     * @param mapType the map type
     * @param time    the time
     * @return the boolean
     */
    public boolean updatedSince(MapType mapType, long time)
    {
        synchronized (imageHolders)
        {
            if (mapType == null)
            {
                for (ImageHolder holder : imageHolders.values())
                {
                    if (holder != null && holder.getImageTimestamp() >= time)
                    {
                        return true;
                    }
                }
            }
            else
            {
                ImageHolder imageHolder = imageHolders.get(mapType);
                if (imageHolder != null && imageHolder.getImageTimestamp() >= time)
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Clear.
     */
    public void clear()
    {
        synchronized (imageHolders)
        {
            for (ImageHolder imageHolder : imageHolders.values())
            {
                imageHolder.clear();
            }
            imageHolders.clear();
        }
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("imageHolders", imageHolders.entrySet())
                .toString();
    }

    /**
     * ************************
     *
     * @return the image size
     */
    protected abstract int getImageSize();

    /**
     * Add holder image holder.
     *
     * @param mapType   the map type
     * @param imageFile the image file
     * @return the image holder
     */
    protected ImageHolder addHolder(MapType mapType, File imageFile)
    {
        return addHolder(new ImageHolder(mapType, imageFile, getImageSize()));
    }

    /**
     * Add holder image holder.
     *
     * @param imageHolder the image holder
     * @return the image holder
     */
    protected ImageHolder addHolder(ImageHolder imageHolder)
    {
        imageHolders.put(imageHolder.mapType, imageHolder);
        return imageHolder;
    }

}
