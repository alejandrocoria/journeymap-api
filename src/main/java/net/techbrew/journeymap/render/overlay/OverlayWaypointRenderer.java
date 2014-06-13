package net.techbrew.journeymap.render.overlay;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.model.Waypoint;
import net.techbrew.journeymap.render.draw.DrawWayPointStep;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders waypoints in the MapOverlay.
 *
 * @author mwoodman
 */
public class OverlayWaypointRenderer
{
    public List<DrawWayPointStep> prepareSteps(List<Waypoint> waypoints, GridRenderer grid, boolean checkDistance)
    {
        Minecraft mc = FMLClientHandler.instance().getClient();
        EntityPlayer player = mc.thePlayer;
        int dimension = player.dimension;
        int maxDistance = JourneyMap.getInstance().waypointProperties.maxDistance.get();
        checkDistance = checkDistance && maxDistance>0;
        Vec3 playerVec = checkDistance ? player.getPosition(1) : null;

        final List<DrawWayPointStep> drawStepList = new ArrayList<DrawWayPointStep>();
        try
        {
            for (Waypoint waypoint : waypoints)
            {
                if (waypoint.isEnable())
                {
                    if(checkDistance)
                    {
                        // Get view distance from waypoint
                        final double actualDistance = playerVec.distanceTo(waypoint.getPosition());
                        if(actualDistance>maxDistance)
                        {
                            continue;
                        }
                    }

                    drawStepList.add(new DrawWayPointStep(waypoint));
                }
            }
        }
        catch (Throwable t)
        {
            JourneyMap.getLogger().severe("Error during prepareSteps: " + LogFormatter.toString(t));
        }

        return drawStepList;
    }
}
