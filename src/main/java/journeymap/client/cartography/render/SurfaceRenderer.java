/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.cartography.render;

import journeymap.client.cartography.IChunkRenderer;
import journeymap.client.cartography.RGB;
import journeymap.client.cartography.Strata;
import journeymap.client.cartography.Stratum;
import journeymap.client.log.StatTimer;
import journeymap.client.model.BlockCoordIntPair;
import journeymap.client.model.BlockMD;
import journeymap.client.model.ChunkMD;
import journeymap.client.model.MapType;
import journeymap.client.render.ComparableBufferedImage;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.Level;

import java.awt.image.BufferedImage;

/**
 * The type Surface renderer.
 */
public class SurfaceRenderer extends BaseRenderer implements IChunkRenderer
{
    /**
     * The Render surface timer.
     */
    protected StatTimer renderSurfaceTimer = StatTimer.get("SurfaceRenderer.renderSurface");
    /**
     * The Render surface prepass timer.
     */
    protected StatTimer renderSurfacePrepassTimer = StatTimer.get("SurfaceRenderer.renderSurface.CavePrepass");
    /**
     * The Strata.
     */
    protected Strata strata = new Strata("Surface", 40, 8, false);
    /**
     * The Max depth.
     */
    protected float maxDepth = 8;

    /**
     * Instantiates a new Surface renderer.
     */
    public SurfaceRenderer()
    {
        updateOptions(null, null);
    }

    @Override
    protected boolean updateOptions(ChunkMD chunkMd, MapType mapType)
    {
        if (super.updateOptions(chunkMd, mapType))
        {
            this.ambientColor = RGB.floats(tweakSurfaceAmbientColor);
            return true;
        }
        return false;
    }

    @Override
    public int getBlockHeight(ChunkMD chunkMd, BlockPos blockPos)
    {
        Integer y = getBlockHeight(chunkMd, blockPos.getX() & 15, null, blockPos.getZ() & 15, null, null);
        return (y == null) ? blockPos.getY() : y;
    }

    /**
     * Render blocks in the chunk for the standard world, day only
     */
    @Override
    public boolean render(final ComparableBufferedImage dayChunkImage, final ChunkMD chunkMd, final Integer ignored)
    {
        return render(dayChunkImage, null, chunkMd, null, false);
    }

    /**
     * Render blocks in the chunk for the standard world
     *
     * @param dayChunkImage   the day chunk image
     * @param nightChunkImage the night chunk image
     * @param chunkMd         the chunk md
     * @return the boolean
     */
    public boolean render(final ComparableBufferedImage dayChunkImage, final BufferedImage nightChunkImage, final ChunkMD chunkMd)
    {
        return render(dayChunkImage, nightChunkImage, chunkMd, null, false);
    }

