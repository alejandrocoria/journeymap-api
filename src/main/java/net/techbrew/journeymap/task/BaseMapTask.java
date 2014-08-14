/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.task;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.cartography.ChunkRenderController;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.log.StatTimer;
import net.techbrew.journeymap.model.ChunkCoord;
import net.techbrew.journeymap.model.ChunkImageCache;
import net.techbrew.journeymap.model.ChunkMD;
import net.techbrew.journeymap.model.RegionImageCache;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public abstract class BaseMapTask implements ITask
{

    final World world;
    final int dimension;
    final boolean underground;
    final Integer vSlice;
    final ChunkMD.Set chunkMdSet;
    final boolean flushCacheWhenDone;
    final ChunkRenderController renderController;

    public BaseMapTask(ChunkRenderController renderController, World world, int dimension, boolean underground, Integer vSlice, ChunkMD.Set chunkMdSet, boolean flushCacheWhenDone)
    {
        this.renderController = renderController;
        this.world = world;
        this.dimension = dimension;
        this.underground = underground;
        this.vSlice = vSlice;
        if(vSlice!=null && vSlice==-1)
        {
            vSlice = null;
        }
        if ((vSlice == null) && underground)
        {
            throw new IllegalStateException("vSlice can't be null (-1) and task be underground");
        }
        this.chunkMdSet = chunkMdSet;
        this.flushCacheWhenDone = flushCacheWhenDone;
    }

    @Override
    public final void performTask(Minecraft mc, JourneyMap jm, File jmWorldDir, boolean threadLogging) throws InterruptedException
    {
        StatTimer timer = StatTimer.get(getClass().getSimpleName() + ".performTask").start();

        try
        {
            final long start = System.nanoTime();
            final Iterator<ChunkMD> chunkIter = chunkMdSet.iterator();
            final ChunkImageCache chunkImageCache = new ChunkImageCache();
            final Logger logger = JourneyMap.getLogger();

            // Check the dimension
            int currentDimension = mc.theWorld.provider.dimensionId;
            if (currentDimension != dimension)
            {
                if (threadLogging)
                {
                    logger.fine("Dimension changed, map task obsolete."); //$NON-NLS-1$
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
                        logger.fine("JM isn't mapping, aborting"); //$NON-NLS-1$
                    }
                    timer.cancel();
                    this.complete(true, false);
                    return;
                }

                if (Thread.interrupted())
                {
                    throw new InterruptedException();
                }

                ChunkMD chunkMd = chunkIter.next();
                if (chunkMd.render)
                {
                    BufferedImage chunkImage = renderController.getChunkImage(chunkMd, vSlice);
                    ChunkCoord cCoord = ChunkCoord.fromChunkMD(jmWorldDir, chunkMd, vSlice, dimension);
                    if (underground)
                    {
                        chunkImageCache.put(cCoord, Constants.MapType.underground, chunkImage);
                    }
                    else
                    {
                        chunkImageCache.put(cCoord, Constants.MapType.day, getSubimage(Constants.MapType.day, chunkImage));
                        chunkImageCache.put(cCoord, Constants.MapType.night, getSubimage(Constants.MapType.night, chunkImage));
                    }
                }
            }

            if (!jm.isMapping())
            {
                if (threadLogging)
                {
                    logger.fine("JM isn't mapping, aborting.");  //$NON-NLS-1$
                }
                timer.cancel();
                this.complete(true, false);
                return;
            }

            if (Thread.interrupted())
            {
                throw new InterruptedException();
            }

            // Push chunk cache to region cache
            int chunks = chunkImageCache.size();
            RegionImageCache.getInstance().putAll(chunkImageCache.values(), flushCacheWhenDone);

            if (threadLogging)
            {
                logger.fine(getClass().getSimpleName() + " mapped " + chunks + " chunks in " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) + "ms with flush:" + flushCacheWhenDone); //$NON-NLS-1$ //$NON-NLS-2$
            }

            chunkMdSet.clear();
            chunkImageCache.clear();
            this.complete(false, false);

        }
        catch (InterruptedException t)
        {
            JourneyMap.getLogger().warning("Task thread interrupted: " + this);
            throw t;
        }
        catch (Throwable t)
        {
            String error = Constants.getMessageJMERR16(LogFormatter.toString(t));
            JourneyMap.getLogger().severe(error);
            this.complete(false, true);
        }
        finally
        {
            timer.stop();

            if (threadLogging)
            {
                timer.report();
            }
        }
    }

    protected abstract void complete(boolean cancelled, boolean hadError);

    private BufferedImage getSubimage(Constants.MapType mapType, BufferedImage image)
    {
        if (image == null)
        {
            return null;
        }
        switch (mapType)
        {
            case night:
            {
                return image.getSubimage(16, 0, 16, 16);
            }
            default:
            {
                return image.getSubimage(0, 0, 16, 16);
            }
        }
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "{" +
                "world=" + world +
                ", dimension=" + dimension +
                ", underground=" + underground +
                ", vSlice=" + vSlice +
                ", chunkMdSet=" + chunkMdSet +
                ", flushCacheWhenDone=" + flushCacheWhenDone +
                '}';
    }
}
