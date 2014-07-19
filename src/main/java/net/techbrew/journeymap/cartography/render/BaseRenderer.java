/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.cartography.render;


import net.minecraft.init.Blocks;
import net.minecraft.world.ChunkCoordIntPair;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.cartography.IChunkRenderer;
import net.techbrew.journeymap.cartography.RGB;
import net.techbrew.journeymap.cartography.Stratum;
import net.techbrew.journeymap.data.DataCache;
import net.techbrew.journeymap.model.BlockCoordIntPair;
import net.techbrew.journeymap.model.BlockMD;
import net.techbrew.journeymap.model.ChunkMD;
import net.techbrew.journeymap.properties.CoreProperties;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Base class for methods reusable across renderers.
 *
 * @author mwoodman
 */
public abstract class BaseRenderer implements IChunkRenderer
{
    static final int BLACK = Color.black.getRGB();
    static final int VOID = RGB.toInteger(17, 12, 25);
    public static AlphaComposite OPAQUE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1F);
    protected final float[] defaultFog = new float[]{0, 0, .1f};
    protected DataCache dataCache = DataCache.instance();
    protected CoreProperties coreProperties = JourneyMap.getInstance().coreProperties;
    protected boolean mapBathymetry;
    protected boolean mapTransparency;
    protected boolean mapCaveLighting;
    protected boolean mapAntialiasing;
    volatile AtomicLong badBlockCount = new AtomicLong(0);
    ArrayList<BlockCoordIntPair> primarySlopeOffsets = new ArrayList<BlockCoordIntPair>(3);
    ArrayList<BlockCoordIntPair> secondarySlopeOffsets = new ArrayList<BlockCoordIntPair>(3);

    protected float slopeMin = 0.2f;
    protected float slopeMax = 1.7f;
    protected float primaryDownslopeMultiplier = .5f;
    protected float primaryUpslopeMultiplier = 1.25f;
    protected float secondaryDownslopeMultiplier = .95f;
    protected float secondaryUpslopeMultiplier = 1.1f;

    protected float moonlightLevel = 3.5f;

    public BaseRenderer()
    {
        updateOptions();

        // Offsets used for primary slope
        primarySlopeOffsets.add(new BlockCoordIntPair(0, -1)); // North
        primarySlopeOffsets.add(new BlockCoordIntPair(-1, 0)); // West
        primarySlopeOffsets.add(new BlockCoordIntPair(-1, -1)); // NorthWest

        // Offsets used for secondary slope
        secondarySlopeOffsets.add(new BlockCoordIntPair(-1, -2)); // North of NorthWest
        secondarySlopeOffsets.add(new BlockCoordIntPair(-2, -1)); // West of NorthWest
        secondarySlopeOffsets.add(new BlockCoordIntPair(-2, -2)); // NorthWest of NorthWest
        secondarySlopeOffsets.add(new BlockCoordIntPair(-1, -2)); // South of NorthWest
    }

    /**
     * Ensures mapping options are up-to-date.
     */
    protected void updateOptions()
    {
        mapBathymetry = coreProperties.mapBathymetry.get();
        mapTransparency = coreProperties.mapTransparency.get();
        mapAntialiasing = coreProperties.mapAntialiasing.get();
        mapCaveLighting = coreProperties.mapCaveLighting.get();
    }

    @Override
    public float[] getFogColor()
    {
        return defaultFog;
    }

    @Override
    public void setStratumColors(Stratum stratum, int lightAttenuation, Integer waterColor, boolean waterAbove, boolean underground, boolean mapCaveLighting)
    {
        if (stratum.isUninitialized())
        {
            throw new IllegalStateException("Stratum wasn't initialized for setStratumColors");
        }

        // Daylight is the greater of sun light (15) attenuated through the stack and the stratum's inherant light level
        float daylightDiff = Math.max(1, Math.max(stratum.getLightLevel(), 15 - lightAttenuation)) / 15f;

        // Nightlight is the greater of moon light (4) attenuated through the stack and the stratum's inherant light level
        float nightLightDiff = Math.max(moonlightLevel, Math.max(stratum.getLightLevel(), moonlightLevel - lightAttenuation)) / 15f;

        int basicColor = stratum.isWater() ? waterColor : stratum.getBlockMD().getColor(stratum.getChunkMd(), stratum.getX(), stratum.getY(), stratum.getZ());
        if (stratum.getBlockMD().getBlock() == Blocks.glowstone || stratum.getBlockMD().getBlock() == Blocks.lit_redstone_lamp)
        {
            basicColor = RGB.darken(basicColor, 1.2f); // magic # to match how it looks in game
        }

        if (waterAbove && waterColor != null)
        {
            // Blend day color with watercolor above, darken for daylight filtered down
            stratum.setDayColor(RGB.blendWith(waterColor, RGB.darken(basicColor, Math.max(daylightDiff, nightLightDiff)), Math.max(daylightDiff, nightLightDiff)));
            stratum.setDayColor(RGB.blendWith(stratum.getDayColor(), waterColor, .15f)); // cheat to get bluer blend in shallow water

            // Darken for night light and blend with watercolor above
            stratum.setNightColor(RGB.darken(stratum.getDayColor(), Math.max(nightLightDiff, .25f)));
        }
        else
        {
            // Just darken based on light levels
            stratum.setDayColor(RGB.darken(basicColor, daylightDiff));

            stratum.setNightColor(RGB.darkenFog(basicColor, nightLightDiff, getFogColor()));
        }

        if (underground)
        {
            stratum.setCaveColor(mapCaveLighting ? stratum.getNightColor() : stratum.getDayColor());
        }
    }

    /**
     * Paint the block with magenta to indicate it's a problem.
     */
    public void paintBadBlock(final int x, final int y, final int z, final Graphics2D g2D)
    {
        long count = badBlockCount.incrementAndGet();
        if (count == 1 || count % 10240 == 0)
        {
            JourneyMap.getLogger().warning(
                    "Bad block at " + x + "," + y + "," + z //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            + ". Total bad blocks painted: " + count
            ); //$NON-NLS-1$
        }
    }

    protected void paintDimOverlay(int x, int z, float alpha, final Graphics2D g2D)
    {
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2D.setPaint(RGB.paintOf(BLACK));
        g2D.fillRect(x, z, 1, 1);
        g2D.setComposite(OPAQUE);
    }

    /**
     * Paint the block.
     */
    public void paintBlock(final int x, final int z, final int color,
                           final Graphics2D g2D)
    {
        g2D.setComposite(OPAQUE);
        g2D.setPaint(RGB.paintOf(color));
        g2D.fillRect(x, z, 1, 1);
    }

    /**
     * Paint the void.
     */
    public void paintVoidBlock(final int x, final int z, final Graphics2D g2D)
    {
        g2D.setComposite(OPAQUE);
        g2D.setPaint(RGB.paintOf(VOID));
        g2D.fillRect(x, z, 1, 1);
    }

    /**
     * Paint the void.
     */
    public void paintBlackBlock(final int x, final int z, final Graphics2D g2D)
    {
        g2D.setComposite(OPAQUE);
        g2D.setPaint(RGB.paintOf(BLACK));
        g2D.fillRect(x, z, 1, 1);
    }


    /**
     * Initialize surface slopes in chunk.  This is the black magic
     * that serves as the stand-in for true bump-mapping.
     */
    protected Float[][] populateSlopes(final ChunkMD chunkMd, Integer vSlice, final ChunkMD.Set neighbors)
    {
        synchronized (chunkMd)
        {
            int y = 0, sliceMinY = 0, sliceMaxY = 0;
            Float[][] slopes = new Float[16][16];
            boolean ignoreWater = mapBathymetry;
            boolean isSurface = (vSlice == null);
            float slope, primarySlope, secondarySlope;
            BlockMD blockMD;

            if (isSurface)
            {
                chunkMd.surfaceSlopes = slopes;
            }
            else
            {
                chunkMd.sliceSlopes.put(vSlice, slopes);
                int[] sliceBounds = getVSliceBounds(chunkMd, vSlice);
                sliceMinY = sliceBounds[0];
                sliceMaxY = sliceBounds[1];
            }

            for (int z = 0; z < 16; z++)
            {
                for (int x = 0; x < 16; x++)
                {
                    // Get block height
                    if (isSurface)
                    {
                        y = chunkMd.getSurfaceBlockHeight(x, z, ignoreWater, true);
                    }
                    else
                    {
                        y = getSliceBlockHeight(chunkMd, x, vSlice, z, sliceMinY, sliceMaxY, ignoreWater, true);
                    }


                    // Calculate primary slope
                    primarySlope = calculateSlope(chunkMd, neighbors, primarySlopeOffsets, ignoreWater, x, y, z, isSurface, vSlice, sliceMinY, sliceMaxY);

                    // Exaggerate primary slope for normal shading
                    slope = primarySlope;
                    if (slope < 1)
                    {
                        slope *= primaryDownslopeMultiplier;
                    }
                    else if (slope > 1)
                    {
                        slope *= primaryUpslopeMultiplier;
                    }


                    // Calculate secondary slope
                    if (mapAntialiasing)
                    {
                        secondarySlope = calculateSlope(chunkMd, neighbors, secondarySlopeOffsets, ignoreWater, x, y, z, isSurface, vSlice, sliceMinY, sliceMaxY);

                        if (secondarySlope > primarySlope)
                        {
                            slope *= secondaryUpslopeMultiplier;
                        }
                        else if (secondarySlope < primarySlope)
                        {
                            slope *= secondaryDownslopeMultiplier;
                        }
                    }

                    // Set that slope.  Set it good.  Aw yeah.

                    slopes[x][z] = Math.min(slopeMax, Math.max(slopeMin, slope));
                }
            }

            return slopes;
        }
    }

    /**
     * Get block height within slice.  Should lazy-populate sliceHeights.
     */
    protected int getSliceBlockHeight(final ChunkMD chunkMd, final int x, final Integer vSlice, final int z, final int sliceMinY, final int sliceMaxY, boolean ignoreWater, boolean ignoreNoShadowBlocks)
    {
        throw new NotImplementedException();
    }

    /**
     * Get the height of the block at the coordinates + offsets.  Uses chunkMd.slopes.
     */
    protected int getSliceBlockHeight(final ChunkMD chunkMd, final int x, final Integer vSlice, final int z, final int sliceMinY, final int sliceMaxY, boolean ignoreWater, boolean ignoreNoShadowBlocks, final int offsetX, int offsetZ, ChunkMD.Set neighbors, int defaultVal)
    {
        ChunkMD chunk = null;
        int blockX = ((chunkMd.coord.chunkXPos << 4) + x + offsetX);
        int blockZ = ((chunkMd.coord.chunkZPos << 4) + z + offsetZ);
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;

        if (chunkX == chunkMd.coord.chunkXPos && chunkZ == chunkMd.coord.chunkZPos)
        {
            chunk = chunkMd;
        }
        else
        {
            ChunkCoordIntPair coord = new ChunkCoordIntPair(chunkX, chunkZ);
            chunk = neighbors.get(coord);
        }

        if (chunk != null)
        {
            return getSliceBlockHeight(chunk, blockX & 15, vSlice, blockZ & 15, sliceMinY, sliceMaxY, ignoreWater, true);
        }
        else
        {
            return defaultVal;
        }
    }

    protected float calculateSlope(final ChunkMD chunkMd, final ChunkMD.Set neighbors, final Collection<BlockCoordIntPair> offsets, boolean ignoreWater, final int x, final int y, final int z, boolean isSurface, Integer vSlice, int sliceMinY, int sliceMaxY)
    {
        // Calculate slope by dividing height by neighbors' heights
        float slopeSum = 0;
        for (BlockCoordIntPair offset : offsets)
        {
            if (isSurface)
            {
                slopeSum += ((y * 1f) / chunkMd.getSurfaceBlockHeight(x, z, offset.x, offset.z, neighbors, y, ignoreWater, true));
            }
            else
            {
                slopeSum += ((y * 1f) / getSliceBlockHeight(chunkMd, x, vSlice, z, sliceMinY, sliceMaxY, ignoreWater, true, offset.x, offset.z, neighbors, y));
            }
        }
        return slopeSum / offsets.size();
    }


    protected int[] getVSliceBounds(final ChunkMD chunkMd, final Integer vSlice)
    {
        if (vSlice == null)
        {
            return null;
        }

        final int sliceMinY = Math.max((vSlice << 4), 0);
        final int hardSliceMaxY = ((vSlice + 1) << 4) - 1;
        int sliceMaxY = Math.min(hardSliceMaxY, chunkMd.worldObj.getActualHeight());
        if (sliceMinY >= sliceMaxY)
        {
            sliceMaxY = sliceMinY + 2;
        }

        return new int[]{sliceMinY, sliceMaxY};
    }

    protected float getSlope(final ChunkMD chunkMd, final BlockMD blockMD, final ChunkMD.Set neighbors, int x, Integer vSlice, int z)
    {
        Float[][] slopes = (vSlice == null) ? chunkMd.surfaceSlopes : chunkMd.sliceSlopes.get(vSlice);

        if (slopes == null || slopes[x][z]==null)
        {
            slopes = populateSlopes(chunkMd, vSlice, neighbors);
        }

        Float slope = slopes[x][z];
        if(slope==null)
        {
            JourneyMap.getLogger().warning("No slope for " + chunkMd + " at " + x + "," + z);
            slope = 1f;
        }
        return slope;
    }


}