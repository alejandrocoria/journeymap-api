/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.server.properties;

import journeymap.common.properties.Category;

import java.util.Arrays;
import java.util.List;

/**
 * Singleton factory / reference of Categories used in the server.
 */
public class ServerCategory
{
    private static int order = 1;
    /**
     * The constant General.
     */
    public static final Category General = create("General", "General Configuration");
    /**
     * The constant Radar.
     */
    public static final Category Radar = create("Radar", "Radar Features");
    /**
     * The constant Cave.
     */
    public static final Category Cave = create("Cave", "Cave Mapping");

    /**
     * The constant values.
     */
    public static final List<Category> values = Arrays.asList(Category.Inherit, Category.Hidden, General, Radar, Cave);

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

    private static Category create(String name, String label)
    {
        return create(name, label, null);
    }

    private static Category create(String name, String label, String tooltip)
    {
        return new Category(name, order++, label, tooltip);
    }

}
