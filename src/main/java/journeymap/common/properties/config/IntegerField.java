package journeymap.common.properties.config;

import journeymap.common.properties.Category;

/**
 * Integer property field.
 */
public class IntegerField extends ConfigField<Integer>
{
    public static final String ATTR_MIN = "min";
    public static final String ATTR_MAX = "max";

    protected IntegerField()
    {
    }

    public IntegerField(Category category, String key, int minValue, int maxValue, int defaultValue)
    {
        this(category, key, minValue, maxValue, defaultValue, 100);
    }

    public IntegerField(Category category, String key, int minValue, int maxValue, int defaultValue, int sortOrder)
    {
        super(category, key);
        range(minValue, maxValue);
        defaultValue(defaultValue);
        sortOrder(sortOrder);
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

    public IntegerField range(int min, int max)
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

    public Integer incrementAndGet()
    {
        Integer value = get()+1;
        set(value);
        return value;
    }
}
