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
import journeymap.client.model.BlockMD;
import journeymap.client.model.ChunkMD;
import journeymap.client.model.MapType;
import journeymap.common.Journeymap;

/**
 * Render a chunk in the Nether.
 *
 * @author mwoodman
 */
public class NetherRenderer extends CaveRenderer implements IChunkRenderer
{
    public NetherRenderer()
    {
        super(null);
    }

    @Override
    protected boolean updateOptions(ChunkMD chunkMd, MapType mapType)
    {
        if (super.updateOptions(chunkMd, mapType))
        {
            this.ambientColor = RGB.floats(tweakNetherAmbientColor);
            this.mapSurfaceAboveCaves = false;
            return true;
        }
        return false;
    }

    /**
     * Get block height within slice.
     */
    @Override
    protected Integer getBlockHeight(final ChunkMD chunkMd, final int x, final Integer vSlice, final int z, final Integer sliceMinY, final Integer sliceMaxY)
    {
        Integer[][] blockSliceHeights = getHeights(chunkMd, vSlice);
        if (blockSliceHeights == null)
        {
            return null;
        }

        Integer intY = blockSliceHeights[x][z];

        if (intY != null)
        {
            return intY;
        }

        int y;

        try
        {
            y = sliceMaxY;

            BlockMD blockMD = BlockMD.getBlockMDFromChunkLocal(chunkMd, x, y, z);
            BlockMD blockMDAbove = BlockMD.getBlockMDFromChunkLocal(chunkMd, x, Math.min(y + 1, sliceMaxY), z);

            while (y > 0)
            {
                if (blockMD.isLava())
                {
                    break;
                }

                if (blockMDAbove.isAir() || blockMDAbove.hasTransparency() || blockMDAbove.hasFlag(BlockMD.Flag.OpenToSky))
                {
                    if (!blockMD.isAir() && !blockMD.hasTransparency() && !blockMD.hasFlag(BlockMD.Flag.OpenToSky))
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

                blockMD = BlockMD.getBlockMDFromChunkLocal(chunkMd, x, y, z);
                blockMDAbove = BlockMD.getBlockMDFromChunkLocal(chunkMd, x, y + 1, z);
            }
        }
        catch (Exception e)
        {
            Journeymap.getLogger().warn("Couldn't get safe slice block height at " + x + "," + z + ": " + e);
            y = sliceMaxY;
        }

        y = Math.max(0, y);

        blockSliceHeights[x][z] = y;
        return y;
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
