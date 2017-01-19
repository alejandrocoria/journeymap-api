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
import journeymap.client.cartography.Strata;
import journeymap.client.cartography.Stratum;
import journeymap.client.log.StatTimer;
import journeymap.client.model.*;
import journeymap.client.render.ComparableBufferedImage;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldProviderHell;

import java.awt.image.BufferedImage;

/**
 * Renders chunk image for caves in the overworld.
 */
public class CaveRenderer extends BaseRenderer implements IChunkRenderer
{
    private static final String PROP_CAVE_SLOPES = "caveSlopes";
    private static final String PROP_CAVE_HEIGHTS = "caveHeights";

    protected SurfaceRenderer surfaceRenderer;
    protected StatTimer renderCaveTimer = StatTimer.get("CaveRenderer.render");
    protected Strata strata = new Strata("Cave", 40, 8, true);
    protected float defaultDim = .2f;
    protected boolean mapSurfaceAboveCaves;

    /**
     * Takes an instance of the surface renderer in order to do a prepass when the surface
     * intersects the slice being mapped.
     */
    public CaveRenderer(SurfaceRenderer surfaceRenderer)
    {
        this.surfaceRenderer = surfaceRenderer;
        cachePrefix = "Cave";

        updateOptions(null);

        // TODO: Put these in properties?
        shadingSlopeMin = 0.2f;
        shadingSlopeMax = 1.1f;
        shadingPrimaryDownslopeMultiplier = .7f;
        shadingPrimaryUpslopeMultiplier = 1.05f;
        shadingSecondaryDownslopeMultiplier = .99f;
        shadingSecondaryUpslopeMultiplier = 1.01f;
    }

    @Override
    protected boolean updateOptions(ChunkMD chunkMd)
    {
        if (super.updateOptions(chunkMd))
        {
            mapSurfaceAboveCaves = Journeymap.getClient().getCoreProperties().mapSurfaceAboveCaves.get();
            return true;
        }
        return false;
    }

    @Override
    public int getBlockHeight(ChunkMD chunkMd, BlockPos blockPos)
    {
        Integer vSlice = blockPos.getY() >> 4;
        final int[] sliceBounds = getVSliceBounds(chunkMd, vSlice);
        final int sliceMinY = sliceBounds[0];
        final int sliceMaxY = sliceBounds[1];
        Integer y = getBlockHeight(chunkMd, blockPos.getX() & 15, vSlice, blockPos.getZ() & 15, sliceMinY, sliceMaxY);

        return (y == null) ? blockPos.getY() : y;
    }

    /**
     * Render chunk image for caves in the overworld.
     */
    @Override
    public synchronized boolean render(final ComparableBufferedImage chunkImage, final ChunkMD chunkMd, final Integer vSlice)
    {
        if (vSlice == null)
        {
            Journeymap.getLogger().warn("ChunkOverworldCaveRenderer is for caves. vSlice can't be null");
            return false;
        }

        updateOptions(chunkMd);
        boolean ok = false;

        renderCaveTimer.start();

        try
        {
            if (!hasSlopes(chunkMd, vSlice))
            {
                populateSlopes(chunkMd, vSlice, getSlopes(chunkMd, vSlice));
            }

            // Get surface image
            BufferedImage chunkSurfaceImage = null;
            if (mapSurfaceAboveCaves)
            {
                MapType mapType = MapType.day(chunkMd.getDimension());
                RegionImageSet ris = RegionImageCache.INSTANCE.getRegionImageSet(chunkMd, mapType);
                if (ris != null && ris.getHolder(mapType).hasTexture())
                {
                    chunkSurfaceImage = ris.getChunkImage(chunkMd, mapType);
                }
            }

            // Render that lovely cave action
            ok = renderUnderground(chunkSurfaceImage, chunkImage, chunkMd, vSlice);

            return ok;
        }
        finally
        {
            renderCaveTimer.stop();
        }
    }

    protected void mask(final BufferedImage chunkSurfaceImage, final BufferedImage chunkImage,
                        final ChunkMD chunkMd, final int x, final int y, final int z)
    {
        if (chunkSurfaceImage == null || !mapSurfaceAboveCaves)
        {
            paintBlackBlock(chunkImage, x, z);
        }
        else
        {
            int surfaceY = Math.max(0, chunkMd.getChunk().getHeightValue(x, z));
            int distance = Math.max(0, surfaceY - y);
            if (distance > 16)
            {
                paintBlackBlock(chunkImage, x, z);
            }
            else
            {
                float dim = Math.max(defaultDim, 16f / distance);
                paintDimOverlay(chunkSurfaceImage, chunkImage, x, z, defaultDim);
            }
        }
    }

