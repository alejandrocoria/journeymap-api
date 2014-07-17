package net.techbrew.journeymap.cartography;

import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.StatTimer;
import net.techbrew.journeymap.model.BlockMD;
import net.techbrew.journeymap.model.ChunkMD;

import java.awt.*;

public class ChunkOverworldSurfaceRenderer extends BaseRenderer implements IChunkRenderer
{
    protected StatTimer renderSurfaceTimer = StatTimer.get("ChunkOverworldSurfaceRenderer.renderSurface");
    protected StatTimer renderSurfacePrepassTimer = StatTimer.get("ChunkOverworldSurfaceRenderer.renderSurface.CavePrepass");
    protected Strata strata = new Strata("OverworldSurface", 40, 8, false);
    protected float maxDepth = 16;

    /**
     * Render blocks in the chunk for the standard world.
     */
    @Override
    public boolean render(final Graphics2D g2D, final ChunkMD chunkMd, final boolean underground,
                          final Integer vSlice, final ChunkMD.Set neighbors)
    {
        return render(g2D, chunkMd, underground, vSlice, neighbors, false);
    }

    /**
     * Render blocks in the chunk for the standard world.
     */
    public boolean render(final Graphics2D g2D, final ChunkMD chunkMd, final boolean underground,
                          final Integer vSlice, final ChunkMD.Set neighbors, final boolean cavePrePass)
    {
        StatTimer timer = cavePrePass ? renderSurfacePrepassTimer : renderSurfaceTimer;
        timer.start();

        updateOptions();

        try
        {
            if (!cavePrePass && (underground || vSlice != null))
            {
                JourneyMap.getLogger().warning(String.format("ChunkOverworldSurfaceRenderer is for surface.  Y u do dis? (%s,%s)", underground, vSlice));
            }

            // Initialize ChunkSub slopes if needed
            if (chunkMd.surfaceSlopes == null)
            {
                populateSlopes(chunkMd, null, neighbors);
            }

            // Render the chunk image
            return renderSurface(g2D, chunkMd, vSlice, neighbors, cavePrePass);
        }
        finally
        {
            strata.reset();
            timer.stop();
        }
    }


    /**
     * Render blocks in the chunk for the surface.
     */
    protected boolean renderSurface(final Graphics2D g2D, final ChunkMD chunkMd, final Integer vSlice, final ChunkMD.Set neighbors, final boolean cavePrePass)
    {
        g2D.setComposite(OPAQUE);

        int sliceMinY = 0;
        int sliceMaxY = 0;

        if (cavePrePass)
        {
            int[] sliceBounds = getVSliceBounds(chunkMd, vSlice);
            sliceMinY = sliceBounds[0];
            sliceMaxY = sliceBounds[1];
        }

        boolean chunkOk = false;
        for (int x = 0; x < 16; x++)
        {
            blockLoop:
            for (int z = 0; z < 16; z++)
            {
                strata.reset();
                BlockMD topBlockMd = null;

                int standardY = Math.max(1, chunkMd.getSurfaceBlockHeight(x, z, mapBathymetry));

                // Should be painted only by cave renderer
                if (cavePrePass && (standardY > sliceMaxY && standardY - sliceMaxY > maxDepth))
                {
                    chunkOk = true;
                    paintBlackBlock(x, z, g2D);
                    continue;
                }

                int roofY = 0;
                int y = standardY;

                roofY = Math.max(1, chunkMd.getAbsoluteHeightValue(x, z));
                if (standardY < roofY)
                {
                    // Is transparent roof above standard height?
                    int checkY = roofY;
                    while (checkY > standardY)
                    {
                        topBlockMd = dataCache.getBlockMD(chunkMd, x, checkY, z);
                        if (topBlockMd.isTransparentRoof())
                        {
                            y = Math.max(standardY, checkY);
                            break;
                        }
                        else
                        {
                            checkY--;
                        }
                    }
                }

                // Get top non-roof block
                topBlockMd = chunkMd.getTopBlockMD(x, standardY, z);

                if (topBlockMd == null)
                {
                    paintBadBlock(x, standardY, z, g2D);
                    continue blockLoop;
                }

                // Start using BlockColors stack
                buildStrata(strata, neighbors, roofY, chunkMd, x, y, z);

                chunkOk = paintStrata(strata, g2D, chunkMd, topBlockMd, vSlice, neighbors, x, y, z, cavePrePass) || chunkOk;
            }
        }

        strata.reset();
        return chunkOk;
    }

