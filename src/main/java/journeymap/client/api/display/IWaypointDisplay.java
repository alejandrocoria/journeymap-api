package journeymap.client.api.display;

import journeymap.client.api.model.MapImage;
import journeymap.client.api.model.MapText;

import java.util.Set;

/**
 * Values related to displaying a Waypoint.
 */
public interface IWaypointDisplay
{
    Set<Integer> getDisplayDimensions();

    MapImage getIcon();

    MapText getLabel();
}
