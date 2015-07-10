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
import net.techbrew.journeymap.forge.helper.ForgeHelper;
import net.techbrew.journeymap.model.BlockMD;
import net.techbrew.journeymap.model.ChunkMD;

/**
 * Render a chunk in the Nether.
 *
 * @author mwoodman
 */
public class NetherRenderer extends CaveRenderer implements IChunkRenderer
{
    // Taken from WorldProviderHell.getFogColor(): {0.20000000298023224f, 0.029999999329447746f, 0.029999999329447746f};
    public NetherRenderer()
    {
        super(null);
        cachePrefix = "Nether";
    }

    @Override
    protected void updateOptions()
    {
        super.updateOptions();
        this.ambientColor = RGB.floats(tweakNetherAmbientColor);
        this.mapSurfaceAboveCaves = false;
    }

    /**
     * Get block height within slice.
     */
    @Override
    protected Integer getSliceBlockHeight(final ChunkMD chunkMd, final int x, final Integer vSlice, final int z, final int sliceMinY, final int sliceMaxY,
                                          final HeightsCache chunkHeights)
    {
        Integer[][] blockSliceHeights = chunkHeights.getUnchecked(chunkMd.getCoord());
        if(blockSliceHeights==null)
        {
            return null;
        }

        Integer y = blockSliceHeights[x][z];

        if (y != null)
        {
            return y;
        }

        try
        {
            y = sliceMaxY;

            BlockMD blockMD = dataCache.getBlockMD(chunkMd, x, y, z);
            BlockMD blockMDAbove = dataCache.getBlockMD(chunkMd, x, Math.min(y + 1, sliceMaxY), z);

            while (y > 0)
            {
                if (blockMD.isLava())
                {
                    break;
                }

                if (blockMDAbove.isAir() || blockMDAbove.hasTranparency() || blockMDAbove.hasFlag(BlockMD.Flag.OpenToSky))
                {
                    if (!blockMD.isAir() && !blockMD.hasTranparency() && !blockMD.hasFlag(BlockMD.Flag.OpenToSky))
                    {
                        break;
                    }
                }
                else if (y == sliceMinY)
                {
                    y = sliceMaxY;
                    break;
                }

                y--;

                blockMD = dataCache.getBlockMD(chunkMd, x, y, z);
                blockMDAbove = dataCache.getBlockMD(chunkMd, x, y + 1, z);
            }
        }
        catch (Exception e)
        {
            JourneyMap.getLogger().warn("Couldn't get safe slice block height at " + x + "," + z + ": " + e);
            y = sliceMaxY;
        }

        y = Math.max(0, y);

        blockSliceHeights[x][z] = y;
        return y;
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
        if (y + 1 >= chunkMd.getWorldActualHeight())
        {
            return 0;
        }

        int actualLight = chunkMd.getSavedLightValue(x, y + 1, z);

        if (actualLight > 0)
        {
            return actualLight;
        }
        else
        {
            //System.out.print(y + "  ");
            return Math.max(adjusted ? 2 : 0, actualLight);
        }

    }

    @Override
    public float[] getAmbientColor()
    {
        return ambientColor;
    }
}
