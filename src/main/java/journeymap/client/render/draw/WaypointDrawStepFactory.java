/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.render.draw;

import journeymap.client.data.DataCache;
import journeymap.client.model.Waypoint;
import journeymap.client.render.map.GridRenderer;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Renders waypoints in the MapOverlay.
 *
 * @author techbrew
 */
public class WaypointDrawStepFactory
{
    final List<DrawWayPointStep> drawStepList = new ArrayList<DrawWayPointStep>();

    public List<DrawWayPointStep> prepareSteps(Collection<Waypoint> waypoints, GridRenderer grid, boolean checkDistance, boolean showLabel)
    {
        Minecraft mc = FMLClientHandler.instance().getClient();
        EntityPlayer player = mc.thePlayer;
        int dimension = player.dimension;
        int maxDistance = Journeymap.getClient().getWaypointProperties().maxDistance.get();
        checkDistance = checkDistance && maxDistance > 0;
        Vec3d playerVec = checkDistance ? player.getPositionVector() : null;
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

                    DrawWayPointStep wayPointStep = DataCache.INSTANCE.getDrawWayPointStep(waypoint);
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
            Journeymap.getLogger().error("Error during prepareSteps: " + LogFormatter.toString(t));
        }

        return drawStepList;
    }
}
