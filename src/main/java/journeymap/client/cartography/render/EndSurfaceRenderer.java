/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.cartography.render;

import journeymap.client.cartography.IChunkRenderer;
import journeymap.client.cartography.color.RGB;
import journeymap.client.model.ChunkMD;
import journeymap.client.model.MapType;

/**
 * Render a chunk surface in the End.
 *
 * @author techbrew
 */
public class EndSurfaceRenderer extends SurfaceRenderer implements IChunkRenderer
{
    /**
     * Constructor.
     */
    public EndSurfaceRenderer()
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
