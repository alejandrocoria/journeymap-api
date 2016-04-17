/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.cartography;

import journeymap.client.JourneymapClient;
import journeymap.client.cartography.render.*;
import journeymap.client.model.*;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import org.apache.logging.log4j.Level;

import java.awt.image.BufferedImage;

/**
 * Delegates rendering job to one or more renderer.
 *
 * @author techbrew
 */
public class ChunkRenderController
{
    private final IChunkRenderer netherRenderer;
    private final IChunkRenderer endRenderer;
    private final SurfaceRenderer overWorldSurfaceRenderer;
    private final TopoRenderer topoRenderer;
    private final IChunkRenderer overWorldCaveRenderer;

    public ChunkRenderController()
    {
        netherRenderer = new NetherRenderer();
        endRenderer = new EndRenderer();
        SurfaceRenderer surfaceRenderer = new SurfaceRenderer();
        overWorldSurfaceRenderer = surfaceRenderer;
        overWorldCaveRenderer = new CaveRenderer(surfaceRenderer);
        topoRenderer = new TopoRenderer();
    }

    public boolean renderChunk(RegionCoord rCoord, MapType mapType, ChunkMD chunkMd)
    {
        if (!JourneymapClient.getInstance().isMapping())
        {
            return false;
        }

        ChunkPainter undergroundG2D = null;
        ChunkPainter cpDay = null;
        ChunkPainter cpNight = null;
        ChunkPainter cpTopo = null;
        boolean renderOkay = false;
        boolean mapTopo = mapType.isTopo() || (!mapType.isUnderground() && JourneymapClient.getCoreProperties().mapTopography.get());

        try
        {
            RegionImageSet regionImageSet = RegionImageCache.instance().getRegionImageSet(rCoord);
            if (mapType.isUnderground())
            {
                BufferedImage image = regionImageSet.getChunkImage(chunkMd, mapType);
                if (image != null)
                {
                    undergroundG2D = new ChunkPainter(image);
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
                        regionImageSet.setChunkImage(chunkMd, mapType, image);
                    }
                }
            }
            else
            {
                BufferedImage imageDay = null;
                BufferedImage imageNight = null;
                BufferedImage imageTopo = null;

                if (!mapType.isTopo())
                {
                    imageDay = regionImageSet.getChunkImage(chunkMd, MapType.day(rCoord.dimension));
                    imageNight = regionImageSet.getChunkImage(chunkMd, MapType.night(rCoord.dimension));
                }
                if (mapTopo)
                {
                    imageTopo = regionImageSet.getChunkImage(chunkMd, MapType.topo(rCoord.dimension));
                }

                if (imageDay != null)
                {
                    cpDay = new ChunkPainter(imageDay);
                }

                if (imageNight != null)
                {
                    cpNight = new ChunkPainter(imageNight);
                }

                if (imageTopo != null)
                {
                    cpTopo = new ChunkPainter(imageTopo);
                }

                if (cpDay != null)
                {
                    renderOkay = overWorldSurfaceRenderer.render(cpDay, cpNight, chunkMd);
                    if (renderOkay)
                    {
                        regionImageSet.setChunkImage(chunkMd, MapType.day(rCoord.dimension), imageDay);
                        regionImageSet.setChunkImage(chunkMd, MapType.night(rCoord.dimension), imageNight);
                        chunkMd.setRendered();
                    }
                }

                if (cpTopo != null)
                {
                    renderOkay = topoRenderer.render(cpTopo, chunkMd, null);
                    if (renderOkay)
                    {
                        regionImageSet.setChunkImage(chunkMd, MapType.topo(rCoord.dimension), imageTopo);
                        chunkMd.setRendered();
                    }
                }
            }
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            Journeymap.getLogger().log(Level.WARN, LogFormatter.toString(e));
            return false; // Can happen when server isn't connected, just wait for next tick
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error("Unexpected error in ChunkRenderController: " + (LogFormatter.toString(t)));
        }
        finally
        {
            if (cpDay != null)
            {
                cpDay.finishPainting();
            }
            if (cpNight != null)
            {
                cpNight.finishPainting();
            }
            if (undergroundG2D != null)
            {
                undergroundG2D.finishPainting();
            }
            if (cpTopo != null)
            {
                cpTopo.finishPainting();
            }
        }

        if (!renderOkay)
        {
            if (Journeymap.getLogger().isDebugEnabled())
            {
                Journeymap.getLogger().debug("Chunk render failed: %s / %s / %s", rCoord, chunkMd, mapType);
            }
        }

        return renderOkay;
    }
}
