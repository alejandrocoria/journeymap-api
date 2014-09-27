package net.techbrew.journeymap.ui.config;

import cpw.mods.fml.client.config.ConfigGuiType;
import cpw.mods.fml.client.config.GuiConfigEntries;
import net.techbrew.journeymap.properties.Config;
import net.techbrew.journeymap.properties.PropertiesBase;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Mark on 9/25/2014.
 */
public class IntegerConfigElement extends BaseConfigElement<AtomicInteger, Integer>
{
    public static IntegerConfigElement create(PropertiesBase properties, Field field)
    {
        Config annotation = field.getAnnotation(Config.class);
        try
        {
            AtomicInteger property = (AtomicInteger) field.get(properties);
            IntegerConfigElement integerConfigElement = new IntegerConfigElement(field.getName(), properties, property, annotation.defaultInt(), annotation.key());
            if (annotation.minFloat() != annotation.maxInt())
            {
                integerConfigElement.minValue = annotation.minInt();
                integerConfigElement.maxValue = annotation.maxInt();
                integerConfigElement.setCustomListEntryClass(GuiConfigEntries.NumberSliderEntry.class);
            }
            return integerConfigElement;
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
            return null;
        }
    }


    private IntegerConfigElement(String name, PropertiesBase properties, AtomicInteger property, Integer defaultValue, String langKey)
    {
        super(name, defaultValue, ConfigGuiType.INTEGER, langKey);
        super.set(property.get());
        super.setProperties(properties);
        super.setProperty(property);
    }

    @Override
    public Object get()
    {
        return property.get();
    }

    @Override
    public void set(Integer value)
    {
        super.set(value);
        Integer oldValue = property.get();
        if (value != oldValue)
        {
            property.set(value);
            properties.save();
        }
    }

    @Override
    protected int getOrder()
    {
        return 0;
    }
}
