/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.common.properties.config;

import journeymap.common.properties.Category;

/**
 * Boolean property field.
 */
public class BooleanField extends ConfigField<Boolean>
{
    /**
     * The constant ATTR_CATEGORY_MASTER.
     */
    public static final String ATTR_CATEGORY_MASTER = "isMaster";

    /**
     * Instantiates a new Boolean field.
     */
    protected BooleanField()
    {
    }

    /**
     * Instantiates a new Boolean field.
     *
     * @param category     the category
     * @param defaultValue the default value
     */
    public BooleanField(Category category, boolean defaultValue)
    {
        this(category, null, defaultValue);
    }

    /**
     * Instantiates a new Boolean field.
     *
     * @param category     the category
     * @param key          the key
     * @param defaultValue the default value
     */
    public BooleanField(Category category, String key, boolean defaultValue)
    {
        this(category, key, defaultValue, false);
    }

    /**
     * Instantiates a new Boolean field.
     *
     * @param category     the category
     * @param key          the key
     * @param defaultValue the default value
     * @param isMaster     the is master
     */
    public BooleanField(Category category, String key, boolean defaultValue, boolean isMaster)
    {
        super(category, key);
        defaultValue(defaultValue);
        setToDefault();
        categoryMaster(isMaster);
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
     * Toggle the boolean value
     *
     * @return the new value
     */
    public boolean toggle()
    {
        set(!get());
        return get();
    }

    /**
     * Toggle the boolean value and save to file
     *
     * @return the new value
     */
    public boolean toggleAndSave()
    {
        set(!get());
        save();
        return get();
    }

    /**
     * Whether this field is the master checkbox for the entire category
     *
     * @return boolean
     */
    public boolean isCategoryMaster()
    {
        return getBooleanAttr(ATTR_CATEGORY_MASTER);
    }

    /**
     * Whether this field is the master checkbox for the entire category
     *
     * @param isMaster the is master
     * @return this boolean field
     */
    public BooleanField categoryMaster(boolean isMaster)
    {
        put(ATTR_CATEGORY_MASTER, isMaster);
        return this;
    }
}
