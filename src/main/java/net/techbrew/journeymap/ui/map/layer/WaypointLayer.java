package net.techbrew.journeymap.ui.map.layer;

import net.minecraft.client.Minecraft;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;
import net.minecraft.world.chunk.Chunk;
import net.techbrew.journeymap.data.WaypointsData;
import net.techbrew.journeymap.model.BlockCoordIntPair;
import net.techbrew.journeymap.model.Waypoint;
import net.techbrew.journeymap.render.draw.DrawStep;
import net.techbrew.journeymap.render.draw.DrawWayPointStep;
import net.techbrew.journeymap.ui.UIManager;
import net.techbrew.journeymap.ui.map.MapOverlay;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Waypoint selection/creation.
 */
public class WaypointLayer implements LayerDelegate.Layer
{
    private final long hoverDelay = 100;
    private final List<DrawStep> drawStepList = new ArrayList<DrawStep>(1);

    BlockCoordIntPair lastCoord = null;
    long lastClicked = 0;
    long startHover = 0;

    DrawWayPointStep selectedWaypointStep = null;
    Waypoint selected = null;


    public WaypointLayer()
    {
    }

    @Override
    public List<DrawStep> onMouseMove(Minecraft mc, double mouseX, double mouseY, int gridWidth, int gridHeight, BlockCoordIntPair blockCoord)
    {
        drawStepList.clear();

        if (!WaypointsData.isNativeEnabled())
        {
            return drawStepList;
        }

        if (lastCoord == null)
        {
            lastCoord = blockCoord;
        }

        long now = System.currentTimeMillis();

        // Get search area
        int proximity = getProximity();
        AxisAlignedBB area = AxisAlignedBB.getBoundingBox(blockCoord.x - proximity, -1, blockCoord.z - proximity,
                blockCoord.x + proximity, mc.theWorld.getActualHeight() + 1, blockCoord.z + proximity);

        if (!lastCoord.equals(blockCoord))
        {
            if (!area.isVecInside(Vec3.createVectorHelper(lastCoord.x, 1, lastCoord.z)))
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
        Collection<Waypoint> waypoints = WaypointsData.getCachedWaypoints();
        ArrayList<Waypoint> proximal = new ArrayList<Waypoint>();
        for (Waypoint waypoint : waypoints)
        {
            if (!waypoint.isReadOnly() && waypoint.isEnable() && waypoint.isInPlayerDimension())
            {
                if (area.isVecInside(Vec3.createVectorHelper(waypoint.getX(dimension), waypoint.getY(dimension), waypoint.getZ(dimension))))
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
    public List<DrawStep> onMouseClick(Minecraft mc, double mouseX, double mouseY, int gridWidth, int gridHeight, BlockCoordIntPair blockCoord)
    {
        if (!WaypointsData.isNativeEnabled())
        {
            return drawStepList;
        }

        // check for double-click
        long sysTime = Minecraft.getSystemTime();
        boolean doubleClick = sysTime - this.lastClicked < 450L;
        this.lastClicked = sysTime;
        if (!doubleClick)
        {
            return drawStepList;
        }

        // Edit selected waypoint
        if (selected != null)
        {
            UIManager.getInstance().openWaypointManager(selected);
            return Collections.EMPTY_LIST;
        }

        // Check chunk
        Chunk chunk = mc.theWorld.getChunkFromChunkCoords(blockCoord.x >> 4, blockCoord.z >> 4);
        int y = -1;
        if (!chunk.isEmpty())
        {
            y = Math.max(1, chunk.getHeightValue(blockCoord.x & 15, blockCoord.z & 15));
        }

        // Create waypoint
        ChunkCoordinates cc = new ChunkCoordinates(blockCoord.x, y, blockCoord.z);
        Waypoint waypoint = Waypoint.at(cc, Waypoint.Type.Normal, mc.thePlayer.dimension);
        UIManager.getInstance().openWaypointEditor(waypoint, true, MapOverlay.class);

        return Collections.EMPTY_LIST;
    }

    private void sortByDistance(List<Waypoint> waypoints, final BlockCoordIntPair blockCoord, final int dimension)
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
                double dx = waypoint.getX(dimension) - blockCoord.x;
                double dz = waypoint.getZ(dimension) - blockCoord.z;
                return (Math.sqrt(dx * dx + dz * dz));
            }
        });
    }

    private void select(Waypoint waypoint)
    {
        selected = waypoint;
        selectedWaypointStep = new DrawWayPointStep(waypoint, waypoint.getColor(), Color.white, true);
        drawStepList.add(selectedWaypointStep);
    }

    private int getProximity()
    {
        int blockSize = (int) Math.max(1, Math.pow(2, MapOverlay.state().currentZoom));
        return Math.max(1, 8 / blockSize);
    }
}