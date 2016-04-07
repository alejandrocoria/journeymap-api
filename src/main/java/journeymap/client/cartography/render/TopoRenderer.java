/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.cartography.render;

import com.google.common.cache.RemovalNotification;
import journeymap.client.JourneymapClient;
import journeymap.client.cartography.ChunkPainter;
import journeymap.client.cartography.IChunkRenderer;
import journeymap.client.cartography.RGB;
import journeymap.client.data.DataCache;
import journeymap.client.log.StatTimer;
import journeymap.client.model.BlockCoordIntPair;
import journeymap.client.model.BlockMD;
import journeymap.client.model.ChunkMD;
import journeymap.client.properties.TopoProperties;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.apache.logging.log4j.Level;

/**
 * Generates topographical map images.
 */
public class TopoRenderer extends BaseRenderer implements IChunkRenderer
{
    protected final Object chunkLock = new Object();
    private Integer[] waterPalette;
    private Integer[] landPalette;
    private int waterPaletteRange;
    private int landPaletteRange;
    private final HeightsCache chunkSurfaceHeights;
    private final SlopesCache chunkSurfaceSlopes;

    protected StatTimer renderTopoTimer = StatTimer.get("TopoRenderer.renderSurface");

    // Vertical size in blocks of each contour
    private Integer contourColor;
    private double waterContourInterval;
    private double landContourInterval;

    TopoProperties topoProperties;


    public TopoRenderer()
    {
        // TODO: Write the caches to disk and we'll have some useful data available.
        this.cachePrefix = "Topo";
        columnPropertiesCache = new BlockColumnPropertiesCache(cachePrefix + "ColumnProps");
        chunkSurfaceHeights = new HeightsCache(cachePrefix + "Heights");
        chunkSurfaceSlopes = new SlopesCache(cachePrefix + "Slopes");
        DataCache.instance().addChunkMDListener(this);
    }

    /**
     * Ensures mapping options are up-to-date.
     */
    protected void updateOptions()
    {
        super.updateOptions();

        World world = FMLClientHandler.instance().getClient().theWorld;
        double worldHeight = world.getHeight();

        topoProperties = JourneymapClient.getTopoProperties();

        contourColor = topoProperties.getContourColor();

        waterPalette = topoProperties.getWaterColors();
        waterPaletteRange = waterPalette.length - 1;
        waterContourInterval = worldHeight / Math.max(1, waterPalette.length);

        landPalette = topoProperties.getLandColors();

        landPaletteRange = landPalette.length - 1;
        landContourInterval = worldHeight / Math.max(1, landPalette.length);
    }

