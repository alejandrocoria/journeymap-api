package journeymap.client.api.display;

import journeymap.client.api.model.MapImage;

import java.util.Set;

/**
 * Values related to displaying a Waypoint.
 */
public interface IWaypointDisplay
{
    Integer getColor();

    Integer getBackgroundColor();

    Set<Integer> getDisplayDimensions();

    MapImage getIcon();
}
