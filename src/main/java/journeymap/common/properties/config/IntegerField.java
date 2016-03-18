package journeymap.common.properties.config;

import com.google.gson.*;
import journeymap.common.Journeymap;
import journeymap.common.properties.Category;

import java.lang.reflect.Type;

/**
 * Integer property field.
 */
public class IntegerField extends ConfigField<Integer>
{
    public IntegerField()
    {
    }

    // Advanced, "jm.advanced.automappoll", minValue = 500, maxValue = 10000, defaultValue = 2000
    public IntegerField(Category category, String key, int minValue, int maxValue, int defaultValue)
    {
        this(category, key, minValue, maxValue, defaultValue, 100);
    }

    public IntegerField(Category category, String key, int minValue, int maxValue, int defaultValue, int sortOrder)
    {
        super(category, key);
        put(ATTR_MIN, minValue);
        put(ATTR_MAX, maxValue);
        put(ATTR_DEFAULT, defaultValue);
        put(ATTR_VALUE, defaultValue);
        put(ATTR_ORDER, sortOrder);
    }

    @Override
    public Integer getDefaultValue()
    {
        return getIntegerAttr(ATTR_DEFAULT);
    }

    @Override
    public Integer get()
    {
        return getIntegerAttr(ATTR_VALUE);
    }

    @Override
    public boolean isValid()
    {
        boolean valid = require(ATTR_MIN, ATTR_MAX) && super.isValid();
        Integer value = get();
        return valid && value!=null && value >= getMinValue() && value<= getMaxValue();
    }

    public int getMinValue()
    {
        return getIntegerAttr(ATTR_MIN);
    }

    public int getMaxValue()
    {
        return getIntegerAttr(ATTR_MAX);
    }

    public Integer incrementAndGet()
    {
        Integer value = get()+1;
        set(value);
        return value;
    }
}
