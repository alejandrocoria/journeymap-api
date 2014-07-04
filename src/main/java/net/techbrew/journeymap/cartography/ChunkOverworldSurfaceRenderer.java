package net.techbrew.journeymap.cartography;

import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.StatTimer;
import net.techbrew.journeymap.model.BlockMD;
import net.techbrew.journeymap.model.BlockUtils;
import net.techbrew.journeymap.model.ChunkMD;
import net.techbrew.journeymap.properties.CoreProperties;

import java.awt.*;

public class ChunkOverworldSurfaceRenderer extends BaseRenderer implements IChunkRenderer
{
    protected CoreProperties coreProperties = JourneyMap.getInstance().coreProperties;
    protected boolean mapBathymetry;
    protected boolean mapSmoothSlopes;
    protected boolean mapTransparency;

    protected StatTimer renderSurfaceTimer = StatTimer.get("ChunkOverworldSurfaceRenderer.renderSurface");
    protected StatTimer renderSurfacePrepassTimer = StatTimer.get("ChunkOverworldSurfaceRenderer.renderSurface-prepass");
    protected StatTimer buildStrataTimer = StatTimer.get("ChunkOverworldSurfaceRenderer.buildStrata");
    protected StatTimer paintStrataTimer = StatTimer.get("ChunkOverworldSurfaceRenderer.paintStrata");


    protected Strata strata = new Strata("OverworldSurface", 40,8);

    /**
     * Render blocks in the chunk for the standard world.
     */
    @Override
    public boolean render(final Graphics2D g2D, final ChunkMD chunkMd, final boolean underground,
                          final Integer vSlice, final ChunkMD.Set neighbors)
    {
        mapBathymetry = coreProperties.mapBathymetry.get();
        mapSmoothSlopes = coreProperties.mapSmoothSlopes.get();
        mapTransparency = coreProperties.mapTransparency.get();

        try
        {
            if(underground || vSlice!=null)
            {
                JourneyMap.getLogger().warning(String.format("ChunkOverworldSurfaceRenderer is for surface.  Y u do dis? (%s,%s)", underground, vSlice));
            }

            // Initialize ChunkSub slopes if needed
            if (chunkMd.surfaceSlopes == null)
            {
                initSurfaceSlopes(chunkMd, neighbors);
            }

            // Render the chunk image
            return renderSurface(g2D, chunkMd, vSlice, neighbors, false);
        }
        finally
        {
            strata.reset();
        }

    }

    /**
     * Initialize surface slopes in chunk if needed.
     *
     * @param chunkMd
     * @param neighbors
     */
    protected void initSurfaceSlopes(final ChunkMD chunkMd, final ChunkMD.Set neighbors)
    {
        float slope, h, hN, hW, hNW;
        chunkMd.surfaceSlopes = new float[16][16];
        boolean ignoreWater = mapBathymetry;
        for (int z = 0; z < 16; z++)
        {
            for (int x = 0; x < 16; x++)
            {
                h = chunkMd.getSlopeHeightValue(x, z, ignoreWater);
                hN = (z == 0) ? getBlockHeight(x, z, 0, -1, chunkMd, neighbors, h, ignoreWater) : chunkMd.getSlopeHeightValue(x, z - 1, ignoreWater);
                hW = (x == 0) ? getBlockHeight(x, z, -1, 0, chunkMd, neighbors, h, ignoreWater) : chunkMd.getSlopeHeightValue(x - 1, z, ignoreWater);
                hNW = getBlockHeight(x, z, -1, -1, chunkMd, neighbors, h, ignoreWater);
                slope = ((h / hN) + (h / hW) + (h / hNW)) / 3f;
                chunkMd.surfaceSlopes[x][z] = slope;
            }
        }
    }

