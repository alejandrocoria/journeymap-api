/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.cartography;

import journeymap.client.cartography.render.*;
import journeymap.client.model.*;
import journeymap.client.render.ComparableBufferedImage;
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
    private final SurfaceRenderer overWorldSurfaceRenderer;
    private final BaseRenderer netherRenderer;
    private final SurfaceRenderer endSurfaceRenderer;
    private final BaseRenderer endCaveRenderer;
    private final BaseRenderer topoRenderer;
    private final BaseRenderer overWorldCaveRenderer;

    public ChunkRenderController()
    {
        overWorldSurfaceRenderer = new SurfaceRenderer();
        overWorldCaveRenderer = new CaveRenderer(overWorldSurfaceRenderer);
        netherRenderer = new NetherRenderer();
        endSurfaceRenderer = new EndSurfaceRenderer();
        endCaveRenderer = new EndCaveRenderer(endSurfaceRenderer);
        topoRenderer = new TopoRenderer();
    }

    /**
     * Get the renderer that would be used for the given params.
     */
    public BaseRenderer getRenderer(RegionCoord rCoord, MapType mapType, ChunkMD chunkMd)
    {
        try
        {
            RegionImageSet regionImageSet = RegionImageCache.INSTANCE.getRegionImageSet(rCoord);
            if (mapType.isUnderground())
            {
                BufferedImage image = regionImageSet.getChunkImage(chunkMd, mapType);
                if (image != null)
                {
                    switch (rCoord.dimension)
                    {
                        case -1:
                        {
                            return netherRenderer;
                        }
                        case 1:
                        {
                            return endCaveRenderer;
                        }
                        default:
                        {
                            return overWorldCaveRenderer;
                        }
                    }
                }
            }
            else
            {
                return overWorldSurfaceRenderer;
            }
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error("Unexpected error in ChunkRenderController: " + (LogFormatter.toPartialString(t)));

        }
        return null;
    }

    public boolean renderChunk(RegionCoord rCoord, MapType mapType, ChunkMD chunkMd)
    {
        if (!Journeymap.getClient().isMapping())
        {
            return false;
        }

        boolean renderOkay = false;

        try
        {
            RegionImageSet regionImageSet = RegionImageCache.INSTANCE.getRegionImageSet(rCoord);
            if (mapType.isUnderground())
            {
                ComparableBufferedImage chunkSliceImage = regionImageSet.getChunkImage(chunkMd, mapType);
                if (chunkSliceImage != null)
                {
                    switch (rCoord.dimension)
                    {
                        case -1:
                        {
                            renderOkay = netherRenderer.render(chunkSliceImage, chunkMd, mapType.vSlice);
                            break;
                        }
                        case 1:
                        {
                            renderOkay = endCaveRenderer.render(chunkSliceImage, chunkMd, mapType.vSlice);
                            break;
                        }
                        default:
                        {
                            renderOkay = overWorldCaveRenderer.render(chunkSliceImage, chunkMd, mapType.vSlice);
                        }
                    }

                    if (renderOkay)
                    {
                        regionImageSet.setChunkImage(chunkMd, mapType, chunkSliceImage);
                    }
                }
            }
            else
            {
                if (mapType.isTopo())
                {
                    ComparableBufferedImage imageTopo = regionImageSet.getChunkImage(chunkMd, MapType.topo(rCoord.dimension));
                    renderOkay = topoRenderer.render(imageTopo, chunkMd, null);
                    if (renderOkay)
                    {
                        regionImageSet.setChunkImage(chunkMd, MapType.topo(rCoord.dimension), imageTopo);
                    }
                }
                else
                {
                    ComparableBufferedImage imageDay = regionImageSet.getChunkImage(chunkMd, MapType.day(rCoord.dimension));
                    ComparableBufferedImage imageNight = regionImageSet.getChunkImage(chunkMd, MapType.night(rCoord.dimension));
                    renderOkay = overWorldSurfaceRenderer.render(imageDay, imageNight, chunkMd);
                    if (renderOkay)
                    {
                        regionImageSet.setChunkImage(chunkMd, MapType.day(rCoord.dimension), imageDay);
                        regionImageSet.setChunkImage(chunkMd, MapType.night(rCoord.dimension), imageNight);
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

        if (!renderOkay)
        {
            if (Journeymap.getLogger().isDebugEnabled())
            {
                Journeymap.getLogger().debug(String.format("Chunk %s render failed for %s", chunkMd.getCoord(),mapType));
            }
        }

        return renderOkay;
    }
}
