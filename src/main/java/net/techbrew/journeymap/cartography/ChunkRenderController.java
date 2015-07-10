/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.cartography;

import com.google.common.base.Objects;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.cartography.render.*;
import net.techbrew.journeymap.io.RegionImageHandler;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.model.*;
import org.apache.logging.log4j.Level;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Delegates rendering job to one or more renderer.
 *
 * @author mwoodman
 */
public class ChunkRenderController
{
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

    public boolean renderChunk(ChunkCoord cCoord, ChunkMD chunkMd, MapType mapType)
    {
        long start = System.nanoTime();
        Graphics2D undergroundG2D = null;
        Graphics2D dayG2D = null;
        Graphics2D nightG2D = null;
        boolean renderOkay = false;
        final RegionCoord rCoord = cCoord.getRegionCoord();

        if (cCoord.isUnderground() != mapType.isUnderground() || !Objects.equal(cCoord.getVerticalSlice(), mapType.vSlice) || chunkMd.getDimension() != mapType.dimension)
        {
            JourneyMap.getLogger().error(String.format("Bad data; Coordinates not compatible with MapType: %s, %s, %s ", cCoord, chunkMd, mapType));
            return false;
        }

        try
        {
            RegionImageSet regionImageSet = RegionImageCache.instance().getRegionImageSet(rCoord);
            if (mapType.isUnderground())
            {
                BufferedImage image = regionImageSet.getChunkImage(cCoord, mapType);
                if (image != null)
                {
                    undergroundG2D = RegionImageHandler.initRenderingHints(image.createGraphics());
                    switch (rCoord.dimension)
                    {
                        case -1:
                        {
                            renderOkay = netherRenderer.render(undergroundG2D, chunkMd, mapType.vSlice);
                            break;
                        }
                        case 1:
                        {
                            renderOkay = endRenderer.render(undergroundG2D, chunkMd, mapType.vSlice);
                            break;
                        }
                        default:
                        {
                            renderOkay = overWorldCaveRenderer.render(undergroundG2D, chunkMd, mapType.vSlice);
                        }
                    }

                    if (renderOkay)
                    {
                        regionImageSet.setChunkImage(cCoord, mapType, image);
                    }
                }
            }
            else
            {
                BufferedImage imageDay = regionImageSet.getChunkImage(cCoord, MapType.day(rCoord.dimension));
                BufferedImage imageNight = regionImageSet.getChunkImage(cCoord, MapType.night(rCoord.dimension));

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
                    regionImageSet.setChunkImage(cCoord, MapType.day(rCoord.dimension), imageDay);
                    regionImageSet.setChunkImage(cCoord, MapType.night(rCoord.dimension), imageNight);
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

        if (!renderOkay)
        {
            if (JourneyMap.getLogger().isDebugEnabled())
            {
                JourneyMap.getLogger().debug("Chunk render failed: %s %s %s", rCoord, cCoord, mapType);
            }
        }

        return renderOkay;
    }
}
