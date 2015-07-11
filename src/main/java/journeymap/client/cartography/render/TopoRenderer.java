/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.cartography.render;

import com.google.common.base.Optional;
import com.google.common.cache.RemovalNotification;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import journeymap.client.JourneyMap;
import journeymap.client.cartography.IChunkRenderer;
import journeymap.client.cartography.RGB;
import journeymap.client.cartography.Strata;
import journeymap.client.data.DataCache;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.log.LogFormatter;
import journeymap.client.log.StatTimer;
import journeymap.client.model.BlockCoordIntPair;
import journeymap.client.model.BlockMD;
import journeymap.client.model.ChunkMD;
import org.apache.logging.log4j.Level;

import java.awt.*;
import java.util.ArrayList;

public class TopoRenderer extends BaseRenderer implements IChunkRenderer
{
    protected final Object chunkLock = new Object();
    final Integer[] waterPalette;
    final Integer[] landPalette;
    private final HeightsCache chunkSurfaceHeights;
    private final SlopesCache chunkSurfaceSlopes;
    private final int waterPaletteRange;
    private final int landPaletteRange;
    protected StatTimer renderTopoTimer = StatTimer.get("TopoRenderer.renderSurface");
    private int lightGray = Color.lightGray.getRGB();
    private int darkGray = Color.darkGray.getRGB();
    private int orthoRange;
    private int orthoStep;

    public TopoRenderer()
    {
        ArrayList<Integer> water = new ArrayList<Integer>(32);
        water.add(new Color(31, 40, 79).getRGB());
        water.add(new Color(31, 40, 79).getRGB());
        water.add(new Color(31, 40, 79).getRGB());
        water.add(new Color(31, 40, 79).getRGB());
        water.add(new Color(31, 40, 79).getRGB());
        water.add(new Color(38, 60, 106).getRGB());
        water.add(new Color(46, 80, 133).getRGB());
        water.add(new Color(53, 99, 160).getRGB());
        water.add(new Color(60, 119, 188).getRGB());
        water.add(new Color(72, 151, 211).getRGB());
        water.add(new Color(90, 185, 233).getRGB());
        water.add(new Color(95, 198, 242).getRGB());
        water.add(new Color(114, 202, 238).getRGB());
        water.add(new Color(141, 210, 239).getRGB());
        waterPaletteRange = water.size() - 1;
        waterPalette = water.toArray(new Integer[0]);

        ArrayList<Integer> land = new ArrayList<Integer>(32);
        land.add(new Color(10, 70, 90).getRGB());
        land.add(new Color(20, 80, 90).getRGB());
        land.add(new Color(30, 90, 100).getRGB());
        land.add(new Color(40, 100, 100).getRGB());
        land.add(new Color(50, 110, 100).getRGB());
        land.add(new Color(60, 120, 100).getRGB());
        land.add(new Color(70, 130, 100).getRGB());
        land.add(new Color(80, 140, 100).getRGB());
        land.add(new Color(90, 150, 100).getRGB());
        land.add(new Color(100, 167, 107).getRGB());
        land.add(new Color(172, 208, 165).getRGB());
        land.add(new Color(148, 191, 139).getRGB());
        land.add(new Color(168, 198, 143).getRGB());
        land.add(new Color(189, 204, 150).getRGB());
        land.add(new Color(209, 215, 171).getRGB());
        land.add(new Color(225, 228, 181).getRGB());
        land.add(new Color(239, 235, 192).getRGB());
        land.add(new Color(232, 225, 182).getRGB());
        land.add(new Color(222, 214, 163).getRGB());
        land.add(new Color(211, 202, 157).getRGB());
        land.add(new Color(202, 185, 130).getRGB());
        land.add(new Color(195, 167, 107).getRGB());
        land.add(new Color(185, 152, 90).getRGB());
        land.add(new Color(170, 135, 83).getRGB());
        land.add(new Color(172, 154, 124).getRGB());
        land.add(new Color(186, 174, 154).getRGB());
        land.add(new Color(202, 195, 184).getRGB());
        land.add(new Color(224, 222, 216).getRGB());
        land.add(new Color(245, 244, 242).getRGB());
        land.add(new Color(255, 255, 255).getRGB());
        landPaletteRange = land.size() - 1;
        landPalette = land.toArray(new Integer[0]);

        // TODO: Write the caches to disk and we'll have some useful data available.
        this.cachePrefix = "Topo";
        columnPropertiesCache = new BlockColumnPropertiesCache(cachePrefix + "ColumnProps");
        chunkSurfaceHeights = new HeightsCache(cachePrefix + "Heights");
        chunkSurfaceSlopes = new SlopesCache(cachePrefix + "Slopes");
        DataCache.instance().addChunkMDListener(this);
    }

    protected void updateOptions()
    {
        World world = ForgeHelper.INSTANCE.getClient().theWorld;
        int seaLevel = world.getActualHeight() / 2;
        orthoStep = 3;
        orthoRange = world.getActualHeight() >> orthoStep;
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
        y = Math.max(0, chunkMd.getHeightValue(x, z));

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
        float h;
        Float slope;
        float hN, hW, hE, hS;
        float nearZero = 0.0001f;
        for (int z = 0; z < 16; z++)
        {
            for (int x = 0; x < 16; x++)
            {
                h = getSurfaceBlockHeight(chunkMd, x, z, chunkHeights);
                hN = getSurfaceBlockHeight(chunkMd, x, z, offsetN, (int) h, chunkHeights);
                hW = getSurfaceBlockHeight(chunkMd, x, z, offsetW, (int) h, chunkHeights);
                hS = getSurfaceBlockHeight(chunkMd, x, z, offsetS, (int) h, chunkHeights);
                hE = getSurfaceBlockHeight(chunkMd, x, z, offsetE, (int) h, chunkHeights);

                h = Math.max(nearZero, (int) h >> 3);
                hN = Math.max(nearZero, (int) hN >> 3);
                hW = Math.max(nearZero, (int) hW >> 3);
                hE = Math.max(nearZero, (int) hE >> 3);
                hS = Math.max(nearZero, (int) hS >> 3);

                if (h != hN && (hN == hW && hN == hE && hN == hS))
                {
                    slope = 1f; // lets ignore one-block elevation changes
                }
                else
                {
                    slope = ((h / hN) + (h / hW) + (h / hE) + (h / hS)) / 4f;
                }

                if (slope == null || slope.isNaN() || slope.isInfinite())
                {
                    JourneyMap.getLogger().warn(String.format("Bad topo slope for %s at %s,%s: %s", chunkMd, x, z, slope));
                    slope = 1f;
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
            color = RGB.darken(color, slope);
        }
        else if (slope > 1)
        {
            color = darkGray;
        }
        else
        {
            color = getBaseBlockColor(topBlockMd, x, y, z);
        }

        paintBlock(x, z, color, g2D);

        return true;
    }

    /**
     * Get the color for a block based on topological height
     */
    protected int getBaseBlockColor(final BlockMD blockMD, int x, int y, int z)
    {
        float orthoY = y >> orthoStep;
        if (blockMD.isWater())
        {
            int index = (int) Math.floor(orthoY / (orthoRange * 1f / waterPaletteRange));

            return waterPalette[index];
        }
        else
        {
            int index = (int) Math.floor(orthoY / (orthoRange * 1f / landPaletteRange));

            return landPalette[index];
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
