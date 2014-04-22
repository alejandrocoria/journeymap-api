package net.techbrew.journeymap.render.overlay;

import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.model.Waypoint;
import net.techbrew.journeymap.render.draw.DrawStep;
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
    public List<DrawStep> prepareSteps(List<Waypoint> waypoints, GridRenderer grid)
    {
        final List<DrawStep> drawStepList = new ArrayList<DrawStep>();
        try
        {
            for (Waypoint waypoint : waypoints)
            {
                if (waypoint.isEnable())
                {
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
