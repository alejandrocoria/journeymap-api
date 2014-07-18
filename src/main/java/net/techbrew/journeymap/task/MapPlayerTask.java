/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.task;

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
    private static HashSet<ChunkCoordIntPair> queuedChunks = new HashSet<ChunkCoordIntPair>();
    private static ChunkCoordinates lastPlayerPos;
    private static Boolean lastUnderground;
    private static DataCache dataCache = DataCache.instance();

    private final int maxRuntime = JourneyMap.getInstance().coreProperties.chunkPoll.get() * 3;

    private MapPlayerTask(ChunkRenderController chunkRenderController, World world, int dimension, boolean underground, Integer chunkY, ChunkMD.Set chunkStubs)
    {
        super(chunkRenderController, world, dimension, underground, chunkY, chunkStubs, false);
    }

    public static boolean queueChunk(ChunkCoordIntPair chunkCoords)
    {
        synchronized (queuedChunks)
        {
            return queuedChunks.add(chunkCoords);
        }
    }

    public static void dequeueChunk(ChunkCoordIntPair chunkCoords)
    {
        synchronized (queuedChunks)
        {
            queuedChunks.remove(chunkCoords);
        }

        dataCache.invalidateChunkMD(chunkCoords);
    }

    private static Comparator<ChunkCoordIntPair> getDistanceComparator()
    {
        return new Comparator<ChunkCoordIntPair>()
        {
            Minecraft minecraft = FMLClientHandler.instance().getClient();

            @Override
            public int compare(ChunkCoordIntPair o1, ChunkCoordIntPair o2)
            {
                if (minecraft.thePlayer == null)
                {
                    return 0;
                }
                double d1 = minecraft.thePlayer.getDistanceSq(o1.getCenterXPos(), minecraft.thePlayer.posY, o1.getCenterZPosition());
                double d2 = minecraft.thePlayer.getDistanceSq(o2.getCenterXPos(), minecraft.thePlayer.posY, o2.getCenterZPosition());
                return Double.compare(d1, d2);
            }
        };
    }

    public static Collection<BaseMapTask> createCavesBelow(ChunkRenderController chunkRenderController, EntityPlayer player, boolean alreadyUnderground)
    {
        List<BaseMapTask> tasks = new ArrayList<BaseMapTask>(2);

        if (FeatureManager.isAllowed(Feature.MapCaves))
        {
            int mapY = alreadyUnderground ? player.chunkCoordY - 1 : player.chunkCoordY;

            while (mapY > 0 && tasks.size() < 2)
            {
                tasks.add(new MapPlayerTask(chunkRenderController, player.worldObj, player.dimension, true, mapY,
                        new ChunkMD.Set(dataCache.getChunkMD(new ChunkCoordIntPair(player.chunkCoordX, player.chunkCoordZ), false))));

                mapY--;
            }
        }

        return tasks;
    }

    public static BaseMapTask create(ChunkRenderController chunkRenderController, EntityPlayer player)
    {
        final ChunkCoordinates playerPos = new ChunkCoordinates(player.chunkCoordX, player.chunkCoordY, player.chunkCoordZ);
        final boolean underground = player.worldObj.provider.hasNoSky || (DataCache.getPlayer().underground && JourneyMap.getInstance().fullMapProperties.showCaves.get());

        if (underground && !FeatureManager.isAllowed(Feature.MapCaves))
        {
            return null;
        }

        final int dimension = DataCache.getPlayer().dimension;

        if (lastUnderground == null)
        {
            lastUnderground = underground;
        }
        if (lastPlayerPos == null)
        {
            lastPlayerPos = playerPos;
        }

        boolean forceNearbyChunks = (lastUnderground == underground);

        int offset = JourneyMap.getInstance().coreProperties.chunkOffset.get();

        if (lastPlayerPos.equals(playerPos) || underground)
        {
            if (offset >= 2)
            {
                offset = offset / 2;
            }
        }

        lastPlayerPos = playerPos;
        lastUnderground = underground;

        final int side = offset + offset + 1;
        final ChunkMD.Set chunks = new ChunkMD.Set(side * 3); // *3 to avoid map growth

        // Pull queued coords with as little delay as possible
        TreeSet<ChunkCoordIntPair> queuedCoords = new TreeSet<ChunkCoordIntPair>(chunkDistanceComparator);
        synchronized (queuedChunks)
        {
            queuedCoords.addAll(queuedChunks);
            queuedChunks.clear();
        }

        // Load em if you've got em, ensure they're refreshed
        ChunkMD chunkMd = null;
        int maxChunks = 512;

        // Get queued chunks
        for (ChunkCoordIntPair coord : queuedCoords)
        {
            if (chunks.size() >= maxChunks)
            {
                JourneyMap.getLogger().warning(String.format("%s queued chunks exceeded max of %s for MapPlayerTask", queuedCoords.size(), maxChunks));
                break;
            }

            chunkMd = dataCache.getChunkMD(coord, true);
            if (chunkMd != null)
            {
                chunkMd.render = true;
                chunkMd.setCurrent(false);
                chunks.add(chunkMd);
            }
        }

        // Now add peripheral chunks
        final World world = player.worldObj;
        final Integer chunkY = underground ? lastPlayerPos.posY : null;
        final ChunkCoordIntPair min = new ChunkCoordIntPair(lastPlayerPos.posX - offset, lastPlayerPos.posZ - offset);
        final ChunkCoordIntPair max = new ChunkCoordIntPair(lastPlayerPos.posX + offset, lastPlayerPos.posZ + offset);

        boolean forceCurrent;
        int renderCount = chunks.size();
        ChunkCoordIntPair coord;
        for (int x = min.chunkXPos; x <= max.chunkXPos; x++)
        {
            for (int z = min.chunkZPos; z <= max.chunkZPos; z++)
            {
                if (chunks.size() >= maxChunks)
                {
                    JourneyMap.getLogger().warning(String.format("Combined chunks exceeded max of %s for MapPlayerTask", maxChunks));
                    break;
                }

                coord = new ChunkCoordIntPair(x, z);
                if (queuedCoords.contains(coord))
                {
                    continue; // Already queued
                }

                // Don't force to be current unless player is in it
                forceCurrent = forceNearbyChunks || ((player.chunkCoordX == x) && (player.chunkCoordZ == z));
                chunkMd = dataCache.getChunkMD(coord, forceCurrent);

                if (chunkMd != null)
                {
                    if (chunkMd.isCurrent())
                    {
                        chunkMd.render = true;
                        chunkMd.setCurrent(false);
                        chunks.add(chunkMd);
                        renderCount++;
                    }
                    else
                    {
                        chunkMd.render = false;
                        chunks.add(chunkMd);
                    }
                }
            }
        }

        //System.out.println("Queued: " + queuedCoords.size() + ", Total Chunks to render: " + renderCount);

        return new MapPlayerTask(chunkRenderController, world, dimension, underground, chunkY, chunks);
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
        final int mapTaskDelay = JourneyMap.getInstance().coreProperties.chunkPoll.get();

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
