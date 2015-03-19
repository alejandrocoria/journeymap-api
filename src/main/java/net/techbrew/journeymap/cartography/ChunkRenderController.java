/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.cartography;

import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.cartography.render.*;
import net.techbrew.journeymap.io.RegionImageHandler;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.model.*;
import org.apache.logging.log4j.Level;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Delegates rendering job to one or more renderer.
 *
 * @author mwoodman
 */
public class ChunkRenderController
{
    private static AtomicInteger updateCounter = new AtomicInteger(0);
    private static AtomicLong updateTime = new AtomicLong(0);
    final boolean fineLogging = JourneyMap.getLogger().isDebugEnabled();
    private final IChunkRenderer netherRenderer;
    private final IChunkRenderer endRenderer;
    private final SurfaceRenderer overWorldSurfaceRenderer;
    private final IChunkRenderer overWorldCaveRenderer;


    public ChunkRenderController()
    {
        netherRenderer = new NetherRenderer();
        endRenderer = new EndRenderer();
        SurfaceRenderer surfaceRenderer = new SurfaceRenderer();
        overWorldSurfaceRenderer = surfaceRenderer;
        overWorldCaveRenderer = new CaveRenderer(surfaceRenderer);
        //standardRenderer = new ChunkTopoRenderer();
    }

    public boolean renderChunk(ChunkCoord cCoord, ChunkMD chunkMd, boolean underground, Integer vSlice)
    {
        long start = System.nanoTime();
        Graphics2D undergroundG2D = null;
        Graphics2D dayG2D = null;
        Graphics2D nightG2D = null;
        boolean renderOkay = false;

        try
        {
            final RegionCoord rCoord = cCoord.getRegionCoord();
            RegionImageSet regionImageSet = RegionImageCache.instance().getRegionImageSet(rCoord);
            if (underground)
            {
                BufferedImage image = regionImageSet.getChunkImage(cCoord, Constants.MapType.underground);
                if (image != null)
                {
                    undergroundG2D = RegionImageHandler.initRenderingHints(image.createGraphics());
                    undergroundG2D.setComposite(BaseRenderer.ALPHA_OPAQUE);
                    switch (rCoord.dimension)
                    {
                        case -1:
                        {
                            renderOkay = netherRenderer.render(undergroundG2D, chunkMd, vSlice);
                            break;
                        }
                        case 1:
                        {
                            renderOkay = endRenderer.render(undergroundG2D, chunkMd, vSlice);
                            break;
                        }
                        default:
                        {
                            renderOkay = overWorldCaveRenderer.render(undergroundG2D, chunkMd, vSlice);
                        }
                    }

                    if (renderOkay)
                    {
                        regionImageSet.setChunkImage(cCoord, Constants.MapType.underground, image);
                    }
                }
            }
            else
            {
                BufferedImage imageDay = regionImageSet.getChunkImage(cCoord, Constants.MapType.day);
                BufferedImage imageNight = regionImageSet.getChunkImage(cCoord, Constants.MapType.night);

                if (imageDay != null)
                {
                    dayG2D = RegionImageHandler.initRenderingHints(imageDay.createGraphics());
                    dayG2D.setComposite(BaseRenderer.ALPHA_OPAQUE);
                }

                if (imageNight != null)
                {
                    nightG2D = RegionImageHandler.initRenderingHints(imageNight.createGraphics());
                    nightG2D.setComposite(BaseRenderer.ALPHA_OPAQUE);
                }

                renderOkay = dayG2D != null && overWorldSurfaceRenderer.render(dayG2D, nightG2D, chunkMd);

                if (renderOkay)
                {
                    regionImageSet.setChunkImage(cCoord, Constants.MapType.day, imageDay);
                    regionImageSet.setChunkImage(cCoord, Constants.MapType.night, imageNight);
                }
            }

            if (renderOkay)
            {
                chunkMd.setRendered();
            }
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            JourneyMap.getLogger().log(Level.WARN, LogFormatter.toString(e));
            return false; // Can happen when server isn't connected, just wait for next tick
        }
        catch (Throwable t)
        {
            JourneyMap.getLogger().error("Unexpected error in ChunkRenderController: " + (LogFormatter.toString(t)));
        }
        finally
        {
            if (dayG2D != null)
            {
                dayG2D.dispose();
            }
            if (nightG2D != null)
            {
                nightG2D.dispose();
            }
            if (undergroundG2D != null)
            {
                undergroundG2D.dispose();
            }
        }

        long stop = System.nanoTime();

        if (fineLogging)
        {
            updateCounter.incrementAndGet();
            updateTime.addAndGet(stop - start);
        }

        if (!renderOkay)
        {
            if (fineLogging)
            {
                JourneyMap.getLogger().log(Level.WARN, "Chunk didn't render for " + cCoord + " vSlice " + vSlice);
            }
        }

        if (fineLogging)
        {
            double counter = updateCounter.get();
            if (counter >= 1000)
            {
                double time = TimeUnit.NANOSECONDS.toMillis(updateTime.get());
                double avg = time / counter;

                JourneyMap.getLogger().info("*** Chunks rendered: " + (int) counter + " in avg " + avg + " ms");

                updateCounter.set(0);
                updateTime.set(0);
            }
        }

        return renderOkay;
    }
}
