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
 * Render a chunk cave layer in the End.
 *
 * @author techbrew
 */
public class EndCaveRenderer extends CaveRenderer implements IChunkRenderer
{
    /**
     * Constructor.
     */
    public EndCaveRenderer(SurfaceRenderer endSurfaceRenderer)
    {
        super(endSurfaceRenderer);
    }

    @Override
    protected boolean updateOptions(ChunkMD chunkMd, MapType mapType)
    {
        if (super.updateOptions(chunkMd, mapType))
        {
            this.ambientColor = RGB.floats(tweakEndAmbientColor);
            return true;
        }
        return false;
    }

    protected int getSliceLightLevel(ChunkMD chunkMd, int x, int y, int z, boolean adjusted)
    {
        return mapCaveLighting ? Math.max(adjusted ? ((int) surfaceRenderer.tweakMoonlightLevel) : 0, chunkMd.getSavedLightValue(x, y + 1, z)) : 15;
    }
}
