/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.common.properties.config;

import com.google.common.base.Objects;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import journeymap.common.properties.Category;
import journeymap.common.properties.PropertiesBase;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Parent class for typed configuration field which has both metadata and data.
 * Intended to be self-describing and JSON-serializable. Values are serialized as
 * Strings and lazily converted to the proper types upon first use.
 */
public abstract class ConfigField<T>
{
    public static final String ATTR_TYPE = "type";
    public static final String ATTR_CATEGORY = "category";
    public static final String ATTR_KEY = "key";
    public static final String ATTR_LABEL = "label";
    public static final String ATTR_TOOLTIP = "tooltip";
    public static final String ATTR_ORDER = "order";
    public static final String ATTR_VALUE = "value";
    public static final String ATTR_DEFAULT = "default";
    public static final String ATTR_VALID_VALUES = "validValues";

    // Map of all attributes
    protected final transient Map<String, Object> attributes = new TreeMap<String, Object>();

    // Owning Properties class
    protected transient PropertiesBase owner;

    // Field name used by owning properties class
    protected transient String fieldName;

    public ConfigField()
    {
        put(ATTR_TYPE, getClass().getSimpleName());
    }

    protected ConfigField(Category category)
    {
        put(ATTR_TYPE, getClass().getSimpleName());
        put(ATTR_CATEGORY, category);
    }

    protected ConfigField(Category category, String key)
    {
        put(ATTR_TYPE, getClass().getSimpleName());
        put(ATTR_CATEGORY, category);
        put(ATTR_KEY, key);
    }

    /**
     * Get the attribute as a String.
     *
     * @param attrName attribute name attribute name
     * @return String value or null
     */
    public String getStringAttr(String attrName)
    {
        Object value = attributes.get(attrName);
        if (value == null)
        {
            return null;
        }
        if (value instanceof Enum)
        {
            return ((Enum) value).name();
        }
        if (value instanceof Class)
        {
            return ((Class) value).getCanonicalName();
        }
        return value.toString();
    }

    /**
     * Put the attribute value into the field
     *
     * @param attrName attribute name
     * @param value    attribute value
     * @return this
     */
    public ConfigField<T> put(String attrName, Object value)
    {
        attributes.put(attrName, value);
        return this;
    }

    /**
     * Gets the default value.
     *
     * @return T
     */
    public abstract T getDefaultValue();

    /**
     * Gets the field value.
     *
     * @return T
     */
    public abstract T get();

    /**
     * Sets the field value
     *
     * @param value T
     * @return this
     */
    public ConfigField<T> set(T value)
    {
        put(ATTR_VALUE, value);
        return this;
    }

    /**
     * Whether the field is valid.
     *
     * @param fix whether to try to fix problems
     * @return true if valid.
     */
    public boolean validate(boolean fix)
    {
        boolean hasRequired = require(ATTR_TYPE, ATTR_VALUE, ATTR_DEFAULT);
        boolean hasCategory = getCategory() != null;
        return hasRequired && hasCategory;
    }

    /**
     * Sets the sort order.
     *
     * @param order sort order
     * @return this.
     */
    public ConfigField<T> sortOrder(int order)
    {
        put(ATTR_ORDER, order);
        return this;
    }

    /**
     * Get the label i18n key.
     *
     * @return the label key
     */
    public String getKey()
    {
        return getStringAttr(ATTR_KEY);
    }

    /**
     * Sets the Category.
     *
     * @param category category
     * @return this
     */
    public ConfigField<T> category(Category category)
    {
        attributes.put(ATTR_CATEGORY, category);
        return this;
    }

    /**
     * Get the field category.
     *
     * @return the Category
     */
    public Category getCategory()
    {
        Object val = get(ATTR_CATEGORY);
        if (val instanceof Category)
        {
            return (Category) val;
        }
        else if (val instanceof String)
        {
            if (owner != null)
            {
                Category category = owner.getCategoryByName((String) val);
                category(category);
                return category;
            }
        }
        return null;
    }

    /**
     * Gets the label.
     *
     * @return label
     */
    public String getLabel()
    {
        return getStringAttr(ATTR_LABEL);
    }

    /**
     * Sets the label.
     *
     * @param label label
     * @return this
     */
    public ConfigField<T> label(String label)
    {
        attributes.put(ATTR_LABEL, label);
        return this;
    }

    /**
     * Gets the tooltip.
     *
     * @return tooltip
     */
    public String getTooltip()
    {
        return getStringAttr(ATTR_TOOLTIP);
    }

    /**
     * Gets the field type.
     *
     * @return type
     */
    public String getType()
    {
        return getStringAttr(ATTR_TYPE);
    }

