/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.model;

import journeymap.client.io.RegionImageHandler;
import journeymap.client.render.ComparableBufferedImage;
import journeymap.client.render.map.Tile;

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
    public ImageHolder getHolder(MapType mapType)
    {
        synchronized (imageHolders)
        {
            ImageHolder imageHolder = imageHolders.get(mapType);
            if (imageHolder == null)
            {
                // Prepare to find image in file
                File imageFile = RegionImageHandler.getRegionImageFile(getRegionCoord(), mapType, false);

                // Add holder
                imageHolder = addHolder(mapType, imageFile);
            }
            return imageHolder;
        }
    }

    public ComparableBufferedImage getChunkImage(ChunkMD chunkMd, MapType mapType)
    {
        RegionCoord regionCoord = getRegionCoord();
        BufferedImage regionImage = getHolder(mapType).getImage();
        BufferedImage sub = regionImage.getSubimage(
                regionCoord.getXOffset(chunkMd.getCoord().chunkXPos),
                regionCoord.getZOffset(chunkMd.getCoord().chunkZPos),
                16, 16);

        ComparableBufferedImage chunk = new ComparableBufferedImage(16, 16, regionImage.getType());
        chunk.setData(sub.getData());

        return chunk;
    }

    public void setChunkImage(ChunkMD chunkMd, MapType mapType, ComparableBufferedImage chunkImage)
    {
        ImageHolder holder = getHolder(mapType);
        boolean wasBlank = holder.blank;
        if (chunkImage.isChanged() || wasBlank)
        {
            RegionCoord regionCoord = getRegionCoord();
            holder.partialImageUpdate(chunkImage, regionCoord.getXOffset(chunkMd.getCoord().chunkXPos), regionCoord.getZOffset(chunkMd.getCoord().chunkZPos));
        }
        if (wasBlank)
        {
            holder.getTexture();
            holder.finishPartialImageUpdates();
            RegionImageCache.INSTANCE.getRegionImageSet(getRegionCoord());
        }
        chunkMd.setRendered(mapType);
    }

    public boolean hasChunkUpdates()
    {
        synchronized (imageHolders)
        {
            for (ImageHolder holder : this.imageHolders.values())
            {
                if (holder.partialUpdate)
                {
                    return true;
                }
            }
        }
        return false;
    }

    public void finishChunkUpdates()
    {
        synchronized (imageHolders)
        {
            for (ImageHolder holder : this.imageHolders.values())
            {
                holder.finishPartialImageUpdates();
            }
        }
    }

    public RegionCoord getRegionCoord()
    {
        return RegionCoord.fromRegionPos(key.worldDir, key.regionX, key.regionZ, key.dimension);
    }

    public long getOldestTimestamp()
    {
        long time = System.currentTimeMillis();
        synchronized (imageHolders)
        {
            for (ImageHolder holder : imageHolders.values())
            {
                if (holder != null)
                {
                    time = Math.min(time, holder.getImageTimestamp());
                }
            }
        }
        return time;
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
