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
import net.techbrew.journeymap.io.RegionImageHandler;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;

/**
 * A RegionImageSet contains one or more Wrappers of image, file, and maptype.
 *
 * @author mwoodman
 */
public class RegionImageSet extends ImageSet
{
    protected final RegionCoord rCoord;

    public RegionImageSet(RegionCoord rCoord)
    {
        super();
        this.rCoord = rCoord;
    }

    @Override
    protected Wrapper getWrapper(Constants.MapType mapType)
    {
        synchronized (imageWrappers)
        {
            // Check wrappers
            Wrapper wrapper = imageWrappers.get(mapType);
            if (wrapper != null)
            {
                return wrapper;
            }

            // Prepare to find image in file
            BufferedImage image = null;
            File imageFile = null;

            // Check for new region file
            imageFile = RegionImageHandler.getRegionImageFile(rCoord, mapType, false);
            image = RegionImageHandler.readRegionImage(imageFile, false);

            // Add wrapper
            wrapper = addWrapper(mapType, imageFile, image);
            return wrapper;
        }
    }

    public Map<MapType, Wrapper> getWrappers()
    {
        return imageWrappers;
    }

    public void setDirty(MapType mapType)
    {
        getWrapper(mapType).setDirty();
    }

    public Graphics2D getChunkImage(ChunkCoord cCoord, MapType mapType)
    {
        BufferedImage regionImage = getWrapper(mapType).getImage();
        if (regionImage == null)
        {
            return null;
        }
        return RegionImageHandler.initRenderingHints(
                regionImage.getSubimage(
                        rCoord.getXOffset(cCoord.chunkX),
                        rCoord.getZOffset(cCoord.chunkZ), 16, 16)
                        .createGraphics());
    }

    @Override
    public int hashCode()
    {
        return 31 * rCoord.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        return rCoord.equals(((RegionImageSet) obj).rCoord);
    }

    @Override
    protected Wrapper addWrapper(Constants.MapType mapType, BufferedImage image)
    {
        return addWrapper(new Wrapper(mapType, RegionImageHandler.getRegionImageFile(rCoord, mapType, false), image));
    }
}
