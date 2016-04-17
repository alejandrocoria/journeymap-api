/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.ui.fullscreen.layer;

import journeymap.client.cartography.RGB;
import journeymap.client.data.DataCache;
import journeymap.client.data.WaypointsData;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.model.Waypoint;
import journeymap.client.render.draw.DrawStep;
import journeymap.client.render.draw.DrawUtil;
import journeymap.client.render.draw.DrawWayPointStep;
import journeymap.client.render.map.GridRenderer;
import journeymap.client.ui.UIManager;
import journeymap.client.ui.fullscreen.Fullscreen;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.input.Mouse;

import java.awt.geom.Point2D;
import java.util.*;

/**
 * Waypoint selection/creation.
 */
public class WaypointLayer implements LayerDelegate.Layer
{
    private final long hoverDelay = 100;
    private final List<DrawStep> drawStepList;
    private final BlockOutlineDrawStep clickDrawStep;
    BlockPos lastCoord = null;

    long startHover = 0;

    DrawWayPointStep selectedWaypointStep = null;
    Waypoint selected = null;

    public WaypointLayer()
    {
        drawStepList = new ArrayList<DrawStep>(1);
        clickDrawStep = new BlockOutlineDrawStep(new BlockPos(0, 0, 0));
    }

    @Override
    public List<DrawStep> onMouseMove(Minecraft mc, GridRenderer gridRenderer, Point2D.Double mousePosition, BlockPos blockCoord, float fontScale)
    {
        drawStepList.clear();

        if (!WaypointsData.isManagerEnabled())
        {
            return drawStepList;
        }

        if (lastCoord == null)
        {
            lastCoord = blockCoord;
        }

        long now = Minecraft.getSystemTime();

        // Be generous to allow shaky user movement
        int proximity = (int) Math.max(1, 8 / gridRenderer.getUIState().blockSize);

        if (clickDrawStep.blockCoord != null && !blockCoord.equals(clickDrawStep.blockCoord))
        {
            unclick();
        }
        else
        {
            drawStepList.add(clickDrawStep);
        }

        AxisAlignedBB area = ForgeHelper.INSTANCE.getBoundingBox(blockCoord.getX() - proximity, -1, blockCoord.getZ() - proximity,
                blockCoord.getX() + proximity, mc.theWorld.getActualHeight() + 1, blockCoord.getZ() + proximity);

        if (!lastCoord.equals(blockCoord))
        {
            if (!area.isVecInside(ForgeHelper.INSTANCE.newVec3(lastCoord.getX(), 1, lastCoord.getZ())))
            {
                selected = null;
                lastCoord = blockCoord;
                startHover = now;
                return drawStepList;
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
            return drawStepList;
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
            return drawStepList;
        }

        if (!drawStepList.contains(clickDrawStep))
        {
            drawStepList.add(clickDrawStep);
        }

        if (!doubleClick)
        {
            click(gridRenderer, blockCoord);
        }
        else
        {
            // Edit selected waypoint
            if (selected != null)
            {
                UIManager.getInstance().openWaypointManager(selected, new Fullscreen()); // TODO: This could be a problem
                return drawStepList;
            }
            else
            {
                // Create waypoint
                Waypoint waypoint = Waypoint.at(blockCoord, Waypoint.Type.Normal, mc.thePlayer.dimension);
                UIManager.getInstance().openWaypointEditor(waypoint, true, new Fullscreen()); // TODO: This could be a problem
            }
        }

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

    private void click(GridRenderer gridRenderer, BlockPos blockCoord)
    {
        clickDrawStep.blockCoord = lastCoord = blockCoord;
        clickDrawStep.pixel = gridRenderer.getBlockPixelInGrid(blockCoord);
        if (!drawStepList.contains(clickDrawStep))
        {
            drawStepList.add(clickDrawStep);
        }
    }

    private void unclick()
    {
        clickDrawStep.blockCoord = null;
        drawStepList.remove(clickDrawStep);
    }

    class BlockOutlineDrawStep implements DrawStep
    {
        BlockPos blockCoord;
        Point2D.Double pixel;

        BlockOutlineDrawStep(BlockPos blockCoord)
        {
            this.blockCoord = blockCoord;
        }

        @Override
        public void draw(Pass pass, double xOffset, double yOffset, GridRenderer gridRenderer, float drawScale, double fontScale, double rotation)
        {
            if (pass != Pass.Object)
            {
                return;
            }

            if (blockCoord == null)
            {
                return;
            }

            if (Mouse.isButtonDown(0))
            {
                return;
            }

            if (xOffset != 0 || yOffset != 0)
            {
                return;
            }

            double size = gridRenderer.getUIState().blockSize;
            double thick = gridRenderer.getZoom() < 2 ? 1 : 2;

            final double x = pixel.x + xOffset;
            final double y = pixel.y + yOffset;

            if (gridRenderer.isOnScreen(pixel))
            {
                DrawUtil.drawRectangle(x - (thick * thick), y - (thick * thick), size + (thick * 4), thick, RGB.BLACK_RGB, .6f);
                DrawUtil.drawRectangle(x - thick, y - thick, size + (thick * thick), thick, RGB.WHITE_RGB, .6f);

                DrawUtil.drawRectangle(x - (thick * thick), y - thick, thick, size + (thick * thick), RGB.BLACK_RGB, .6f);
                DrawUtil.drawRectangle(x - thick, y, thick, size, RGB.WHITE_RGB, .6f);

                DrawUtil.drawRectangle(x + size, y, thick, size, RGB.WHITE_RGB, .6f);
                DrawUtil.drawRectangle(x + size + thick, y - thick, thick, size + (thick * thick), RGB.BLACK_RGB, .6f);

                DrawUtil.drawRectangle(x - thick, y + size, size + (thick * thick), thick, RGB.WHITE_RGB, .6f);
                DrawUtil.drawRectangle(x - (thick * thick), y + size + thick, size + (thick * 4), thick, RGB.BLACK_RGB, .6f);
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
