/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.cartography.render;

import com.google.common.cache.RemovalNotification;
import journeymap.client.cartography.ChunkPainter;
import journeymap.client.cartography.IChunkRenderer;
import journeymap.client.cartography.RGB;
import journeymap.client.data.DataCache;
import journeymap.client.log.StatTimer;
import journeymap.client.model.BlockCoordIntPair;
import journeymap.client.model.BlockMD;
import journeymap.client.model.ChunkMD;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.apache.logging.log4j.Level;

import java.awt.*;
import java.util.ArrayList;

/**
 * Generates topographical map images.
 */
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

    // Vertical size in blocks of each contour
    private int contourColor;
    private double waterContourInterval;
    private double landContourInterval;

    public TopoRenderer()
    {
        // TODO: Get these from properties file
        ArrayList<Integer> water = new ArrayList<Integer>(32);
        water.add(new Color(0x000040).getRGB());
        water.add(new Color(0x010159).getRGB());
        water.add(new Color(0x020266).getRGB());
        water.add(new Color(0x030372).getRGB());
        water.add(new Color(0x05057F).getRGB());
        water.add(new Color(0x080899).getRGB());
        water.add(new Color(0x0D0DB2).getRGB());
        water.add(new Color(0x0F0FBF).getRGB());
        water.add(new Color(0x1818E5).getRGB());
        water.add(new Color(0x1F1FFF).getRGB());
        water.add(new Color(0x2A2AFF).getRGB());
        water.add(new Color(0x4141FF).getRGB());
        water.add(new Color(0x5858FF).getRGB());
        water.add(new Color(0x6464FF).getRGB());
        water.add(new Color(0x6F6FFF).getRGB());
        water.add(new Color(0x7B7BFF).getRGB());
        water.add(new Color(0x8686FF).getRGB());
        water.add(new Color(0x9292FF).getRGB());
        water.add(new Color(0x9D9DFF).getRGB());
        water.add(new Color(0xA9A9FF).getRGB());
        water.add(new Color(0xB4B4FF).getRGB());
        water.add(new Color(0xC0C0FF).getRGB());
        water.add(new Color(0xCCCCFF).getRGB());
        water.add(new Color(0xDDDDFF).getRGB());
        water.add(new Color(0xDFDfFF).getRGB());
        water.add(new Color(0xE1E1FF).getRGB());
        water.add(new Color(0xE4E4FF).getRGB());
        water.add(new Color(0xE6E6FF).getRGB());
        water.add(new Color(0xE9E9FF).getRGB());
        water.add(new Color(0xEBEBFF).getRGB());
        water.add(new Color(0xEDEDFF).getRGB());
        water.add(new Color(0xEEEEFF).getRGB());

        waterPaletteRange = water.size() - 1;
        waterPalette = water.toArray(new Integer[0]);

        // TODO: Get these from properties file
        ArrayList<Integer> land = new ArrayList<Integer>(32);
        land.add(new Color(1, 1, 1).getRGB());
        land.add(new Color(10, 70, 90).getRGB());
        land.add(new Color(20, 80, 90).getRGB());
        land.add(new Color(30, 90, 100).getRGB());
        land.add(new Color(40, 100, 100).getRGB());
        land.add(new Color(50, 110, 100).getRGB());
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
        land.add(new Color(224, 195, 216).getRGB());
        land.add(new Color(224, 222, 216).getRGB());
        land.add(new Color(224, 244, 216).getRGB());
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

    /**
     * Ensures mapping options are up-to-date.
     */
    protected void updateOptions()
    {
        // TODO:  Check timestamp of properties file, reload colors if needed
        contourColor = Color.darkGray.getRGB();

        World world = FMLClientHandler.instance().getClient().theWorld;
        double worldHeight = world.getHeight();
        landContourInterval = worldHeight / landPaletteRange;
        waterContourInterval = worldHeight / waterPaletteRange;
    }

    /**
     * Render blocks in the chunk for the standard world.
     */
    @Override
    public boolean render(final ChunkPainter painter, final ChunkMD chunkMd, final Integer vSlice)
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
                    // Bathymetry - need to use water height instead of standardY, so we get the color blend
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
        Integer y;

        y = heights[x][z];

        if (y != null)
        {
            // Already set
            return y;
        }

        // Find the actual height.
        y = Math.max(0, chunkMd.getPrecipitationHeight(x, z));

        try
        {
            BlockMD blockMD = BlockMD.getBlockMD(chunkMd, x, y, z);
            while (y > 0)
            {
                if (blockMD.isWater() || blockMD.isIce())
                {
                    if (!mapBathymetry)
                    {
                        break;
                    }
                }
                else if (!blockMD.isAir() && !blockMD.hasFlag(BlockMD.Flag.NoTopo))
                {
                    break;
                }
                y--;
                blockMD = BlockMD.getBlockMD(chunkMd, x, y, z);
            }

            if (blockMD.isWater() || blockMD.isIce())
            {
                if (mapBathymetry)
                {
                    setColumnProperty(PROP_WATER_HEIGHT, y, chunkMd, x, z);
                }
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
                h = (int) Math.max(nearZero, h - (h % contourInterval));
                hN = (int) Math.max(nearZero, hN - (hN % contourInterval));
                hW = (int) Math.max(nearZero, hW - (hW % contourInterval));
                hE = (int) Math.max(nearZero, hE - (hE % contourInterval));
                hS = (int) Math.max(nearZero, hS - (hS % contourInterval));

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
                    // Your math is bad and you should feel bad
                    Journeymap.getLogger().warn(String.format("Bad topo slope for %s at %s,%s: %s", chunkMd, x, z, slope));
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
        if (slope > 1)
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
        }
    }
}
