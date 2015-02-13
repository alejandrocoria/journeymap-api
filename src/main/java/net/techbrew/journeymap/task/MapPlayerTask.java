/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.task;

import com.google.common.collect.TreeMultimap;
import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.cartography.ChunkRenderController;
import net.techbrew.journeymap.data.DataCache;
import net.techbrew.journeymap.feature.Feature;
import net.techbrew.journeymap.feature.FeatureManager;
import net.techbrew.journeymap.model.ChunkMD;

import java.util.*;

public class MapPlayerTask extends BaseMapTask
{
    private static volatile long lastTaskCompleted;
    private static Comparator<ChunkCoordIntPair> chunkDistanceComparator = getDistanceComparator();
    private static ChunkCoordinates lastPlayerPos;
    private static Boolean lastUnderground;
    private static DataCache dataCache = DataCache.instance();
    private static int lastChunkOffset;
    private static HashSet<ChunkCoordIntPair> reservedPeripheralCoordsSet = new HashSet<ChunkCoordIntPair>();

    private final int maxRuntime = JourneyMap.getCoreProperties().chunkPoll.get() * 3;

    private MapPlayerTask(ChunkRenderController chunkRenderController, World world, int dimension, boolean underground, Integer chunkY, Collection<ChunkCoordIntPair> chunkCoords)
    {
        super(chunkRenderController, world, dimension, underground, chunkY, chunkCoords, false, 1000);
    }

    public static void forceNearbyRemap()
    {
        synchronized (MapPlayerTask.class)
        {
            DataCache.instance().invalidateChunkMDCache();
            lastPlayerPos = null;
        }
    }

    private static Comparator<ChunkCoordIntPair> getDistanceComparator()
    {
        return new Comparator<ChunkCoordIntPair>()
        {
            Minecraft minecraft = FMLClientHandler.instance().getClient();

            @Override
            public int compare(ChunkCoordIntPair o1, ChunkCoordIntPair o2)
            {
                int comp = distanceToPlayer(minecraft, o1).compareTo(distanceToPlayer(minecraft, o2));
                if (comp == 0)
                {
                    comp = Integer.compare(o1.chunkXPos, o2.chunkXPos);
                }
                if (comp == 0)
                {
                    comp = Integer.compare(o1.chunkZPos, o2.chunkZPos);
                }
                return comp;
            }
        };
    }

    private static Comparator<ChunkCoordIntPair> getStaleComparator()
    {
        return new Comparator<ChunkCoordIntPair>()
        {
            long now = System.currentTimeMillis();

            @Override
            public int compare(ChunkCoordIntPair o1, ChunkCoordIntPair o2)
            {
                return Long.compare(getAge(o2), getAge(o1));
            }

            private Long getAge(ChunkCoordIntPair coord)
            {
                ChunkMD chunkMD = dataCache.getChunkMD(coord, false);
                if (chunkMD == null)
                {
                    return 0L;
                }
                return now - chunkMD.getLastRendered();
            }
        };
    }

    private static Float distanceToPlayer(Minecraft minecraft, ChunkCoordIntPair coord)
    {
        float x = coord.chunkXPos - minecraft.thePlayer.chunkCoordX;
        float z = coord.chunkZPos - minecraft.thePlayer.chunkCoordZ;
        return (x * x) + (z * z);
    }

    private static boolean inRange(Minecraft minecraft, ChunkCoordIntPair coord, int offset)
    {
        float x = Math.abs(minecraft.thePlayer.chunkCoordX - coord.chunkXPos);
        float z = Math.abs(minecraft.thePlayer.chunkCoordZ - coord.chunkZPos);
        return x <= offset && z <= offset;
    }

    private static int countChunksInArea(int offset)
    {
        int side = offset + 1 + offset;
        return side * side;
    }

