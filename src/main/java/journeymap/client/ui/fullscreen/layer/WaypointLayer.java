/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.ui.fullscreen.layer;

import journeymap.client.JourneymapClient;
import journeymap.client.cartography.RGB;
import journeymap.client.data.DataCache;
import journeymap.client.data.WaypointsData;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.model.ChunkMD;
import journeymap.client.model.Waypoint;
import journeymap.client.properties.FullMapProperties;
import journeymap.client.render.draw.DrawStep;
import journeymap.client.render.draw.DrawUtil;
import journeymap.client.render.draw.DrawWayPointStep;
import journeymap.client.render.map.GridRenderer;
import journeymap.client.ui.UIManager;
import journeymap.client.ui.fullscreen.Fullscreen;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.ChunkCoordIntPair;
import org.lwjgl.input.Mouse;

import java.awt.geom.Point2D;
import java.util.*;

/**
 * Waypoint selection/creation.
 */
public class WaypointLayer implements LayerDelegate.Layer
{
    private final long hoverDelay = 100;
    private final List<DrawStep> drawStepList = new ArrayList<DrawStep>(1);
    private final BlockOutlineDrawStep clickDrawStep = new BlockOutlineDrawStep(new BlockPos(0, 0, 0));
    BlockPos lastCoord = null;

    long lastClick = 0;
    long startHover = 0;

    DrawWayPointStep selectedWaypointStep = null;
    Waypoint selected = null;


    public WaypointLayer()
    {
    }

    @Override
    public List<DrawStep> onMouseMove(Minecraft mc, GridRenderer gridRenderer, Point2D.Double mousePosition, BlockPos blockCoord, float fontScale)
    {
        if (!WaypointsData.isManagerEnabled())
        {
            return Collections.EMPTY_LIST;
        }

        drawStepList.clear();
        drawStepList.add(clickDrawStep);

        if (lastCoord == null)
        {
            lastCoord = blockCoord;
        }

        long now = Minecraft.getSystemTime();

        // Add click draw step
        if (!blockCoord.equals(clickDrawStep.blockCoord))
        {
            unclick();
        }

        // Get search area
        int proximity = getProximity();

        AxisAlignedBB area = ForgeHelper.INSTANCE.getBoundingBox(blockCoord.getX() - proximity, -1, blockCoord.getZ() - proximity,
                blockCoord.getX() + proximity, mc.theWorld.getActualHeight() + 1, blockCoord.getZ() + proximity);

        if (!lastCoord.equals(blockCoord))
        {
            if (!area.isVecInside(ForgeHelper.INSTANCE.newVec3(lastCoord.getX(), 1, lastCoord.getZ())))
            {
                selected = null;
                lastCoord = blockCoord;
                startHover = now;
                return Collections.EMPTY_LIST;
            }
        }
        else
        {
            if (selected != null)
            {
                select(selected);
                return drawStepList;
            }
        }

        if (now - startHover < hoverDelay)
        {
            return Collections.EMPTY_LIST;
        }

        int dimension = mc.thePlayer.dimension;

        // check for existing
        Collection<Waypoint> waypoints = DataCache.instance().getWaypoints(false);
        ArrayList<Waypoint> proximal = new ArrayList<Waypoint>();
        for (Waypoint waypoint : waypoints)
        {
            if (waypoint.isEnable() && waypoint.isInPlayerDimension())
            {
                if (area.isVecInside(ForgeHelper.INSTANCE.newVec3(waypoint.getX(), waypoint.getY(), waypoint.getZ())))
                {
                    proximal.add(waypoint);
                }
            }
        }

        if (!proximal.isEmpty())
        {
            if (proximal.size() > 1)
            {
                sortByDistance(proximal, blockCoord, dimension);
            }
            select(proximal.get(0));
        }

        return drawStepList;
    }

    @Override
    public List<DrawStep> onMouseClick(Minecraft mc, GridRenderer gridRenderer, Point2D.Double mousePosition, BlockPos blockCoord, int button, boolean doubleClick, float fontScale)
    {
        if (!WaypointsData.isManagerEnabled())
        {
            return Collections.EMPTY_LIST;
        }

        if (!drawStepList.contains(clickDrawStep))
        {
            drawStepList.add(clickDrawStep);
        }

        if (!doubleClick || !blockCoord.equals(clickDrawStep.blockCoord))
        {
            clickDrawStep.blockCoord = blockCoord;
            return drawStepList;
        }

        // Edit selected waypoint
        if (selected != null)
        {
            UIManager.getInstance().openWaypointManager(selected, new Fullscreen()); // TODO: This could be a problem
            return drawStepList;
        }

        // Check chunk
        ChunkMD chunkMD = DataCache.instance().getChunkMD(new ChunkCoordIntPair(blockCoord.getX() >> 4, blockCoord.getZ() >> 4));
        int y = -1;
        if (chunkMD != null && !chunkMD.getChunk().isEmpty())
        {
            y = Math.max(1, chunkMD.getPrecipitationHeight(blockCoord.getX() & 15, blockCoord.getZ() & 15));
        }

        // Create waypoint
        BlockPos blockPos = new BlockPos(blockCoord.getX(), 0, blockCoord.getZ());
        Waypoint waypoint = Waypoint.at(blockPos, Waypoint.Type.Normal, mc.thePlayer.dimension);
        UIManager.getInstance().openWaypointEditor(waypoint, true, new Fullscreen()); // TODO: This could be a problem

        return drawStepList;
    }

