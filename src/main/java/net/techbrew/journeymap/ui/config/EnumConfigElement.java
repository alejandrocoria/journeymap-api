package net.techbrew.journeymap.ui.config;

import cpw.mods.fml.client.config.ConfigGuiType;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.properties.Config;
import net.techbrew.journeymap.properties.PropertiesBase;
import org.apache.logging.log4j.core.helpers.Strings;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Mark on 9/25/2014.
 */
public class EnumConfigElement extends BaseConfigElement<AtomicReference<Enum>, String>
{
    Class<? extends Enum> enumClass;
    HashMap<String, Enum> enumLabels;

    public static EnumConfigElement create(PropertiesBase properties, Field field)
    {
        Config annotation = field.getAnnotation(Config.class);
        try
        {
            AtomicReference<Enum> property = (AtomicReference<Enum>) field.get(properties);
            EnumSet<?> enumSet = EnumSet.allOf(property.get().getClass());

            HashMap<String, Enum> enumLabels = new HashMap<String, Enum>();

            ArrayList<String> labels = new ArrayList<String>();
            ArrayList<String> names = new ArrayList<String>();
            for (Enum value : enumSet)
            {
                String label = value.toString();
                enumLabels.put(label, value);
                labels.add(label);
                names.add(value.name());
            }

            String defaultVal = annotation.defaultEnum();
            if (Strings.isEmpty(defaultVal) || !names.contains(defaultVal))
            {
                defaultVal = labels.get(0);
            }
            else
            {
                defaultVal = labels.get(names.indexOf(defaultVal));
            }

            return new EnumConfigElement(field.getName(), properties, property, enumLabels, defaultVal, labels.toArray(new String[labels.size()]), annotation.key());

        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
        return null;
    }


    private EnumConfigElement(String name, PropertiesBase properties, AtomicReference<Enum> property, HashMap<String, Enum> enumLabels, String defaultValue, String[] validValues, String langKey)
    {
        super(name, defaultValue, ConfigGuiType.STRING, langKey, validValues);
        this.enumLabels = enumLabels;
        super.set(property.get().name());
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
        Enum enumVal = enumLabels.get(value);
        if (enumVal != null)
        {
            Enum oldValue = property.get();
            if (!enumVal.equals(oldValue))
            {
                property.set(enumVal);
                properties.save();
            }
        }
        else
        {
            JourneyMap.getLogger().warn("Bad value set for %s: %s", enumClass, value);
        }
    }

    @Override
    protected int getOrder()
    {
        return 10;
    }
}
