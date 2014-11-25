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
import net.techbrew.journeymap.data.DataCache;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.log.StatTimer;
import net.techbrew.journeymap.model.BlockCoordIntPair;
import net.techbrew.journeymap.model.BlockMD;
import net.techbrew.journeymap.model.ChunkMD;
import org.apache.logging.log4j.Level;

import java.awt.*;
import java.util.ArrayList;

public class TopoRenderer extends BaseRenderer implements IChunkRenderer
{
    protected final Object chunkLock = new Object();
    private final HeightsCache chunkSurfaceHeights;
    private final SlopesCache chunkSurfaceSlopes;
    protected StatTimer renderTopoTimer = StatTimer.get("TopoRenderer.renderSurface");
    ArrayList<Color> water = new ArrayList<Color>(32);
    ArrayList<Color> land = new ArrayList<Color>(32);
    private int lightGray = Color.lightGray.getRGB();
    private int darkGray = Color.darkGray.getRGB();

    public TopoRenderer()
    {
        water.add(new Color(31, 40, 79));
        water.add(new Color(38, 60, 106));
        water.add(new Color(46, 80, 133));
        water.add(new Color(53, 99, 160));
        water.add(new Color(60, 119, 188));
        water.add(new Color(72, 151, 211));
        water.add(new Color(90, 185, 233));
        water.add(new Color(95, 198, 242));
        water.add(new Color(114, 202, 238));
        water.add(new Color(141, 210, 239));


        land.add(new Color(113, 171, 216));
        land.add(new Color(121, 178, 222));
        land.add(new Color(132, 185, 227));
        land.add(new Color(141, 193, 234));
        land.add(new Color(150, 201, 240));
        land.add(new Color(161, 210, 247));
        land.add(new Color(172, 219, 251));
        land.add(new Color(185, 227, 255));
        land.add(new Color(198, 236, 255));
        land.add(new Color(216, 242, 254));
        land.add(new Color(172, 208, 165));
        land.add(new Color(148, 191, 139));
        land.add(new Color(168, 198, 143));
        land.add(new Color(189, 204, 150));
        land.add(new Color(209, 215, 171));
        land.add(new Color(225, 228, 181));
        land.add(new Color(239, 235, 192));
        land.add(new Color(232, 225, 182));
        land.add(new Color(222, 214, 163));
        land.add(new Color(211, 202, 157));
        land.add(new Color(202, 185, 130));
        land.add(new Color(195, 167, 107));
        land.add(new Color(185, 152, 90));
        land.add(new Color(170, 135, 83));
        land.add(new Color(172, 154, 124));
        land.add(new Color(186, 174, 154));
        land.add(new Color(202, 195, 184));
        land.add(new Color(224, 222, 216));
        land.add(new Color(245, 244, 242));
        land.add(new Color(255, 255, 255));

        chunkSurfaceHeights = new HeightsCache("TopoHeights");
        chunkSurfaceSlopes = new SlopesCache("TopoSlopes");
        DataCache.instance().addChunkMDListener(this);
    }

