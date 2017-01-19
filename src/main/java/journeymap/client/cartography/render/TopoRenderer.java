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
import journeymap.client.log.StatTimer;
import journeymap.client.model.BlockCoordIntPair;
import journeymap.client.model.BlockMD;
import journeymap.client.model.ChunkMD;
import journeymap.client.properties.TopoProperties;
import journeymap.client.render.ComparableBufferedImage;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.apache.logging.log4j.Level;

import java.awt.image.BufferedImage;

/**
 * Generates topographical map images.
 */
public class TopoRenderer extends BaseRenderer implements IChunkRenderer
{
    private static final String PROP_SHORE = "isShore";
    private Integer[] waterPalette;
    private Integer[] landPalette;
    private int waterPaletteRange;
    private int landPaletteRange;
    private long lastTopoFileUpdate;

    protected StatTimer renderTopoTimer = StatTimer.get("TopoRenderer.renderSurface");

    // Vertical size in blocks of each contour
    private Integer landContourColor;
    private Integer waterContourColor;

    private double waterContourInterval;
    private double landContourInterval;

    TopoProperties topoProperties;


    public TopoRenderer()
    {
        // TODO: Write the caches to disk and we'll have some useful data available.
        this.cachePrefix = "Topo";

        primarySlopeOffsets.clear();
        secondarySlopeOffsets.clear();

        primarySlopeOffsets.add(new BlockCoordIntPair(0, -1)); // North
        primarySlopeOffsets.add(new BlockCoordIntPair(-1, 0)); // West
        primarySlopeOffsets.add(new BlockCoordIntPair(0, 1)); // South
        primarySlopeOffsets.add(new BlockCoordIntPair(1, 0)); // East

    }

    /**
     * Ensures mapping options are up-to-date.
     */
    @Override
    protected boolean updateOptions(ChunkMD chunkMd)
    {
        super.updateOptions(chunkMd);
        boolean needUpdate = false;
        World world = FMLClientHandler.instance().getClient().theWorld;
        double worldHeight = world.getHeight();

        topoProperties = Journeymap.getClient().getTopoProperties();
        if (System.currentTimeMillis() - lastTopoFileUpdate > 5000 && lastTopoFileUpdate < topoProperties.lastModified())
        {
            needUpdate = true;
            Journeymap.getLogger().info("Loading " + topoProperties.getFileName());
            topoProperties.load();
            lastTopoFileUpdate = topoProperties.lastModified();

            landContourColor = topoProperties.getLandContourColor();
            waterContourColor = topoProperties.getWaterContourColor();

            waterPalette = topoProperties.getWaterColors();
            waterPaletteRange = waterPalette.length - 1;
            waterContourInterval = worldHeight / Math.max(1, waterPalette.length);

            landPalette = topoProperties.getLandColors();

            landPaletteRange = landPalette.length - 1;
            landContourInterval = worldHeight / Math.max(1, landPalette.length);
        }

        if (chunkMd != null)
        {
            Long lastUpdate = (Long) chunkMd.getProperty("lastTopoPropFileUpdate", lastTopoFileUpdate);
            if (needUpdate || (lastUpdate < lastTopoFileUpdate))
            {
                ;
            }
            {
                needUpdate = true;
                resetHeights(chunkMd, null);
                resetSlopes(chunkMd, null);
                resetWaterHeights(chunkMd, null);
                resetShore(chunkMd);
            }
            chunkMd.setProperty("lastTopoPropFileUpdate", lastTopoFileUpdate);
        }

        return needUpdate;
    }

