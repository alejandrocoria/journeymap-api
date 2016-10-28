/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.cartography.render;


import com.google.common.cache.*;
import journeymap.client.cartography.IChunkRenderer;
import journeymap.client.cartography.RGB;
import journeymap.client.cartography.Stratum;
import journeymap.client.data.DataCache;
import journeymap.client.model.BlockCoordIntPair;
import journeymap.client.model.BlockMD;
import journeymap.client.model.ChunkMD;
import journeymap.client.properties.CoreProperties;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.block.Block;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Base class for methods reusable across renderers.
 *
 * @author mwoodman
 */
public abstract class BaseRenderer implements IChunkRenderer, RemovalListener<ChunkPos, ChunkMD>
{
    public static final int COLOR_BLACK = Color.black.getRGB();
    public static final int COLOR_VOID = RGB.toInteger(17, 12, 25);
    public static volatile AtomicLong badBlockCount = new AtomicLong(0);
    public static final String PROP_WATER_HEIGHT = "waterHeight";
    protected static final float[] DEFAULT_FOG = new float[]{0, 0, .1f};
    protected final DataCache dataCache = DataCache.INSTANCE;
    protected CoreProperties coreProperties;
    protected BlockColumnPropertiesCache columnPropertiesCache = null;
    // Updated in updateOptions()
    protected boolean mapBathymetry;
    protected boolean mapTransparency;
    protected boolean mapCaveLighting;
    protected boolean mapAntialiasing;
    protected boolean mapCrops;
    protected boolean mapPlants;
    protected boolean mapPlantShadows;
    protected float[] ambientColor;

    protected ArrayList<BlockCoordIntPair> primarySlopeOffsets = new ArrayList<BlockCoordIntPair>(3);
    protected ArrayList<BlockCoordIntPair> secondarySlopeOffsets = new ArrayList<BlockCoordIntPair>(4);
    protected String cachePrefix = "";

    // Need to go in properties
    protected float shadingSlopeMin; // Range: 0-1
    protected float shadingSlopeMax; // Range: 0-?
    protected float shadingPrimaryDownslopeMultiplier; // Range: 0-1
    protected float shadingPrimaryUpslopeMultiplier; // Range: 1-?
    protected float shadingSecondaryDownslopeMultiplier; // Range: 0-1
    protected float shadingSecondaryUpslopeMultiplier; // Range: 1-?

    protected float tweakMoonlightLevel; // Range: 0 - 15
    protected float tweakBrightenDaylightDiff; // Range: 0-1
    protected float tweakBrightenLightsourceBlock; // Range: 0 - 1.5
    protected float tweakBlendShallowWater; // Range: 0 - 1
    protected float tweakMinimumDarkenNightWater; // Range: 0 - 1
    protected float tweakWaterColorBlend; // Range 0-1
    protected int tweakDarkenWaterColorMultiplier; // Range: int rg
    protected int tweakSurfaceAmbientColor; // Range: int rgb
    protected int tweakCaveAmbientColor; // Range: int rgb
    protected int tweakNetherAmbientColor; // Range: int rgb
    protected int tweakEndAmbientColor; // Range: int rgb

    public BaseRenderer()
    {
        updateOptions();

        // TODO: Put in properties
        this.shadingSlopeMin = 0.2f;
        this.shadingSlopeMax = 1.7f;
        this.shadingPrimaryDownslopeMultiplier = .65f;
        this.shadingPrimaryUpslopeMultiplier = 1.20f;
        this.shadingSecondaryDownslopeMultiplier = .95f;
        this.shadingSecondaryUpslopeMultiplier = 1.05f;

        this.tweakMoonlightLevel = 3.5f;
        this.tweakBrightenDaylightDiff = 0.06f;
        this.tweakBrightenLightsourceBlock = 1.2f;
        this.tweakBlendShallowWater = .15f;
        this.tweakMinimumDarkenNightWater = .25f;
        this.tweakWaterColorBlend = .66f;
        this.tweakDarkenWaterColorMultiplier = 0x7A90BF;
        this.tweakSurfaceAmbientColor = 0x00001A;
        this.tweakCaveAmbientColor = 0x000000;
        this.tweakNetherAmbientColor = 0x330808;
        this.tweakEndAmbientColor = 0x00001A;

        // Slope offsets
        primarySlopeOffsets.add(new BlockCoordIntPair(0, -1)); // North
        primarySlopeOffsets.add(new BlockCoordIntPair(-1, -1)); // NorthWest
        primarySlopeOffsets.add(new BlockCoordIntPair(-1, 0)); // West

        secondarySlopeOffsets.add(new BlockCoordIntPair(-1, -2)); // North of NorthWest
        secondarySlopeOffsets.add(new BlockCoordIntPair(-2, -1)); // West of NorthWest
        secondarySlopeOffsets.add(new BlockCoordIntPair(-2, -2)); // NorthWest of NorthWest
        secondarySlopeOffsets.add(new BlockCoordIntPair(-2, 0)); // SouthWest of NorthWest
        secondarySlopeOffsets.add(new BlockCoordIntPair(0, -2)); // West of West
    }

