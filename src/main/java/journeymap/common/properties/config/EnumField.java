package journeymap.common.properties.config;

import com.google.common.base.Joiner;
import com.google.gson.*;
import journeymap.common.Journeymap;
import journeymap.common.properties.Category;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.EnumSet;

/**
 * Enum property field.
 */
public class EnumField<E extends Enum> extends ConfigField<E>
{
    public EnumField()
    {
    }

    // category = Advanced, key = "jm.advanced.loglevel", stringListProvider = JMLogger.LogLevelStringProvider.class
    public EnumField(Category category, String key, E defaultValue)
    {
        super(category, key);
        put(ATTR_ENUM_TYPE, getClass().getName());
        put(ATTR_DEFAULT, defaultValue.name());
        put(ATTR_VALUE, defaultValue.name());
    }

    @Override
    public E getDefaultValue()
    {
        return (E) getEnumAttr(ATTR_DEFAULT);
    }

    @Override
    public EnumField<E> set(E value)
    {
        put(ATTR_VALUE, value.name());
        return this;
    }

    @Override
    public E get()
    {
        return (E) getEnumAttr(ATTR_VALUE);
    }


    public Class<E> getEnumClass()
    {
        String enumTypeName = get(ATTR_ENUM_TYPE);
        if(enumTypeName!=null)
        {
            try
            {
                return (Class<E>) Class.forName(enumTypeName);
            }
            catch (Exception e)
            {
                Journeymap.getLogger().warn(String.format("Couldn't get Enum Class %s : %s", enumTypeName, e.getMessage()));
            }
        }
        return null;
    }

    public EnumSet<?> getValidValues()
    {
        return EnumSet.allOf(getEnumClass());
    }

    @Override
    public boolean isValid()
    {
        return require(ATTR_ENUM_TYPE) && super.isValid();
    }
}
