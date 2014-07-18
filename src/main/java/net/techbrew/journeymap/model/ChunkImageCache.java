/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.model;

import net.techbrew.journeymap.Constants.MapType;

import java.awt.image.BufferedImage;
import java.util.HashMap;

public class ChunkImageCache extends HashMap<ChunkCoord, ChunkImageSet>
{

    public ChunkImageCache()
    {
        super(768);
    }

    public void put(ChunkCoord cCoord, MapType mapType, BufferedImage chunkImage)
    {
        ChunkImageSet cis = get(cCoord);
        if (cis == null)
        {
            cis = new ChunkImageSet(cCoord);
            put(cCoord, cis);
        }
        cis.getWrapper(mapType).setImage(chunkImage);
    }
}
