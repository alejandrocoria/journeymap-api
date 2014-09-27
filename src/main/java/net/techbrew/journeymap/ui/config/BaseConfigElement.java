package net.techbrew.journeymap.ui.config;

import cpw.mods.fml.client.config.ConfigGuiType;
import cpw.mods.fml.client.config.DummyConfigElement;
import net.minecraft.client.resources.I18n;
import net.techbrew.journeymap.properties.PropertiesBase;

import java.util.regex.Pattern;

/**
 * Created by Mark on 9/25/2014.
 */
public abstract class BaseConfigElement<P, T> extends DummyConfigElement<T> implements Comparable<BaseConfigElement>
{
    PropertiesBase properties;
    P property;

    public BaseConfigElement(String name, T defaultValue, ConfigGuiType type, String langKey, String[] validValues, Pattern validStringPattern, T minValue, T maxValue)
    {
        super(name, defaultValue, type, langKey, validValues, validStringPattern, minValue, maxValue);
    }

    public BaseConfigElement(String name, T defaultValue, ConfigGuiType type, String langKey, Pattern validStringPattern)
    {
        super(name, defaultValue, type, langKey, validStringPattern);
    }

    public BaseConfigElement(String name, T defaultValue, ConfigGuiType type, String langKey, String[] validValues)
    {
        super(name, defaultValue, type, langKey, validValues);
    }

    public BaseConfigElement(String name, T defaultValue, ConfigGuiType type, String langKey)
    {
        super(name, defaultValue, type, langKey);
    }

    public BaseConfigElement(String name, T defaultValue, ConfigGuiType type, String langKey, T minValue, T maxValue)
    {
        super(name, defaultValue, type, langKey, minValue, maxValue);
    }

    public PropertiesBase getProperties()
    {
        return properties;
    }

    public void setProperties(PropertiesBase properties)
    {
        this.properties = properties;
    }

    public P getProperty()
    {
        return property;
    }

    public void setProperty(P property)
    {
        this.property = property;
    }

    @Override
    public void set(T value)
    {
        this.value = value;
    }

    @Override
    public String getComment()
    {
        String tooltipKey = langKey + ".tooltip";
        String tooltip = I18n.format(tooltipKey);
        if (tooltip.equals(tooltipKey))
        {
            return "?";
        }
        else
        {
            return tooltip;
        }
    }

    @Override
    public String getName()
    {
        String name = super.getName();
        if (name.equals(langKey))
        {
            return langKey;
        }
        else
        {
            return name;
        }
    }

    protected abstract int getOrder();

    @Override
    public int compareTo(BaseConfigElement o)
    {
        if (!getClass().equals(o.getClass()))
        {
            int result = Integer.compare(getOrder(), o.getOrder());
            if (result != 0)
            {
                return result;
            }
        }
        return getName().compareTo(o.getName());
    }
}