    public static Collection<BaseMapTask> createCavesBelow(ChunkRenderController chunkRenderController, EntityPlayer player, boolean alreadyUnderground)
    {
        List<BaseMapTask> tasks = new ArrayList<BaseMapTask>(2);

        if (FeatureManager.isAllowed(Feature.MapCaves))
        {
            if (alreadyUnderground)
            {
                // above
                if (player.chunkCoordY + 1 <= (player.worldObj.provider.getActualHeight() - 1 >> 4))
                {
                    tasks.add(new MapPlayerTask(chunkRenderController, player.worldObj, player.dimension, true, player.chunkCoordY + 1,
                            new ArrayList<ChunkCoordIntPair>(Arrays.asList(new ChunkCoordIntPair(player.chunkCoordX, player.chunkCoordZ)))));
                }
            }

            // below
            if (player.chunkCoordY - 1 >= 0)
            {
                tasks.add(new MapPlayerTask(chunkRenderController, player.worldObj, player.dimension, true, player.chunkCoordY - 1,
                        new ArrayList<ChunkCoordIntPair>(Arrays.asList(new ChunkCoordIntPair(player.chunkCoordX, player.chunkCoordZ)))));
            }

            if (player.chunkCoordY - 2 >= 0)
            {
                tasks.add(new MapPlayerTask(chunkRenderController, player.worldObj, player.dimension, true, player.chunkCoordY - 2,
                        new ArrayList<ChunkCoordIntPair>(Arrays.asList(new ChunkCoordIntPair(player.chunkCoordX, player.chunkCoordZ)))));
            }
        }

        return tasks;
    }

    public static BaseMapTask create(ChunkRenderController chunkRenderController, final EntityPlayer player)
    {
        final int offset = JourneyMap.getCoreProperties().chunkOffset.get();
        final ChunkCoordinates playerPos = new ChunkCoordinates(player.chunkCoordX, player.chunkCoordY, player.chunkCoordZ);
        boolean underground = player.worldObj.provider.hasNoSky || (DataCache.getPlayer().underground && JourneyMap.getFullMapProperties().showCaves.get());

        if (underground && !FeatureManager.isAllowed(Feature.MapCaves))
        {
            if (player.worldObj.provider.hasNoSky)
            {
                return null;
            }
            underground = false;
        }

        boolean recalcPeripheralChunks = lastPlayerPos == null
                || lastPlayerPos.posX != playerPos.posX
                || lastPlayerPos.posZ != playerPos.posZ
                || lastChunkOffset != offset
                || reservedPeripheralCoordsSet.isEmpty();

        synchronized (MapPlayerTask.class)
        {
            // Update last values
            lastPlayerPos = playerPos;
            lastUnderground = underground;
            lastChunkOffset = offset;
        }

        final int maxFinalChunks = Math.max(9, countChunksInArea((int) Math.ceil(offset * 1.0 / 1.0)));
        final int reservedPeripheralChunks = Math.max(9, maxFinalChunks / 2);
        final int maxStaleChunks = (maxFinalChunks - reservedPeripheralChunks) / 2;

        if (recalcPeripheralChunks)
        {
            // Add peripheral coords around player
            List<ChunkCoordIntPair> peripheralCoords = new ArrayList<ChunkCoordIntPair>(maxFinalChunks);
            for (int x = (player.chunkCoordX - offset); x <= (player.chunkCoordX + offset); x++)
            {
                for (int z = (player.chunkCoordZ - offset); z <= (player.chunkCoordZ + offset); z++)
                {
                    ChunkCoordIntPair coord = new ChunkCoordIntPair(x, z);
                    if (dataCache.getChunkMD(coord, true) != null)
                    {
                        peripheralCoords.add(new ChunkCoordIntPair(x, z));
                    }
                }
            }

            // Sort by distance and truncate to those reserved
            Collections.sort(peripheralCoords, chunkDistanceComparator);
            reservedPeripheralCoordsSet.clear();
            reservedPeripheralCoordsSet.addAll(peripheralCoords.subList(0, Math.min(peripheralCoords.size(), reservedPeripheralChunks)));
        }

        // Get cached coords and sort by how stale they are, oldest first
        final long now = System.currentTimeMillis();
        final long minAge = JourneyMap.getCoreProperties().chunkPoll.get();
        Minecraft minecraft = FMLClientHandler.instance().getClient();

        TreeMultimap<Long, ChunkCoordIntPair> staleCoordsMM = TreeMultimap.create(new Comparator<Long>()
        {
            public int compare(Long o1, Long o2)
            {
                return o2.compareTo(o1);
            }
        }, chunkDistanceComparator);

        for (ChunkCoordIntPair coord : DataCache.instance().getCachedChunkCoordinates())
        {
            if (!reservedPeripheralCoordsSet.contains(coord))
            {
                ChunkMD chunkMD = dataCache.getChunkMD(coord, false);
                if (chunkMD != null)
                {
                    long age = now - chunkMD.getLastRendered();
                    if (age > minAge)
                    {
                        if (inRange(minecraft, coord, offset))
                        {
                            staleCoordsMM.put(age, coord);
                        }
                    }
                }
            }
        }

        // Now limit how many stale coords we'll map this round
        List<ChunkCoordIntPair> staleCoords = new ArrayList<ChunkCoordIntPair>(staleCoordsMM.values()).subList(0, Math.min(staleCoordsMM.size(), maxStaleChunks));

        List<ChunkCoordIntPair> finalList = new ArrayList<ChunkCoordIntPair>(reservedPeripheralCoordsSet);
        finalList.addAll(staleCoords);

        if (finalList.size() == 0)
        {
            return null;
        }
        else
        {
            // TODO: log fine or some kinda cool stat
            //System.out.println(String.format("Chunks to Render: %d (Peripheral: %d, Stale: %d / %d)", finalList.size(), reservedPeripheralCoordsSet.size(), staleCoords.size(), staleCoordsMM.size()));

            // TODO: Always map surface
            final Integer vSlice = underground ? lastPlayerPos.posY : null;
            return new MapPlayerTask(chunkRenderController, player.worldObj, player.dimension, underground, vSlice, finalList);
        }
    }

