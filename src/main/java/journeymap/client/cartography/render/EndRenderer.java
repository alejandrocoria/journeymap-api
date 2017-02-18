/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.cartography.render;

import journeymap.client.cartography.IChunkRenderer;
import journeymap.client.cartography.RGB;
import journeymap.client.model.ChunkMD;
import journeymap.client.model.MapType;

/**
 * Render a chunk in the End.
 *
 * @author mwoodman
 */
public class EndRenderer extends SurfaceRenderer implements IChunkRenderer
{
    public EndRenderer()
    {
    }

    @Override
    protected boolean updateOptions(ChunkMD chunkMd, MapType mapType)
    {
        if (super.updateOptions(chunkMd, mapType))
        {
            this.ambientColor = RGB.floats(tweakEndAmbientColor);
            this.tweakMoonlightLevel = 5f;
            return true;
        }
        return false;
    }
}
