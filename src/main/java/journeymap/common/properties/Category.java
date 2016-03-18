package journeymap.common.properties;

import journeymap.common.properties.config.ConfigField;
import org.apache.logging.log4j.core.helpers.Strings;

/**
 * Options Manager category enum.
 */
public enum Category
{
    Inherit(""),
    Hidden("jm.config.category.hidden"),
    MiniMap1("jm.config.category.minimap"),
    MiniMap2("jm.config.category.minimap2"),
    FullMap("jm.config.category.fullmap"),
    WebMap("jm.config.category.webmap"),
    Radar("jm.config.category.radar"),
    Waypoint("jm.config.category.waypoint"),
    WaypointBeacon("jm.config.category.waypoint_beacons"),
    Cartography("jm.config.category.cartography"),
    Advanced("jm.config.category.advanced");

    public final String key;

    private Category(String key)
    {
        this.key = key;
    }

    public static Category fromKey(String key)
    {
        if(Strings.isEmpty(key))
        {
            return Inherit;
        }

        for(Category value : Category.values())
        {
            if(value.key.equals(key))
            {
                return value;
            }
        }
        return Inherit;
    }
}
