package journeymap.common.properties.config;

import com.google.common.base.Joiner;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import journeymap.common.properties.Category;
import org.apache.logging.log4j.core.helpers.Strings;

import java.util.Arrays;
import java.util.List;

/**
 * String property field.
 */
public class StringField extends ConfigField<String>
{
    public static final String ATTR_VALUE_PROVIDER = "valueProvider";
    public static final String ATTR_VALUE_PATTERN = "pattern";
    public static final String ATTR_MULTILINE = "multiline";

    protected StringField()
    {
    }

    public StringField(Category category, String key)
    {
        this(category, key, null, null);
    }

    public StringField(Category category, String key, String[] validValues, String defaultValue)
    {
        super(category, key);
        if (validValues != null)
        {
            put(ATTR_VALID_VALUES, Joiner.on(",").join(validValues));
        }
        if (!Strings.isEmpty(defaultValue))
        {
            defaultValue(defaultValue);
            setToDefault();
        }
    }

    public StringField(Category category, String key, Class<? extends ValuesProvider> valueProviderClass)
    {
        super(category, key);
        if (valueProviderClass != null)
        {
            put(ATTR_VALUE_PROVIDER, valueProviderClass);
            try
            {
                ValuesProvider valuesProvider = valueProviderClass.newInstance();
                validValues(valuesProvider.getStrings());
                defaultValue(valuesProvider.getDefaultString());
                setToDefault();

                if (!getValidValues().contains(getDefaultValue()))
                {
                    Journeymap.getLogger().error(String.format("Default value '%s' isn't in one of the valid values '%s' for %s",
                            getDefaultValue(), getStringAttr(ATTR_VALID_VALUES), this));
                }
            }
            catch (Throwable t)
            {
                Journeymap.getLogger().error(String.format("Couldn't use ValuesProvider %s: %s", valueProviderClass,
                        LogFormatter.toString(t)));
            }
        }
    }

    @Override
    public String getDefaultValue()
    {
        return getStringAttr(ATTR_DEFAULT);
    }

    @Override
    public String get()
    {
        return getStringAttr(ATTR_VALUE);
    }

    @Override
    public StringField set(String value)
    {
        super.set(value);
        return this;
    }

    /**
     * Sets a regex pattern used to ensureValid the value;
     *
     * @param regexPattern pattern
     * @return this
     */
    public StringField pattern(String regexPattern)
    {
        put(ATTR_VALUE_PATTERN, regexPattern);
        return this;
    }

    /**
     * Gets the regex pattern used to ensureValid the value
     *
     * @return regex
     */
    public String getPattern()
    {
        return getStringAttr(ATTR_VALUE_PATTERN);
    }

    /**
     * Class that provides default and valid values
     *
     * @return
     */
    public Class<? extends ValuesProvider> getValuesProviderClass()
    {
        Object value = get(ATTR_VALUE_PROVIDER);
        if (value == null)
        {
            return null;
        }
        if (value instanceof Class)
        {
            return (Class<? extends ValuesProvider>) value;
        }
        if (value instanceof String)
        {
            try
            {
                value = Class.forName((String) value);
                put(ATTR_VALUE_PROVIDER, value);
                return (Class<? extends ValuesProvider>) value;
            }
            catch (Exception e)
            {
                Journeymap.getLogger().warn(String.format("Couldn't get ValuesProvider Class %s : %s", value, e.getMessage()));
            }
        }
        return null;
    }

    @Override
    public boolean validate(boolean fix)
    {
        boolean hasRequired = require(ATTR_TYPE);
        boolean hasCategory = getCategory() != null;
        boolean valid = hasRequired && hasCategory;

        String value = get();
        if (Strings.isNotEmpty(value))
        {
            String pattern = getPattern();
            if (Strings.isNotEmpty(pattern))
            {
                boolean patternValid = value.matches(pattern);
                if (!patternValid)
                {
                    Journeymap.getLogger().warn(String.format("Value '%s' doesn't match pattern '%s' for %s", value, pattern, this));
                    if (fix && Strings.isNotEmpty(getDefaultValue()))
                    {
                        setToDefault();
                        Journeymap.getLogger().warn(String.format("Value set to default '%s' for %s", getDefaultValue(), this));
                    }
                    else
                    {
                        valid = false;
                    }
                }
            }
        }

        List<String> validValues = getValidValues();
        if (validValues != null)
        {
            if (!validValues.contains(value))
            {
                Journeymap.getLogger().warn(String.format("Value '%s' isn't in one of the valid values '%s' for %s", value,
                        getStringAttr(ATTR_VALID_VALUES), this));

                String defaultValue = getDefaultValue();
                if (fix && Strings.isNotEmpty(defaultValue))
                {
                    setToDefault();
                    Journeymap.getLogger().warn(String.format("Value set to default '%s' for %s", defaultValue, this));
                }
                else
                {
                    valid = false;
                }
            }
        }

        return valid;
    }

    /**
     * Gets the list of valid values, or null if there aren't any specified.
     *
     * @return list or null
     */
    public List<String> getValidValues()
    {
        String validValuesString = getStringAttr(ATTR_VALID_VALUES);
        if (!Strings.isEmpty(validValuesString))
        {
            return Arrays.asList(validValuesString.split(","));
        }
        return null;
    }

    /**
     * Sets the valid values.
     *
     * @param values list
     * @return this
     */
    public StringField validValues(Iterable<String> values)
    {
        put(ATTR_VALID_VALUES, Joiner.on(",").join(values));
        return this;
    }

    /**
     * Whether this should be displayed with multiple lines
     *
     * @return true if multiline
     */
    public boolean isMultiline()
    {
        Boolean val = getBooleanAttr(ATTR_MULTILINE);
        return (val == null) ? false : val;
    }

    /**
     * Sets whether this should be displayed with multiple lines
     *
     * @param isMultiline true if multiline
     * @return this
     */
    public StringField multiline(boolean isMultiline)
    {
        put(ATTR_MULTILINE, isMultiline);
        return this;
    }

    /**
     * Provides a list of valid strings as options ub a StringField
     */
    public static interface ValuesProvider
    {
        public List<String> getStrings();

        public String getDefaultString();
    }
}
