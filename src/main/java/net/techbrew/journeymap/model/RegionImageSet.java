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
import net.techbrew.journeymap.render.map.Tile;

import java.awt.image.BufferedImage;
import java.io.File;

/**
 * A RegionImageSet contains one or more ImageHolders for Region images
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
    public ImageHolder getHolder(Constants.MapType mapType)
    {
        synchronized (imageHolders)
        {
            // Check holder
            ImageHolder imageHolder = imageHolders.get(mapType);
            if (imageHolder != null)
            {
                return imageHolder;
            }

            // Prepare to find image in file
            BufferedImage image = null;
            File imageFile = null;

            // Check for new region file
            imageFile = RegionImageHandler.getRegionImageFile(rCoord, mapType, false);
            image = RegionImageHandler.readRegionImage(imageFile, false);

            // Add holder
            imageHolder = addHolder(mapType, imageFile, image);
            return imageHolder;
        }
    }

    public BufferedImage getChunkImage(ChunkCoord cCoord, MapType mapType)
    {
        BufferedImage regionImage = getHolder(mapType).getImage();
        BufferedImage current = regionImage.getSubimage(
                rCoord.getXOffset(cCoord.chunkX),
                rCoord.getZOffset(cCoord.chunkZ),
                16, 16);

        //BufferedImage copy = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        //Graphics g2D = RegionImageHandler.initRenderingHints(copy.createGraphics());
        //g2D.drawImage(current, 0, 0, null);
        //g2D.dispose();
        //return copy;
        return current;
    }

    public void setChunkImage(ChunkCoord cCoord, MapType mapType, BufferedImage chunkImage)
    {
        ImageHolder holder = getHolder(mapType);
        holder.partialImageUpdate(chunkImage, rCoord.getXOffset(cCoord.chunkX), rCoord.getZOffset(cCoord.chunkZ));
    }

    public boolean hasChunkUpdates()
    {
        for (ImageHolder holder : this.imageHolders.values())
        {
            if (holder.partialUpdate)
            {
                return true;
            }
        }
        return false;
    }

    public void finishChunkUpdates()
    {
        for (ImageHolder holder : this.imageHolders.values())
        {
            holder.finishPartialImageUpdates();
        }
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
    protected ImageHolder addHolder(Constants.MapType mapType, BufferedImage image)
    {
        return addHolder(new ImageHolder(mapType, RegionImageHandler.getRegionImageFile(rCoord, mapType, false), image, getImageSize()));
    }

    @Override
    protected int getImageSize()
    {
        return Tile.TILESIZE;
    }
}
