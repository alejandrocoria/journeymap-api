/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.cartography.render;

import com.google.common.base.Optional;
import com.google.common.cache.RemovalNotification;
import net.minecraft.world.ChunkCoordIntPair;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.cartography.IChunkRenderer;
import net.techbrew.journeymap.cartography.RGB;
import net.techbrew.journeymap.cartography.Strata;
import net.techbrew.journeymap.cartography.Stratum;
import net.techbrew.journeymap.data.DataCache;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.log.StatTimer;
import net.techbrew.journeymap.model.BlockMD;
import net.techbrew.journeymap.model.ChunkMD;
import org.apache.logging.log4j.Level;

import java.awt.*;

public class SurfaceRenderer extends BaseRenderer implements IChunkRenderer
{
    private final Object chunkLock = new Object();
    private final HeightsCache chunkSurfaceHeights;
    private final SlopesCache chunkSurfaceSlopes;
    protected StatTimer renderSurfaceTimer = StatTimer.get("SurfaceRenderer.renderSurface");
    protected StatTimer renderSurfacePrepassTimer = StatTimer.get("SurfaceRenderer.renderSurface.CavePrepass");
    protected Strata strata = new Strata("Surface", 40, 8, false);
    protected float maxDepth = 8;

    public SurfaceRenderer()
    {
        // TODO: Write the caches to disk and we'll have some useful data available.
        cachePrefix = "Surface";
        columnPropertiesCache = new BlockColumnPropertiesCache(cachePrefix + "ColumnProps");
        chunkSurfaceHeights = new HeightsCache(cachePrefix + "Heights");
        chunkSurfaceSlopes = new SlopesCache(cachePrefix + "Slopes");
        DataCache.instance().addChunkMDListener(this);
    }

    @Override
    protected void updateOptions()
    {
        super.updateOptions();
        this.ambientColor = RGB.floats(tweakSurfaceAmbientColor);
    }

    /**
     * Render blocks in the chunk for the standard world.
     */
    @Override
    public boolean render(final Graphics2D g2D, final ChunkMD chunkMd, final Integer vSlice)
    {
        return render(g2D, chunkMd, null, false);
    }

