/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.io.nbt;

import net.minecraft.client.Minecraft;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.techbrew.journeymap.Constants.MapType;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.io.FileHandler;
import net.techbrew.journeymap.io.RegionImageHandler;
import net.techbrew.journeymap.model.ChunkCoord;
import net.techbrew.journeymap.model.RegionCoord;
import net.techbrew.journeymap.model.RegionImageCache;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegionLoader
{

    private static final Pattern anvilPattern = Pattern.compile("r\\.([^\\.]+)\\.([^\\.]+)\\.mca");

    final Logger logger = JourneyMap.getLogger();

    final MapType mapType;
    final Integer vSlice;
    final Stack<RegionCoord> regions;
    final int regionsFound;

    public RegionLoader(final Minecraft minecraft, final int dimension, final MapType mapType, final Integer vSlice, boolean all) throws IOException
    {
        this.mapType = mapType;
        this.vSlice = vSlice;
        if (mapType == MapType.underground && (vSlice == null || vSlice == -1))
        {
            throw new IllegalArgumentException("Underground map requires vSlice");
        }
        this.regions = findRegions(minecraft, vSlice, dimension, all);
        this.regionsFound = regions.size();
    }

    public Iterator<RegionCoord> regionIterator()
    {
        return regions.iterator();
    }

    public Stack<RegionCoord> getRegions()
    {
        return regions;
    }

    public int getRegionsFound()
    {
        return regionsFound;
    }

    public boolean isUnderground()
    {
        return mapType == MapType.underground;
    }

    public Integer getVSlice()
    {
        return vSlice;
    }

    Stack<RegionCoord> findRegions(final Minecraft mc, final Integer vSlice, final int dimension, boolean all)
    {

        final File mcWorldDir = FileHandler.getMCWorldDir(mc, dimension);
        final File regionDir = new File(mcWorldDir, "region");
        if (!regionDir.exists() || regionDir.isFile())
        {
            logger.warn("MC world region directory doesn't exist: " + regionDir);
            return null;
        }

        final File jmImageWorldDir = FileHandler.getJMWorldDir(mc);
        final RegionImageHandler rfh = RegionImageHandler.getInstance();
        final Stack<RegionCoord> stack = new Stack<RegionCoord>();

        RegionImageCache.getInstance().clear();

        AnvilChunkLoader anvilChunkLoader = ChunkLoader.getAnvilChunkLoader(mc);

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
                    RegionCoord rc = new RegionCoord(jmImageWorldDir, Integer.parseInt(x), vSlice, Integer.parseInt(z), dimension);
                    if (all)
                    {
                        stack.add(rc);
                    }
                    else
                    {
                        if (!RegionImageHandler.getRegionImageFile(rc, mapType, false).exists())
                        {
                            List<ChunkCoordIntPair> chunkCoords = rc.getChunkCoordsInRegion();
                            for (ChunkCoordIntPair coord : chunkCoords)
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
        ChunkCoord cc = ChunkCoord.fromChunkPos(jmImageWorldDir, mc.thePlayer.chunkCoordX, vSlice, mc.thePlayer.chunkCoordZ, dimension);
        final RegionCoord playerRc = cc.getRegionCoord();
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

}