    /**
     * Render blocks in the chunk for underground.
     */
    protected boolean renderUnderground(final BufferedImage chunkSurfaceImage, final BufferedImage chunkSliceImage, final ChunkMD chunkMd, final int vSlice)
    {
        final int[] sliceBounds = getVSliceBounds(chunkMd, vSlice);
        final int sliceMinY = sliceBounds[0];
        final int sliceMaxY = sliceBounds[1];

        int y;

        boolean chunkOk = false;

        for (int z = 0; z < 16; z++)
        {
            blockLoop:
            for (int x = 0; x < 16; x++)
            {
                strata.reset();

                try
                {
                    final int ceiling = getBlockHeight(chunkMd, x, vSlice, z, sliceMinY, sliceMaxY);

                    // Oh look, a hole in the world.
                    if (ceiling < 0)
                    {
                        paintVoidBlock(chunkSliceImage, x, z);
                        chunkOk = true;
                        continue;
                    }

                    // Nothing even in this slice.
                    if (ceiling < sliceMinY)
                    {
                        mask(chunkSurfaceImage, chunkSliceImage, chunkMd, x, sliceMinY, z);
                        chunkOk = true;
                        continue;
                    }

                    if (ceiling > sliceMaxY)
                    {
                        // Solid stuff above the slice. Shouldn't be painted by surface renderer.
                        y = sliceMaxY;
                    }
                    else
                    {
                        // Ceiling within slice. Should be painted by by surface renderer... should we dim it?
                        y = ceiling;
                    }

                    buildStrata(strata, sliceMinY, chunkMd, x, y, z);

                    if (strata.isEmpty())
                    {
                        // No lit blocks
                        if (strata.isBlocksFound())
                        {
                            mask(chunkSurfaceImage, chunkSliceImage, chunkMd, x, y, z);
                        }
                        else
                        {
                            // No blocks at all?  Okay then.
                            paintVoidBlock(chunkSliceImage, x, z);
                        }

                        chunkOk = true;
                    }
                    else
                    {
                        // Paint that action
                        chunkOk = paintStrata(strata, chunkSliceImage, chunkMd, vSlice, x, ceiling, z) || chunkOk;
                    }

                }
                catch (Throwable t)
                {
                    paintBadBlock(chunkSliceImage, x, vSlice, z);
                    String error = "CaveRenderer error at x,vSlice,z = " + x + "," //$NON-NLS-1$ //$NON-NLS-2$
                            + vSlice + "," + z + " : " + LogFormatter.toString(t); //$NON-NLS-1$ //$NON-NLS-2$
                    Journeymap.getLogger().error(error);
                }
            }
        }
        strata.reset();
        return chunkOk;
    }

    /**
     * Create Strata for caves, using first lit blocks found.
     */
    protected void buildStrata(Strata strata, int minY, ChunkMD chunkMd, int x, final int topY, int z)
    {
        BlockMD blockMD;
        BlockMD blockAboveMD;
        BlockMD lavaBlockMD = null;

        try
        {
            int lightLevel;
            int y = getBlockHeight(chunkMd, x, topY >> 4, z, minY, topY);

            while (y > 0)
            {
                blockMD = BlockMD.getBlockMDFromChunkLocal(chunkMd, x, y, z);

                if (!blockMD.isAir() && !blockMD.hasFlag(BlockMD.Flag.OpenToSky))
                {
                    strata.setBlocksFound(true);
                    blockAboveMD = BlockMD.getBlockMDFromChunkLocal(chunkMd, x, y + 1, z);

                    if (blockMD.isLava() && (blockAboveMD.isLava() || y < minY))
                    {
                        // Ignores the myriad tiny one-block pockets of lava in the Nether
                        lavaBlockMD = blockMD;
                    }

                    if (blockAboveMD.isAir() || blockAboveMD.hasFlag(BlockMD.Flag.OpenToSky))
                    {
                        if (!chunkMd.canBlockSeeTheSky(x, y + 1, z))
                        {
                            lightLevel = getSliceLightLevel(chunkMd, x, y, z, true);

                            if (lightLevel > 0)
                            {
                                strata.push(chunkMd, blockMD, x, y, z, lightLevel);
                                if (blockMD.getAlpha() == 1f || !mapTransparency)
                                {
                                    break;
                                }
                            }
                            else if (y < minY)
                            {
                                break;
                            }
                        }
                    }
                    else
                    {
                        x = x;
                        break;
                    }
                }
                y--;
            }
        }
        finally
        {
            // Corner case where the column has lava but no air in it.
            // This is a nether thing
            if (strata.isEmpty() && lavaBlockMD != null && chunkMd.getWorld().provider instanceof WorldProviderHell)
            {
                strata.push(chunkMd, lavaBlockMD, x, topY, z, 14);
            }
        }
    }