    /**
     * Render blocks in the chunk for the standard world.
     */
    public synchronized boolean render(final Graphics2D g2D, final ChunkMD chunkMd, final Integer vSlice, final boolean cavePrePass)
    {
        StatTimer timer = cavePrePass ? renderSurfacePrepassTimer : renderSurfaceTimer;

        try
        {
            timer.start();

            updateOptions();

            // Initialize ChunkSub slopes if needed
            if (chunkSurfaceSlopes.getIfPresent(chunkMd.getCoord()) == null)
            {
                populateSlopes(chunkMd, null, chunkSurfaceHeights, chunkSurfaceSlopes);
            }

            // Render the chunk image
            return renderSurface(g2D, chunkMd, vSlice, cavePrePass);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            return false;
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
    protected boolean renderSurface(final Graphics2D g2D, final ChunkMD chunkMd, final Integer vSlice, final boolean cavePrePass)
    {
        boolean chunkOk = false;

        try
        {
            g2D.setComposite(ALPHA_OPAQUE);
            int sliceMaxY = 0;

            if (cavePrePass)
            {
                int[] sliceBounds = getVSliceBounds(chunkMd, vSlice);
                sliceMaxY = sliceBounds[1];
            }

            for (int x = 0; x < 16; x++)
            {
                blockLoop:
                for (int z = 0; z < 16; z++)
                {
                    strata.reset();
                    BlockMD topBlockMd = null;

                    int standardY = Math.max(0, getSurfaceBlockHeight(chunkMd, x, z, chunkSurfaceHeights));

                    // Should be painted only by cave renderer
                    if (cavePrePass && (standardY > sliceMaxY && (standardY - sliceMaxY) > maxDepth))
                    {
                        chunkOk = true;
                        paintBlackBlock(x, z, g2D);
                        continue;
                    }

                    int roofY = 0;
                    int y = standardY;

                    roofY = Math.max(0, chunkMd.getAbsoluteHeightValue(x, z));
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

                    if(roofY==0 || standardY==0)
                    {
                        paintVoidBlock(x, z, g2D);
                        chunkOk = true;
                        continue blockLoop;
                    }

                    // Bathymetry - need to use water height instead of standardY, so we get the color blend
                    if(mapBathymetry)
                    {
                        standardY = getColumnProperty(PROP_WATER_HEIGHT, standardY, chunkMd, x, z);
                    }

                    topBlockMd = chunkMd.getTopBlockMD(x, standardY, z);

                    if (topBlockMd == null)
                    {
                        paintBadBlock(x, standardY, z, g2D);
                        continue blockLoop;
                    }

                    // Check a block up
                    if(mapPlants || mapCrops)
                    {
                        BlockMD temp = chunkMd.getTopBlockMD(x, standardY+1, z);
                        if((mapPlants && temp.hasFlag(BlockMD.Flag.Plant)) ||
                           (mapCrops && temp.hasFlag(BlockMD.Flag.Crop)))
                        {
                            standardY++;
                        }
                    }

                    // Start using BlockColors stack
                    buildStrata(strata, roofY, chunkMd, x, standardY, z);

                    chunkOk = paintStrata(strata, g2D, chunkMd, topBlockMd, vSlice, x, y, z, cavePrePass) || chunkOk;
                }
            }
        }
        catch (Throwable t)
        {
            JourneyMap.getLogger().log(Level.WARN, LogFormatter.toString(t));
        }
        finally
        {
            strata.reset();
            // return chunkOk;
            return true;
        }
    }

    /**
     * Create a BlockStack.
     */
    protected void buildStrata(Strata strata, int roofY, ChunkMD chunkMd, int x, int y, int z)
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
                        strata.push(chunkMd, blockMD, x, roofY, z);
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
            while (y >= 0)
            {
                blockMD = dataCache.getBlockMD(chunkMd, x, y, z);

                if (!blockMD.isAir())
                {
                    strata.push(chunkMd, blockMD, x, y, z);

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
    protected boolean paintStrata(final Strata strata, final Graphics2D g2D, final ChunkMD chunkMd, final BlockMD topBlockMd, final Integer vSlice, final int x, final int y, final int z, final boolean cavePrePass)
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
                stratum = strata.nextUp(this, true);

//                // Bathymetry check
//                Integer waterHeight = null;
//                if(mapBathymetry)
//                {
//                    waterHeight = getColumnProperty(PROP_WATER_HEIGHT, null, chunkMd, x, z);
//                }
//
//                // Override stratum color for bathymetry.  I don't like doing this here.
//                if(mapBathymetry && waterHeight!=null)
//                {
//                    strata.setRenderDayColor(stratum.getDayColor());
//                    if (!cavePrePass)
//                    {
//                        strata.setRenderNightColor(stratum.getNightColor());
//                    }
//
//                    strata.determineWaterColor(chunkMd, x, waterHeight, z);
//                    stratum.setWater(true);
//                    setStratumColors(stratum, 0, strata.getWaterColor(), true, false, false);
//
//                    strata.setRenderDayColor(RGB.blendWith(strata.getRenderDayColor(), stratum.getDayColor(), .9f)); // TODO: Use light attenuation
//                    if (!cavePrePass)
//                    {
//                        strata.setRenderNightColor(RGB.blendWith(strata.getRenderNightColor(), stratum.getNightColor(), .9f)); // TODO: Use light attenuation
//                    }
//                }
//                else
                // Simple surface render
                if (strata.getRenderDayColor() == null || strata.getRenderNightColor() == null)
                {
                    strata.setRenderDayColor(stratum.getDayColor());
                    if (!cavePrePass)
                    {
                        strata.setRenderNightColor(stratum.getNightColor());
                    }
                }
                else
                {
                    strata.setRenderDayColor(RGB.blendWith(strata.getRenderDayColor(), stratum.getDayColor(), stratum.getBlockMD().getAlpha()));
                    if (!cavePrePass)
                    {
                        strata.setRenderNightColor(RGB.blendWith(strata.getRenderNightColor(), stratum.getNightColor(), stratum.getBlockMD().getAlpha()));
                    }
                }

                strata.release(stratum);

            } // end color stack

            // Shouldn't happen
            if (strata.getRenderDayColor() == null)
            {
                paintBadBlock(x, y, z, g2D);
                return false;
            }

            if (!cavePrePass)
            {
                // Shouldn't happen
                if (strata.getRenderNightColor() == null)
                {
                    paintBadBlock(x + 16, y, z, g2D);
                    return false;
                }
            }

            // Now add bevel for slope
            if ((topBlockMd.isWater() && mapBathymetry) || !topBlockMd.hasFlag(BlockMD.Flag.NoShadow))
            {
                float slope = getSlope(chunkMd, topBlockMd, x, null, z, chunkSurfaceHeights, chunkSurfaceSlopes);
                if (slope != 1f)
                {
                    strata.setRenderDayColor(RGB.bevelSlope(strata.getRenderDayColor(), slope));
                    if (!cavePrePass)
                    {
                        strata.setRenderNightColor(RGB.bevelSlope(strata.getRenderNightColor(), slope));
                    }
                }
            }

            // And draw to the actual chunkimage
            g2D.setComposite(ALPHA_OPAQUE);
            g2D.setPaint(RGB.paintOf(strata.getRenderDayColor()));
            g2D.fillRect(x, z, 1, 1);

            if (!cavePrePass)
            {
                g2D.setPaint(RGB.paintOf(strata.getRenderNightColor()));
                g2D.fillRect(x + 16, z, 1, 1);
            }
        }
        catch (RuntimeException e)
        {
            throw e;
        }

        return true;
    }

    @Override
    public void onRemoval(RemovalNotification<ChunkCoordIntPair, Optional<ChunkMD>> notification)
    {
        synchronized (chunkLock)
        {
            ChunkCoordIntPair coord = notification.getKey();
            chunkSurfaceHeights.invalidate(coord);
            chunkSurfaceSlopes.invalidate(coord);

            //JourneyMap.getLogger().info("Invalidated data related to chunk " + coord);
        }
    }
}
