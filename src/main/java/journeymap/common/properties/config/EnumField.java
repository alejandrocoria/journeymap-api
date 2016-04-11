package journeymap.common.properties.config;

import journeymap.common.Journeymap;
import journeymap.common.properties.Category;

import java.util.EnumSet;

/**
 * Enum property field.
 */
public class EnumField<E extends Enum> extends ConfigField<E>
{
    public static final String ATTR_ENUM_TYPE = "enumType";

    protected EnumField()
    {
    }

    // category = Advanced, key = "jm.advanced.loglevel", stringListProvider = JMLogger.LogLevelStringProvider.class
    public EnumField(Category category, String key, E defaultValue)
    {
        super(category, key);
        put(ATTR_ENUM_TYPE, defaultValue.getClass().getName());
        defaultValue(defaultValue);
        setToDefault();
    }

    @Override
    public E getDefaultValue()
    {
        return (E) getEnumAttr(ATTR_DEFAULT, getEnumClass());
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
        return (E) getEnumAttr(ATTR_VALUE, getEnumClass());
    }


    public Class<E> getEnumClass()
    {
        Object value = get(ATTR_ENUM_TYPE);
        if (value instanceof Class)
        {
            return (Class<E>) value;
        }
        else if (value instanceof String)
        {
            try
            {
                value = (Class<E>) Class.forName((String) value);
                attributes.put(ATTR_ENUM_TYPE, value);
                return (Class<E>) value;
            }
            catch (Exception e)
            {
                Journeymap.getLogger().warn(String.format("Couldn't get Enum Class %s : %s", ATTR_ENUM_TYPE, e.getMessage()));
            }
        }
        return null;
    }

    public EnumSet<?> getValidValues()
    {
        return EnumSet.allOf(getEnumClass());
    }

    @Override
    public boolean validate(boolean fix)
    {
        return require(ATTR_ENUM_TYPE) && super.validate(fix);
    }
}
