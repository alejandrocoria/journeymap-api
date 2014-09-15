/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.cartography.render;


import net.techbrew.journeymap.cartography.IChunkRenderer;
import net.techbrew.journeymap.cartography.RGB;
import net.techbrew.journeymap.cartography.Strata;
import net.techbrew.journeymap.model.BlockMD;
import net.techbrew.journeymap.model.ChunkMD;

import java.awt.*;

/**
 * Render a chunk in the End.
 *
 * @author mwoodman
 */
public class EndRenderer extends SurfaceRenderer implements IChunkRenderer
{
    private static final int MIN_LIGHT_LEVEL = 2;

    public EndRenderer()
    {
        super();
        cachePrefix = "End";
    }

    @Override
    protected void updateOptions()
    {
        super.updateOptions();
        this.ambientColor = RGB.floats(tweakEndAmbientColor);
        this.tweakMoonlightLevel = 5f;
    }

    /**
     * Create Strata.
     */
    @Override
    protected void buildStrata(Strata strata, int minY, ChunkMD chunkMd, int x, final int topY, int z)//, HeightsCache chunkHeights, SlopesCache chunkSlopes)
    {
        super.buildStrata(strata, minY, chunkMd, x, topY, z);//, chunkHeights, chunkSlopes);
    }


    /**
     * Paint the image with the color derived from a BlockStack
     */
    protected boolean paintStrata(final Strata strata, final Graphics2D g2D, final ChunkMD chunkMd, final BlockMD topBlockMd, final Integer vSlice, final int x, final int y, final int z, final boolean cavePrePass)
    {
        return super.paintStrata(strata, g2D, chunkMd, topBlockMd, vSlice, x, y, z, cavePrePass);
    }

}