    /**
     * Render blocks in the chunk for the standard world.
     */
    @Override
    public boolean render(final ChunkPainter painter, final ChunkMD chunkMd, final Integer vSlice)
    {
        StatTimer timer = renderTopoTimer;

        if (landPalette == null || landPalette.length < 1 || waterPalette == null || waterPalette.length < 1)
        {
            return false;
        }

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
            return renderSurface(painter, chunkMd, vSlice, false);
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
    protected boolean renderSurface(final ChunkPainter painter, final ChunkMD chunkMd, final Integer vSlice, final boolean cavePrePass)
    {
        boolean chunkOk = false;

        try
        {
            for (int x = 0; x < 16; x++)
            {
                blockLoop:
                for (int z = 0; z < 16; z++)
                {
                    BlockMD topBlockMd = null;

                    int y = Math.max(0, getSurfaceBlockHeight(chunkMd, x, z, chunkSurfaceHeights));

                    if (mapBathymetry)
                    {
                        y = getColumnProperty(PROP_WATER_HEIGHT, y, chunkMd, x, z);
                    }

                    topBlockMd = chunkMd.getTopBlockMD(x, y, z);
                    if (topBlockMd == null)
                    {
                        painter.paintBadBlock(x, y, z);
                        continue blockLoop;
                    }

                    chunkOk = paintContour(painter, chunkMd, topBlockMd, x, y, z) || chunkOk;
                }
            }
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().log(Level.WARN, LogFormatter.toString(t));
        }

        return chunkOk;
    }

    public Integer getSurfaceBlockHeight(final ChunkMD chunkMd, int x, int z, final HeightsCache chunkHeights)
    {
        Integer[][] heights = chunkHeights.getUnchecked(chunkMd.getCoord());
        if (heights == null)
        {
            // Not in cache anymore
            return null;
        }

        Integer y = heights[x][z];
        if (y != null)
        {
            // Already set
            return y;
        }

        // Find the actual height.
        y = Math.max(0, chunkMd.getPrecipitationHeight(x, z));

        try
        {
            boolean propUnsetWaterHeight = true;
            BlockMD blockMD = BlockMD.getBlockMD(chunkMd, x, y, z);
            while (y > 0)
            {
                if (blockMD.isWater() || blockMD.isIce())
                {
                    if (!mapBathymetry)
                    {
                        break;
                    }
                    else
                    {
                        setColumnProperty(PROP_WATER_HEIGHT, y, chunkMd, x, z);
                    }
                }
                else if (!blockMD.isAir() && !blockMD.hasFlag(BlockMD.Flag.NoTopo))
                {
                    break;
                }
                y--;
                blockMD = BlockMD.getBlockMD(chunkMd, x, y, z);
            }
        }
        catch (Exception e)
        {
            Journeymap.getLogger().debug("Couldn't get safe surface block height at " + x + "," + z + ": " + e);
        }

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

        if (Double.valueOf(waterContourInterval).isInfinite())
        {
            return slopes;
        }

        float h;
        Float slope;
        double contourInterval;
        float hN, hW, hE, hS;
        float nearZero = 0.0001f;
        for (int z = 0; z < 16; z++)
        {
            for (int x = 0; x < 16; x++)
            {
                h = getSurfaceBlockHeight(chunkMd, x, z, chunkHeights);

                BlockMD blockMD = BlockMD.getBlockMD(chunkMd, x, (int) h, z);
                if (blockMD.isWater() || blockMD.isIce())
                {
                    contourInterval = waterContourInterval;
                }
                else
                {
                    contourInterval = landContourInterval;
                }

                hN = getSurfaceBlockHeight(chunkMd, x, z, offsetN, (int) h, chunkHeights);
                hW = getSurfaceBlockHeight(chunkMd, x, z, offsetW, (int) h, chunkHeights);
                hS = getSurfaceBlockHeight(chunkMd, x, z, offsetS, (int) h, chunkHeights);
                hE = getSurfaceBlockHeight(chunkMd, x, z, offsetE, (int) h, chunkHeights);

                // Shift to nearest contour
                h = (float) Math.max(nearZero, h - (h % contourInterval));
                hN = (float) Math.max(nearZero, hN - (hN % contourInterval));
                hW = (float) Math.max(nearZero, hW - (hW % contourInterval));
                hE = (float) Math.max(nearZero, hE - (hE % contourInterval));
                hS = (float) Math.max(nearZero, hS - (hS % contourInterval));

                if (h != hN && hN == hW && hW == hE && hE == hS)
                {
                    // ignore one-block elevation changes
                    slope = 1f;
                }
                else
                {
                    slope = ((h / hN) + (h / hW) + (h / hE) + (h / hS)) / 4f;
                }

                if (slope.isNaN() || slope.isInfinite())
                {
                    // Journeymap.getLogger().warn(String.format("Bad topo slope for %s at %s,%s: %s", chunkMd, x, z, slope));
                    slope = 1f;
                }

                slopes[x][z] = slope;
            }
        }
        return slopes;
    }

    protected boolean paintContour(final ChunkPainter painter, final ChunkMD chunkMd, final BlockMD topBlockMd, final int x, final int y, final int z)
    {
        float slope = getSlope(chunkMd, topBlockMd, x, null, z, chunkSurfaceHeights, chunkSurfaceSlopes);

        int color;
        if (slope > 1 && contourColor!=null)
        {
            // Contour ring between ortho step
            color = contourColor;
        }
        else
        {
            if (topBlockMd.isLava())
            {
                // Use standard lava color
                color = topBlockMd.getColor();
            }
            else if (topBlockMd.isWater())
            {
                // Get color from water palette
                int index = (int) Math.floor((y - (y % waterContourInterval)) / waterContourInterval);
                // Precautionary - ensure in range
                index = Math.max(0, Math.min(index, waterPaletteRange));
                color = waterPalette[index];
            }
            else if (topBlockMd.isIce())
            {
                // Get color from water palette
                int index = (int) Math.floor((y - (y % waterContourInterval)) / waterContourInterval);
                // Precautionary - ensure in range
                index = Math.max(0, Math.min(index, waterPaletteRange));
                color = waterPalette[index];
            }
            else
            {
                // Get color from land palette
                int index = (int) Math.floor((y - (y % landContourInterval)) / landContourInterval);
                // Precautionary - ensure in range
                index = Math.max(0, Math.min(index, landPaletteRange));
                color = landPalette[index];
                // Darken next to contour ring if slope
                color = RGB.adjustBrightness(color, slope);
            }
        }

        painter.paintBlock(x, z, color);

        return true;
    }

    @Override
    public void onRemoval(RemovalNotification<ChunkCoordIntPair, ChunkMD> notification)
    {
        synchronized (chunkLock)
        {
            ChunkCoordIntPair coord = notification.getKey();
            chunkSurfaceHeights.invalidate(coord);
            chunkSurfaceSlopes.invalidate(coord);
            columnPropertiesCache.invalidate(coord);
        }
    }
}