    protected void complete(boolean cancelled, boolean hadError)
    {
        if (!cancelled)
        {
            lastTaskCompleted = System.currentTimeMillis();
        }
    }

    @Override
    public int getMaxRuntime()
    {
        return maxRuntime;
    }

    /**
     * ITaskManager for MapPlayerTasks
     *
     * @author mwoodman
     */
    public static class Manager implements ITaskManager
    {
        final int mapTaskDelay = JourneyMap.getCoreProperties().chunkPoll.get();

        boolean enabled;

        @Override
        public Class<? extends BaseMapTask> getTaskClass()
        {
            return MapPlayerTask.class;
        }

        @Override
        public boolean enableTask(Minecraft minecraft, Object params)
        {
            enabled = true;
            return enabled;
        }

        @Override
        public boolean isEnabled(Minecraft minecraft)
        {
            return enabled;
        }

        @Override
        public void disableTask(Minecraft minecraft)
        {
            enabled = false;
        }

        @Override
        public ITask getTask(Minecraft minecraft)
        {
            // Ensure player chunk is loaded
            if (enabled && minecraft.thePlayer.addedToChunk)
            {
                if ((System.currentTimeMillis() - lastTaskCompleted) >= mapTaskDelay)
                {
                    ChunkRenderController chunkRenderController = JourneyMap.getInstance().getChunkRenderController();
                    BaseMapTask normalPlayerTask = MapPlayerTask.create(chunkRenderController, minecraft.thePlayer);

                    if (normalPlayerTask != null && FeatureManager.isAllowed(Feature.MapCaves))
                    {
                        List<ITask> tasks = new ArrayList<ITask>(3);
                        tasks.add(normalPlayerTask);
                        tasks.addAll(MapPlayerTask.createCavesBelow(chunkRenderController, minecraft.thePlayer, normalPlayerTask.underground));
                        return new TaskBatch(tasks);
                    }
                    else
                    {
                        return normalPlayerTask;
                    }
                }
            }

            return null;
        }

        @Override
        public void taskAccepted(ITask task, boolean accepted)
        {
            // nothing to do
        }

    }
}
