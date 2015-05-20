/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.task.multi;

import net.minecraft.client.Minecraft;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.cartography.ChunkRenderController;
import net.techbrew.journeymap.data.DataCache;
import net.techbrew.journeymap.feature.Feature;
import net.techbrew.journeymap.feature.FeatureManager;
import net.techbrew.journeymap.io.nbt.ChunkLoader;
import net.techbrew.journeymap.io.nbt.RegionLoader;
import net.techbrew.journeymap.log.ChatLog;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.model.*;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MapRegionTask extends BaseMapTask
{
    private static final int MAX_RUNTIME = 30000;
    private static final Logger logger = JourneyMap.getLogger();
    private static volatile long lastTaskCompleted;

    final RegionCoord rCoord;
    final Collection<ChunkCoordIntPair> retainedCoords;

    private MapRegionTask(ChunkRenderController renderController, World world, MapType mapType, RegionCoord rCoord, Collection<ChunkCoordIntPair> chunkCoords, Collection<ChunkCoordIntPair> retainCoords)
    {
        super(renderController, world, mapType, chunkCoords, true, 5000);
        this.rCoord = rCoord;
        this.retainedCoords = retainCoords;
    }

    public static BaseMapTask create(ChunkRenderController renderController, RegionCoord rCoord, MapType mapType, Minecraft minecraft)
    {

        int missing = 0;

        final World world = minecraft.theWorld;

        final List<ChunkCoordIntPair> renderCoords = rCoord.getChunkCoordsInRegion(mapType.vSlice);
        final List<ChunkCoordIntPair> retainedCoords = new ArrayList<ChunkCoordIntPair>(renderCoords.size());

        // Ensure chunks north, west, nw are kept alive for slope calculations
        for (ChunkCoordIntPair coord : renderCoords)
        {
            for (ChunkCoordIntPair keepAliveOffset : keepAliveOffsets)
            {
                ChunkCoordIntPair keepAliveCoord = new ChunkCoordIntPair(coord.chunkXPos + keepAliveOffset.chunkXPos, coord.chunkZPos + keepAliveOffset.chunkZPos);
                if (!renderCoords.contains(keepAliveCoord))
                {
                    retainedCoords.add(keepAliveCoord);
                }
            }
        }

        return new MapRegionTask(renderController, world, mapType, rCoord, renderCoords, retainedCoords);

    }

    @Override
    public final void performTask(Minecraft mc, JourneyMap jm, File jmWorldDir, boolean threadLogging) throws InterruptedException
    {
        AnvilChunkLoader loader = ChunkLoader.getAnvilChunkLoader(mc);

        int missing = 0;
        for (ChunkCoordIntPair coord : retainedCoords)
        {
            ChunkMD chunkMD = ChunkLoader.getChunkMD(loader, mc, coord);
            if (chunkMD != null)
            {
                DataCache.instance().addChunkMD(chunkMD);
            }
        }

        for (ChunkCoordIntPair coord : chunkCoords)
        {
            ChunkMD chunkMD = ChunkLoader.getChunkMD(loader, mc, coord);
            if (chunkMD != null)
            {
                DataCache.instance().addChunkMD(chunkMD);
            }
            else
            {
                missing++;
            }
        }

        logger.info(String.format("Chunks found in %s: %s/%s", rCoord, chunkCoords.size() - missing, chunkCoords.size()));

        if (chunkCoords.size() - missing >= 16)
        {
            super.performTask(mc, jm, jmWorldDir, threadLogging);
        }
        else
        {
            logger.info("Skipping underpopulated region: " + rCoord);
        }
    }

    @Override
    protected void complete(boolean cancelled, boolean hadError)
    {
        lastTaskCompleted = System.currentTimeMillis();
        if (!cancelled)
        {
            RegionImageCache.instance().flushToDisk();
        }
        DataCache.instance().invalidateChunkMDCache();
        if (hadError || cancelled)
        {
            logger.warn("MapRegionTask cancelled %s hadError %s", cancelled, hadError);
        }
    }

    @Override
    public int getMaxRuntime()
    {
        return MAX_RUNTIME;
    }

    /**
     * Stateful ITaskManager for MapRegionTasks
     *
     * @author mwoodman
     */
    public static class Manager implements ITaskManager
    {
        final int mapTaskDelay = 0;

        RegionLoader regionLoader;
        boolean enabled;

        @Override
        public Class<? extends ITask> getTaskClass()
        {
            return MapRegionTask.class;
        }

        @Override
        public boolean enableTask(Minecraft minecraft, Object params)
        {
            EntityDTO player = DataCache.getPlayer();
            final boolean cavesAllowed = FeatureManager.isAllowed(Feature.MapCaves);
            final boolean worldHasSky = !player.entityLiving.worldObj.provider.hasNoSky;
            boolean underground = player.entityLiving.worldObj.provider.hasNoSky || player.underground;

            if (underground && !cavesAllowed)
            {
                if (worldHasSky)
                {
                    underground = false;
                }
                else
                {
                    logger.info("Cave mapping not permitted.");
                    return false;
                }
            }

            enabled = (params != null);
            if (!enabled)
            {
                return false;
            }

            if ((System.currentTimeMillis() - lastTaskCompleted) < JourneyMap.getCoreProperties().autoMapPoll.get())
            {
                return false;
            }

            enabled = false; // assume the worst
            if (minecraft.isIntegratedServerRunning())
            {
                try
                {
                    MapType mapType;
                    if (underground)
                    {
                        mapType = MapType.underground(player);
                    }
                    else
                    {
                        final long time = minecraft.theWorld.getWorldInfo().getWorldTime() % 24000L;
                        mapType = (time < 13800) ? MapType.day(player) : MapType.night(player);
                    }

                    Boolean mapAll = params == null ? false : (Boolean) params;

                    regionLoader = new RegionLoader(minecraft, mapType, mapAll);
                    if (regionLoader.getRegionsFound() == 0)
                    {
                        disableTask(minecraft);
                    }
                    else
                    {
                        this.enabled = true;
                    }
                }
                catch (Throwable t)
                {
                    String error = "Couldn't Auto-Map: " + t.getMessage(); //$NON-NLS-1$
                    ChatLog.announceError(error);
                    logger.error(error + ": " + LogFormatter.toString(t));
                }
            }
            return this.enabled;
        }

        @Override
        public boolean isEnabled(Minecraft minecraft)
        {
            return this.enabled;
        }

        @Override
        public void disableTask(Minecraft minecraft)
        {
            if (regionLoader != null)
            {
                if (regionLoader.isUnderground())
                {
                    ChatLog.announceI18N("jm.common.automap_complete_underground", regionLoader.getVSlice());
                }
                else
                {
                    ChatLog.announceI18N("jm.common.automap_complete");
                }
            }
            enabled = false;

            if (regionLoader != null)
            {
                RegionImageCache.instance().flushToDisk();
                RegionImageCache.instance().clear();
                regionLoader.getRegions().clear();
                regionLoader = null;
            }

        }

        @Override
        public BaseMapTask getTask(Minecraft minecraft)
        {

            if (!enabled)
            {
                return null;
            }

            if (regionLoader.getRegions().isEmpty())
            {
                disableTask(minecraft);
                return null;
            }

            RegionCoord rCoord = regionLoader.getRegions().peek();
            ChunkRenderController chunkRenderController = JourneyMap.getInstance().getChunkRenderController();
            BaseMapTask baseMapTask = MapRegionTask.create(chunkRenderController, rCoord, regionLoader.getMapType(), minecraft);
            return baseMapTask;
        }

        @Override
        public void taskAccepted(ITask task, boolean accepted)
        {
            if (accepted)
            {
                regionLoader.getRegions().pop();
                float total = 1F * regionLoader.getRegionsFound();
                float remaining = total - regionLoader.getRegions().size();
                String percent = new DecimalFormat("##.#").format(remaining * 100 / total) + "%";
                if (regionLoader.isUnderground())
                {
                    ChatLog.announceI18N("jm.common.automap_status_underground", regionLoader.getVSlice(), percent);
                }
                else
                {
                    ChatLog.announceI18N("jm.common.automap_status", percent);
                }
            }
        }
    }
}
