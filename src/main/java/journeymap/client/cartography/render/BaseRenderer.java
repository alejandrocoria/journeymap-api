/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.cartography.render;


import journeymap.client.cartography.IChunkRenderer;
import journeymap.client.cartography.RGB;
import journeymap.client.cartography.Stratum;
import journeymap.client.data.DataCache;
import journeymap.client.model.BlockCoordIntPair;
import journeymap.client.model.BlockMD;
import journeymap.client.model.ChunkMD;
import journeymap.client.model.MapType;
import journeymap.client.properties.CoreProperties;
import journeymap.common.Journeymap;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Base class for methods reusable across renderers.
 *
 * @author techbrew
 */
public abstract class BaseRenderer implements IChunkRenderer
{
    public static final int COLOR_BLACK = Color.black.getRGB();
    public static final int COLOR_VOID = RGB.toInteger(17, 12, 25);
    public static volatile AtomicLong badBlockCount = new AtomicLong(0);

    protected static final float[] DEFAULT_FOG = new float[]{0, 0, .1f};
    protected final DataCache dataCache = DataCache.INSTANCE;
    protected CoreProperties coreProperties;
    // Updated in updateOptions()
    protected boolean mapBathymetry;
    protected boolean mapTransparency;
    protected boolean mapCaveLighting;
    protected boolean mapAntialiasing;
    protected boolean mapCrops;
    protected boolean mapPlants;
    protected boolean mapPlantShadows;
    protected float[] ambientColor;
    protected long lastPropFileUpdate;

    protected ArrayList<BlockCoordIntPair> primarySlopeOffsets = new ArrayList<>(3);
    protected ArrayList<BlockCoordIntPair> secondarySlopeOffsets = new ArrayList<>(4);

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

    private static final String PROP_SLOPES = "slopes";
    private static final String PROP_HEIGHTS = "heights";
    private static final String PROP_WATER_HEIGHTS = "waterHeights";

    private MapType currentMapType;

