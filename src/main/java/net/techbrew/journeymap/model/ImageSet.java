/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.model;

import com.google.common.base.Objects;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An ImageSet contains one or more ImageHolders
 *
 * @author mwoodman
 */
public abstract class ImageSet
{
    protected final Map<MapType, ImageHolder> imageHolders;

    public ImageSet()
    {
        imageHolders = Collections.synchronizedMap(new HashMap<MapType, ImageHolder>(8));
    }

    protected abstract ImageHolder getHolder(MapType mapType);

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);


    public BufferedImage getImage(MapType mapType)
    {
        return getHolder(mapType).getImage();
    }

    public boolean writeToDisk(boolean force)
    {
        boolean updated = false;
        for (ImageHolder imageHolder : imageHolders.values())
        {
            if (force || imageHolder.isDirty())
            {
                imageHolder.writeToDisk();
                updated = true;
            }
        }
        return updated;
    }

    public boolean updatedSince(MapType mapType, long time)
    {
        for (ImageHolder imageHolder : imageHolders.values())
        {
            if (mapType != null)
            {
                if (imageHolder.getMapType() == mapType && imageHolder.getImageTimestamp() > time)
                {
                    return true;
                }
            }
            else if (imageHolder.getImageTimestamp() > time)
            {
                return true;
            }
        }
        return false;
    }

    public void clear()
    {
        for (ImageHolder imageHolder : imageHolders.values())
        {
            imageHolder.clear();
        }
        imageHolders.clear();
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
     */

    protected abstract int getImageSize();

    protected ImageHolder addHolder(MapType mapType, File imageFile)
    {
        return addHolder(new ImageHolder(mapType, imageFile, getImageSize()));
    }

    protected ImageHolder addHolder(ImageHolder imageHolder)
    {
        imageHolders.put(imageHolder.mapType, imageHolder);
        return imageHolder;
    }

}
