package net.techbrew.journeymap.ui.config;

import cpw.mods.fml.client.config.ConfigGuiType;
import net.techbrew.journeymap.properties.Config;
import net.techbrew.journeymap.properties.PropertiesBase;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Mark on 9/25/2014.
 */
public class StringConfigElement extends BaseConfigElement<AtomicReference<String>, String>
{
    public static StringConfigElement create(PropertiesBase properties, Field field)
    {
        Config annotation = field.getAnnotation(Config.class);
        try
        {
            AtomicReference<String> property = (AtomicReference<String>) field.get(properties);
            if (!annotation.stringListProvider().equals(Config.NoStringProvider.class))
            {
                StringListProvider slp = annotation.stringListProvider().newInstance();
                return new StringConfigElement(field.getName(), properties, property, slp.getDefaultString(), slp.getStrings(), annotation.key());
            }

        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
        return null;
    }


    private StringConfigElement(String name, PropertiesBase properties, AtomicReference<String> property, String defaultValue, String[] validValues, String langKey)
    {
        super(name, defaultValue, ConfigGuiType.STRING, langKey, validValues);
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
    public void set(String value)
    {
        super.set(value);
        String oldValue = property.get();
        if (!value.equals(oldValue))
        {
            property.set(value);
            properties.save();
        }
    }

    @Override
    protected int getOrder()
    {
        return 10;
    }
}
