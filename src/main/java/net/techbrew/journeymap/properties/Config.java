package net.techbrew.journeymap.properties;

/**
 * Created by Mark on 9/21/2014.
 */
public @interface Config
{
    Category category();
    String key() default "";

    public enum Category
    {
        Advanced("jm.config.category.advanced"),
        General("jm.config.category.general"),
        MapStyle("jm.config.category.mapstyle"),
        Radar("jm.config.category.radar");

        public final String key;
        private Category(String key)
        {
            this.key = key;
        }
    }
}
