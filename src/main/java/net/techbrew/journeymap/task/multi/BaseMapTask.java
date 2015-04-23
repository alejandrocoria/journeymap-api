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
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.cartography.ChunkRenderController;
import net.techbrew.journeymap.data.DataCache;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.log.StatTimer;
import net.techbrew.journeymap.model.ChunkCoord;
import net.techbrew.journeymap.model.ChunkMD;
import net.techbrew.journeymap.model.MapType;
import net.techbrew.journeymap.model.RegionImageCache;
import org.apache.logging.log4j.Logger;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;

public abstract class BaseMapTask implements ITask
{
    static final Logger logger = JourneyMap.getLogger();
    protected static ChunkCoordIntPair[] keepAliveOffsets = new ChunkCoordIntPair[]{new ChunkCoordIntPair(0, -1), new ChunkCoordIntPair(-1, 0), new ChunkCoordIntPair(-1, -1)};
    private static BufferedImage blankChunkImage = null;
    private static BufferedImage blankChunkImageUnderground = null;
    final World world;
    final int dimension;
    final boolean underground;
    final Integer vSlice;
    final Collection<ChunkCoordIntPair> chunkCoords;
    final boolean flushCacheWhenDone;
    final ChunkRenderController renderController;
    final int elapsedLimit;

    public BaseMapTask(ChunkRenderController renderController, World world, int dimension, boolean underground, Integer vSlice, Collection<ChunkCoordIntPair> chunkCoords, boolean flushCacheWhenDone, int elapsedLimit)
    {
        this.renderController = renderController;
        this.world = world;
        this.dimension = dimension;
        this.underground = underground;
        this.vSlice = vSlice;
        if (vSlice != null && vSlice == -1)
        {
            vSlice = null;
        }
        if ((vSlice == null) && underground)
        {
            throw new IllegalStateException("vSlice can't be null (-1) and task be underground");
        }
        this.chunkCoords = chunkCoords;
        this.flushCacheWhenDone = flushCacheWhenDone;
        this.elapsedLimit = elapsedLimit;
    }

    public void initTask(Minecraft mc, JourneyMap jm, File jmWorldDir, boolean threadLogging) throws InterruptedException
    {

    }


    @Override
    public void performTask(Minecraft mc, JourneyMap jm, File jmWorldDir, boolean threadLogging) throws InterruptedException
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
            int currentDimension = mc.theWorld.provider.dimensionId;
            if (currentDimension != dimension)
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

                MapType mapType = MapType.from(vSlice, dimension);
                ChunkCoordIntPair coord = chunkIter.next();
                ChunkMD chunkMd = DataCache.instance().getChunkMD(coord);
                if (chunkMd != null && chunkMd.hasChunk())
                {
                    try
                    {
                        ChunkCoord cCoord = ChunkCoord.fromChunkMD(jmWorldDir, mapType, chunkMd);
                        renderController.renderChunk(cCoord, chunkMd, underground, vSlice);
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
            JourneyMap.getLogger().warn("Task thread interrupted: " + this);
            timer.cancel();
            throw t;
        }
        catch (Throwable t)
        {
            String error = "Unexpected error in BaseMapTask: " + (LogFormatter.toString(t));
            JourneyMap.getLogger().error(error);
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
                ", dimension=" + dimension +
                ", underground=" + underground +
                ", vSlice=" + vSlice +
                ", chunkCoords=" + chunkCoords +
                ", flushCacheWhenDone=" + flushCacheWhenDone +
                '}';
    }
}