    /**
     * Ensures mapping options are up-to-date.
     */
    protected void updateOptions()
    {
        coreProperties = Journeymap.getClient().getCoreProperties();
        mapBathymetry = coreProperties.mapBathymetry.get();
        mapTransparency = coreProperties.mapTransparency.get();
        mapAntialiasing = coreProperties.mapAntialiasing.get();
        mapCaveLighting = coreProperties.mapCaveLighting.get();
        mapPlants = coreProperties.mapPlants.get();
        mapPlantShadows = coreProperties.mapPlantShadows.get();
        mapCrops = coreProperties.mapCrops.get();

        // Subclasses should override
        this.ambientColor = new float[]{0, 0, 0};
    }

    @Override
    public float[] getAmbientColor()
    {
        return DEFAULT_FOG;
    }

    @Override
    public void setStratumColors(Stratum stratum, int lightAttenuation, Integer waterColor, boolean waterAbove, boolean underground, boolean mapCaveLighting)
    {
        if (stratum.isUninitialized())
        {
            throw new IllegalStateException("Stratum wasn't initialized for setStratumColors");
        }

        // Daylight is the greater of sun light (15) attenuated through the stack and the stratum's inherent light level
        float dayAmbient = 15;
        float daylightDiff;
        float nightLightDiff;
        boolean noSky = stratum.getWorldHasNoSky();
        if (noSky)
        {
            dayAmbient = stratum.getWorldAmbientLight();
            daylightDiff = Math.max(1, Math.max(stratum.getLightLevel(), dayAmbient - lightAttenuation)) / 15f;
            nightLightDiff = daylightDiff;
        }
        else
        {
            daylightDiff = Math.max(1, Math.max(stratum.getLightLevel(), dayAmbient - lightAttenuation)) / 15f;
            daylightDiff += tweakBrightenDaylightDiff;
            // Nightlight is the greater of moon light (4) attenuated through the stack and the stratum's inherent light level
            nightLightDiff = Math.max(tweakMoonlightLevel, Math.max(stratum.getLightLevel(), tweakMoonlightLevel - lightAttenuation)) / 15f;
        }

        int basicColor;
        if (stratum.isWater())
        {
            basicColor = waterColor;
        }
        else
        {
            ChunkMD chunkMD = stratum.getChunkMd();
            basicColor = stratum.getBlockMD().getColor(chunkMD, stratum.getBlockPos());
        }

        Block block = stratum.getBlockMD().getBlockState().getBlock();
        if (block == Blocks.GLOWSTONE || block == Blocks.LIT_REDSTONE_LAMP)
        {
            basicColor = RGB.adjustBrightness(basicColor, tweakBrightenLightsourceBlock); // magic #
        }

        if ((waterAbove) && waterColor != null)
        {
            // Blend day color with watercolor above, adjustBrightness for daylight filtered down
            int adjustedWaterColor = RGB.multiply(waterColor, tweakDarkenWaterColorMultiplier);
            int adjustedBasicColor = RGB.adjustBrightness(basicColor, Math.max(daylightDiff, nightLightDiff));
            stratum.setDayColor(RGB.blendWith(adjustedBasicColor, adjustedWaterColor, tweakWaterColorBlend));

            if (noSky)
            {
                stratum.setNightColor(stratum.getDayColor());
            }
            else
            {
                // Darken for night light and blend with watercolor above
                stratum.setNightColor(RGB.adjustBrightness(stratum.getDayColor(), Math.max(nightLightDiff, tweakMinimumDarkenNightWater)));
            }
        }
        else
        {
            stratum.setDayColor(RGB.adjustBrightness(basicColor, daylightDiff));
            if (noSky)
            {
                stratum.setNightColor(stratum.getDayColor());
            }
            else
            {
                // Just adjustBrightness based on light levels
                stratum.setNightColor(RGB.darkenAmbient(basicColor, nightLightDiff, getAmbientColor()));
            }
        }

        if (underground)
        {
            stratum.setCaveColor(mapCaveLighting ? stratum.getNightColor() : stratum.getDayColor());
        }
    }


