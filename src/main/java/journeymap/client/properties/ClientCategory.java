/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
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
    /**
     * The constant MiniMap1.
     */
    public static final Category MiniMap1 = create("MiniMap1", "jm.config.category.minimap");
    /**
     * The constant MiniMap2.
     */
    public static final Category MiniMap2 = create("MiniMap2", "jm.config.category.minimap2");
    /**
     * The constant FullMap.
     */
    public static final Category FullMap = create("FullMap", "jm.config.category.fullmap");
    /**
     * The constant WebMap.
     */
    public static final Category WebMap = create("WebMap", "jm.config.category.webmap");
    /**
     * The constant Waypoint.
     */
    public static final Category Waypoint = create("Waypoint", "jm.config.category.waypoint");
    /**
     * The constant WaypointBeacon.
     */
    public static final Category WaypointBeacon = create("WaypointBeacon", "jm.config.category.waypoint_beacons");
    /**
     * The constant Cartography.
     */
    public static final Category Cartography = create("Cartography", "jm.config.category.cartography");
    /**
     * The constant Advanced.
     */
    public static final Category Advanced = create("Advanced", "jm.config.category.advanced");

    /**
     * The constant values.
     */
    public static final List<Category> values = Arrays.asList(Category.Inherit, Category.Hidden,
            MiniMap1, MiniMap2, FullMap, WebMap, Waypoint, WaypointBeacon, Cartography, Advanced);

    private static Category create(String name, String key)
    {
        return new Category(name, order++, Constants.getString(key), Constants.getString(key + ".tooltip"));
    }

    /**
     * Value of category.
     *
     * @param name the name
     * @return the category
     */
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
