/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.properties;

import journeymap.client.Constants;
import journeymap.common.properties.Category;

import java.util.Arrays;
import java.util.List;

/**
 * Singleton factory / reference of Categories used in the client.
 */
public class ClientCategory
{
    private static int order = 1;
    public static final Category MiniMap1 = create("MiniMap1", "jm.config.category.minimap");
    public static final Category MiniMap2 = create("MiniMap2", "jm.config.category.minimap2");
    public static final Category FullMap = create("FullMap", "jm.config.category.fullmap");
    public static final Category WebMap = create("WebMap", "jm.config.category.webmap");
    public static final Category Waypoint = create("Waypoint", "jm.config.category.waypoint");
    public static final Category WaypointBeacon = create("WaypointBeacon", "jm.config.category.waypoint_beacons");
    public static final Category Cartography = create("Cartography", "jm.config.category.cartography");
    public static final Category Advanced = create("Advanced", "jm.config.category.advanced");

    public static final List<Category> values = Arrays.asList(Category.Inherit, Category.Hidden,
            MiniMap1, MiniMap2, FullMap, WebMap, Waypoint, WaypointBeacon, Cartography, Advanced);

    private static Category create(String name, String key)
    {
        return new Category(name, order++, Constants.getString(key), Constants.getString(key + ".tooltip"));
    }

    public static Category valueOf(String name)
    {
        for (Category category : values)
        {
            if (category.getName().equalsIgnoreCase(name))
            {
                return category;
            }
        }
        return null;
    }

}