    @Override
    public boolean propagateClick()
    {
        return true;
    }

    private void sortByDistance(List<Waypoint> waypoints, final BlockPos blockCoord, final int dimension)
    {
        Collections.sort(waypoints, new Comparator<Waypoint>()
        {
            @Override
            public int compare(Waypoint o1, Waypoint o2)
            {
                return Double.compare(getDistance(o1), getDistance(o2));
            }

            private double getDistance(Waypoint waypoint)
            {
                double dx = waypoint.getX() - blockCoord.getX();
                double dz = waypoint.getZ() - blockCoord.getZ();
                return (Math.sqrt(dx * dx + dz * dz));
            }
        });
    }

    private void select(Waypoint waypoint)
    {
        selected = waypoint;
        selectedWaypointStep = new DrawWayPointStep(waypoint, waypoint.getColor(), RGB.WHITE_RGB, true);
        drawStepList.add(selectedWaypointStep);
    }

    private int getProximity()
    {
        FullMapProperties fullMapProperties = JourneymapClient.getFullMapProperties();
        int blockSize = (int) Math.max(1, Math.pow(2, fullMapProperties.zoomLevel.get()));
        return Math.max(1, 8 / blockSize);
    }

    private void unclick()
    {
        clickDrawStep.blockCoord = new BlockPos(Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
        drawStepList.remove(clickDrawStep);
    }

    class BlockOutlineDrawStep implements DrawStep
    {
        BlockPos blockCoord;

        BlockOutlineDrawStep(BlockPos blockCoord)
        {
            this.blockCoord = blockCoord;
        }

        @Override
        public void draw(double xOffset, double yOffset, GridRenderer gridRenderer, float drawScale, double fontScale, double rotation)
        {

            if (Mouse.isButtonDown(0))
            {
                return;
            }

            if (xOffset != 0 || yOffset != 0)
            {
                unclick();
                return;
            }

            double x = blockCoord.getX();
            double z = blockCoord.getZ();
            double size = Math.pow(2, gridRenderer.getZoom());
            double thick = gridRenderer.getZoom() < 2 ? 1 : 2;

            Point2D.Double pixel = gridRenderer.getBlockPixelInGrid(x, z);
            pixel.setLocation(pixel.getX() + xOffset, gridRenderer.getHeight() - pixel.getY() + yOffset);
            if (gridRenderer.isOnScreen(pixel))
            {
                DrawUtil.drawRectangle(pixel.getX() - (thick * thick), pixel.getY() - (thick * thick), size + (thick * 4), thick, RGB.BLACK_RGB, 150);
                DrawUtil.drawRectangle(pixel.getX() - thick, pixel.getY() - thick, size + (thick * thick), thick, RGB.WHITE_RGB, 255);

                DrawUtil.drawRectangle(pixel.getX() - (thick * thick), pixel.getY() - thick, thick, size + (thick * thick), RGB.BLACK_RGB, 150);
                DrawUtil.drawRectangle(pixel.getX() - thick, pixel.getY(), thick, size, RGB.WHITE_RGB, 255);

                DrawUtil.drawRectangle(pixel.getX() + size, pixel.getY(), thick, size, RGB.WHITE_RGB, 255);
                DrawUtil.drawRectangle(pixel.getX() + size + thick, pixel.getY() - thick, thick, size + (thick * thick), RGB.BLACK_RGB, 150);

                DrawUtil.drawRectangle(pixel.getX() - thick, pixel.getY() + size, size + (thick * thick), thick, RGB.WHITE_RGB, 255);
                DrawUtil.drawRectangle(pixel.getX() - (thick * thick), pixel.getY() + size + thick, size + (thick * 4), thick, RGB.BLACK_RGB, 150);
            }
        }

        @Override
        public int getDisplayOrder()
        {
            return 0;
        }

        @Override
        public String getModId()
        {
            return Journeymap.MOD_ID;
        }
    }
}
