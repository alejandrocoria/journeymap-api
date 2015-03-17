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

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * An ImageSet contains one or more ImageHolders
 *
 * @author mwoodman
 */
public abstract class ImageSet
{
    protected final Map<Constants.MapType, ImageHolder> imageHolders;
    protected long lastTouched;

    public ImageSet()
    {
        imageHolders = Collections.synchronizedMap(new HashMap<Constants.MapType, ImageHolder>(3));
        touch();
    }

    protected abstract ImageHolder getHolder(Constants.MapType mapType);

    public <T extends ImageSet> T touch()
    {
        lastTouched = System.currentTimeMillis();
        return (T) this;
    }

    public long getLastTouched()
    {
        return lastTouched;
    }

    public BufferedImage getImage(Constants.MapType mapType)
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

    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName()).append("[ ");
        Iterator<ImageHolder> iter = imageHolders.values().iterator();
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
        for (ImageHolder imageHolder : imageHolders.values())
        {
            imageHolder.clear();
        }
        imageHolders.clear();
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    /**
     * ************************
     */

    protected abstract ImageHolder addHolder(Constants.MapType mapType, BufferedImage image);

    protected ImageHolder addHolder(Constants.MapType mapType, File imageFile, BufferedImage image)
    {
        return addHolder(new ImageHolder(mapType, imageFile, image));
    }

    protected ImageHolder addHolder(ImageHolder imageHolder)
    {
        imageHolders.put(imageHolder.mapType, imageHolder);
        return imageHolder;
    }

}