    /**
     * Render blocks in the chunk for the standard world.
     *
     * @param dayChunkImage   the day chunk image
     * @param nightChunkImage the night chunk image
     * @param chunkMd         the chunk md
     * @param vSlice          the v slice
     * @param cavePrePass     the cave pre pass
     * @return the boolean
     */
    public synchronized boolean render(final ComparableBufferedImage dayChunkImage, final BufferedImage nightChunkImage, final ChunkMD chunkMd, final Integer vSlice, final boolean cavePrePass)
    {
        StatTimer timer = cavePrePass ? renderSurfacePrepassTimer : renderSurfaceTimer;

        try
        {
            timer.start();

            updateOptions(chunkMd, MapType.from(MapType.Name.surface, null, chunkMd.getDimension()));

            // Initialize ChunkSub slopes if needed
            if (!hasSlopes(chunkMd, vSlice))
            {
                populateSlopes(chunkMd, vSlice, getSlopes(chunkMd, vSlice));
            }

            // Render the chunk image
            return renderSurface(dayChunkImage, nightChunkImage, chunkMd, vSlice, cavePrePass);
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
     *
     * @param dayChunkImage   the day chunk image
     * @param nightChunkImage the night chunk image
     * @param chunkMd         the chunk md
     * @param vSlice          the v slice
     * @param cavePrePass     the cave pre pass
     * @return the boolean
     */
    protected boolean renderSurface(final BufferedImage dayChunkImage, final BufferedImage nightChunkImage, final ChunkMD chunkMd, final Integer vSlice, final boolean cavePrePass)
    {
        boolean chunkOk = false;

        try
        {
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

                    int standardY = Math.max(0, getBlockHeight(chunkMd, x, null, z, null, null));

                    if (standardY == 0)
                    {
                        paintVoidBlock(dayChunkImage, x, z);
                        if (!cavePrePass && nightChunkImage != null)
                        {
                            paintVoidBlock(nightChunkImage, x, z);
                        }
                        chunkOk = true;
                        continue blockLoop;
                    }

                    // Should be painted only by cave renderer
                    if (cavePrePass && (standardY > sliceMaxY && (standardY - sliceMaxY) > maxDepth))
                    {
                        chunkOk = true;
                        paintBlackBlock(dayChunkImage, x, z);
                        continue;
                    }

                    int roofY = 0;
                    int y = standardY;

                    // TODO: Re-evaluate whether this section is necessary now that
                    // precipHeight is always used
                    roofY = Math.max(0, chunkMd.getPrecipitationHeight(x, z));
                    if (standardY < roofY)
                    {
                        // Is transparent roof above standard height?
                        int checkY = roofY;
                        while (checkY > standardY)
                        {
                            topBlockMd = BlockMD.getBlockMDFromChunkLocal(chunkMd, x, checkY, z);
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
                        paintVoidBlock(dayChunkImage, x, z);
                        if (!cavePrePass && nightChunkImage != null)
                        {
                            paintVoidBlock(nightChunkImage, x, z);
                        }
                        chunkOk = true;
                        continue blockLoop;
                    }

                    // Bathymetry - need to use water height instead of standardY, so we get the color blend
                    if (mapBathymetry)
                    {
                        Integer[][] waterHeights = getWaterHeights(chunkMd, null);
                        Integer waterHeight = waterHeights[z][x];
                        if (waterHeight == null)
                        {
                            waterHeights[z][x] = standardY;
                        }
                        else
                        {
                            standardY = waterHeight;
                        }
                    }

                    topBlockMd = chunkMd.getTopBlockMD(x, standardY, z);

                    if (topBlockMd == null)
                    {
                        paintBadBlock(dayChunkImage, x, standardY, z);
                        paintBadBlock(nightChunkImage, x, standardY, z);
                        continue blockLoop;
                    }

                    // Plants/crops/double-tall need to check one or two blocks up
                    if (mapPlants || mapCrops)
                    {
                        BlockMD temp = chunkMd.getTopBlockMD(x, standardY + 1, z);
                        if ((mapPlants && temp.hasFlag(BlockMD.Flag.Plant)) || (mapCrops && temp.hasFlag(BlockMD.Flag.Crop)))
                        {
                            standardY += 1;
                        }
                    }

                    // Start using BlockColors stack
                    buildStrata(strata, roofY, chunkMd, x, standardY, z);

                    chunkOk = paintStrata(strata, dayChunkImage, nightChunkImage, chunkMd, topBlockMd, vSlice, x, y, z, cavePrePass) || chunkOk;
                }
            }
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().log(Level.WARN, LogFormatter.toString(t));
        }
        finally
        {
            strata.reset();
        }
        return chunkOk;
    }

    /**
     * Get the height of the block at the coordinates + offsets.  Uses chunkMd.slopes.
     *
     * @param chunkMd    the chunk md
     * @param x          the x
     * @param z          the z
     * @param offset     the offset
     * @param defaultVal the default val
     * @return the surface block height
     */
    public int getSurfaceBlockHeight(final ChunkMD chunkMd, int x, int z, BlockCoordIntPair offset, int defaultVal)
    {
        ChunkMD targetChunkMd = getOffsetChunk(chunkMd, x, z, offset);
        final int newX = ((chunkMd.getCoord().chunkXPos << 4) + (x + offset.x)) & 15;
        final int newZ = ((chunkMd.getCoord().chunkZPos << 4) + (z + offset.z)) & 15;

        if (targetChunkMd != null)
        {
            Integer height = getBlockHeight(targetChunkMd, newX, null, newZ, null, null);
            if (height == null)
            {
                return defaultVal;
            }
            else
            {
                return height;
            }
        }
        else
        {
            return defaultVal;
        }
    }

    /**
     * Added because getHeight() sometimes returns an air block.
     * Returns the value in the height map at this x, z coordinate in the chunk, disregarding
     * blocks that shouldn't be used as the top block.
     */
    @Override
    public Integer getBlockHeight(final ChunkMD chunkMd, int localX, Integer vSlice, int localZ, Integer sliceMinY, Integer sliceMaxY)
    {
        Integer[][] heights = getHeights(chunkMd, null);
        if (heights == null)
        {
            // Not in cache anymore
            return null;
        }
        Integer y;

        y = heights[localX][localZ];

        if (y != null)
        {
            // Already set
            return y;
        }

        // Find the height.
        y = Math.max(0, chunkMd.getPrecipitationHeight(localX, localZ));

        if (y == 0)
        {
            return 0;
        }

        BlockMD blockMD;
        boolean propUnsetWaterHeight = true;

        try
        {
            while (y > 0)
            {
                blockMD = BlockMD.getBlockMDFromChunkLocal(chunkMd, localX, y, localZ);

                if (blockMD.isAir())
                {
                    y--;
                    continue;
                }
                else if (blockMD.isWater())
                {
                    if (!mapBathymetry)
                    {
                        break;
                    }
                    else if (propUnsetWaterHeight)
                    {
                        getWaterHeights(chunkMd, null)[localZ][localX] = y;
                        propUnsetWaterHeight = false;
                    }
                    y--;
                    continue;
                }
                else if (blockMD.hasFlag(BlockMD.Flag.Plant))
                {
                    if (!mapPlants)
                    {
                        y--;
                        continue;
                    }

                    if (!mapPlantShadows || !blockMD.hasNoShadow())
                    {
                        y--;
                    }

                    break;
                }
                else if (blockMD.hasFlag(BlockMD.Flag.Crop))
                {
                    if (!mapCrops)
                    {
                        y--;
                        continue;
                    }

                    if (!mapPlantShadows || !blockMD.hasNoShadow())
                    {
                        y--;
                    }

                    break;

                }
                else if (!blockMD.isLava() && blockMD.hasNoShadow())
                {
                    y--;
                }

                break;
            }
        }
        catch (Exception e)
        {
            Journeymap.getLogger().warn(String.format("Couldn't get safe surface block height for %s coords %s,%s: %s",
                    chunkMd, localX, localZ, LogFormatter.toString(e)));
        }

        //why is height 4 set on a chunk to the left?
        y = Math.max(0, y);

        heights[localX][localZ] = y;

        return y;
    }

    /**
     * Create a BlockStack.
     *
     * @param strata  the strata
     * @param roofY   the roof y
     * @param chunkMd the chunk md
     * @param x       the x
     * @param y       the y
     * @param z       the z
     */
    protected void buildStrata(Strata strata, int roofY, ChunkMD chunkMd, int x, int y, int z)
    {
        BlockMD blockMD;

        // If under glass, add to color stack
        if (roofY > y)
        {
            while (roofY > y)
            {
                blockMD = BlockMD.getBlockMDFromChunkLocal(chunkMd, x, roofY, z);
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
                blockMD = BlockMD.getBlockMDFromChunkLocal(chunkMd, x, y, z);

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
     *
     * @param strata          the strata
     * @param dayChunkImage   the day chunk image
     * @param nightChunkImage the night chunk image
     * @param chunkMd         the chunk md
     * @param topBlockMd      the top block md
     * @param vSlice          the v slice
     * @param x               the x
     * @param y               the y
     * @param z               the z
     * @param cavePrePass     the cave pre pass
     * @return the boolean
     */
    protected boolean paintStrata(final Strata strata, final BufferedImage dayChunkImage, final BufferedImage nightChunkImage, final ChunkMD chunkMd, final BlockMD topBlockMd, final Integer vSlice, final int x, final int y, final int z, final boolean cavePrePass)
    {
        if (strata.isEmpty())
        {
            if (dayChunkImage != null)
            {
                paintBadBlock(dayChunkImage, x, y, z);
            }
            if (nightChunkImage != null)
            {
                paintBadBlock(nightChunkImage, x, y, z);
            }
            return false;
        }

        try
        {
            Stratum stratum;
            while (!strata.isEmpty())
            {
                stratum = strata.nextUp(this, true);
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
                paintBadBlock(dayChunkImage, x, y, z);
                paintBadBlock(nightChunkImage, x, y, z);
                return false;
            }

            if (nightChunkImage != null)
            {
                // Shouldn't happen
                if (strata.getRenderNightColor() == null)
                {
                    paintBadBlock(nightChunkImage, x, y, z);
                    return false;
                }
            }

            // Now add bevel for slope
            if ((topBlockMd.isWater() && mapBathymetry) || !topBlockMd.hasNoShadow())
            {
                float slope = getSlope(chunkMd, topBlockMd, x, null, z);
                if (slope != 1f)
                {
                    strata.setRenderDayColor(RGB.bevelSlope(strata.getRenderDayColor(), slope));
                    if (!cavePrePass)
                    {
                        strata.setRenderNightColor(RGB.bevelSlope(strata.getRenderNightColor(), slope));
                    }
                }
            }

            paintBlock(dayChunkImage, x, z, strata.getRenderDayColor());
            if (nightChunkImage != null)
            {
                paintBlock(nightChunkImage, x, z, strata.getRenderNightColor());
            }
        }
        catch (RuntimeException e)
        {
            throw e;
        }

        return true;
    }
}
