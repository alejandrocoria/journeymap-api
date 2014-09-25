package net.techbrew.journeymap.properties;

/**
 * Created by Mark on 9/21/2014.
 */
public @interface Config
{
    Category category();
    String subcategory() default "";
    String key() default "";
    String onKey() default "jm.common.on";
    String offKey() default "jm.common.off";
    int minInt() default 0;
    int maxInt() default 0;
    float minFloat() default 0f;
    float maxFloat() default 0f;

    public enum Category
    {
        Advanced("jm.config.category.advanced"),
        General("jm.common.general_display_title"),
        MapStyle("jm.common.map_style_title"),
        MapUI("jm.config.category.mapui"),
        MiniMap("jm.minimap.options"),
        Radar("jm.config.category.radar"),
        Waypoint("jm.waypoint.options"),
        WebMap("jm.webmap.enable")
        ;

        public final String key;
        private Category(String key)
        {
            this.key = key;
        }
    }
}
