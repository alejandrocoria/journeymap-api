/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.model;

import com.google.common.base.Objects;
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
    protected final Key key;

    public RegionImageSet(Key key)
    {
        super();
        this.key = key;
    }

    @Override
    public ImageHolder getHolder(Constants.MapType mapType, Integer vSlice)
    {
        if (vSlice != null)
        {
            assert (mapType == MapType.underground);
        }
        else
        {
            assert (mapType != MapType.underground);
        }

        synchronized (imageHolders)
        {
            for (ImageHolder holder : imageHolders.get(mapType))
            {
                if (Objects.equal(vSlice, holder.vSlice))
                {
                    return holder;
                }
            }

            // Prepare to find image in file
            File imageFile = RegionImageHandler.getRegionImageFile(getRegionCoord(vSlice), mapType, false);

            // Add holder
            ImageHolder imageHolder = addHolder(mapType, vSlice, imageFile);
            return imageHolder;
        }
    }

    public BufferedImage getChunkImage(ChunkCoord cCoord, MapType mapType)
    {
        BufferedImage regionImage = getHolder(mapType, cCoord.vSlice).getImage();
        RegionCoord regionCoord = getRegionCoord(cCoord.vSlice);
        BufferedImage current = regionImage.getSubimage(
                regionCoord.getXOffset(cCoord.chunkX),
                regionCoord.getZOffset(cCoord.chunkZ),
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
        ImageHolder holder = getHolder(mapType, cCoord.vSlice);
        RegionCoord regionCoord = getRegionCoord(cCoord.vSlice);
        holder.partialImageUpdate(chunkImage, regionCoord.getXOffset(cCoord.chunkX), regionCoord.getZOffset(cCoord.chunkZ));
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

    public RegionCoord getRegionCoordFor(ImageHolder imageHolder)
    {
        return getRegionCoord(imageHolder.vSlice);
    }


    public RegionCoord getRegionCoord(Integer vSlice)
    {
        return RegionCoord.fromRegionPos(key.worldDir, key.regionX, vSlice, key.regionZ, key.dimension);
    }

    @Override
    public int hashCode()
    {
        return key.hashCode();
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
        return key.equals(((RegionImageSet) obj).key);
    }

    @Override
    protected int getImageSize()
    {
        return Tile.TILESIZE;
    }

    public static class Key
    {
        private final File worldDir;
        private final int regionX;
        private final int regionZ;
        private final int dimension;

        private Key(File worldDir, int regionX, int regionZ, int dimension)
        {
            this.worldDir = worldDir;
            this.regionX = regionX;
            this.regionZ = regionZ;
            this.dimension = dimension;
        }

        public static Key from(RegionCoord rCoord)
        {
            return new Key(rCoord.worldDir, rCoord.regionX, rCoord.regionZ, rCoord.dimension);
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            Key key = (Key) o;

            if (dimension != key.dimension)
            {
                return false;
            }
            if (regionX != key.regionX)
            {
                return false;
            }
            if (regionZ != key.regionZ)
            {
                return false;
            }
            if (!worldDir.equals(key.worldDir))
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = worldDir.hashCode();
            result = 31 * result + regionX;
            result = 31 * result + regionZ;
            result = 31 * result + dimension;
            return result;
        }
    }
}
