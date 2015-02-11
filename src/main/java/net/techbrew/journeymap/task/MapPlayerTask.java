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

import java.util.*;

public class MapPlayerTask extends BaseMapTask
{
    private static volatile long lastTaskCompleted;
    private static Comparator<ChunkCoordIntPair> chunkDistanceComparator = getDistanceComparator();
    private static ChunkCoordinates lastPlayerPos;
    private static Boolean lastUnderground;
    private static DataCache dataCache = DataCache.instance();

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
                int comp = distanceToPlayer(o1).compareTo(distanceToPlayer(o2));
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

            Float distanceToPlayer(ChunkCoordIntPair coord)
            {
                float x = coord.chunkXPos - minecraft.thePlayer.chunkCoordX;
                float z = coord.chunkZPos - minecraft.thePlayer.chunkCoordZ;
                return (x * x) + (z * z);
            }
        };
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

        synchronized (MapPlayerTask.class)
        {
            // Update last values
            lastPlayerPos = playerPos;
            lastUnderground = underground;
        }


        // Add peripheral coords around player
        final int offset = JourneyMap.getCoreProperties().chunkOffset.get();

        int chunkRowLength = (offset * 2) + 1;

        List<ChunkCoordIntPair> renderCoords = new ArrayList<ChunkCoordIntPair>(chunkRowLength * chunkRowLength);
        for (int x = (player.chunkCoordX - offset); x <= (player.chunkCoordX + offset); x++)
        {
            for (int z = (player.chunkCoordZ - offset); z <= (player.chunkCoordZ + offset); z++)
            {
                renderCoords.add(new ChunkCoordIntPair(x, z));
            }
        }

        Collections.sort(renderCoords, chunkDistanceComparator);

        Iterator<ChunkCoordIntPair> iter = renderCoords.iterator();
        while (iter.hasNext())
        {
            ChunkCoordIntPair coord = iter.next();
            if (dataCache.getChunkMD(coord, true) == null)
            {
                iter.remove();
            }
        }

        if (renderCoords.size() == 0)
        {
            return null;
        }

        {
            // TODO: log fine or some kinda cool stat
            System.out.println(String.format("Chunks to Render: %d (Missed: %d)", renderCoords.size(), (chunkRowLength * chunkRowLength) - renderCoords.size()));

            // TODO: Always map surface
            final Integer vSlice = underground ? lastPlayerPos.posY : null;
            return new MapPlayerTask(chunkRenderController, player.worldObj, player.dimension, underground, vSlice, renderCoords);
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
