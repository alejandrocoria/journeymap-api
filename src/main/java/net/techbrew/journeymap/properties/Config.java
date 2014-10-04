package net.techbrew.journeymap.properties;

import net.techbrew.journeymap.ui.option.StringListProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Mark on 9/21/2014.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Config
{
    Category category();

    boolean master() default false;

    String key();

    String defaultEnum() default "";

    Class<? extends StringListProvider> stringListProvider() default NoStringProvider.class;

    double minValue() default 0;

    double maxValue() default 0;

    double defaultValue() default 0;

    boolean defaultBoolean() default true;

    public enum Category
    {
        Inherit(""),
        MiniMap("jm.config.category.minimap"),
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
    }

    public enum None
    {
        NONE
    }

    class NoStringProvider implements StringListProvider
    {
        @Override
        public String[] getStrings()
        {
            return new String[0];
        }

        @Override
        public String getDefaultString()
        {
            return null;
        }
    }
}
