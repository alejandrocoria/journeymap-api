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

    private MapPlayerTask(ChunkRenderController chunkRenderController, World world, int dimension, boolean underground, Integer chunkY, Collection<ChunkCoordIntPair> chunkCoords)
    {
        super(chunkRenderController, world, dimension, underground, chunkY, chunkCoords, false);
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
                ArrayList<ChunkCoordIntPair> list = new ArrayList<ChunkCoordIntPair>(1);
                list.add(new ChunkCoordIntPair(player.chunkCoordX, player.chunkCoordZ));
                tasks.add(new MapPlayerTask(chunkRenderController, player.worldObj, player.dimension, true, mapY, list));

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

        // Should we force the chunks around the player to be rendered?
        boolean forceNearbyChunks = false;
        if(lastUnderground==null || lastUnderground!=underground)
        {
            forceNearbyChunks = true;
        }
        if (lastPlayerPos == null || !playerPos.equals(lastPlayerPos))
        {
            forceNearbyChunks = true;
        }

        // Update last values
        lastPlayerPos = playerPos;
        lastUnderground = underground;

        // Pull queued coords with as little delay as possible
        TreeSet<ChunkCoordIntPair> queuedCoords = new TreeSet<ChunkCoordIntPair>(chunkDistanceComparator);
        synchronized (queuedChunks)
        {
            queuedCoords.addAll(queuedChunks);
            queuedChunks.clear();
        }

        int forced = 0;
        if(forceNearbyChunks)
        {
            // Add peripheral coords around player
            final int offset = 1;
            for (int x = (player.chunkCoordX - offset); x <= (player.chunkCoordX + offset); x++)
            {
                for (int z = (player.chunkCoordZ - offset); z <= (player.chunkCoordZ + offset); z++)
                {
                    queuedCoords.add(new ChunkCoordIntPair(x, z));
                    forced++;
                }
            }
        }
        else
        {
            // Add just player's coords
            queuedCoords.add(new ChunkCoordIntPair(player.chunkCoordX, player.chunkCoordZ));
            forced++;
        }

        // Load em if you've got em, ensure they're refreshed
        ChunkMD chunkMd = null;
        int maxChunks = 1024;

        // Get queued chunks
        int keptAlive = 0;
        int keptAliveMissed = 0;
        int keptAliveUnnecessary = 0;
        int missed = 0;
        final TreeSet<ChunkCoordIntPair> renderCoords = new TreeSet<ChunkCoordIntPair>(chunkDistanceComparator);

        for (ChunkCoordIntPair coord : queuedCoords)
        {
            if (renderCoords.size() >= maxChunks)
            {
                JourneyMap.getLogger().fine(String.format("%s queued chunks exceeded max of %s for MapPlayerTask", renderCoords.size(), maxChunks));
                break;
            }

            chunkMd = dataCache.getChunkMD(coord, true);
            if (chunkMd != null)
            {
                renderCoords.add(coord);

                // Ensure chunks north, west, nw are kept alive for slope calculations
                for(ChunkCoordIntPair keepAliveOffset : keepAliveOffsets)
                {
                    ChunkCoordIntPair keepAliveCoord = new ChunkCoordIntPair(coord.chunkXPos + keepAliveOffset.chunkXPos, coord.chunkZPos + keepAliveOffset.chunkZPos);
                    if (!renderCoords.contains(keepAliveCoord))
                    {
                        ChunkMD keepAliveChunk = dataCache.getChunkMD(keepAliveCoord, false);
                        if(keepAliveChunk!=null)
                        {
                            keptAlive++;
                        }
                        else
                        {
                            keptAliveMissed++;
                        }
                    }
                    else
                    {
                        keptAliveUnnecessary++;
                    }
                }
            }
            else
            {
                missed++;
            }
        }

        if(renderCoords.size()==0)
        {
            // TODO: Comment this out
            System.out.println("No chunks to render");
            return null;
        }
        else
        {
            // TODO: log fine or some kinda cool stat
            //System.out.println(String.format("Chunks to Render: %d (Queued: %d, Forced: %d, Missed: %d), Kept Alive: %d, Kept Alive Missed: %d, Kept Alive Unnecessary: %d", renderChunks.size(), queuedSize, forced, missed, keptAlive, keptAliveMissed, keptAliveUnnecessary));

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