    public BaseRenderer()
    {
        updateOptions(null, null);

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
    protected boolean updateOptions(ChunkMD chunkMd, MapType mapType)
    {
        this.currentMapType = mapType;

        boolean updateNeeded = false;
        coreProperties = Journeymap.getClient().getCoreProperties();
        long lastUpdate = Journeymap.getClient().getCoreProperties().lastModified();
        if (lastUpdate == 0 || lastPropFileUpdate != lastUpdate)
        {
            updateNeeded = true;
            lastPropFileUpdate = lastUpdate;
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

        if (chunkMd != null)
        {
            Long lastChunkUpdate = (Long) chunkMd.getProperty("lastPropFileUpdate", lastPropFileUpdate);
            updateNeeded = true;
            chunkMd.resetBlockData(getCurrentMapType());
            chunkMd.setProperty("lastPropFileUpdate", lastChunkUpdate);
        }

        return updateNeeded;
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
    protected Float[][] populateSlopes(final ChunkMD chunkMd, Integer vSlice, Float[][] slopes)
    {
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
                y = getBlockHeight(chunkMd, x, vSlice, z, sliceMinY, sliceMaxY);

                // Calculate primary slope
                primarySlope = calculateSlope(chunkMd, primarySlopeOffsets, x, y, z, isSurface, vSlice, sliceMinY, sliceMaxY);

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
                    secondarySlope = calculateSlope(chunkMd, secondarySlopeOffsets, x, y, z, isSurface, vSlice, sliceMinY, sliceMaxY);

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
    
    protected MapType getCurrentMapType()
    {
        return this.currentMapType;
    }

    public abstract int getBlockHeight(final ChunkMD chunkMd, BlockPos blockPos);

    /**
     * Get block height on surface or within slice.  Should lazy-populate sliceHeights. Can return null.
     */
    protected abstract Integer getBlockHeight(final ChunkMD chunkMd, final int x, final Integer vSlice, final int z, final Integer sliceMinY, final Integer sliceMaxY);

    /**
     * Get the height of the block at the coordinates + offsets.
     */
    protected int getOffsetBlockHeight(final ChunkMD chunkMd, final int x, final Integer vSlice, final int z, final Integer sliceMinY,
                                       final Integer sliceMaxY, BlockCoordIntPair offset, int defaultVal)
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
            return getBlockHeight(targetChunkMd, blockX & 15, vSlice, blockZ & 15, sliceMinY, sliceMaxY);
        }
        else
        {
            return defaultVal;
        }
    }

    protected float calculateSlope(final ChunkMD chunkMd, final Collection<BlockCoordIntPair> offsets, final int x, final int y, final int z, boolean isSurface,
                                   Integer vSlice, int sliceMinY, int sliceMaxY)
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
            offsetHeight = getOffsetBlockHeight(chunkMd, x, vSlice, z, sliceMinY, sliceMaxY, offset, defaultHeight);
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

    protected float getSlope(final ChunkMD chunkMd, final BlockMD blockMD, int x, Integer vSlice, int z)
    {
        Float[][] slopes = getSlopes(chunkMd, vSlice);

        Float slope = slopes[x][z];
        if (slope == null)
        {
            populateSlopes(chunkMd, vSlice, slopes);
            slope = slopes[x][z];
        }

        if (slope == null || slope.isNaN())
        {
            Journeymap.getLogger().warn(String.format("Bad slope for %s at %s,%s: %s", chunkMd.getCoord(), x, z, slope));
            slope = 1f;
        }
        return slope;
    }

    protected final String getKey(String propName, Integer vSlice)
    {
        return vSlice == null ? propName : propName + vSlice;
    }

    protected final Integer[][] getHeights(ChunkMD chunkMd, Integer vSlice)
    {
        return chunkMd.getBlockDataInts(getCurrentMapType()).get(getKey(PROP_HEIGHTS, vSlice));
    }

    protected final boolean hasHeights(ChunkMD chunkMd, Integer vSlice)
    {
        return chunkMd.getBlockDataInts(getCurrentMapType()).has(getKey(PROP_HEIGHTS, vSlice));
    }

    protected final void resetHeights(ChunkMD chunkMd, Integer vSlice)
    {
        chunkMd.getBlockDataInts(getCurrentMapType()).clear(getKey(PROP_HEIGHTS, vSlice));
    }

    protected final Float[][] getSlopes(ChunkMD chunkMd, Integer vSlice)
    {
        return chunkMd.getBlockDataFloats(getCurrentMapType()).get(getKey(PROP_SLOPES, vSlice));
    }

    protected final boolean hasSlopes(ChunkMD chunkMd, Integer vSlice)
    {
        return chunkMd.getBlockDataFloats(getCurrentMapType()).has(getKey(PROP_SLOPES, vSlice));
    }

    protected final void resetSlopes(ChunkMD chunkMd, Integer vSlice)
    {
        chunkMd.getBlockDataFloats(getCurrentMapType()).clear(getKey(PROP_SLOPES, vSlice));
    }

    protected final Integer[][] getWaterHeights(ChunkMD chunkMd, Integer vSlice)
    {
        return chunkMd.getBlockDataInts(getCurrentMapType()).get(getKey(PROP_WATER_HEIGHTS, vSlice));
    }

    protected final boolean hasWaterHeights(ChunkMD chunkMd, Integer vSlice)
    {
        return chunkMd.getBlockDataInts(getCurrentMapType()).has(getKey(PROP_WATER_HEIGHTS, vSlice));
    }

    protected final void resetWaterHeights(ChunkMD chunkMd, Integer vSlice)
    {
        chunkMd.getBlockDataInts(getCurrentMapType()).clear(getKey(PROP_WATER_HEIGHTS, vSlice));
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
     * Darken the existing color.
     */
    public void paintDimOverlay(BufferedImage image, int x, int z, float alpha)
    {
        Integer color = image.getRGB(x, z);
        paintBlock(image, x, z, RGB.adjustBrightness(color, alpha));
    }

    /**
     * Darken the existing color.
     */
    public void paintDimOverlay(BufferedImage sourceImage, BufferedImage targetImage, int x, int z, float alpha)
    {
        Integer color = sourceImage.getRGB(x, z);
        paintBlock(targetImage, x, z, RGB.adjustBrightness(color, alpha));
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

}
