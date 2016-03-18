package journeymap.common.properties.config;

import com.google.gson.*;
import journeymap.common.properties.Category;

import java.lang.reflect.Type;

/**
 * Boolean property field.
 */
public class BooleanField extends ConfigField<Boolean>
{
    public BooleanField()
    {
    }

    public BooleanField(Category category, String key, boolean defaultValue)
    {
        this(category, key, defaultValue, false);
    }

    public BooleanField(Category category, String key, boolean defaultValue, boolean isMaster)
    {
        super(category, key);
        put(ATTR_DEFAULT, defaultValue);
        put(ATTR_VALUE, defaultValue);
        put(ATTR_CATEGORY_MASTER, isMaster);
    }

    @Override
    public Boolean getDefaultValue()
    {
        return getBooleanAttr(ATTR_DEFAULT);
    }

    @Override
    public BooleanField set(Boolean value)
    {
        put(ATTR_VALUE, value);
        return this;
    }

    @Override
    public Boolean get()
    {
        return getBooleanAttr(ATTR_VALUE);
    }

    /**
     * Whether this field is the master checkbox for the entire category
     * @return
     */
    public boolean isCategoryMaster()
    {
        return getBooleanAttr(ATTR_CATEGORY_MASTER);
    }
}
