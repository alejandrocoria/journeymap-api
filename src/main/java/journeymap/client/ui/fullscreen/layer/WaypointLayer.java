/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.ui.fullscreen.layer;

import journeymap.client.cartography.color.RGB;
import journeymap.client.data.DataCache;
import journeymap.client.data.WaypointsData;
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
import net.minecraft.util.math.Vec3d;
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
    /**
     * The Last coord.
     */
    BlockPos lastCoord = null;

    /**
     * The Start hover.
     */
    long startHover = 0;

    /**
     * The Selected waypoint step.
     */
    DrawWayPointStep selectedWaypointStep = null;
    /**
     * The Selected.
     */
    Waypoint selected = null;

    /**
     * Instantiates a new Waypoint layer.
     */
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

        AxisAlignedBB area = new AxisAlignedBB(blockCoord.getX() - proximity, -1, blockCoord.getZ() - proximity,
                blockCoord.getX() + proximity, mc.world.getActualHeight() + 1, blockCoord.getZ() + proximity);

        if (!lastCoord.equals(blockCoord))
        {
            if (!area.isVecInside(new Vec3d(lastCoord.getX(), 1, lastCoord.getZ())))
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

        int dimension = mc.player.dimension;

        // check for existing
        Collection<Waypoint> waypoints = DataCache.INSTANCE.getWaypoints(false);
        ArrayList<Waypoint> proximal = new ArrayList<Waypoint>();
        for (Waypoint waypoint : waypoints)
        {
            if (waypoint.isEnable() && waypoint.isInPlayerDimension())
            {
                if (area.isVecInside(new Vec3d(waypoint.getX(), waypoint.getY(), waypoint.getZ())))
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
                UIManager.INSTANCE.openWaypointManager(selected, new Fullscreen()); // TODO: This could be a problem
                return drawStepList;
            }
            else
            {
                // Create waypoint
                Waypoint waypoint = Waypoint.at(blockCoord, Waypoint.Type.Normal, mc.player.dimension);
                UIManager.INSTANCE.openWaypointEditor(waypoint, true, new Fullscreen()); // TODO: This could be a problem
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

    /**
     * The type Block outline draw step.
     */
    class BlockOutlineDrawStep implements DrawStep
    {
        /**
         * The Block coord.
         */
        BlockPos blockCoord;
        /**
         * The Pixel.
         */
        Point2D.Double pixel;

        /**
         * Instantiates a new Block outline draw step.
         *
         * @param blockCoord the block coord
         */
        BlockOutlineDrawStep(BlockPos blockCoord)
        {
            this.blockCoord = blockCoord;
        }

        @Override
        public void draw(Pass pass, double xOffset, double yOffset, GridRenderer gridRenderer, double fontScale, double rotation)
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