    /**
     * Render blocks in the chunk for the standard world.
     */
    @Override
    public boolean render(final ComparableBufferedImage chunkImage, final ChunkMD chunkMd, final Integer vSlice)
    {
        StatTimer timer = renderTopoTimer;

        if (landPalette == null || landPalette.length < 1 || waterPalette == null || waterPalette.length < 1)
        {
            return false;
        }

        try
        {
            timer.start();

            updateOptions(chunkMd);

            // Initialize ChunkSub slopes if needed
            if (!hasSlopes(chunkMd, null))
            {
                populateSlopes(chunkMd);
            }

            // Render the chunk image
            return renderSurface(chunkImage, chunkMd, vSlice, false);
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
    protected boolean renderSurface(final BufferedImage chunkImage, final ChunkMD chunkMd, final Integer vSlice, final boolean cavePrePass)
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

                    int y = Math.max(0, getBlockHeight(chunkMd, x, null, z, null, null));

                    if (mapBathymetry)
                    {
                        y = getWaterHeights(chunkMd, null)[z][x];
                    }

                    topBlockMd = chunkMd.getTopBlockMD(x, y, z);
                    if (topBlockMd == null)
                    {
                        paintBadBlock(chunkImage, x, y, z);
                        continue blockLoop;
                    }

                    chunkOk = paintContour(chunkImage, chunkMd, topBlockMd, x, y, z) || chunkOk;
                }
            }
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().log(Level.WARN, "Error in renderSurface: " + LogFormatter.toString(t));
        }

        return chunkOk;
    }

    @Override
    public Integer getBlockHeight(final ChunkMD chunkMd, int localX, Integer vSlice, int localZ, Integer sliceMinY, Integer sliceMaxY)
    {
        Integer[][] heights = getHeights(chunkMd, null);
        if (heights == null)
        {
            // Not in cache anymore
            return null;
        }

        Integer y = heights[localX][localZ];
        if (y != null)
        {
            // Already set
            return y;
        }

        // Find the actual height.
        y = Math.max(0, chunkMd.getPrecipitationHeight(localX, localZ));

        try
        {
            BlockMD blockMD = BlockMD.getBlockMDFromChunkLocal(chunkMd, localX, y, localZ);
            while (y > 0)
            {
                if (blockMD.isWater() || blockMD.isIce())
                {
                    if (mapBathymetry)
                    {
                        getWaterHeights(chunkMd, null)[localZ][localX] = y;
                    }
                    else
                    {
                        break;
                    }
                }
                else if (!blockMD.isAir() && !blockMD.hasFlag(BlockMD.Flag.NoTopo))
                {
                    break;
                }
                y--;
                blockMD = BlockMD.getBlockMDFromChunkLocal(chunkMd, localX, y, localZ);
            }
        }
        catch (Exception e)
        {
            Journeymap.getLogger().debug("Couldn't get safe surface block height at " + localX + "," + localZ + ": " + e);
        }

        y = Math.max(0, y);

        heights[localX][localZ] = y;

        return y;
    }

    /**
     * Initialize surface slopes in chunk if needed.
     */
    protected Float[][] populateSlopes(final ChunkMD chunkMd)
    {
        Float[][] slopes = getSlopes(chunkMd, null);

        float h;
        Float slope;
        double contourInterval;
        float nearZero = 0.0001f;
        for (int z = 0; z < 16; z++)
        {
            for (int x = 0; x < 16; x++)
            {
                h = getBlockHeight(chunkMd, x, null, z, null, null);

                BlockMD blockMD = BlockMD.getBlockMDFromChunkLocal(chunkMd, x, (int) h, z);

                boolean isWater = false;

                if (blockMD.isWater() || blockMD.isIce() || (mapBathymetry && getWaterHeights(chunkMd, null)[z][x] != null))
                {
                    isWater = true;
                    contourInterval = waterContourInterval;
                }
                else
                {

                    contourInterval = landContourInterval;
                }

                float[] heights = new float[primarySlopeOffsets.size()];
                Float lastOffsetHeight = null;
                boolean flatOffsets = true;
                boolean isShore = false;
                for (int i = 0; i < heights.length; i++)
                {
                    BlockCoordIntPair offset = primarySlopeOffsets.get(i);

                    // Get height
                    float offsetHeight = getOffsetBlockHeight(chunkMd, x, null, z, null, null, offset, (int) h);

                    // If water, check for neighboring land
                    if (isWater && !isShore)
                    {
                        // offset is either also  not set, try to manually find out
                        ChunkMD targetChunkMd = getOffsetChunk(chunkMd, x, z, offset);
                        final int newX = ((chunkMd.getCoord().chunkXPos << 4) + (x + offset.x)) & 15;
                        final int newZ = ((chunkMd.getCoord().chunkZPos << 4) + (z + offset.z)) & 15;

                        if (targetChunkMd != null)
                        {
                            if (mapBathymetry && (mapBathymetry && getWaterHeights(chunkMd, null)[z][x] == null))
                            {
                                isShore = true;
                            }
                            else
                            {
                                int ceiling = targetChunkMd.ceiling(newX, newZ);
                                BlockMD offsetBlock = targetChunkMd.getTopBlockMD(newX, ceiling, newZ);
                                if (!offsetBlock.isWater() && !offsetBlock.isIce())
                                {
                                    isShore = true;
                                }
                            }
                        }
                    }

                    // Shift to nearest contour
                    offsetHeight = (float) Math.max(nearZero, offsetHeight - (offsetHeight % contourInterval));
                    heights[i] = offsetHeight;

                    if (lastOffsetHeight == null)
                    {
                        lastOffsetHeight = offsetHeight;
                    }
                    else if (flatOffsets)
                    {
                        flatOffsets = (lastOffsetHeight == offsetHeight);
                    }
                }

                if (isWater)
                {
                    getShore(chunkMd)[z][x] = isShore;
                }


                // Shift height to nearest contour
                h = (float) Math.max(nearZero, h - (h % contourInterval));

                if (flatOffsets)
                {
                    slope = 1f;
                }
                else
                {
                    slope = 0f;
                    for (float offsetHeight : heights)
                    {
                        slope += (h / offsetHeight);
                    }
                    slope = slope / heights.length;
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

    @Override
    public int getBlockHeight(ChunkMD chunkMd, BlockPos blockPos)
    {
        return FMLClientHandler.instance().getClient().theWorld.getChunkFromBlockCoords(blockPos).getPrecipitationHeight(blockPos).getY();
    }

    protected boolean paintContour(final BufferedImage chunkImage, final ChunkMD chunkMd, final BlockMD topBlockMd, final int x, final int y, final int z)
    {
        if (!chunkMd.hasChunk())
        {
            return false;
        }

        try
        {

            float slope = getSlope(chunkMd, topBlockMd, x, null, z);
            boolean isWater = topBlockMd.isWater() || topBlockMd.isIce();

            int color;
            if (slope > 1)
            {
                // Contour lines between intervals
                color = isWater ? waterContourColor : landContourColor;
            }
            else
            {
                if (topBlockMd.isLava())
                {
                    // Use standard lava color
                    color = topBlockMd.getColor();
                }
                else if (isWater)
                {
                    if (getShore(chunkMd)[z][x] == Boolean.TRUE)
                    {
                        // water is touching land, use the contour color
                        color = waterContourColor;
                    }
                    else
                    {
                        // Get color from water palette
                        int index = (int) Math.floor((y - (y % waterContourInterval)) / waterContourInterval);

                        // Precautionary - ensure in range
                        index = Math.max(0, Math.min(index, waterPaletteRange));
                        color = waterPalette[index];

                        // Darken downhill of contour line
                        if (slope < 1)
                        {
                            color = RGB.adjustBrightness(color, .90f);
                        }
                    }
                }
                else
                {
                    // Get color from land palette
                    int index = (int) Math.floor((y - (y % landContourInterval)) / landContourInterval);

                    // Precautionary - ensure in range
                    index = Math.max(0, Math.min(index, landPaletteRange));
                    color = landPalette[index];

                    // Darken downhill of contour line
                    if (slope < 1)
                    {
                        color = RGB.adjustBrightness(color, .85f);
                    }
                }
            }

            paintBlock(chunkImage, x, z, color);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return true;
    }

    protected final Boolean[][] getShore(ChunkMD chunkMd)
    {
        return chunkMd.getBlockDataBooleans(cachePrefix).get(PROP_SHORE);
    }

    protected final boolean hasShore(ChunkMD chunkMd)
    {
        return chunkMd.getBlockDataBooleans(cachePrefix).has(PROP_SHORE);
    }

    protected final void resetShore(ChunkMD chunkMd)
    {
        chunkMd.getBlockDataBooleans(cachePrefix).clear(PROP_SHORE);
    }
}