    /**
     * Initialize surface slopes in chunk.  This is the black magic
     * that serves as the stand-in for true bump-mapping.
     */
    protected Float[][] populateSlopes(final ChunkMD chunkMd, Integer vSlice, HeightsCache chunkHeights, SlopesCache chunkSlopes)
    {

        Float[][] slopes = chunkSlopes.getUnchecked(chunkMd.getCoord());
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

                // Calculate primary slope
                primarySlope = calculateSlope(chunkMd, primarySlopeOffsets, x, y, z, isSurface, vSlice, sliceMinY, sliceMaxY, chunkHeights);

                // Exaggerate primary slope for normal shading
                slope = primarySlope;
                if (slope < 1)
                {
                    slope *= shadingPrimaryDownslopeMultiplier;
                }
                else if (slope > 1)
                {
                    slope *= shadingPrimaryUpslopeMultiplier;
                }

                // Calculate secondary slope
                if (mapAntialiasing && primarySlope == 1f)
                {
                    secondarySlope = calculateSlope(chunkMd, secondarySlopeOffsets, x, y, z, isSurface, vSlice, sliceMinY, sliceMaxY, chunkHeights);

                    if (secondarySlope > primarySlope)
                    {
                        slope *= shadingSecondaryUpslopeMultiplier;
                    }
                    else if (secondarySlope < primarySlope)
                    {
                        slope *= shadingSecondaryDownslopeMultiplier;
                    }
                }

                // Set that slope.  Set it good.  Aw yeah.
                if (slope.isNaN())
                {
                    slope = 1f;
                }

                slopes[x][z] = Math.min(shadingSlopeMax, Math.max(shadingSlopeMin, slope));
            }
        }

        return slopes;

    }

    public abstract int getBlockHeight(final ChunkMD chunkMd, BlockPos blockPos);

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
        final int blockX = (chunkMd.getCoord().chunkXPos << 4) + (x + offset.x);
        final int blockZ = (chunkMd.getCoord().chunkZPos << 4) + (z + offset.z);
        final ChunkPos targetCoord = new ChunkPos(blockX >> 4, blockZ >> 4);
        ChunkMD targetChunkMd = null;

        if (targetCoord.equals(chunkMd.getCoord()))
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
        if (y <= 0)
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
        Float slope = slopeSum / offsets.size();
        if (slope.isNaN())
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
        int sliceMaxY = Math.min(hardSliceMaxY, chunkMd.getWorld().getActualHeight());
        if (sliceMinY >= sliceMaxY)
        {
            sliceMaxY = sliceMinY + 2;
        }

        return new int[]{sliceMinY, sliceMaxY};
    }

    protected float getSlope(final ChunkMD chunkMd, final BlockMD blockMD, int x, Integer vSlice, int z, HeightsCache chunkHeights, SlopesCache chunkSlopes)
    {
        Float[][] slopes = chunkSlopes.getIfPresent(chunkMd.getCoord());

        if (slopes == null || slopes[x][z] == null)
        {
            slopes = populateSlopes(chunkMd, vSlice, chunkHeights, chunkSlopes);
        }

        Float slope = slopes[x][z];
        if (slope == null || slope.isNaN())
        {
            Journeymap.getLogger().warn(String.format("Bad slope for %s at %s,%s: %s", chunkMd.getCoord(), x, z, slope));
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
     * Added because getHeight() sometimes returns an air block.
     * Returns the value in the height map at this x, z coordinate in the chunk, disregarding
     * blocks that shouldn't be used as the top block.
     */
    public Integer getSurfaceBlockHeight(final ChunkMD chunkMd, int localX, int localZ, final HeightsCache chunkHeights)
    {
        Integer[][] heights = chunkHeights.getUnchecked(chunkMd.getCoord());
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
                        setColumnProperty(PROP_WATER_HEIGHT, y, chunkMd, localX, localZ);
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

                    if (!mapPlantShadows || !blockMD.hasFlag(BlockMD.Flag.NoShadow))
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

                    if (!mapPlantShadows || !blockMD.hasFlag(BlockMD.Flag.NoShadow))
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
     * Get the col property at the coordinates + offsets.
     * Returns null if target chunk not loaded.
     */
    protected <V extends Serializable> V getColumnProperty(String name, V defaultValue, ChunkMD chunkMd, int x, int z, BlockCoordIntPair offset)
    {
        final int blockX = (chunkMd.getCoord().chunkXPos << 4) + (x + offset.x);
        final int blockZ = (chunkMd.getCoord().chunkZPos << 4) + (z + offset.z);
        final ChunkPos targetCoord = new ChunkPos(blockX >> 4, blockZ >> 4);
        ChunkMD targetChunkMd = null;

        if (targetCoord.equals(chunkMd.getCoord()))
        {
            targetChunkMd = chunkMd;
        }
        else
        {
            targetChunkMd = dataCache.getChunkMD(targetCoord);
        }

        if (targetChunkMd != null)
        {
            return getColumnProperty(name, defaultValue, targetChunkMd, x, z);
        }
        else
        {
            return null;
        }
    }

    /**
     * Get chunk using coord offsets.
     * @return null if chunk not in memory
     */
    public ChunkMD getOffsetChunk(final ChunkMD chunkMd, int x, int z, BlockCoordIntPair offset)
    {
        final int blockX = (chunkMd.getCoord().chunkXPos << 4) + (x + offset.x);
        final int blockZ = (chunkMd.getCoord().chunkZPos << 4) + (z + offset.z);
        final ChunkPos targetCoord = new ChunkPos(blockX >> 4, blockZ >> 4);
        if (targetCoord.equals(chunkMd.getCoord()))
        {
            return chunkMd;
        }
        else
        {
           return dataCache.getChunkMD(targetCoord);
        }
    }

    /**
     * Get the height of the block at the coordinates + offsets.  Uses chunkMd.slopes.
     */
    public int getSurfaceBlockHeight(final ChunkMD chunkMd, int x, int z, BlockCoordIntPair offset, int defaultVal,
                                     final HeightsCache chunkHeights)
    {
        ChunkMD targetChunkMd = getOffsetChunk(chunkMd, x, z, offset);
        final int newX = ((chunkMd.getCoord().chunkXPos << 4) + (x + offset.x)) & 15;
        final int newZ = ((chunkMd.getCoord().chunkZPos << 4) + (z + offset.z)) & 15;

        if (targetChunkMd != null)
        {
            Integer height = getSurfaceBlockHeight(targetChunkMd, newX, newZ, chunkHeights);
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
     * Get a CacheBuilder with common configuration already set.
     *
     * @return
     */
    private CacheBuilder<Object, Object> getCacheBuilder()
    {
        CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();
        if (Journeymap.getClient().getCoreProperties().recordCacheStats.get())
        {
            builder.recordStats();
        }
        return builder;
    }

    protected void setColumnProperty(String name, Serializable value, ChunkMD chunkMD, int x, int z)
    {
        getColumnProperties(chunkMD, x, z, true).put(name, value);
    }

    protected <V extends Serializable> V getColumnProperty(String name, V defaultValue, ChunkMD chunkMD, int x, int z)
    {
        HashMap<String, Serializable> props = getColumnProperties(chunkMD, x, z);
        V value = null;
        if (props != null)
        {
            value = (V) props.get(name);
        }
        return (value != null) ? value : defaultValue;
    }

    protected HashMap<String, Serializable> getColumnProperties(ChunkMD chunkMD, int x, int z)
    {
        return getColumnProperties(chunkMD, x, z, false);
    }

    protected HashMap<String, Serializable> getColumnProperties(ChunkMD chunkMD, int x, int z, boolean forceCreation)
    {
        try
        {
            HashMap<String, Serializable>[][] propArr = columnPropertiesCache.get(chunkMD.getCoord());
            HashMap<String, Serializable> props = propArr[x][z];
            if (props == null && forceCreation)
            {
                props = new HashMap<String, Serializable>();
                propArr[x][z] = props;
            }
            return props;
        }
        catch (Exception e)
        {
            Journeymap.getLogger().warn("Error in getColumnProperties(): " + e.getMessage());
            return null;
        }
    }

    /**
     * Darken the existing color.
     */
    public void paintDimOverlay(BufferedImage image, int x, int z, float alpha)
    {
        Integer color = image.getRGB(x, z);
        paintBlock(image, x, z, RGB.adjustBrightness(color, alpha));
    }

    /**
     * Paint the block.
     */
    public void paintBlock(BufferedImage image, final int x, final int z, final int color)
    {
        // Use alpha mask since color is rgb, but bufferedimage is argb
        image.setRGB(x, z, 0xFF000000 | color);
    }

    /**
     * Paint the void.
     */
    public void paintVoidBlock(BufferedImage image, final int x, final int z)
    {
        paintBlock(image, x, z, COLOR_VOID);
    }

    /**
     * Paint the void.
     */
    public void paintBlackBlock(BufferedImage image, final int x, final int z)
    {
        paintBlock(image, x, z, COLOR_BLACK);
    }

    /**
     * It's a problem
     */
    public void paintBadBlock(BufferedImage image, final int x, final int y, final int z)
    {
        long count = badBlockCount.incrementAndGet();
        if (count == 1 || count % 10240 == 0)
        {
            Journeymap.getLogger().warn(
                    "Bad block at " + x + "," + y + "," + z + ". Total bad blocks: " + count
            );
        }
    }

    /**
     * Cache for storing block heights in a 2-dimensional array, keyed to chunk coordinates.
     */
    public class HeightsCache extends ForwardingLoadingCache<ChunkPos, Integer[][]>
    {
        final LoadingCache<ChunkPos, Integer[][]> internal;

        protected HeightsCache(String name)
        {
            this.internal = getCacheBuilder().build(new CacheLoader<ChunkPos, Integer[][]>()
            {
                @Override
                public Integer[][] load(ChunkPos key) throws Exception
                {
                    //JourneyMap.getLogger().info("Initialized heights for chunk " + key);
                    return new Integer[16][16];
                }
            });
            DataCache.INSTANCE.addPrivateCache(name, this);
        }

        @Override
        protected LoadingCache<ChunkPos, Integer[][]> delegate()
        {
            return internal;
        }
    }

    /**
     * Cache for storing block slopes in a 2-dimensional array, keyed to chunk coordinates.
     */
    public class SlopesCache extends ForwardingLoadingCache<ChunkPos, Float[][]>
    {
        final LoadingCache<ChunkPos, Float[][]> internal;

        protected SlopesCache(String name)
        {
            this.internal = getCacheBuilder().build(new CacheLoader<ChunkPos, Float[][]>()
            {
                @Override
                public Float[][] load(ChunkPos key) throws Exception
                {
                    //JourneyMap.getLogger().info("Initialized slopes for chunk " + key);
                    return new Float[16][16];
                }
            });
            DataCache.INSTANCE.addPrivateCache(name, this);
        }

        @Override
        protected LoadingCache<ChunkPos, Float[][]> delegate()
        {
            return internal;
        }
    }

    /**
     * Cache for storing block properties in a 2-dimensional array, keyed to chunk coordinates.
     */
    public class BlockColumnPropertiesCache extends ForwardingLoadingCache<ChunkPos, HashMap<String, Serializable>[][]>
    {
        final LoadingCache<ChunkPos, HashMap<String, Serializable>[][]> internal;

        protected BlockColumnPropertiesCache(String name)
        {
            this.internal = getCacheBuilder().build(new CacheLoader<ChunkPos, HashMap<String, Serializable>[][]>()
            {
                @Override
                public HashMap<String, Serializable>[][] load(ChunkPos key) throws Exception
                {
                    //JourneyMap.getLogger().info("Initialized column properties for chunk " + key);
                    return new HashMap[16][16];
                }
            });
            DataCache.INSTANCE.addPrivateCache(name, this);
        }

        @Override
        protected LoadingCache<ChunkPos, HashMap<String, Serializable>[][]> delegate()
        {
            return internal;
        }
    }

}
