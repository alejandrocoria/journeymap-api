/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.task.multi;

import journeymap.client.Constants;
import journeymap.client.JourneymapClient;
import journeymap.client.api.display.Context;
import journeymap.client.api.display.DisplayType;
import journeymap.client.api.display.PolygonOverlay;
import journeymap.client.api.impl.ClientAPI;
import journeymap.client.api.model.MapPolygon;
import journeymap.client.api.model.ShapeProperties;
import journeymap.client.api.model.TextProperties;
import journeymap.client.cartography.ChunkRenderController;
import journeymap.client.data.DataCache;
import journeymap.client.feature.Feature;
import journeymap.client.feature.FeatureManager;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.io.FileHandler;
import journeymap.client.io.nbt.ChunkLoader;
import journeymap.client.io.nbt.RegionLoader;
import journeymap.client.log.ChatLog;
import journeymap.client.model.*;
import journeymap.client.ui.fullscreen.Fullscreen;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Maps an entire Minecraft region (512x512)
 */
public class MapRegionTask extends BaseMapTask
{
    private static final int MAX_RUNTIME = 30000;
    private static final Logger logger = Journeymap.getLogger();
    private static volatile long lastTaskCompleted;

    final PolygonOverlay regionOverlay;
    final RegionCoord rCoord;
    final Collection<ChunkCoordIntPair> retainedCoords;

    private MapRegionTask(ChunkRenderController renderController, World world, MapType mapType, RegionCoord rCoord, Collection<ChunkCoordIntPair> chunkCoords, Collection<ChunkCoordIntPair> retainCoords)
    {
        super(renderController, world, mapType, chunkCoords, true, false, 5000);
        this.rCoord = rCoord;
        this.retainedCoords = retainCoords;
        this.regionOverlay = createOverlay();
    }

    public static BaseMapTask create(ChunkRenderController renderController, RegionCoord rCoord, MapType mapType, Minecraft minecraft)
    {
        final World world = minecraft.theWorld;

        final List<ChunkCoordIntPair> renderCoords = rCoord.getChunkCoordsInRegion();
        final List<ChunkCoordIntPair> retainedCoords = new ArrayList<ChunkCoordIntPair>(renderCoords.size());

        HashMap<RegionCoord, Boolean> existingRegions = new HashMap<RegionCoord, Boolean>();

        // Ensure chunks north, west, nw are loaded for slope calculations
        for (ChunkCoordIntPair coord : renderCoords)
        {
            for (ChunkCoordIntPair keepAliveOffset : keepAliveOffsets)
            {
                ChunkCoordIntPair keepAliveCoord = new ChunkCoordIntPair(coord.chunkXPos + keepAliveOffset.chunkXPos, coord.chunkZPos + keepAliveOffset.chunkZPos);
                RegionCoord neighborRCoord = RegionCoord.fromChunkPos(rCoord.worldDir, mapType, keepAliveCoord.chunkXPos, keepAliveCoord.chunkZPos);
                if (!existingRegions.containsKey(neighborRCoord))
                {
                    existingRegions.put(neighborRCoord, neighborRCoord.exists());
                }

                if (!renderCoords.contains(keepAliveCoord) && existingRegions.get(neighborRCoord))
                {
                    retainedCoords.add(keepAliveCoord);
                }
            }
        }

        return new MapRegionTask(renderController, world, mapType, rCoord, renderCoords, retainedCoords);

    }

    @Override
    public final void performTask(Minecraft mc, JourneymapClient jm, File jmWorldDir, boolean threadLogging) throws InterruptedException
    {
        ClientAPI.INSTANCE.show(regionOverlay);

        AnvilChunkLoader loader = new AnvilChunkLoader(FileHandler.getWorldSaveDir(mc));

        int missing = 0;
        for (ChunkCoordIntPair coord : retainedCoords)
        {
            ChunkMD chunkMD = ChunkLoader.getChunkMD(loader, mc, coord, true);
            if (chunkMD != null && !chunkMD.getChunk().isEmpty())
            {
                DataCache.instance().addChunkMD(chunkMD);
            }
        }

        for (ChunkCoordIntPair coord : chunkCoords)
        {
            ChunkMD chunkMD = ChunkLoader.getChunkMD(loader, mc, coord, true);
            if (chunkMD != null && chunkMD.hasChunk())
            {
                DataCache.instance().addChunkMD(chunkMD);
            }
            else
            {
                missing++;
            }
        }

        if (chunkCoords.size() - missing > 0)
        {
            try
            {
                logger.info(String.format("Potential chunks to map in %s: %s of %s", rCoord, chunkCoords.size() - missing, chunkCoords.size()));
                super.performTask(mc, jm, jmWorldDir, threadLogging);
            }
            finally
            {
                regionOverlay.getShapeProperties().setFillColor(0xffffff).setStrokeColor(0xffffff);
                String label = String.format("%s\nRegion [%s,%s]", Constants.getString("jm.common.automap_region_complete"), rCoord.regionX, rCoord.regionZ);
                regionOverlay.setLabel(label);
                regionOverlay.flagForRerender();
            }
        }
        else
        {
            logger.info(String.format("Skipping empty region: %s", rCoord));
        }
    }

