/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.ui.fullscreen.layer;

import journeymap.client.cartography.ChunkRenderController;
import journeymap.client.cartography.render.BaseRenderer;
import journeymap.client.data.DataCache;
import journeymap.client.io.FileHandler;
import journeymap.client.model.ChunkMD;
import journeymap.client.model.RegionCoord;
import journeymap.client.render.draw.DrawStep;
import journeymap.client.render.map.GridRenderer;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Delegates mouse actions in MapOverlay to Layer impls.
 */
public class LayerDelegate
{
    long lastClick = 0;
    private List<DrawStep> drawSteps = new ArrayList<DrawStep>();
    private List<Layer> layers = new ArrayList<Layer>();

    public LayerDelegate()
    {
        layers.add(new ModOverlayLayer());
        layers.add(new BlockInfoLayer());
        layers.add(new WaypointLayer());
        layers.add(new KeybindingInfoLayer());
    }

    public void onMouseMove(Minecraft mc, GridRenderer gridRenderer, Point2D.Double mousePosition, float fontScale)
    {
        BlockPos blockCoord = getBlockPos(mc, gridRenderer, mousePosition);

        drawSteps.clear();
        for (Layer layer : layers)
        {
            try
            {
                drawSteps.addAll(layer.onMouseMove(mc, gridRenderer, mousePosition, blockCoord, fontScale));
            }
            catch (Exception e)
            {
                Journeymap.getLogger().error(LogFormatter.toString(e));
            }
        }
    }

    public void onMouseClicked(Minecraft mc, GridRenderer gridRenderer, Point2D.Double mousePosition, int button, float fontScale)
    {
        BlockPos blockCoord = gridRenderer.getBlockAtPixel(mousePosition);

        // check for double-click
        long sysTime = Minecraft.getSystemTime();
        boolean doubleClick = sysTime - this.lastClick < 450L;
        this.lastClick = sysTime;

        drawSteps.clear();
        for (Layer layer : layers)
        {
            try
            {
                drawSteps.addAll(layer.onMouseClick(mc, gridRenderer, mousePosition, blockCoord, button, doubleClick, fontScale));
                if (!layer.propagateClick())
                {
                    break;
                }
            }
            catch (Exception e)
            {
                Journeymap.getLogger().error(LogFormatter.toString(e));
            }
        }
    }

    public BlockPos getBlockPos(Minecraft mc, GridRenderer gridRenderer, Point2D.Double mousePosition)
    {
        BlockPos seaLevel = gridRenderer.getBlockAtPixel(mousePosition);
        ChunkMD chunkMD = DataCache.INSTANCE.getChunkMD(seaLevel);
        if (chunkMD != null)
        {
            ChunkRenderController crc = Journeymap.getClient().getChunkRenderController();
            if (crc != null)
            {
                ChunkPos chunkCoord = chunkMD.getCoord();
                RegionCoord rCoord = RegionCoord.fromChunkPos(FileHandler.getJMWorldDir(mc), gridRenderer.getMapType(), chunkCoord.x, chunkCoord.z);
                BaseRenderer chunkRenderer = crc.getRenderer(rCoord, gridRenderer.getMapType(), chunkMD);
                int blockY = chunkRenderer.getBlockHeight(chunkMD, seaLevel);
                return new BlockPos(seaLevel.getX(), blockY, seaLevel.getZ());
            }
        }
        return seaLevel;
    }

    public List<DrawStep> getDrawSteps()
    {
        return drawSteps;
    }

    public interface Layer
    {
        public List<DrawStep> onMouseMove(Minecraft mc, GridRenderer gridRenderer, Point2D.Double mousePosition, BlockPos blockCoord, float fontScale);

        public List<DrawStep> onMouseClick(Minecraft mc, GridRenderer gridRenderer, Point2D.Double mousePosition, BlockPos blockCoord, int button, boolean doubleClick, float fontScale);

        public boolean propagateClick();
    }

}
