package journeymap.common.properties.config;

import journeymap.common.properties.Category;
import org.apache.logging.log4j.util.Strings;

import static journeymap.common.properties.config.IntegerField.ATTR_MAX;
import static journeymap.common.properties.config.IntegerField.ATTR_MIN;

/**
 * Custom field, stores items as free form text. Options Factory displays as a textbox.
 */
public class CustomField extends ConfigField<Object>
{
    private static final String ATTR_NEG = "ATTR_NEG";
    private static final String ATTR_NUM = "ATTR_NUM";

    protected CustomField()
    {
    }

    public CustomField(Category category, String key)
    {
        this(category, key, null);
    }

    public CustomField(Category category, String key, String defaultValue)
    {
        super(category, key);

        if (!Strings.isEmpty(defaultValue))
        {
            defaultValue(defaultValue);
            setToDefault();
        }
        setIsNum(false);
    }

    public CustomField(Category category, String key, Integer minValue, Integer maxValue, Integer defaultValue, Boolean allowNeg)
    {
        this(category, key, minValue, maxValue, defaultValue, 100, allowNeg);
    }

    public CustomField(Category category, String key, Integer minValue, Integer maxValue, Integer defaultValue, Integer sortOrder, Boolean allowNeg)
    {
        super(category, key);
        range(minValue, maxValue);
        defaultValue(defaultValue);
        setToDefault();
        sortOrder(sortOrder);
        setAllowNeg(allowNeg);
        setIsNum(true);
    }

    private void setAllowNeg(Boolean allowNeg)
    {
        put(ATTR_NEG, allowNeg);
    }

    private void setIsNum(Boolean number)
    {
        put(ATTR_NUM, number);
    }

    @Override
    public boolean validate(boolean fix)
    {
        Object value = get();

        if (value instanceof Integer)
        {
            return validateInt(fix);
        }
        else if (value instanceof String)
        {
            return super.validate(fix);
        }
        return false;
    }

    private boolean validateInt(boolean fix)
    {
        boolean valid = super.validate(fix);
        valid = require(ATTR_MIN, ATTR_MAX) && valid;
        Integer value = getAsInteger();
        if (value == null || !(value >= getMinValue() && value <= getMaxValue()))
        {
            if (fix)
            {
                setToDefault();
            }
            else
            {
                valid = false;
            }
        }
        return valid;
    }

    public CustomField range(int min, int max)
    {
        put(ATTR_MIN, min);
        put(ATTR_MAX, max);
        return this;
    }

    public int getMinValue()
    {
        return getIntegerAttr(ATTR_MIN);
    }

    public int getMaxValue()
    {
        return getIntegerAttr(ATTR_MAX);
    }

    @Override
    public Object getDefaultValue()
    {
        return get(ATTR_DEFAULT);
    }

    public String getAsString()
    {
        return getStringAttr(ATTR_VALUE);
    }

    public Integer getAsInteger()
    {
        try
        {
            Integer val = Integer.valueOf(get().toString());
            if (!(val >= getMinValue() && val <= getMaxValue()))
            {
                setToDefault();
            }
        }
        catch (NumberFormatException nfe)
        {
            setToDefault();
        }
        return getIntegerAttr(ATTR_VALUE);
    }

    @Override
    public Object get()
    {
        return get(ATTR_VALUE);
    }

    @Override
    public CustomField set(Object value)
    {
        try
        {
            Integer val = Integer.valueOf(value.toString());
            if (!(val >= getMinValue() && val <= getMaxValue()))
            {
                setToDefault();
            }
        }
        catch (NumberFormatException nfe)
        {
            // it's a string.
        }
        super.set(value);
        return this;
    }

    public boolean allowNeg()
    {
        return getBooleanAttr(ATTR_NEG);
    }

    public boolean isNumber()
    {
        return getBooleanAttr(ATTR_NUM);
    }
}
