/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.task.multi;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import journeymap.client.Constants;
import journeymap.client.JourneymapClient;
import journeymap.client.cartography.ChunkRenderController;
import journeymap.client.data.DataCache;
import journeymap.client.feature.Feature;
import journeymap.client.feature.FeatureManager;
import journeymap.client.model.ChunkMD;
import journeymap.client.model.EntityDTO;
import journeymap.client.model.MapType;
import journeymap.client.properties.CoreProperties;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * The type Map player task.
 */
public class MapPlayerTask extends BaseMapTask
{
    private static int MAX_STALE_MILLISECONDS = 30000;
    private static int MAX_BATCH_SIZE = 32;
    private static DecimalFormat decFormat = new DecimalFormat("##.#");
    private static volatile long lastTaskCompleted;
    private static long lastTaskTime;
    private static double lastTaskAvgChunkTime;
    private static Cache<String, String> tempDebugLines = CacheBuilder.newBuilder()
            .maximumSize(20)
            .expireAfterWrite(1500, TimeUnit.MILLISECONDS)
            .build();

    private final int maxRuntime = Journeymap.getClient().getCoreProperties().renderDelay.get() * 3000;
    private int scheduledChunks = 0;
    private long startNs;
    private long elapsedNs;

    private MapPlayerTask(ChunkRenderController chunkRenderController, World world, MapType mapType, Collection<ChunkPos> chunkCoords)
    {
        super(chunkRenderController, world, mapType, chunkCoords, false, true, 10000);
    }

    /**
     * Force nearby remap.
     */
    public static void forceNearbyRemap()
    {
        synchronized (MapPlayerTask.class)
        {
            DataCache.INSTANCE.invalidateChunkMDCache();
        }
    }

    /**
     * Create map player task batch.
     *
     * @param chunkRenderController the chunk render controller
     * @param player                the player
     * @return the map player task batch
     */
    public static MapPlayerTaskBatch create(ChunkRenderController chunkRenderController, final EntityDTO player)
    {
        final boolean surfaceAllowed = FeatureManager.isAllowed(Feature.MapSurface);
        final boolean cavesAllowed = FeatureManager.isAllowed(Feature.MapCaves);
        if (!surfaceAllowed && !cavesAllowed)
        {
            return null;
        }

        final EntityLivingBase playerEntity = player.entityLivingRef.get();
        if (playerEntity == null)
        {
            return null;
        }
        boolean underground = player.underground;

        MapType mapType;
        if (underground)
        {
            mapType = MapType.underground(player);
        }
        else
        {
            final long time = playerEntity.world.getWorldInfo().getWorldTime() % 24000L;
            mapType = (time < 13800) ? MapType.day(player) : MapType.night(player);
        }

        List<ITask> tasks = new ArrayList<ITask>(2);
        tasks.add(new MapPlayerTask(chunkRenderController, playerEntity.world, mapType, new ArrayList<ChunkPos>()));

        if (underground)
        {
            if (surfaceAllowed && Journeymap.getClient().getCoreProperties().alwaysMapSurface.get())
            {
                tasks.add(new MapPlayerTask(chunkRenderController, playerEntity.world, MapType.day(player), new ArrayList<ChunkPos>()));
            }
        }
        else
        {
            if (cavesAllowed && Journeymap.getClient().getCoreProperties().alwaysMapCaves.get())
            {
                tasks.add(new MapPlayerTask(chunkRenderController, playerEntity.world, MapType.underground(player), new ArrayList<ChunkPos>()));
            }
        }

        if (Journeymap.getClient().getCoreProperties().mapTopography.get())
        {
            tasks.add(new MapPlayerTask(chunkRenderController, playerEntity.world, MapType.topo(player), new ArrayList<ChunkPos>()));
        }

        return new MapPlayerTaskBatch(tasks);
    }