    protected PolygonOverlay createOverlay()
    {
        String displayId = "AutoMap" + rCoord;
        String groupName = "AutoMap";
        String label = String.format("%s\nRegion [%s,%s]", Constants.getString("jm.common.automap_region_start"), rCoord.regionX, rCoord.regionZ);

        // Style the polygon
        ShapeProperties shapeProps = new ShapeProperties()
                .setStrokeWidth(2)
                .setStrokeColor(0x0000ff).setStrokeOpacity(.7f)
                .setFillColor(0x00ff00).setFillOpacity(.2f);

        // Style the text
        TextProperties textProps = new TextProperties()
                .setBackgroundColor(0x000022)
                .setBackgroundOpacity(.5f)
                .setColor(0x00ff00)
                .setOpacity(1f)
                .setFontShadow(true);

        // Define the shape
        int x = this.rCoord.getMinChunkX() << 4;
        int y = 70;
        int z = this.rCoord.getMinChunkZ() << 4;
        int maxX = (this.rCoord.getMaxChunkX() << 4) + 15;
        int maxZ = (this.rCoord.getMaxChunkZ() << 4) + 15;
        BlockPos sw = new BlockPos(x, y, maxZ);
        BlockPos se = new BlockPos(maxX, y, maxZ);
        BlockPos ne = new BlockPos(maxX, y, z);
        BlockPos nw = new BlockPos(x, y, z);
        MapPolygon polygon = new MapPolygon(sw, se, ne, nw);

        // Create the overlay
        PolygonOverlay regionOverlay = new PolygonOverlay(Journeymap.MOD_ID, displayId, rCoord.dimension, shapeProps, polygon);

        // Set the text
        regionOverlay.setOverlayGroupName(groupName)
                .setLabel(label)
                .setTextProperties(textProps)
                .setActiveUIs(EnumSet.of(Context.UI.Any))
                .setActiveMapTypes(EnumSet.of(Context.MapType.Any));

        return regionOverlay;
    }

    @Override
    protected void complete(int mappedChunks, boolean cancelled, boolean hadError)
    {
        lastTaskCompleted = System.currentTimeMillis();

        // Flush images to disk
        RegionImageCache.instance().flushToDiskAsync(true);

        // Ensure no chunks are forcefully retained.
        DataCache.instance().stopChunkMDRetention();

        if (hadError || cancelled)
        {
            logger.warn("MapRegionTask cancelled %s hadError %s", cancelled, hadError);
        }
        else
        {
            logger.info(String.format("Actual chunks mapped in %s: %s ", rCoord, mappedChunks));
            regionOverlay.setTitle(Constants.getString("jm.common.automap_region_chunks", mappedChunks));
        }

        System.gc();
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
            final boolean worldHasSky = !ForgeHelper.INSTANCE.hasNoSky(player.entityLivingRef.get());
            boolean underground = ForgeHelper.INSTANCE.hasNoSky(player.entityLivingRef.get()) || player.underground;

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

            if ((System.currentTimeMillis() - lastTaskCompleted) < JourneymapClient.getCoreProperties().autoMapPoll.get())
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
                        mapType = Fullscreen.state().getMapType(false);
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
                // Write files synchronously before clearing
                RegionImageCache.instance().flushToDisk(true);
                RegionImageCache.instance().clear();
                regionLoader.getRegions().clear();
                regionLoader = null;
            }

            ClientAPI.INSTANCE.removeAll(Journeymap.MOD_ID, DisplayType.Polygon);

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
            ChunkRenderController chunkRenderController = JourneymapClient.getInstance().getChunkRenderController();
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
