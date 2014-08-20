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
import net.techbrew.journeymap.cartography.render.CaveRenderer;
import net.techbrew.journeymap.cartography.render.EndRenderer;
import net.techbrew.journeymap.cartography.render.NetherRenderer;
import net.techbrew.journeymap.cartography.render.SurfaceRenderer;
import net.techbrew.journeymap.io.RegionImageHandler;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.model.ChunkMD;
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
    private final IChunkRenderer overWorldSurfaceRenderer;
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

    public BufferedImage getChunkImage(ChunkMD chunkMd, Integer vSlice)
    {
        boolean underground = vSlice!=null;

        // Initialize image for the chunk
        BufferedImage chunkImage = new BufferedImage(underground ? 16 : 32, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2D = RegionImageHandler.initRenderingHints(chunkImage.createGraphics());

        int dimension = chunkMd.getWorldObj().provider.dimensionId;
        boolean renderOkay = false;

        long start = System.nanoTime();
        try
        {
            switch (dimension)
            {
                case -1:
                {
                    if (!underground || vSlice == null)
                    {
                        JourneyMap.getLogger().warn("Map task isn't underground, can't perform in Nether.");
                        renderOkay = false;
                    }
                    else
                    {
                        renderOkay = netherRenderer.render(g2D, chunkMd, vSlice);
                    }
                    break;
                }
                case 1:
                {
                    if (!underground || vSlice == null)
                    {
                        JourneyMap.getLogger().warn("Map task isn't underground, can't perform in End.");
                        renderOkay = false;
                    }
                    else
                    {
                        renderOkay = endRenderer.render(g2D, chunkMd, vSlice);
                    }
                    break;
                }
                default:
                {
                    if (!underground || vSlice == null || vSlice == -1)
                    {
                        renderOkay = overWorldSurfaceRenderer.render(g2D, chunkMd, null);
                    }
                    else
                    {
                        renderOkay = overWorldCaveRenderer.render(g2D, chunkMd, vSlice);
                    }
                }
            }

        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            JourneyMap.getLogger().log(Level.WARN, LogFormatter.toString(e));
            return null; // Can happen when server isn't connected, just wait for next tick
        }
        catch (Throwable t)
        {
            JourneyMap.getLogger().error(Constants.getMessageJMERR16(LogFormatter.toString(t)));
        }
        finally
        {
            g2D.dispose();
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
                JourneyMap.getLogger().log(Level.WARN, "Chunk didn't render for dimension " + dimension + ": " + chunkMd);
            }
            // Use blank
            // chunkImage = underground ? getBlankChunkImageUnderground() : getBlankChunkImage();
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


        return chunkImage;

    }
}
