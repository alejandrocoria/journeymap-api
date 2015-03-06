/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.task;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.cartography.ChunkRenderController;
import net.techbrew.journeymap.data.DataCache;
import net.techbrew.journeymap.feature.Feature;
import net.techbrew.journeymap.feature.FeatureManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MapPlayerTask extends BaseMapTask
{
    private static volatile long lastTaskCompleted;
    private static volatile int lastChunkStats;
    private static volatile long lastChunkStatsTime;
    private static volatile double lastChunkStatsAverage;
    private static volatile RenderSpec lastSurfaceRenderSpec;
    private static volatile RenderSpec lastUndergroundRenderSpec;
    private final int maxRuntime = JourneyMap.getCoreProperties().renderFrequency.get() * 3000;
    private int scheduledChunks = 0;

    private MapPlayerTask(ChunkRenderController chunkRenderController, World world, int dimension, boolean underground, Integer chunkY, Collection<ChunkCoordIntPair> chunkCoords)
    {
        super(chunkRenderController, world, dimension, underground, chunkY, chunkCoords, false, 10000);
    }

    public static void forceNearbyRemap()
    {
        synchronized (MapPlayerTask.class)
        {
            DataCache.instance().invalidateChunkMDCache();
            lastSurfaceRenderSpec = null;
            lastUndergroundRenderSpec = null;
        }
    }

    public static MapPlayerTaskBatch create(ChunkRenderController chunkRenderController, final EntityPlayer player)
    {
        final boolean cavesAllowed = FeatureManager.isAllowed(Feature.MapCaves);
        final boolean worldHasSky = !player.worldObj.provider.hasNoSky;
        boolean underground = player.worldObj.provider.hasNoSky || DataCache.getPlayer().underground;

        if (underground && !cavesAllowed)
        {
            if (worldHasSky)
            {
                underground = false;
            }
            else
            {
                return null;
            }
        }

        List<ITask> tasks = new ArrayList<ITask>(2);
        tasks.add(new MapPlayerTask(chunkRenderController, player.worldObj, player.dimension, underground, underground ? player.chunkCoordY : null, new ArrayList<ChunkCoordIntPair>()));

        if (underground)
        {
            if (worldHasSky && JourneyMap.getCoreProperties().alwaysMapSurface.get())
            {
                tasks.add(new MapPlayerTask(chunkRenderController, player.worldObj, player.dimension, false, null, new ArrayList<ChunkCoordIntPair>()));
            }
        }
        else
        {
            if (cavesAllowed && JourneyMap.getCoreProperties().alwaysMapCaves.get())
            {
                tasks.add(new MapPlayerTask(chunkRenderController, player.worldObj, player.dimension, true, player.chunkCoordY, new ArrayList<ChunkCoordIntPair>()));
            }
        }

        return new MapPlayerTaskBatch(tasks);
    }

    public static int getLastChunkStats()
    {
        return lastChunkStats;
    }

    public static long getLastChunkStatsTime()
    {
        return lastChunkStatsTime;
    }

    public static double getLastChunkStatsAvg()
    {
        return lastChunkStatsAverage;
    }

    @Override
    public void initTask(Minecraft minecraft, JourneyMap jm, File jmWorldDir, boolean threadLogging) throws InterruptedException
    {
        final RenderSpec lastRenderSpec = underground ? lastUndergroundRenderSpec : lastSurfaceRenderSpec;
        RenderSpec renderSpec = new RenderSpec(minecraft, this.underground);
        if (renderSpec.equals(lastRenderSpec))
        {
            renderSpec = lastRenderSpec;
        }
        else
        {
            if (underground)
            {
                lastUndergroundRenderSpec = renderSpec;
            }
            else
            {
                lastSurfaceRenderSpec = renderSpec;
            }
        }

        chunkCoords.addAll(renderSpec.getRenderAreaCoords());
        this.scheduledChunks = chunkCoords.size();
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
        final int mapTaskDelay = JourneyMap.getCoreProperties().renderFrequency.get() * 1000;

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
                    return MapPlayerTask.create(chunkRenderController, minecraft.thePlayer);
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

    public static class MapPlayerTaskBatch extends TaskBatch
    {
        public MapPlayerTaskBatch(List<ITask> tasks)
        {
            super(tasks);
        }

        @Override
        public void performTask(Minecraft mc, JourneyMap jm, File jmWorldDir, boolean threadLogging) throws InterruptedException
        {
            List<ITask> tasks = new ArrayList<ITask>(taskList);
            super.performTask(mc, jm, jmWorldDir, threadLogging);

            int chunkCount = 0;
            for (ITask task : tasks)
            {
                if (task instanceof MapPlayerTask)
                {
                    chunkCount += ((MapPlayerTask) task).scheduledChunks;
                }
            }
            lastChunkStats = chunkCount;
            lastChunkStatsTime = TimeUnit.NANOSECONDS.toMillis(this.elapsedNs);
            lastChunkStatsAverage = this.elapsedNs / Math.max(1, chunkCount) / 1000000D;
        }
    }

}