    /**
     * Get debug stats string [ ].
     *
     * @return the string [ ]
     */
    public static String[] getDebugStats()
    {
        try
        {
            final CoreProperties coreProperties = Journeymap.getClient().getCoreProperties();
            final boolean underground = DataCache.getPlayer().underground;
            ArrayList<String> lines = new ArrayList<>(tempDebugLines.asMap().values());

            if (underground || coreProperties.alwaysMapCaves.get())
            {
                lines.add(RenderSpec.getUndergroundSpec().getDebugStats());
            }

            if (!underground || coreProperties.alwaysMapSurface.get())
            {
                lines.add(RenderSpec.getSurfaceSpec().getDebugStats());
            }

            if (!underground && coreProperties.mapTopography.get())
            {
                lines.add(RenderSpec.getTopoSpec().getDebugStats());
            }

            return lines.toArray(new String[lines.size()]);
        }
        catch (Throwable t)
        {
            logger.error(t);
            return new String[0];
        }
    }

    /**
     * Add temp debug message.
     *
     * @param key     the key
     * @param message the message
     */
    public static void addTempDebugMessage(String key, String message)
    {
        if (Minecraft.getMinecraft().gameSettings.showLagometer)
        {
            tempDebugLines.put(key, message);
        }
    }

    /**
     * Remove temp debug message.
     *
     * @param key the key
     */
    public static void removeTempDebugMessage(String key)
    {
        tempDebugLines.invalidate(key);
    }

    /**
     * Gets simple stats.
     *
     * @return the simple stats
     */
    public static String getSimpleStats()
    {
        int primaryRenderSize = 0;
        int secondaryRenderSize = 0;
        int totalChunks = 0;

        if (DataCache.getPlayer().underground || Journeymap.getClient().getCoreProperties().alwaysMapCaves.get())
        {
            RenderSpec spec = RenderSpec.getUndergroundSpec();
            if (spec != null)
            {
                primaryRenderSize += spec.getPrimaryRenderSize();
                secondaryRenderSize += spec.getLastSecondaryRenderSize();
                totalChunks += spec.getLastTaskChunks();
            }
        }

        if (!DataCache.getPlayer().underground || Journeymap.getClient().getCoreProperties().alwaysMapSurface.get())
        {
            RenderSpec spec = RenderSpec.getSurfaceSpec();
            if (spec != null)
            {
                primaryRenderSize += spec.getPrimaryRenderSize();
                secondaryRenderSize += spec.getLastSecondaryRenderSize();
                totalChunks += spec.getLastTaskChunks();
            }
        }

        return Constants.getString("jm.common.renderstats",
                totalChunks,
                primaryRenderSize,
                secondaryRenderSize,
                lastTaskTime,
                decFormat.format(lastTaskAvgChunkTime));
    }

    /**
     * Gets task completed.
     *
     * @return the task completed
     */
    public static long getlastTaskCompleted()
    {
        return lastTaskCompleted;
    }

    @Override
    public void initTask(Minecraft minecraft, JourneymapClient jm, File jmWorldDir, boolean threadLogging) throws InterruptedException
    {
        startNs = System.nanoTime();

        // Get candidate coordinates from the renderspec
        RenderSpec renderSpec = null;
        if (mapType.isUnderground())
        {
            renderSpec = RenderSpec.getUndergroundSpec();
        }
        else if (mapType.isTopo())
        {
            renderSpec = RenderSpec.getTopoSpec();
        }
        else
        {
            renderSpec = RenderSpec.getSurfaceSpec();
        }

        long now = System.currentTimeMillis();
        List<ChunkPos> renderArea = renderSpec.getRenderAreaCoords();
        int maxBatchSize = renderArea.size() / 4;

        // Remove coords of unloaded chunks or if they've been mapped too recently
        renderArea.removeIf(chunkPos -> {
            ChunkMD chunkMD = DataCache.INSTANCE.getChunkMD(chunkPos);
            if (chunkMD == null || !chunkMD.hasChunk() || (now - chunkMD.getLastRendered(mapType)) < 30000)
            {
                // Remove it
                return true;
            }
            else if (chunkMD.getDimension() != mapType.dimension)
            {
                // Theoretically this can happen because of a chunkloader, so remove it
                return true;
            }
            else
            {
                // Clear blockdata to ensure it is remapped
                chunkMD.resetBlockData(mapType);

                // Keep it
                return false;
            }
        });

        // Keep batch size reasonable
        if (renderArea.size() <= maxBatchSize)
        {
            chunkCoords.addAll(renderArea);
        }
        else
        {
//            // Add player's current chunk every time.
//            ChunkPos playerPos = new ChunkPos(minecraft.player.getPosition());
//            ChunkMD playerChunk = DataCache.INSTANCE.getChunkMD(playerPos);
//            if(playerChunk!=null) {
//                playerChunk.resetBlockData(mapType);
//                renderArea.remove(playerChunk);
//                chunkCoords.add(playerChunk.getCoord());
//            }

            List<ChunkPos> list = Arrays.asList(renderArea.toArray(new ChunkPos[renderArea.size()]));
            chunkCoords.addAll(list.subList(0, maxBatchSize));
        }

        this.scheduledChunks = chunkCoords.size();
    }