    /**
     * Paint the image with the color derived from a BlockStack
     */
    protected boolean paintStrata(final Strata strata, final BufferedImage chunkSliceImage, final ChunkMD chunkMd, final Integer vSlice, final int x, final int y, final int z)
    {
        if (strata.isEmpty())
        {
            paintBadBlock(chunkSliceImage, x, y, z);
            return false;
        }

        try
        {
            Stratum stratum = null;
            BlockMD blockMD = null;

            while (!strata.isEmpty())
            {
                stratum = strata.nextUp(this, true);

                // Simple surface render
                if (strata.getRenderCaveColor() == null)
                {
                    strata.setRenderCaveColor(stratum.getCaveColor());
                }
                else
                {
                    strata.setRenderCaveColor(RGB.blendWith(strata.getRenderCaveColor(), stratum.getCaveColor(), stratum.getBlockMD().getAlpha()));
                }

                blockMD = stratum.getBlockMD();
                strata.release(stratum);

            } // end color stack

            // Shouldn't happen
            if (strata.getRenderCaveColor() == null)
            {
                paintBadBlock(chunkSliceImage, x, y, z);
                return false;
            }

            // Now add bevel for slope
            if (!(blockMD.hasNoShadow()))
            {
                float slope = getSlope(chunkMd, blockMD, x, vSlice, z);
                if (slope != 1f)
                {
                    strata.setRenderCaveColor(RGB.bevelSlope(strata.getRenderCaveColor(), slope));
                }
            }

            // And draw to the actual chunkimage
            paintBlock(chunkSliceImage, x, z, strata.getRenderCaveColor());
        }
        catch (RuntimeException e)
        {
            paintBadBlock(chunkSliceImage, x, y, z);
            throw e;
        }

        return true;
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

        Integer y = blockSliceHeights[x][z];

        if (y != null)
        {
            return y;
        }

        try
        {
            y = Math.min(chunkMd.getHeight(new BlockPos(x, 0, z)), sliceMaxY) - 1;
            if (y <= sliceMinY)
            {
                return y;
            }

            BlockMD blockMDAbove;

            // Improves results near surface when trees above, etc.
            if (y + 1 < sliceMaxY)
            {
                while (y > 0 && y > sliceMinY)
                {
                    blockMDAbove = BlockMD.getBlockMDFromChunkLocal(chunkMd, x, y + 1, z);
                    if (!blockMDAbove.isAir() && !blockMDAbove.hasFlag(BlockMD.Flag.OpenToSky))
                    {
                        break;
                    }
                    y--;
                }
            }

            blockMDAbove = BlockMD.getBlockMDFromChunkLocal(chunkMd, x, y + 1, z);
            BlockMD blockMD = BlockMD.getBlockMDFromChunkLocal(chunkMd, x, y, z);

            boolean inAirPocket = false;

            while (y > 0 && y > sliceMinY)
            {

                if (mapBathymetry && blockMD.isWater())
                {
                    y--;
                }

                inAirPocket = blockMD.isAir();

                if (blockMDAbove.isAir() || blockMDAbove.hasTranparency() || blockMDAbove.hasFlag(BlockMD.Flag.OpenToSky))
                {
                    if (!blockMD.isAir() || !blockMD.hasTranparency() || !blockMD.hasFlag(BlockMD.Flag.OpenToSky))
                    {
                        break;
                    }
                }

                y--;

                blockMD = BlockMD.getBlockMDFromChunkLocal(chunkMd, x, y, z);
                blockMDAbove = BlockMD.getBlockMDFromChunkLocal(chunkMd, x, y + 1, z);

                if (y < sliceMinY && !inAirPocket)
                {
                    break;
                }
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
    protected int getSliceLightLevel(ChunkMD chunkMd, int x, int y, int z, boolean adjusted)
    {
        return mapCaveLighting ? chunkMd.getSavedLightValue(x, y + 1, z) : 15;
    }
}
