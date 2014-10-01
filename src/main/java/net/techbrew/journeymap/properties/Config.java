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

    Class<? extends Enum> enumClass() default None.class;

    Class<? extends StringListProvider> stringListProvider() default NoStringProvider.class;

    int minInt() default 0;

    int maxInt() default 0;

    float minFloat() default 0f;

    float maxFloat() default 0f;

    String defaultEnum() default "";

    int defaultInt() default 0;

    boolean defaultBoolean() default true;

    String defaultString() default "";

    public enum Category
    {
        Inherit(""),
        MiniMap("jm.config.category.minimap"),
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