    /**
     * Render blocks in the chunk for the standard world.
     */
    @Override
    public boolean render(final Graphics2D g2D, final ChunkMD chunkMd, final Integer vSlice)
    {
        StatTimer timer = renderTopoTimer;

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
            return renderSurface(g2D, chunkMd, vSlice, false);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            return false;
        }
        finally
        {
            //strata.reset();
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

            for (int x = 0; x < 16; x++)
            {
                blockLoop:
                for (int z = 0; z < 16; z++)
                {
                    BlockMD topBlockMd = null;

                    int standardY = Math.max(0, getSurfaceBlockHeight(chunkMd, x, z, chunkSurfaceHeights));

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

                    if (roofY == 0 || standardY == 0)
                    {
                        paintVoidBlock(x, z, g2D);
                        chunkOk = true;
                        continue blockLoop;
                    }

                    // Bathymetry - need to use water height instead of standardY, so we get the color blend
                    if (mapBathymetry)
                    {
                        standardY = getColumnProperty(PROP_WATER_HEIGHT, standardY, chunkMd, x, z);
                    }

                    topBlockMd = chunkMd.getTopBlockMD(x, standardY, z);

                    if (topBlockMd == null)
                    {
                        paintBadBlock(x, standardY, z, g2D);
                        continue blockLoop;
                    }

                    chunkOk = paintStrata(null, g2D, chunkMd, topBlockMd, vSlice, x, standardY, z, cavePrePass) || chunkOk;
                }
            }
        }
        catch (Throwable t)
        {
            JourneyMap.getLogger().log(Level.WARN, LogFormatter.toString(t));
        }
        finally
        {

        }
        return true;  // todo: return chunkok
    }

    public Integer getSurfaceBlockHeight(final ChunkMD chunkMd, int x, int z, final HeightsCache chunkHeights)
    {
        Integer[][] heights = chunkHeights.getUnchecked(chunkMd.getCoord());
        if (heights == null)
        {
            // Not in cache anymore
            return null;
        }
        Integer y;

        y = heights[x][z];

        if (y != null)
        {
            // Already set
            return y;
        }

        // Find the height.
        // TODO: This doesn't catch glass or all that anymore, does it?  Use precip height?
        y = Math.max(0, chunkMd.getChunk().getHeightValue(x, z));

        try
        {
            BlockMD blockMD = dataCache.getBlockMD(chunkMd, x, y, z);
            boolean propUnsetWaterHeight = true;

            while (y > 0)
            {
                if (blockMD.isWater())
                {
                    if (!mapBathymetry)
                    {
                        break;
                    }
                    else if (propUnsetWaterHeight)
                    {
                        setColumnProperty(PROP_WATER_HEIGHT, y, chunkMd, x, z);
                        propUnsetWaterHeight = false;
                    }
                }
                else if (!blockMD.isAir() && !blockMD.hasFlag(BlockMD.Flag.NoTopo))
                {
                    break;
                }
                y--;
                blockMD = dataCache.getBlockMD(chunkMd, x, y, z);
            }
        }
        catch (Exception e)
        {
            JourneyMap.getLogger().warn("Couldn't get safe surface block height at " + x + "," + z + ": " + e);
        }

        //why is height 4 set on a chunk to the left?
        y = Math.max(0, y);

        heights[x][z] = y;

        return y;
    }

    /**
     * Initialize surface slopes in chunk if needed.
     */
    protected Float[][] populateSlopes(final ChunkMD chunkMd, Integer vSlice,
                                       final HeightsCache chunkHeights,
                                       final SlopesCache chunkSlopes)
    {
        BlockCoordIntPair offsetN = new BlockCoordIntPair(0, -1);
        BlockCoordIntPair offsetW = new BlockCoordIntPair(-1, 0);
        BlockCoordIntPair offsetS = new BlockCoordIntPair(0, 1);
        BlockCoordIntPair offsetE = new BlockCoordIntPair(1, 0);

        Float[][] slopes = chunkSlopes.getUnchecked(chunkMd.getCoord());
        int h;
        float slope, hN, hW, hE, hS;
        for (int z = 0; z < 16; z++)
        {
            for (int x = 0; x < 16; x++)
            {
                h = getSurfaceBlockHeight(chunkMd, x, z, chunkHeights);
                hN = getSurfaceBlockHeight(chunkMd, x, z, offsetN, h, chunkHeights);
                hW = getSurfaceBlockHeight(chunkMd, x, z, offsetW, h, chunkHeights);
                hS = getSurfaceBlockHeight(chunkMd, x, z, offsetS, h, chunkHeights);
                hE = getSurfaceBlockHeight(chunkMd, x, z, offsetE, h, chunkHeights);

                h = h >> 3;
                hN = (int) hN >> 3;
                hW = (int) hW >> 3;
                hE = (int) hE >> 3;
                hS = (int) hS >> 3;

                if (h != hN && (hN == hW && hN == hE && hN == hS))
                {
                    slope = 1; // lets ignore one-block elevation changes
                }
                else
                {
                    slope = ((h / hN) + (h / hW) + (h / hE) + (h / hS)) / 4f;
                }
                slopes[x][z] = slope;
            }
        }
        return slopes;
    }

    protected boolean paintStrata(final Strata strata, final Graphics2D g2D, final ChunkMD chunkMd, final BlockMD topBlockMd, final Integer vSlice, final int x, final int y, final int z, final boolean cavePrePass)
    {

        int color = 0;
        float slope = getSlope(chunkMd, topBlockMd, x, null, z, chunkSurfaceHeights, chunkSurfaceSlopes);

        if (slope < 1)
        {
            color = getBaseBlockColor(topBlockMd, x, y, z);
            color = RGB.darken(color, .9f);
        }
        else if (slope > 1)
        {
            color = darkGray;
        }
        else
        {
            color = getBaseBlockColor(topBlockMd, x, y, z);
        }

        paintBlock(x, z, color, g2D, true);

        return true;
    }

    /**
     * Get the color for a block based on topological height
     */
    protected int getBaseBlockColor(final BlockMD blockMD, int x, int y, int z)
    {
        float orthoY = y >> 3;
        if (blockMD.isWater())
        {
            float saturation = orthoY == 0 ? 0 : (orthoY / 32f);
            return RGB.toInteger(saturation, saturation, 1);
        }
        else
        {
            int index = 0;
            if (orthoY > 0 && y <= 63)
            {
                // At or below sea level: Use values 1-9 in land
                index = (int) Math.floor(orthoY / (15f / 9)); // 15 orthos in range across 10 values
            }
            else
            {
                // Above sea level: Use last 20 values in land
                index = 2 + (int) orthoY;
            }

            //index = (int) Math.floor((orthoY/20)*(land.size()-1));
            index = Math.min(index, land.size() - 1);

            return land.get(index).getRGB();
        }
    }

    @Override
    public void onRemoval(RemovalNotification<ChunkCoordIntPair, Optional<ChunkMD>> notification)
    {
        synchronized (chunkLock)
        {
            ChunkCoordIntPair coord = notification.getKey();
            chunkSurfaceHeights.invalidate(coord);
            chunkSurfaceSlopes.invalidate(coord);
        }
    }
}
