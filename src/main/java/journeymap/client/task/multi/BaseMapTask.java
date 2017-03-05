/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.task.multi;

import journeymap.client.JourneymapClient;
import journeymap.client.cartography.ChunkRenderController;
import journeymap.client.data.DataCache;
import journeymap.client.log.StatTimer;
import journeymap.client.model.ChunkMD;
import journeymap.client.model.MapType;
import journeymap.client.model.RegionCoord;
import journeymap.client.model.RegionImageCache;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

/**
 * The type Base map task.
 */
public abstract class BaseMapTask implements ITask
{
    /**
     * The Logger.
     */
    static final Logger logger = Journeymap.getLogger();
    /**
     * The constant keepAliveOffsets.
     */
    protected static ChunkPos[] keepAliveOffsets = new ChunkPos[]{new ChunkPos(0, -1), new ChunkPos(-1, 0), new ChunkPos(-1, -1)};
    /**
     * The World.
     */
    final World world;
    /**
     * The Chunk coords.
     */
    final Collection<ChunkPos> chunkCoords;
    /**
     * The Flush cache when done.
     */
    final boolean flushCacheWhenDone;
    /**
     * The Render controller.
     */
    final ChunkRenderController renderController;
    /**
     * The Elapsed limit.
     */
    final int elapsedLimit;
    /**
     * The Map type.
     */
    final MapType mapType;
    /**
     * The Async file writes.
     */
    final boolean asyncFileWrites;

    /**
     * Instantiates a new Base map task.
     *
     * @param renderController   the render controller
     * @param world              the world
     * @param mapType            the map type
     * @param chunkCoords        the chunk coords
     * @param flushCacheWhenDone the flush cache when done
     * @param asyncFileWrites    the async file writes
     * @param elapsedLimit       the elapsed limit
     */
    public BaseMapTask(ChunkRenderController renderController, World world, MapType mapType, Collection<ChunkPos> chunkCoords, boolean flushCacheWhenDone, boolean asyncFileWrites, int elapsedLimit)
    {
        this.renderController = renderController;
        this.world = world;
        this.mapType = mapType;
        this.chunkCoords = chunkCoords;
        this.asyncFileWrites = asyncFileWrites;
        this.flushCacheWhenDone = flushCacheWhenDone;
        this.elapsedLimit = elapsedLimit;
    }

    /**
     * Init task.
     *
     * @param mc            the mc
     * @param jm            the jm
     * @param jmWorldDir    the jm world dir
     * @param threadLogging the thread logging
     * @throws InterruptedException the interrupted exception
     */
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
            if (mc.world == null)
            {
                this.complete(count, true, false);
                return;
            }

            final Iterator<ChunkPos> chunkIter = chunkCoords.iterator();

            // Check the dimension
            int currentDimension = FMLClientHandler.instance().getClient().player.world.provider.getDimension();
            if (currentDimension != mapType.dimension)
            {
                if (threadLogging)
                {
                    logger.debug("Dimension changed, map task obsolete."); //$NON-NLS-1$
                }
                timer.cancel();
                this.complete(count, true, false);
                return;
            }

            ChunkPos playerChunk = new ChunkPos(FMLClientHandler.instance().getClient().player.getPosition());

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
                    this.complete(count, true, false);
                    return;
                }

                if (Thread.interrupted())
                {
                    throw new InterruptedException();
                }

                ChunkPos coord = chunkIter.next();
                ChunkMD chunkMd = DataCache.INSTANCE.getChunkMD(coord);
                if (chunkMd != null)
                {
                    try
                    {
                        RegionCoord rCoord = RegionCoord.fromChunkPos(jmWorldDir, mapType, chunkMd.getCoord().chunkXPos, chunkMd.getCoord().chunkZPos);
                        boolean rendered = renderController.renderChunk(rCoord, mapType, chunkMd);
                        if (rendered)
                        {
                            count++;
                        }
                    }
                    catch (Throwable t)
                    {
                        logger.warn("Error rendering chunk " + chunkMd + ": " + t.getMessage());
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
                this.complete(count, true, false);
                return;
            }

            if (Thread.interrupted())
            {
                timer.cancel();
                throw new InterruptedException();
            }

            // Push chunk cache to region cache
            RegionImageCache.INSTANCE.updateTextures(flushCacheWhenDone, asyncFileWrites);
            chunkCoords.clear();
            this.complete(count, false, false);
            timer.stop();
        }
        catch (InterruptedException t)
        {
            Journeymap.getLogger().warn("Task thread interrupted: " + this);
            timer.cancel();
            throw t;
        }
        catch (Throwable t)
        {
            String error = "Unexpected error in BaseMapTask: " + (LogFormatter.toString(t));
            Journeymap.getLogger().error(error);
            this.complete(count, false, true);
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

    /**
     * Complete.
     *
     * @param mappedChunks the mapped chunks
     * @param cancelled    the cancelled
     * @param hadError     the had error
     */
    protected abstract void complete(int mappedChunks, boolean cancelled, boolean hadError);

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