    @Override
    protected void complete(int mappedChunks, boolean cancelled, boolean hadError)
    {
        elapsedNs = System.nanoTime() - startNs;
    }

    @Override
    public int getMaxRuntime()
    {
        return maxRuntime;
    }

    /**
     * ITaskManager for MapPlayerTasks
     *
     * @author techbrew
     */
    public static class Manager implements ITaskManager
    {
        /**
         * The Map task delay.
         */
        final int mapTaskDelay = Journeymap.getClient().getCoreProperties().renderDelay.get() * 1000;

        /**
         * The Enabled.
         */
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
            if (enabled && minecraft.player.addedToChunk)
            {
                if ((System.currentTimeMillis() - lastTaskCompleted) >= mapTaskDelay)
                {
                    ChunkRenderController chunkRenderController = Journeymap.getClient().getChunkRenderController();
                    return MapPlayerTask.create(chunkRenderController, DataCache.getPlayer());
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

    /**
     * The type Map player task batch.
     */
    public static class MapPlayerTaskBatch extends TaskBatch
    {
        /**
         * Instantiates a new Map player task batch.
         *
         * @param tasks the tasks
         */
        public MapPlayerTaskBatch(List<ITask> tasks)
        {
            super(tasks);
        }

        @Override
        public void performTask(Minecraft mc, JourneymapClient jm, File jmWorldDir, boolean threadLogging) throws InterruptedException
        {
            if (mc.player == null)
            {
                return;
            }

            startNs = System.nanoTime();
            List<ITask> tasks = new ArrayList<ITask>(taskList);
            super.performTask(mc, jm, jmWorldDir, threadLogging);

            elapsedNs = System.nanoTime() - startNs;
            lastTaskTime = TimeUnit.NANOSECONDS.toMillis(elapsedNs);
            lastTaskCompleted = System.currentTimeMillis();

            // Report on timing
            int chunkCount = 0;
            for (ITask task : tasks)
            {
                if (task instanceof MapPlayerTask)
                {
                    MapPlayerTask mapPlayerTask = (MapPlayerTask) task;
                    chunkCount += mapPlayerTask.scheduledChunks;
                    if (mapPlayerTask.mapType.isUnderground())
                    {
                        RenderSpec.getUndergroundSpec().setLastTaskInfo(mapPlayerTask.scheduledChunks, mapPlayerTask.elapsedNs);
                    }
                    else if (mapPlayerTask.mapType.isTopo())
                    {
                        RenderSpec.getTopoSpec().setLastTaskInfo(mapPlayerTask.scheduledChunks, mapPlayerTask.elapsedNs);
                    }
                    else
                    {
                        RenderSpec.getSurfaceSpec().setLastTaskInfo(mapPlayerTask.scheduledChunks, mapPlayerTask.elapsedNs);
                    }
                }
                else
                {
                    Journeymap.getLogger().warn("Unexpected task in batch: " + task);
                }
            }
            lastTaskAvgChunkTime = elapsedNs / Math.max(1, chunkCount) / 1000000D;
        }
    }

}
