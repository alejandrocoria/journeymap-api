package journeymap.common.properties.config;

import com.google.common.base.Objects;
import journeymap.common.Journeymap;
import journeymap.common.properties.Category;

import java.util.Map;
import java.util.TreeMap;

/**
 * Parent class for typed configuration field which has both
 * metadata and data.  Intended to be self-describing and JSON-serializable.
 */
public abstract class ConfigField<T>
{
    public static final String ATTR_TYPE = "type";
    public static final String ATTR_CATEGORY = "category";
    public static final String ATTR_CATEGORY_LABEL = "categoryLabel";
    public static final String ATTR_KEY = "key";
    public static final String ATTR_LABEL = "label";
    public static final String ATTR_TOOLTIP = "tooltip";
    public static final String ATTR_ORDER = "order";
    public static final String ATTR_VALUE = "value";
    public static final String ATTR_DEFAULT = "default";
    public static final String ATTR_MIN = "min";
    public static final String ATTR_MAX = "max";
    public static final String ATTR_VALID_VALUES = "validValues";
    public static final String ATTR_CATEGORY_MASTER = "isMaster";
    public static final String ATTR_ENUM_TYPE = "enumType";
    
    protected final transient Map<String, String> attributes = new TreeMap<String, String>();

    protected ConfigField()
    {
    }

    protected ConfigField(Category category, String key)
    {
        put(ATTR_TYPE, getClass().getSimpleName());
        put(ATTR_CATEGORY, category.name());
        put(ATTR_KEY, key);
    }

    protected ConfigField(String categoryLabel, String key)
    {
        put(ATTR_TYPE, getClass().getSimpleName());
        put(ATTR_CATEGORY_LABEL, categoryLabel);
        put(ATTR_KEY, key);
    }

    public String get(String attrName) {
        return attributes.get(attrName);
    }

    public ConfigField<T> put(String attrName, Object value) {
        attributes.put(attrName, value.toString());
        return this;
    }

    public abstract T getDefaultValue();

    public abstract T get();
    
    public ConfigField<T> set(T value) {
        put(ATTR_VALUE, value);
        return this;
    }

    public boolean isValid() {
        return require(ATTR_TYPE, ATTR_VALUE, ATTR_DEFAULT);
    }

    public ConfigField<T> setSortOrder(int order)
    {
        put(ATTR_ORDER, order);
        return this;
    }

    public String getKey()
    {
        return get(ATTR_KEY);
    }

    public Category getCategory()
    {
        return getEnumAttr(ATTR_CATEGORY, Category.class);
    }

    public String getCategoryLabel()
    {
        return get(ATTR_CATEGORY_LABEL);
    }

    public String getLabel()
    {
        return get(ATTR_LABEL);
    }

    public String getTooltip()
    {
        return get(ATTR_TOOLTIP);
    }

    public String getType()
    {
        return get(ATTR_TYPE);
    }

    public int getSortOrder()
    {
        Integer order = getIntegerAttr(ATTR_ORDER);
        if(order==null) {
            order = 100;
        }
        return order;
    }
    
    public Integer getIntegerAttr(String attrName)
    {
        String str = get(attrName);
        if(str!=null)
        {
            try
            {
                return Integer.parseInt(str);
            }
            catch (NumberFormatException e)
            {
                Journeymap.getLogger().warn(String.format("Couldn't get Integer %s from %s: %s", attrName, str, e.getMessage()));
            }
        }
        return null;
    }

    public boolean getBooleanAttr(String attrName)
    {
        return Boolean.valueOf(get(attrName));
    }

    public Enum<?> getEnumAttr(String attrName)
    {
        String enumName = get(attrName);
        String enumTypeName = get(ATTR_ENUM_TYPE);
        if(enumName!=null && enumTypeName!=null)
        {
            try
            {
                Class<? extends Enum> enumType = (Class<? extends Enum>) Class.forName(enumTypeName);
                return Enum.valueOf(enumType, enumName);
            }
            catch (Exception e)
            {
                Journeymap.getLogger().warn(String.format("Couldn't get Enum %s as %s: %s", enumName, enumTypeName, e.getMessage()));
            }
        }
        return null;
    }

    public <E extends Enum> E getEnumAttr(String attrName, Class<E> enumType)
    {
        String enumName = get(attrName);
        if(enumName!=null)
        {
            try
            {
                return (E) Enum.valueOf(enumType, enumName);
            }
            catch (Exception e)
            {
                Journeymap.getLogger().warn(String.format("Couldn't get Enum %s as %s: %s", enumName, enumType, e.getMessage()));
            }
        }
        return null;
    }

    /**
     * Validates the field state, returns true
     * if values had to be altered.
     */
    public final boolean saveNeeded() throws Exception
    {
        boolean saveNeeded = false;
        T oldValue = get();
        if(!isValid())
        {
            setToDefault();
            warnPropertyValue(oldValue, getDefaultValue());
            saveNeeded = true;
        }
        return saveNeeded;
    }

    public void setToDefault()
    {
        set(getDefaultValue());
    }

    protected boolean require(String... attrNames)
    {
        boolean pass = true;
        for(String attrName : attrNames)
        {
            Object attr = get(attrName);
            if(attr==null) {
                Journeymap.getLogger().warn(String.format("%s is missing required attribute: %s", getClass(), attrName));
                pass = false;
            }
        }
        return pass;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof ConfigField))
        {
            return false;
        }
        ConfigField<?> that = (ConfigField<?>) o;

        // TODO:  This probably won't work for server-side configs.
        return Objects.equal(getKey(), that.getKey()) &&
                getCategory() == that.getCategory() &&
                Objects.equal(get(), that.get());
    }

    @Override
    public int hashCode()
    {
        // TODO:  This probably won't work for server-side configs.
        return Objects.hashCode(getKey(), getCategory(), get());
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(attributes)
                .add("attributes", attributes)
                .toString();
    }

    /**
     * Warn a property's value has to be adjusted.
     */
    protected void warnPropertyValue(Object oldValue, Object newValue)
    {
        Journeymap.getLogger().warn(String.format("Property %s.%s invalid: %s . Changing to: %s", getClass().getSimpleName(), getKey(), oldValue, newValue));
    }

    public Map<String, String> getAttributes()
    {
        return attributes;
    }



}