    /**
     * Create a BlockStack.
     */
    protected void buildStrata(Strata strata, final ChunkMD.Set neighbors, int roofY, ChunkMD chunkMd, int x, int y, int z)
    {
        BlockMD blockMD;

        // If under glass, add to color stack
        if (roofY > y)
        {
            while (roofY > y)
            {
                blockMD = dataCache.getBlockMD(chunkMd, x, roofY, z);
                if (!blockMD.isAir())
                {
                    if (blockMD.isTransparentRoof())
                    {
                        strata.push(neighbors, chunkMd, blockMD, x, roofY, z);
                        if (!mapTransparency)
                        {
                            break;
                        }
                    }
                }
                roofY--;
            }
        }

        if (mapTransparency || strata.isEmpty())
        {
            while (y > 0)
            {
                blockMD = dataCache.getBlockMD(chunkMd, x, y, z);

                if (!blockMD.isAir())
                {
                    strata.push(neighbors, chunkMd, blockMD, x, y, z);

                    if (blockMD.getAlpha() == 1f || !mapTransparency)
                    {
                        break;
                    }
                }
                y--;
            }
        }
    }

    /**
     * Paint the image with the color derived from a BlockStack
     */
    protected boolean paintStrata(final Strata strata, final Graphics2D g2D, final ChunkMD chunkMd, final BlockMD topBlockMd, final Integer vSlice, final ChunkMD.Set neighbors, final int x, final int y, final int z, final boolean cavePrePass)
    {
        if (strata.isEmpty())
        {
            paintBadBlock(x, y, z, g2D);
            return false;
        }

        try
        {
            Stratum stratum;
            while (!strata.isEmpty())
            {
                stratum = strata.pop(this, true);

                // Simple surface render
                if (strata.renderDayColor == null || strata.renderNightColor == null)
                {
                    strata.renderDayColor = stratum.dayColor;
                    if (!cavePrePass)
                    {
                        strata.renderNightColor = stratum.nightColor;
                    }
                    continue;
                }
                else
                {
                    strata.renderDayColor = RGB.blendWith(strata.renderDayColor, stratum.dayColor, stratum.blockMD.getAlpha());
                    if (!cavePrePass)
                    {
                        strata.renderNightColor = RGB.blendWith(strata.renderNightColor, stratum.nightColor, stratum.blockMD.getAlpha());
                    }
                }

                strata.release(stratum);

            } // end color stack

            // Shouldn't happen
            if (strata.renderDayColor == null)
            {
                paintBadBlock(x, y, z, g2D);
                return false;
            }

            // Now add bevel for slope
            if (!topBlockMd.hasFlag(BlockMD.Flag.NoShadow))
            {
                float slope = getSlope(chunkMd, topBlockMd, neighbors, x, null, z);
                if (slope != 1f)
                {
                    strata.renderDayColor = RGB.bevelSlope(strata.renderDayColor, slope);
                    if (!cavePrePass)
                    {
                        strata.renderNightColor = RGB.bevelSlope(strata.renderNightColor, slope);
                    }
                }
            }

            // And draw to the actual chunkimage
            g2D.setComposite(OPAQUE);
            g2D.setPaint(RGB.paintOf(strata.renderDayColor));
            g2D.fillRect(x, z, 1, 1);

            if (!cavePrePass)
            {
                g2D.setPaint(RGB.paintOf(strata.renderNightColor));
                g2D.fillRect(x + 16, z, 1, 1);
            }
        }
        catch (RuntimeException e)
        {
            throw e;
        }

        return true;
    }

    /**
     * Not used.
     */
    protected int getSliceBlockHeight(final ChunkMD chunkMd, final int x, final Integer vSlice, final int z, final int sliceMinY, final int sliceMaxY, boolean ignoreWater)
    {
        throw new UnsupportedOperationException("Should not be called.");
    }
}