    /**
     * Render blocks in the chunk for the surface.
     */
    protected boolean renderSurface(final Graphics2D g2D, final ChunkMD chunkMd, final Integer vSlice, final ChunkMD.Set neighbors, final boolean cavePrePass)
    {
        StatTimer timer = cavePrePass ? renderSurfacePrepassTimer : renderSurfaceTimer;

        g2D.setComposite(BlockUtils.OPAQUE);

        boolean chunkOk = false;
        for (int x = 0; x < 16; x++)
        {
            blockLoop:
            for (int z = 0; z < 16; z++)
            {
                timer.start();

                strata.reset();
                BlockMD topBlockMd = null;

                boolean isUnderRoof = false;
                int standardY = Math.max(1, chunkMd.getHeightValue(x, z));
                int roofY = 0;
                int y = standardY;

                roofY = Math.max(1, chunkMd.getAbsoluteHeightValue(x, z));
                if (standardY < roofY)
                {
                    // Is transparent roof above standard height?
                    int checkY = roofY;
                    while (checkY > standardY)
                    {
                        topBlockMd = BlockMD.getBlockMD(chunkMd, x, checkY, z);
                        if (topBlockMd != null && topBlockMd.isTransparentRoof())
                        {
                            isUnderRoof = true;
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
                topBlockMd = getTopBlockMD(chunkMd, x, y, z);

                if (topBlockMd == null)
                {
                    paintBadBlock(x, y, z, g2D);
                    timer.stop();
                    continue blockLoop;
                }

                // Start using BlockColors stack
                buildStrata(strata, neighbors, roofY, chunkMd, x, y, z);

                chunkOk = paintStrata(strata, g2D, chunkMd, topBlockMd, vSlice, neighbors, x, y, z, cavePrePass) || chunkOk;

                timer.stop();
            }
        }

        strata.reset();
        return chunkOk;
    }

    /**
     * Create a BlockStack.
     * TODO: Is this generic enough to use for caves?
     */
    protected void buildStrata(Strata strata, final ChunkMD.Set neighbors, int roofY, ChunkMD chunkMd, int x, int y, int z)
    {
        buildStrataTimer.start();

        BlockMD blockMD;

        try
        {
            // If under glass, add to color stack
            if (roofY > y)
            {
                while (roofY > y)
                {
                    blockMD = BlockMD.getBlockMD(chunkMd, x, roofY, z);
                    if (blockMD != null && !blockMD.isAir())
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
                    blockMD = BlockMD.getBlockMD(chunkMd, x, y, z);

                    if (blockMD != null && !blockMD.isAir())
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
        finally
        {
            buildStrataTimer.stop();
        }
    }

    /**
     * Paint the image with the color derived from a BlockStack
     * TODO: Is this generic enough to use for caves?  Probably not the slope part, but everything else should be.
     */
    protected boolean paintStrata(final Strata strata, final Graphics2D g2D, final ChunkMD chunkMd, final BlockMD topBlockMd, final Integer vSlice, final ChunkMD.Set neighbors, final int x, final int y, final int z, final boolean cavePrePass)
    {
        if (strata.isEmpty())
        {
            paintBadBlock(x, y, z, g2D);
            return false;
        }

        paintStrataTimer.start();

        try
        {
            Stratum stratum;
            while (!strata.isEmpty())
            {
                stratum = strata.pop(true);

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
                        strata.renderNightColor = RGB.blendWith(strata.renderNightColor, stratum.dayColor, stratum.blockMD.getAlpha());
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
            if (!topBlockMd.hasFlag(BlockUtils.Flag.NoShadow))
            {
                int slopeY = (mapBathymetry && strata.hasWater()) ? strata.bottomWaterY : strata.topY;
                float slope = getSurfaceSlope(chunkMd, topBlockMd, neighbors, x, slopeY, z);
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
            g2D.setComposite(BlockUtils.OPAQUE);
            g2D.setPaint(RGB.paintOf(strata.renderDayColor));
            g2D.fillRect(x, z, 1, 1);

            if (!cavePrePass)
            {
                g2D.setPaint(RGB.paintOf(strata.renderNightColor));
                g2D.fillRect(x + 16, z, 1, 1);
            }

            paintStrataTimer.stop();
        }
        catch (RuntimeException e)
        {
            paintStrataTimer.cancel();
            throw e;
        }

        return true;
    }

    protected float getSurfaceSlope(final ChunkMD chunkMd, final BlockMD blockMD, final ChunkMD.Set neighbors, int x, int ignored, int z)
    {
        float slope, bevel, sN, sNW, sNE, sW, sS, sSW, sE, sSE, sAvg;
        sN = sNW = sNE = sW = sS = sSW = sE = sSE = 1f;

        slope = chunkMd.surfaceSlopes[x][z];

        if (!blockMD.isFoliage())
        {
            // Trees look more distinct if just beveled on upper left corners
            sN = getBlockSlope(x, z, 0, -1, chunkMd, neighbors, slope, false);
            sNW = getBlockSlope(x, z, -1, -1, chunkMd, neighbors, slope, false);
            sW = getBlockSlope(x, z, -1, 0, chunkMd, neighbors, slope, false);
            sAvg = (sN + sNW + sW) / 3f;
        }
        else
        {
            // Everything else gets beveled based on average slope across n,s,e,w

            sN =  getBlockSlope(x, z,  0, -1, chunkMd, neighbors, slope, false);
            sS =  getBlockSlope(x, z,  0, 1, chunkMd, neighbors, slope, false);
            sW =  getBlockSlope(x, z, -1, 0, chunkMd, neighbors, slope, false);
            sE =  getBlockSlope(x, z,  1, 0, chunkMd, neighbors, slope, false);

            /*

            *********** Verify this is worth it
                ******** water is broken
                *********** use DataCache instead of neighbors?  maybe not
                *********** however, the slice slopes on chunkMD have to be reset or stored per slice.  maybe writable raster is a good way to do it.
                *********** and object pooling chunkMDs with them would be a good call, perhaps?
                * */

            if(mapSmoothSlopes)
            {
                sNW = getBlockSlope(x, z, -1, -1, chunkMd, neighbors, slope, false);
                sNE = getBlockSlope(x, z,  1, -1, chunkMd, neighbors, slope, false);
                sSW = getBlockSlope(x, z, -1, 1, chunkMd, neighbors, slope, false);
                sSE = getBlockSlope(x, z,  1, 1, chunkMd, neighbors, slope, false);

                float upperLeft = (sW + sN + sNW + sNE) / 4f;
                float lowerRight = (sE + sS + sSW + sSE) / 4f;

                //sAvg = (sN + sNW + sNE + sS + +sSW + sSE + sW + sE) / 8f;
                sAvg = (upperLeft + lowerRight) / 2f;
            }
            else
            {
                sAvg = (sN + sS + sW + sE) / 4f;
            }
        }

        bevel = 1f;
        if (slope < 1)
        {
            if (slope <= sAvg)
            {
                slope = slope * .6f;
            }
            else if (slope > sAvg)
            {
                if (!blockMD.isFoliage())
                {
                    slope = (slope + sAvg) / 2f;
                }
            }
            bevel = Math.max(slope * .8f, .1f);
        }
        else if (slope > 1)
        {
            if (sAvg > 1)
            {
                if (slope >= sAvg)
                {
                    if (!blockMD.isFoliage())
                    {
                        slope = slope * 1.2f;
                    }
                }
            }
            bevel = Math.min(slope * 1.2f, 1.4f);
        }

        return bevel;
    }


}
