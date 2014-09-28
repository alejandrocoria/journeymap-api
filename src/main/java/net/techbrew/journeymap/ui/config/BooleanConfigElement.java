package net.techbrew.journeymap.ui.config;

import cpw.mods.fml.client.config.ConfigGuiType;
import net.techbrew.journeymap.properties.Config;
import net.techbrew.journeymap.properties.PropertiesBase;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Mark on 9/25/2014.
 */
public class BooleanConfigElement extends BaseConfigElement<AtomicBoolean, Boolean>
{
    public static BooleanConfigElement create(PropertiesBase properties, Field field)
    {
        Config annotation = field.getAnnotation(Config.class);
        try
        {
            AtomicBoolean property = (AtomicBoolean) field.get(properties);
            BooleanConfigElement element = new BooleanConfigElement(field.getName(), properties, property, annotation.defaultBoolean(), annotation.key());
            //lement.setCustomListEntryClass()
            return element;
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private BooleanConfigElement(String name, PropertiesBase properties, AtomicBoolean property, Boolean defaultValue, String langKey)
    {
        super(name, defaultValue, ConfigGuiType.BOOLEAN, langKey);
        super.set(property.get());
        super.setProperties(properties);
        super.setProperty(property);
        super.setConfigEntryClass(ConfigManagerFactory.CheckBooleanEntry.class);
    }

    @Override
    public Object get()
    {
        return property.get();
    }

    @Override
    public void set(Boolean value)
    {
        super.set(value);
        Boolean oldValue = property.get();
        if (value != oldValue)
        {
            property.set(value);
            properties.save();
        }
    }

    @Override
    protected int getOrder()
    {
        return 20;
    }
}
