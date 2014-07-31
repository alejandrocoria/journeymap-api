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
    protected boolean mapCrops;
    protected boolean mapPlants;
    volatile AtomicLong badBlockCount = new AtomicLong(0);
    ArrayList<BlockCoordIntPair> primarySlopeOffsets = new ArrayList<BlockCoordIntPair>(3);
    ArrayList<BlockCoordIntPair> secondarySlopeOffsets = new ArrayList<BlockCoordIntPair>(3);

    protected float slopeMin;
    protected float slopeMax;
    protected float primaryDownslopeMultiplier;
    protected float primaryUpslopeMultiplier;
    protected float secondaryDownslopeMultiplier;
    protected float secondaryUpslopeMultiplier;

    protected float moonlightLevel = 3.5f;

    public BaseRenderer()
    {
        updateOptions();

        this.slopeMin = 0.2f;
        this.slopeMax = 1.7f;
        this.primaryDownslopeMultiplier = .65f;
        this.primaryUpslopeMultiplier = 1.20f;
        this.secondaryDownslopeMultiplier = .95f;
        this.secondaryUpslopeMultiplier = 1.05f;

        primarySlopeOffsets.clear();
        primarySlopeOffsets.add(new BlockCoordIntPair(0, -1)); // North
        primarySlopeOffsets.add(new BlockCoordIntPair(-1, -1)); // NorthWest
        primarySlopeOffsets.add(new BlockCoordIntPair(-1, 0)); // West

        secondarySlopeOffsets.clear();
        secondarySlopeOffsets.add(new BlockCoordIntPair(-1, -2)); // North of NorthWest
        secondarySlopeOffsets.add(new BlockCoordIntPair(-2, -1)); // West of NorthWest
        secondarySlopeOffsets.add(new BlockCoordIntPair(-2, -2)); // NorthWest of NorthWest
        secondarySlopeOffsets.add(new BlockCoordIntPair(-2, 0)); // SouthWest of NorthWest
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
        mapPlants = coreProperties.mapPlants.get();
        mapCrops = coreProperties.mapCrops.get();

        this.slopeMin = 0.2f;
        this.slopeMax = 1.7f;
        this.primaryDownslopeMultiplier = .65f;
        this.primaryUpslopeMultiplier = 1.20f;
        this.secondaryDownslopeMultiplier = .95f;
        this.secondaryUpslopeMultiplier = 1.05f;

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
                        y = getSurfaceBlockHeight(chunkMd, x, z);
                    }
                    else
                    {
                        y = getSliceBlockHeight(chunkMd, x, vSlice, z, sliceMinY, sliceMaxY);
                    }

                    // Calculate primary slope
                    primarySlope = calculateSlope(chunkMd, neighbors, primarySlopeOffsets, x, y, z, isSurface, vSlice, sliceMinY, sliceMaxY);

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
                    if (mapAntialiasing && primarySlope==1f )
                    {
                        secondarySlope = calculateSlope(chunkMd, neighbors, secondarySlopeOffsets, x, y, z, isSurface, vSlice, sliceMinY, sliceMaxY);

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
    protected int getSliceBlockHeight(final ChunkMD chunkMd, final int x, final Integer vSlice, final int z, final int sliceMinY, final int sliceMaxY)
    {
        throw new NotImplementedException();
    }

    /**
     * Get the height of the block at the coordinates + offsets.  Uses chunkMd.slopes.
     */
    protected int getSliceBlockHeight(final ChunkMD chunkMd, final int x, final Integer vSlice, final int z, final int sliceMinY, final int sliceMaxY, final int offsetX, int offsetZ, ChunkMD.Set neighbors, int defaultVal)
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
            return getSliceBlockHeight(chunk, blockX & 15, vSlice, blockZ & 15, sliceMinY, sliceMaxY);
        }
        else
        {
            return defaultVal;
        }
    }

    protected float calculateSlope(final ChunkMD chunkMd, final ChunkMD.Set neighbors, final Collection<BlockCoordIntPair> offsets, final int x, final int y, final int z, boolean isSurface, Integer vSlice, int sliceMinY, int sliceMaxY)
    {
        // Calculate slope by dividing height by neighbors' heights
        float slopeSum = 0;
        for (BlockCoordIntPair offset : offsets)
        {
            if (isSurface)
            {
                slopeSum += ((y * 1f) / getSurfaceBlockHeight(chunkMd, x, z, offset.x, offset.z, neighbors, y));
            }
            else
            {
                slopeSum += ((y * 1f) / getSliceBlockHeight(chunkMd, x, vSlice, z, sliceMinY, sliceMaxY, offset.x, offset.z, neighbors, y));
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

//    /**
//     * Get the height of the block at the x, z coordinate in the chunk, optionally ignoring NoShadow blocks.
//     * Ignoring NoShadow blocks won't change the saved surfaceHeights.
//     */
//    public int getSurfaceBlockHeight(final ChunkMD chunkMd, int x, int z, boolean ignoreNoShadowBlocks)
//    {
//        int y = getSurfaceBlockHeight(chunkMd, x, z, ignoreWater);
//        if(ignoreNoShadowBlocks)
//        {
//            BlockMD blockMD = dataCache.getBlockMD(chunkMd, x, y, z);
//            while (y > 0 && blockMD.hasFlag(BlockMD.Flag.NoShadow) || blockMD.isAir() || (ignoreWater && (blockMD.isWater())))
//            {
//                y--;
//                blockMD = dataCache.getBlockMD(chunkMd, x, y, z);
//            }
//        }
//        return y;
//    }

    /**
     * Added because getHeightValue() sometimes returns an air block.
     * Returns the value in the height map at this x, z coordinate in the chunk, disregarding
     * blocks that shouldn't be used as the top block.
     */
    public Integer getSurfaceBlockHeight(final ChunkMD chunkMd, int x, int z)
    {
        Integer y = chunkMd.surfaceHeights[x][z];

        if (y != null)
        {
            return chunkMd.surfaceHeights[x][z];
        }

        y = Math.max(0, chunkMd.stub.getHeightValue(x, z));

        try
        {
            BlockMD blockMD = dataCache.getBlockMD(chunkMd, x, y, z);

            while (y > 0)
            {
                if(blockMD.isWater())
                {
                    if(!mapBathymetry)
                    {
                        break;
                    }
                }
                else if(!blockMD.isAir() && !blockMD.hasFlag(BlockMD.Flag.NoShadow))
                {
                    break;
                }
                y--;
                blockMD = dataCache.getBlockMD(chunkMd, x, y, z);
            }
        }
        catch (Exception e)
        {
            JourneyMap.getLogger().warning("Couldn't get safe surface block height at " + x + "," + z + ": " + e);
        }

        if(y==null)
        {
            y = chunkMd.stub.getHeightValue(x, z);
        }

        chunkMd.surfaceHeights[x][z] = y;

        return Math.max(0, y);
    }

    /**
     * Get the height of the block at the coordinates + offsets.  Uses chunkMd.slopes.
     */
    public Float getSurfaceBlockHeight(final ChunkMD chunkMd, int x, int z, int offsetX, int offsetZ, ChunkMD.Set neighbors, float defaultVal)
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
            return (float) getSurfaceBlockHeight(chunkMd, blockX & 15, blockZ & 15);
        }
        else
        {
            return defaultVal;
        }
    }
}
