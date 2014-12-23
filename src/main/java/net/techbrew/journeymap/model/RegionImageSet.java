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
import net.techbrew.journeymap.io.RegionImageHandler;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * A RegionImageSet contains one or more Wrappers of image, file, and maptype.
 *
 * @author mwoodman
 */
public class RegionImageSet extends ImageSet
{

    protected final RegionCoord rCoord;
    Logger logger = JourneyMap.getLogger();

    public RegionImageSet(RegionCoord rCoord)
    {
        super();
        this.rCoord = rCoord;
    }

    @Override
    protected Wrapper getWrapper(Constants.MapType mapType)
    {
        synchronized (lock)
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
            boolean useLegacy = !imageFile.exists();
            image = RegionImageHandler.readRegionImage(imageFile, rCoord, 1, false, false);

            // Add wrapper
            wrapper = addWrapper(mapType, imageFile, image);
            if (!useLegacy)
            {
                return wrapper;
            }

            // Fallback check for legacy (nightAndDay) region file
            File legacyFile = RegionImageHandler.getRegionImageFileLegacy(rCoord);
            if (legacyFile.exists())
            {

                // Get image for wrapper from legacy
                BufferedImage legacyImage = RegionImageHandler.readRegionImage(legacyFile, rCoord, 1, true, false);
                wrapper.setImage(getSubimage(rCoord, mapType, legacyImage));

                // Add other wrappers for day/night
                if (mapType == Constants.MapType.day)
                {
                    addWrapperFromLegacy(Constants.MapType.night, legacyImage);
                }
                else if (mapType == Constants.MapType.night)
                {
                    addWrapperFromLegacy(Constants.MapType.day, legacyImage);
                }

                // Add legacy wrapper
                addWrapper(Constants.MapType.OBSOLETE, legacyFile, null);

            }
            return wrapper;
        }
    }

    public void insertChunk(final ChunkImageSet cis)
    {
        synchronized (lock)
        {
            for (ChunkImageSet.Wrapper cisWrapper : cis.imageWrappers.values())
            {
                insertChunk(cis.getCCoord(), cisWrapper.getImage(), cisWrapper.getMapType());
            }
        }
    }

    protected void insertChunk(ChunkCoord cCoord, BufferedImage chunkImage, MapType mapType)
    {
        final Wrapper wrapper = getWrapper(mapType);
        final int x = rCoord.getXOffset(cCoord.chunkX);
        final int z = rCoord.getZOffset(cCoord.chunkZ);
        final BufferedImage subRegion = wrapper.getImage().getSubimage(x, z, 16, 16);
        final Graphics2D g2d = subRegion.createGraphics();
        g2d.drawImage(chunkImage, 0, 0, null);
        g2d.dispose();
        wrapper.setDirty();
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

    protected BufferedImage getSubimage(RegionCoord rCoord, MapType mapType, BufferedImage image)
    {
        if (image == null)
        {
            return null;
        }
        switch (mapType)
        {
            case night:
            {
                return copyImage(image.getSubimage(512, 0, 512, 512));
            }
            default:
            {
                return copyImage(image.getSubimage(0, 0, 512, 512));
            }
        }
    }

    protected Wrapper addWrapperFromLegacy(Constants.MapType mapType, BufferedImage legacyImage)
    {
        BufferedImage image = getSubimage(rCoord, mapType, legacyImage);
        return addWrapper(mapType, image);
    }

    @Override
    protected Wrapper addWrapper(Constants.MapType mapType, BufferedImage image)
    {
        return addWrapper(new Wrapper(mapType, RegionImageHandler.getRegionImageFile(rCoord, mapType, false), image));
    }
}
