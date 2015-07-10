/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package net.techbrew.journeymap.render.draw;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.data.DataCache;
import net.techbrew.journeymap.forge.helper.ForgeHelper;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.model.Waypoint;
import net.techbrew.journeymap.render.map.GridRenderer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Renders waypoints in the MapOverlay.
 *
 * @author mwoodman
 */
public class WaypointDrawStepFactory
{
    final List<DrawWayPointStep> drawStepList = new ArrayList<DrawWayPointStep>();

    public List<DrawWayPointStep> prepareSteps(Collection<Waypoint> waypoints, GridRenderer grid, boolean checkDistance, boolean showLabel)
    {
        Minecraft mc = ForgeHelper.INSTANCE.getClient();
        EntityPlayer player = mc.thePlayer;
        int dimension = player.dimension;
        int maxDistance = JourneyMap.getWaypointProperties().maxDistance.get();
        checkDistance = checkDistance && maxDistance > 0;
        Vec3 playerVec = checkDistance ? ForgeHelper.INSTANCE.getEntityPositionVector(player) : null;
        drawStepList.clear();

        try
        {
            for (Waypoint waypoint : waypoints)
            {
                if (waypoint.isEnable() && waypoint.isInPlayerDimension())
                {
                    if (checkDistance)
                    {
                        // Get view distance from waypoint
                        final double actualDistance = playerVec.distanceTo(waypoint.getPosition());
                        if (actualDistance > maxDistance)
                        {
                            continue;
                        }
                    }

                    DrawWayPointStep wayPointStep = DataCache.instance().getDrawWayPointStep(waypoint);
                    if (wayPointStep != null)
                    {
                        drawStepList.add(wayPointStep);
                        wayPointStep.setShowLabel(showLabel);
                    }
                }
            }
        }
        catch (Throwable t)
        {
            JourneyMap.getLogger().error("Error during prepareSteps: " + LogFormatter.toString(t));
        }

        return drawStepList;
    }
}
