package journeymap.server.properties;

import journeymap.common.properties.Category;

/**
 * Singleton factory / reference of Categories used in the server.
 */
public class ServerCategory
{
    private static int order;
    public static final Category Inherit = create("Inherit", "");
    public static final Category Hidden = create("Hidden", "");
    public static final Category General = create("General", "General Configuration");
    public static final Category Radar = create("Radar", "Radar Features");
    public static final Category Cave = create("Cave", "Cave Mapping");

    private static Category create(String name, String label)
    {
        return create(name, label, null);
    }

    private static Category create(String name, String label, String tooltip)
    {
        return new Category(name, order++, label, tooltip);
    }

}
