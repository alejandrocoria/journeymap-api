/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.io.nbt;

import journeymap.client.io.FileHandler;
import journeymap.client.io.RegionImageHandler;
import journeymap.client.model.MapType;
import journeymap.client.model.RegionCoord;
import journeymap.client.model.RegionImageCache;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The type Region loader.
 */
public class RegionLoader
{
    private static final Pattern anvilPattern = Pattern.compile("r\\.([^\\.]+)\\.([^\\.]+)\\.mca");

    /**
     * The Logger.
     */
    final Logger logger = Journeymap.getLogger();

    /**
     * The Map type.
     */
    final MapType mapType;
    /**
     * The Regions.
     */
    final Stack<RegionCoord> regions;
    /**
     * The Regions found.
     */
    final int regionsFound;

    /**
     * Instantiates a new Region loader.
     *
     * @param minecraft the minecraft
     * @param mapType   the map type
     * @param all       the all
     * @throws IOException the io exception
     */
    public RegionLoader(final Minecraft minecraft, final MapType mapType, boolean all) throws IOException
    {
        this.mapType = mapType;
        this.regions = findRegions(minecraft, mapType, all);
        this.regionsFound = regions.size();
    }

    /**
     * Gets region file.
     *
     * @param minecraft the minecraft
     * @param dimension the dimension
     * @param chunkX    the chunk x
     * @param chunkZ    the chunk z
     * @return the region file
     */
    public static File getRegionFile(Minecraft minecraft, int dimension, int chunkX, int chunkZ)
    {
        File regionDir = new File(FileHandler.getWorldSaveDir(minecraft), "region");
        File regionFile = new File(regionDir, String.format("r.%s.%s.mca", (chunkX >> 5), (chunkZ >> 5)));
        return regionFile;
    }

    /**
     * Gets region file.
     *
     * @param minecraft the minecraft
     * @param chunkX    the chunk x
     * @param chunkZ    the chunk z
     * @return the region file
     */
    public static File getRegionFile(Minecraft minecraft, int chunkX, int chunkZ)
    {
        File regionDir = new File(FileHandler.getWorldSaveDir(minecraft), "region");
        File regionFile = new File(regionDir, String.format("r.%s.%s.mca", (chunkX >> 5), (chunkZ >> 5)));
        return regionFile;
    }

    /**
     * Region iterator iterator.
     *
     * @return the iterator
     */
    public Iterator<RegionCoord> regionIterator()
    {
        return regions.iterator();
    }

    /**
     * Gets regions.
     *
     * @return the regions
     */
    public Stack<RegionCoord> getRegions()
    {
        return regions;
    }

    /**
     * Gets regions found.
     *
     * @return the regions found
     */
    public int getRegionsFound()
    {
        return regionsFound;
    }

    /**
     * Is underground boolean.
     *
     * @return the boolean
     */
    public boolean isUnderground()
    {
        return mapType.isUnderground();
    }

    /**
     * Gets v slice.
     *
     * @return the v slice
     */
    public Integer getVSlice()
    {
        return mapType.vSlice;
    }

    /**
     * Find regions stack.
     *
     * @param mc      the mc
     * @param mapType the map type
     * @param all     the all
     * @return the stack
     */
    Stack<RegionCoord> findRegions(final Minecraft mc, final MapType mapType, boolean all)
    {

        final File mcWorldDir = FileHandler.getMCWorldDir(mc, mapType.dimension);
        final File regionDir = new File(mcWorldDir, "region");
        if (!regionDir.exists() && !regionDir.mkdirs())
        {
            logger.warn("MC world region directory isn't usable: " + regionDir);
            return new Stack<RegionCoord>();
        }

        // Flush synchronously so it's done before clearing
        RegionImageCache.INSTANCE.flushToDisk(false);
        RegionImageCache.INSTANCE.clear();

        final File jmImageWorldDir = FileHandler.getJMWorldDir(mc);
        final Stack<RegionCoord> stack = new Stack<RegionCoord>();

        AnvilChunkLoader anvilChunkLoader = new AnvilChunkLoader(FileHandler.getWorldSaveDir(mc), DataFixesManager.createFixer());

        int validFileCount = 0;
        int existingImageCount = 0;
        final File[] anvilFiles = regionDir.listFiles();
        for (File anvilFile : anvilFiles)
        {
            Matcher matcher = anvilPattern.matcher(anvilFile.getName());
            if (!anvilFile.isDirectory() && matcher.matches())
            {
                validFileCount++;
                String x = matcher.group(1);
                String z = matcher.group(2);
                if (x != null && z != null)
                {
                    RegionCoord rc = new RegionCoord(jmImageWorldDir, Integer.parseInt(x), Integer.parseInt(z), mapType.dimension);
                    if (all)
                    {
                        stack.add(rc);
                    }
                    else
                    {
                        if (!RegionImageHandler.getRegionImageFile(rc, mapType, false).exists())
                        {
                            List<ChunkPos> chunkCoords = rc.getChunkCoordsInRegion();
                            for (ChunkPos coord : chunkCoords)
                            {
                                if (anvilChunkLoader.chunkExists(mc.theWorld, coord.chunkXPos, coord.chunkZPos))
                                {
                                    stack.add(rc);
                                    break;
                                }
                            }
                        }
                        else
                        {
                            existingImageCount++;
                        }
                    }
                }
            }
        }
        if (stack.isEmpty() && (validFileCount != existingImageCount))
        {
            logger.warn("Anvil region files in " + regionDir + ": " + validFileCount + ", matching image files: " + existingImageCount + ", but found nothing to do for mapType " + mapType);
        }

        // Add player's current region
        final RegionCoord playerRc = RegionCoord.fromChunkPos(jmImageWorldDir, mapType, mc.thePlayer.chunkCoordX, mc.thePlayer.chunkCoordZ);
        if (stack.contains(playerRc))
        {
            stack.remove(playerRc);
        }

        Collections.sort(stack, new Comparator<RegionCoord>()
        {
            @Override
            public int compare(RegionCoord o1, RegionCoord o2)
            {
                Float d1 = distanceToPlayer(o1);
                Float d2 = distanceToPlayer(o2);
                int comp = d2.compareTo(d1);
                if (comp == 0)
                {
                    return o2.compareTo(o1);
                }
                return comp;
            }

            float distanceToPlayer(RegionCoord rc)
            {
                float x = rc.regionX - playerRc.regionX;
                float z = rc.regionZ - playerRc.regionZ;
                return (x * x) + (z * z);
            }

        });
        stack.add(playerRc);
        return stack;
    }

    /**
     * Gets map type.
     *
     * @return the map type
     */
    public MapType getMapType()
    {
        return mapType;
    }
}
