/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
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
    /**
     * The Last click.
     */
    long lastClick = 0;
    private List<DrawStep> drawSteps = new ArrayList<DrawStep>();
    private List<Layer> layers = new ArrayList<Layer>();

    /**
     * Instantiates a new Layer delegate.
     */
    public LayerDelegate()
    {
        layers.add(new ModOverlayLayer());
        layers.add(new BlockInfoLayer());
        layers.add(new WaypointLayer());
    }

    /**
     * On mouse move.
     *
     * @param mc            the mc
     * @param gridRenderer  the grid renderer
     * @param mousePosition the mouse position
     * @param fontScale     the font scale
     */
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

    /**
     * On mouse clicked.
     *
     * @param mc            the mc
     * @param gridRenderer  the grid renderer
     * @param mousePosition the mouse position
     * @param button        the button
     * @param fontScale     the font scale
     */
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

    private BlockPos getBlockPos(Minecraft mc, GridRenderer gridRenderer, Point2D.Double mousePosition)
    {
        BlockPos seaLevel = gridRenderer.getBlockAtPixel(mousePosition);
        ChunkMD chunkMD = DataCache.INSTANCE.getChunkMD(seaLevel);
        if (chunkMD != null)
        {
            ChunkRenderController crc = Journeymap.getClient().getChunkRenderController();
            if (crc != null)
            {
                ChunkPos chunkCoord = chunkMD.getCoord();
                RegionCoord rCoord = RegionCoord.fromChunkPos(FileHandler.getJMWorldDir(mc), gridRenderer.getMapType(), chunkCoord.chunkXPos, chunkCoord.chunkZPos);
                BaseRenderer chunkRenderer = crc.getRenderer(rCoord, gridRenderer.getMapType(), chunkMD);
                int blockY = chunkRenderer.getBlockHeight(chunkMD, seaLevel);
                return new BlockPos(seaLevel.getX(), blockY, seaLevel.getZ());
            }
        }
        return seaLevel;
    }

    /**
     * Gets draw steps.
     *
     * @return the draw steps
     */
    public List<DrawStep> getDrawSteps()
    {
        return drawSteps;
    }

    /**
     * The interface Layer.
     */
    public interface Layer
    {
        /**
         * On mouse move list.
         *
         * @param mc            the mc
         * @param gridRenderer  the grid renderer
         * @param mousePosition the mouse position
         * @param blockCoord    the block coord
         * @param fontScale     the font scale
         * @return the list
         */
        public List<DrawStep> onMouseMove(Minecraft mc, GridRenderer gridRenderer, Point2D.Double mousePosition, BlockPos blockCoord, float fontScale);

        /**
         * On mouse click list.
         *
         * @param mc            the mc
         * @param gridRenderer  the grid renderer
         * @param mousePosition the mouse position
         * @param blockCoord    the block coord
         * @param button        the button
         * @param doubleClick   the double click
         * @param fontScale     the font scale
         * @return the list
         */
        public List<DrawStep> onMouseClick(Minecraft mc, GridRenderer gridRenderer, Point2D.Double mousePosition, BlockPos blockCoord, int button, boolean doubleClick, float fontScale);

        /**
         * Propagate click boolean.
         *
         * @return the boolean
         */
        public boolean propagateClick();
    }

}
