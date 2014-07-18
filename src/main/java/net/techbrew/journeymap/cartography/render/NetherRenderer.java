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
import net.techbrew.journeymap.cartography.Strata;
import net.techbrew.journeymap.model.BlockMD;
import net.techbrew.journeymap.model.ChunkMD;

/**
 * Render a chunk in the Nether.
 *
 * @author mwoodman
 */
public class NetherRenderer extends OverworldCaveRenderer implements IChunkRenderer
{
    // Taken from WorldProviderHell.getFogColor()
    private float[] fog = new float[]{0.20000000298023224f, 0.029999999329447746f, 0.029999999329447746f};

    public NetherRenderer()
    {
        super(null);
    }

    /**
     * Get block height within slice.
     */
    @Override
    protected int getSliceBlockHeight(final ChunkMD chunkMd, final int x, final Integer vSlice, final int z, final int sliceMinY, final int sliceMaxY, boolean ignoreWater)
    {
        Integer[][] blockSliceHeights = chunkMd.sliceHeights.get(vSlice);
        if (blockSliceHeights == null)
        {
            blockSliceHeights = new Integer[16][16];
            chunkMd.sliceHeights.put(vSlice, blockSliceHeights);
        }

        Integer y = blockSliceHeights[x][z];

        if (y != null)
        {
            return y;
        }

        try
        {
            y = sliceMaxY - 1;

            BlockMD blockMD = dataCache.getBlockMD(chunkMd, x, y, z);
            BlockMD blockMDAbove = dataCache.getBlockMD(chunkMd, x, y + 1, z);

            while (y > 0)
            {
                if (blockMD.isLava())
                {
                    break;
                }

                if (blockMDAbove.isAir() || blockMDAbove.hasTranparency() || blockMDAbove.hasFlag(BlockMD.Flag.OpenToSky) || blockMDAbove.hasFlag(BlockMD.Flag.TransparentRoof))
                {
                    if (!blockMD.isAir())
                    {
                        break;
                    }
                }
                y--;
                blockMD = dataCache.getBlockMD(chunkMd, x, y, z);
                blockMDAbove = dataCache.getBlockMD(chunkMd, x, y + 1, z);
            }
        }
        catch (Exception e)
        {
            JourneyMap.getLogger().warning("Couldn't get safe slice block height at " + x + "," + z + ": " + e);
            y = sliceMaxY;
        }

        blockSliceHeights[x][z] = y;
        return y;
    }

    /**
     * Create Strata for caves, using first lit blocks found.
     */
    protected void buildStrata(Strata strata, final ChunkMD.Set neighbors, int minY, ChunkMD chunkMd, int x, final int topY, int z)
    {
        super.buildStrata(strata, neighbors, minY, chunkMd, x, topY, z);
    }

    /**
     * Get the light level for the block in the slice.  Can be overridden to provide an ambient light minimum.
     */
    @Override
    protected int getSliceLightLevel(ChunkMD chunkMd, int x, int y, int z, boolean adjusted)
    {
        return Math.max(adjusted ? 2 : 0, chunkMd.getSavedLightValue(EnumSkyBlock.Block, x, y + 1, z));
    }

    @Override
    public float[] getFogColor()
    {
        return fog;
    }
}