    /**
     * Gets the sort order for the field when displayed.
     *
     * @return order
     */
    public int getSortOrder()
    {
        Integer order = getIntegerAttr(ATTR_ORDER);
        if (order == null)
        {
            order = 100;
        }
        return order;
    }


    /**
     * Get the attribute as an Object.
     *
     * @param attrName attribute name attribute name
     * @return String value or null
     */
    public Object get(String attrName)
    {
        return attributes.get(attrName);
    }

    /**
     * Gets an Integer attribute value.
     *
     * @param attrName attribute name
     * @return Integer or null
     */
    public Integer getIntegerAttr(String attrName)
    {
        Object value = attributes.get(attrName);
        if (value instanceof Integer)
        {
            return (Integer) value;
        }
        else if (value instanceof String)
        {
            try
            {
                value = Integer.parseInt((String) value);
                attributes.put(attrName, value);
                return (Integer) value;
            }
            catch (NumberFormatException e)
            {
                Journeymap.getLogger().warn(String.format("Couldn't get Integer %s from %s: %s", attrName, value, e.getMessage()));
            }
        }
        return null;
    }

    /**
     * Gets a Boolean attribute value.
     *
     * @param attrName attribute name
     * @return Boolean or null
     */
    public Boolean getBooleanAttr(String attrName)
    {
        Object value = attributes.get(attrName);
        if (value instanceof Boolean)
        {
            return (Boolean) value;
        }
        else if (value instanceof String)
        {
            try
            {
                value = Boolean.valueOf((String) value);
                attributes.put(attrName, value);
                return (Boolean) value;
            }
            catch (NumberFormatException e)
            {
                Journeymap.getLogger().warn(String.format("Couldn't get Boolean %s from %s: %s", attrName, value, e.getMessage()));
            }
        }
        return null;
    }

    /**
     * Get the attribute of the Enum type specified.
     *
     * @param attrName attribute name
     * @param enumType Enum class
     * @param <E>      Enum
     * @return Enum value of attribute
     */
    public <E extends Enum> E getEnumAttr(String attrName, Class<E> enumType)
    {
        Object value = attributes.get(attrName);
        if (value instanceof Enum)
        {
            return (E) value;
        }
        else if (value instanceof String)
        {
            try
            {
                return (E) Enum.valueOf(enumType, (String) value);
            }
            catch (Exception e)
            {
                Journeymap.getLogger().warn(String.format("Couldn't get %s as Enum %s with value %s: %s", attrName, enumType, value,
                        LogFormatter.toString(e)));
            }
        }
        // Resort to the default
        setToDefault();
        return (E) getDefaultValue();
    }

    /**
     * Set current value to the default value.
     */
    public void setToDefault()
    {
        set(getDefaultValue());
    }

    /**
     * Sets the default value
     *
     * @param defaultValue defaultValue
     * @return this
     */
    public ConfigField<T> defaultValue(T defaultValue)
    {
        if (defaultValue == null)
        {
            Journeymap.getLogger().warn("defaultValue shouldn't be null");
        }
        put(ATTR_DEFAULT, defaultValue);
        return this;
    }

    /**
     * Ensure the name attributes are present on the field.
     *
     * @param attrNames attribute names required attribute names
     * @return true if all attributes are present
     */
    protected boolean require(String... attrNames)
    {
        boolean pass = true;
        for (String attrName : attrNames)
        {
            Object attr = get(attrName);
            if (attr == null)
            {
                Journeymap.getLogger().warn(String.format("Missing required attribute '%s' in %s", attrName, getDeclaredField()));
                pass = false;
            }
        }
        return pass;
    }

    /**
     * Get the map of attributes
     *
     * @return map
     */
    public Map<String, Object> getAttributeMap()
    {
        return attributes;
    }

    /**
     * Get the set of attribute names
     *
     * @return set
     */
    public Set<String> getAttributeNames()
    {
        return attributes.keySet();
    }

    /**
     * Gets the owning properties class
     *
     * @return owner or null if not set
     */
    public PropertiesBase getOwner()
    {
        return this.owner;
    }

    /**
     * Sets the owning properties class and the fieldname it declared.
     *
     * @param properties owner
     */
    public void setOwner(String fieldName, PropertiesBase properties)
    {
        this.fieldName = fieldName;
        this.owner = properties;
    }

    /**
     * Save the owner properties file so this value is persisted.
     *
     * @return
     */
    public boolean save()
    {
        if (owner != null)
        {
            return owner.save();
        }
        return false;
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

    public String getDeclaredField()
    {
        if (owner == null)
        {
            return null;
        }
        return String.format("%s.%s", owner.getClass().getSimpleName(), fieldName);
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("on", getDeclaredField())
                .add("attributes", attributes)
                .toString();
    }
}
