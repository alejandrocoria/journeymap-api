/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.cartography.render;


import com.google.common.base.Optional;
import com.google.common.cache.*;
import net.minecraft.block.Block;
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
public abstract class BaseRenderer implements IChunkRenderer, RemovalListener<ChunkCoordIntPair, Optional<ChunkMD>>
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

        // TODO: Move back to constructor
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
    protected Float[][] populateSlopes(final ChunkMD chunkMd, Integer vSlice,
                                       final HeightsCache chunkHeights,
                                       final SlopesCache chunkSlopes)
    {

        Float[][] slopes = chunkSlopes.getUnchecked(chunkMd.coord);
        int y = 0, sliceMinY = 0, sliceMaxY = 0;
        boolean isSurface = (vSlice == null);
        Float slope, primarySlope, secondarySlope;
        BlockMD blockMD;

        if (!isSurface)
        {
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
                    y = getSurfaceBlockHeight(chunkMd, x, z, chunkHeights);
                }
                else
                {
                    y = getSliceBlockHeight(chunkMd, x, vSlice, z, sliceMinY, sliceMaxY, chunkHeights);
                }

                if(Blocks.glowstone==DataCache.instance().getBlockMD(chunkMd, x, y, z).getBlock())
                {
                    slope = 0f;
                }

                // Calculate primary slope
                primarySlope = calculateSlope(chunkMd, primarySlopeOffsets, x, y, z, isSurface, vSlice, sliceMinY, sliceMaxY, chunkHeights);

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
                    secondarySlope = calculateSlope(chunkMd, secondarySlopeOffsets, x, y, z, isSurface, vSlice, sliceMinY, sliceMaxY, chunkHeights);

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
                if(slope.isNaN())
                {
                    slope = 1f;
                }

                slopes[x][z] = Math.min(slopeMax, Math.max(slopeMin, slope));
            }
        }

        return slopes;

    }

    /**
     * Get block height within slice.  Should lazy-populate sliceHeights. Can return null.
     */
    protected Integer getSliceBlockHeight(final ChunkMD chunkMd, final int x, final Integer vSlice, final int z, final int sliceMinY, final int sliceMaxY,
                                      final HeightsCache chunkHeights)
    {
        throw new NotImplementedException();
    }

    /**
     * Get the height of the block at the coordinates + offsets.  Uses chunkMd.slopes.
     */
    protected int getSliceBlockHeight(final ChunkMD chunkMd, final int x, final Integer vSlice, final int z, final int sliceMinY,
                                      final int sliceMaxY, BlockCoordIntPair offset, int defaultVal,
                                      final HeightsCache chunkHeights)
    {
        final int blockX = (chunkMd.coord.chunkXPos << 4) + (x + offset.x);
        final int blockZ = (chunkMd.coord.chunkZPos << 4) + (z + offset.z);
        final ChunkCoordIntPair targetCoord = new ChunkCoordIntPair(blockX >> 4, blockZ >> 4);
        ChunkMD targetChunkMd = null;

        if(targetCoord.equals(chunkMd.coord))
        {
            targetChunkMd = chunkMd;
        }
        else
        {
            targetChunkMd = dataCache.getChunkMD(targetCoord);
        }

        if (targetChunkMd != null)
        {
            return getSliceBlockHeight(targetChunkMd, blockX & 15, vSlice, blockZ & 15, sliceMinY, sliceMaxY, chunkHeights);
        }
        else
        {
            return defaultVal;
        }
    }

    protected float calculateSlope(final ChunkMD chunkMd, final Collection<BlockCoordIntPair> offsets, final int x, final int y, final int z, boolean isSurface,
                                   Integer vSlice, int sliceMinY, int sliceMaxY,
                                   final HeightsCache chunkHeights)
    {
        if(y<=0)
        {
            // Flat
            return 1f;
        }

        // Calculate slope by dividing height by neighbors' heights
        float slopeSum = 0;
        int defaultHeight = y;

        float offsetHeight;
        for (BlockCoordIntPair offset : offsets)
        {
            if (isSurface)
            {
                offsetHeight = getSurfaceBlockHeight(chunkMd, x, z, offset, defaultHeight, chunkHeights);
            }
            else
            {
                offsetHeight = getSliceBlockHeight(chunkMd, x, vSlice, z, sliceMinY, sliceMaxY, offset, defaultHeight, chunkHeights);
            }
            slopeSum += ((y * 1f) / offsetHeight);
        }
        Float slope =  slopeSum / offsets.size();
        if(slope.isNaN())
        {
            slope = 1f;
        }

        return slope;
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

    protected float getSlope(final ChunkMD chunkMd, final BlockMD blockMD, int x, Integer vSlice, int z,
                             final HeightsCache chunkHeights,
                             final SlopesCache chunkSlopes)
    {
        Float[][] slopes = chunkSlopes.getIfPresent(chunkMd.coord);

        if (slopes == null || slopes[x][z]==null)
        {
            slopes = populateSlopes(chunkMd, vSlice, chunkHeights, chunkSlopes);
        }

        Float slope = slopes[x][z];
        if(slope==null || slope.isNaN())
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
    public Integer getSurfaceBlockHeight(final ChunkMD chunkMd, int x, int z, final HeightsCache chunkHeights)
    {
        Integer[][] heights = chunkHeights.getUnchecked(chunkMd.coord);
        if(heights==null)
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

        //why is height 4 set on a chunk to the left?
        y = Math.max(0, y);

        heights[x][z] = y;

        return y;
    }


    /**
     * Get the height of the block at the coordinates + offsets.  Uses chunkMd.slopes.
     */
    public int getSurfaceBlockHeight(final ChunkMD chunkMd, int x, int z, BlockCoordIntPair offset, int defaultVal,
                                     final HeightsCache chunkHeights)
    {
        final int blockX = (chunkMd.coord.chunkXPos << 4) + (x + offset.x);
        final int blockZ = (chunkMd.coord.chunkZPos << 4) + (z + offset.z);
        final ChunkCoordIntPair targetCoord = new ChunkCoordIntPair(blockX >> 4, blockZ >> 4);
        ChunkMD targetChunkMd = null;

        if(targetCoord.equals(chunkMd.coord))
        {
            targetChunkMd = chunkMd;
        }
        else
        {
            targetChunkMd = dataCache.getChunkMD(targetCoord);
        }

        if (targetChunkMd != null)
        {
            Integer height = getSurfaceBlockHeight(targetChunkMd, blockX & 15, blockZ & 15, chunkHeights);
            if(height==null)
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
     * Get a CacheBuilder with common configuration already set.
     *
     * @return
     */
    private CacheBuilder<Object, Object> getCacheBuilder()
    {
        CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();
        if (JourneyMap.getInstance().coreProperties.recordCacheStats.get())
        {
            builder.recordStats();
        }
        return builder;
    }

    public class HeightsCache extends ForwardingLoadingCache<ChunkCoordIntPair, Integer[][]>
    {
        final LoadingCache<ChunkCoordIntPair, Integer[][]> internal;

        protected HeightsCache(String name)
        {
            this.internal = getCacheBuilder().build(new CacheLoader<ChunkCoordIntPair, Integer[][]>()
            {
                @Override
                public Integer[][] load(ChunkCoordIntPair key) throws Exception
                {
                    //JourneyMap.getLogger().info("Initialized heights for chunk " + key);
                    return new Integer[16][16];
                }
            });
            DataCache.instance().addPrivateCache(name, this);
        }

        @Override
        protected LoadingCache<ChunkCoordIntPair, Integer[][]> delegate()
        {
            return internal;
        }
    }

    public class SlopesCache extends ForwardingLoadingCache<ChunkCoordIntPair, Float[][]>
    {
        final LoadingCache<ChunkCoordIntPair, Float[][]> internal;

        protected SlopesCache(String name)
        {
            this.internal = getCacheBuilder().build(new CacheLoader<ChunkCoordIntPair, Float[][]>()
            {
                @Override
                public Float[][] load(ChunkCoordIntPair key) throws Exception
                {
                    //JourneyMap.getLogger().info("Initialized slopes for chunk " + key);
                    return new Float[16][16];
                }
            });
            DataCache.instance().addPrivateCache(name, this);
        }

        @Override
        protected LoadingCache<ChunkCoordIntPair, Float[][]> delegate()
        {
            return internal;
        }
    }
}
