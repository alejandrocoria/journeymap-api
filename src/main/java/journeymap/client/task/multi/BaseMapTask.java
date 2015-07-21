/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.task.multi;

import journeymap.client.JourneymapClient;
import journeymap.client.cartography.ChunkRenderController;
import journeymap.client.data.DataCache;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.common.log.LogFormatter;
import journeymap.common.log.StatTimer;
import journeymap.client.model.ChunkCoord;
import journeymap.client.model.ChunkMD;
import journeymap.client.model.MapType;
import journeymap.client.model.RegionImageCache;
import net.minecraft.client.Minecraft;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import org.apache.logging.log4j.Logger;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;

public abstract class BaseMapTask implements ITask
{
    static final Logger logger = JourneymapClient.getLogger();
    protected static ChunkCoordIntPair[] keepAliveOffsets = new ChunkCoordIntPair[]{new ChunkCoordIntPair(0, -1), new ChunkCoordIntPair(-1, 0), new ChunkCoordIntPair(-1, -1)};
    private static BufferedImage blankChunkImage = null;
    private static BufferedImage blankChunkImageUnderground = null;
    final World world;
    final Collection<ChunkCoordIntPair> chunkCoords;
    final boolean flushCacheWhenDone;
    final ChunkRenderController renderController;
    final int elapsedLimit;
    final MapType mapType;

    public BaseMapTask(ChunkRenderController renderController, World world, MapType mapType, Collection<ChunkCoordIntPair> chunkCoords, boolean flushCacheWhenDone, int elapsedLimit)
    {
        this.renderController = renderController;
        this.world = world;
        this.mapType = mapType;
        this.chunkCoords = chunkCoords;
        this.flushCacheWhenDone = flushCacheWhenDone;
        this.elapsedLimit = elapsedLimit;
    }

    public void initTask(Minecraft mc, JourneymapClient jm, File jmWorldDir, boolean threadLogging) throws InterruptedException
    {

    }


    @Override
    public void performTask(Minecraft mc, JourneymapClient jm, File jmWorldDir, boolean threadLogging) throws InterruptedException
    {
        StatTimer timer = StatTimer.get(getClass().getSimpleName() + ".performTask", 5, elapsedLimit).start();

        initTask(mc, jm, jmWorldDir, threadLogging);

        int count = 0;

        try
        {
            if (mc.theWorld == null)
            {
                this.complete(true, false);
                return;
            }

            final long start = System.nanoTime();
            final Iterator<ChunkCoordIntPair> chunkIter = chunkCoords.iterator();


            // Check the dimension
            int currentDimension = ForgeHelper.INSTANCE.getPlayerDimension();
            if (currentDimension != mapType.dimension)
            {
                if (threadLogging)
                {
                    logger.debug("Dimension changed, map task obsolete."); //$NON-NLS-1$
                }
                timer.cancel();
                this.complete(true, false);
                return;
            }

            // Map the chunks
            while (chunkIter.hasNext())
            {
                if (!jm.isMapping())
                {
                    if (threadLogging)
                    {
                        logger.debug("JM isn't mapping, aborting"); //$NON-NLS-1$
                    }
                    timer.cancel();
                    this.complete(true, false);
                    return;
                }

                if (Thread.interrupted())
                {
                    throw new InterruptedException();
                }

                ChunkCoordIntPair coord = chunkIter.next();
                ChunkMD chunkMd = DataCache.instance().getChunkMD(coord);
                if (chunkMd != null && chunkMd.hasChunk())
                {
                    try
                    {
                        ChunkCoord cCoord = ChunkCoord.fromChunkMD(jmWorldDir, mapType, chunkMd);
                        renderController.renderChunk(cCoord, chunkMd, mapType);
                        count++;
                    }
                    catch (ChunkMD.ChunkMissingException e)
                    {
                        logger.info(e.getMessage());
                    }
                }
            }

            if (!jm.isMapping())
            {
                if (threadLogging)
                {
                    logger.debug("JM isn't mapping, aborting.");  //$NON-NLS-1$
                }
                timer.cancel();
                this.complete(true, false);
                return;
            }

            if (Thread.interrupted())
            {
                timer.cancel();
                throw new InterruptedException();
            }

            // Push chunk cache to region cache
            RegionImageCache.instance().updateTextures(flushCacheWhenDone);
            chunkCoords.clear();
            this.complete(false, false);
            timer.stop();
        }
        catch (InterruptedException t)
        {
            JourneymapClient.getLogger().warn("Task thread interrupted: " + this);
            timer.cancel();
            throw t;
        }
        catch (Throwable t)
        {
            String error = "Unexpected error in BaseMapTask: " + (LogFormatter.toString(t));
            JourneymapClient.getLogger().error(error);
            this.complete(false, true);
            timer.cancel();
        }
        finally
        {
            if (threadLogging)
            {
                timer.report();
            }
        }
    }

    protected abstract void complete(boolean cancelled, boolean hadError);

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "{" +
                "world=" + world +
                ", mapType=" + mapType +
                ", chunkCoords=" + chunkCoords +
                ", flushCacheWhenDone=" + flushCacheWhenDone +
                '}';
    }
}
