/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.common.properties.config;

import journeymap.common.properties.Category;

/**
 * Integer property field.
 */
public class IntegerField extends ConfigField<Integer>
{
    /**
     * The constant ATTR_MIN.
     */
    public static final String ATTR_MIN = "min";
    /**
     * The constant ATTR_MAX.
     */
    public static final String ATTR_MAX = "max";

    /**
     * Instantiates a new Integer field.
     */
    protected IntegerField()
    {
    }

    /**
     * Instantiates a new Integer field.
     *
     * @param category     the category
     * @param key          the key
     * @param minValue     the min value
     * @param maxValue     the max value
     * @param defaultValue the default value
     */
    public IntegerField(Category category, String key, int minValue, int maxValue, int defaultValue)
    {
        this(category, key, minValue, maxValue, defaultValue, 100);
    }

    /**
     * Instantiates a new Integer field.
     *
     * @param category     the category
     * @param key          the key
     * @param minValue     the min value
     * @param maxValue     the max value
     * @param defaultValue the default value
     * @param sortOrder    the sort order
     */
    public IntegerField(Category category, String key, int minValue, int maxValue, int defaultValue, int sortOrder)
    {
        super(category, key);
        range(minValue, maxValue);
        defaultValue(defaultValue);
        setToDefault();
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
    public boolean validate(boolean fix)
    {
        boolean valid = super.validate(fix);
        valid = require(ATTR_MIN, ATTR_MAX) && valid;
        Integer value = get();
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

    /**
     * Range integer field.
     *
     * @param min the min
     * @param max the max
     * @return the integer field
     */
    public IntegerField range(int min, int max)
    {
        put(ATTR_MIN, min);
        put(ATTR_MAX, max);
        return this;
    }

    /**
     * Gets min value.
     *
     * @return the min value
     */
    public int getMinValue()
    {
        return getIntegerAttr(ATTR_MIN);
    }

    /**
     * Gets max value.
     *
     * @return the max value
     */
    public int getMaxValue()
    {
        return getIntegerAttr(ATTR_MAX);
    }

    /**
     * Increment and get integer.
     *
     * @return the integer
     */
    public Integer incrementAndGet()
    {
        Integer value = Math.min(getMaxValue(), get() + 1);
        set(value);
        return value;
    }

    /**
     * Decrement and get integer.
     *
     * @return the integer
     */
    public Integer decrementAndGet()
    {
        Integer value = Math.max(getMinValue(), get() - 1);
        set(value);
        return value;
    }
}
