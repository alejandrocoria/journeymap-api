/*
 *
 * JourneyMap API
 * http://journeymap.info
 * http://bitbucket.org/TeamJM/journeymap-api
 *
 * Copyright (c) 2011-2015 Techbrew.  All Rights Reserved.
 * The following limited rights are granted to you:
 *
 * You MAY:
 *   + Write your own code that uses this API source code as a dependency.
 *   + Distribute compiled classes of unmodified API source code which your code depends upon.
 *   + Fork and modify API source code for the purpose of submitting Pull Requests to the
 *        TeamJM/journeymap-api repository.  Submitting new or modified code to the repository
 *        means that you are granting Techbrew all rights over the code.
 *
 * You MAY NOT:
 *   - Submit any code to the TeamJM/journeymap-api repository with a different license than this one.
 *   - Distribute modified versions of the API source code or compiled artifacts of  modified API
 *        source code.  In this context, "modified" means changes which have not been both approved
 *        and merged into the TeamJM/journeymap-api repository.
 *   - Use or distribute the API code in any way not explicitly granted by this license statement.
 *
 */

package journeymap.api.client.v1;

import com.google.common.base.Verify;

/**
 * Specification defining how a waypoint will be suggested to a user.
 */
public class WaypointDefinition
{
    private String waypointId;
    private String waypointGroupName;
    private String waypointName;
    private MapPoint point;
    private int[] dimensions;
    private int color;
    private MapIcon icon;

    /**
     * Constructor.
     *
     * @param waypointId        Unique id for waypoint (scoped to your mod)
     * @param waypointGroupName (Optional) Group or category name for the waypoint.
     * @param waypointName      Waypoint name.
     * @param point             Waypoint location.
     * @param icon              (Optional) Icon to display at the point.
     * @param color             rgb color of waypoint label
     * @param dimensions        Dimensions where waypoint should be displayed.
     */
    public WaypointDefinition(String waypointId, String waypointGroupName, String waypointName, MapPoint point, MapIcon icon, int color, int[] dimensions)
    {
        Verify.verifyNotNull(waypointId);
        Verify.verifyNotNull(waypointName);
        Verify.verifyNotNull(point);
        Verify.verifyNotNull(icon);

        this.waypointId = waypointId;
        this.waypointGroupName = waypointGroupName;
        this.waypointName = waypointName;
        this.point = point;
        this.icon = icon;
        this.color = Math.max(0x000000, Math.min(color, 0xffffff));
        this.dimensions = dimensions;
    }

    /**
     * Unique id (scoped to your mod)
     */
    public String getWaypointId()
    {
        return waypointId;
    }

    /**
     * (Optional) Group or category name for the waypoint.
     */
    public String getWaypointGroupName()
    {
        return waypointGroupName;
    }

    /**
     * Waypoint name.
     */
    public String getWaypointName()
    {
        return waypointName;
    }

    /**
     * Waypoint location.
     */
    public MapPoint getPoint()
    {
        return point;
    }

    /**
     * Color for waypoint label.
     *
     * @return rgb int
     */
    public int getColor()
    {
        return color;
    }

    /**
     * Dimensions where waypoint should be displayed.
     */
    public int[] getDimensions()
    {
        return dimensions;
    }

    /**
     * Icon specification for waypoint.
     *
     * @return spec
     */
    public MapIcon getIcon()
    {
        return icon;
    }
}