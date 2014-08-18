/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.cartography.render;


import net.minecraft.world.EnumSkyBlock;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.cartography.IChunkRenderer;
import net.techbrew.journeymap.cartography.RGB;
import net.techbrew.journeymap.cartography.Strata;
import net.techbrew.journeymap.model.BlockMD;
import net.techbrew.journeymap.model.ChunkMD;

/**
 * Render a chunk in the End.
 *
 * @author mwoodman
 */
public class EndRenderer extends CaveRenderer implements IChunkRenderer
{
    public EndRenderer()
    {
        super(null);
        cachePrefix = "End";
    }

    @Override
    protected void updateOptions()
    {
        super.updateOptions();
        this.ambientColor = RGB.floats(tweakEndAmbientColor);
    }

    /**
     * Create Strata for caves, using first lit blocks found.
     */
    protected void buildStrata(Strata strata, int minY, ChunkMD chunkMd, int x, final int topY, int z, HeightsCache chunkHeights, SlopesCache chunkSlopes)
    {
        super.buildStrata(strata, minY, chunkMd, x, topY, z, chunkHeights, chunkSlopes);
    }

    /**
     * Get the light level for the block in the slice.  Can be overridden to provide an ambient light minimum.
     */
    @Override
    protected int getSliceLightLevel(ChunkMD chunkMd, int x, int y, int z, boolean adjusted)
    {
        return Math.max(adjusted ? 2 : 0, chunkMd.getSavedLightValue(EnumSkyBlock.Block, x, y + 1, z));
    }

}
