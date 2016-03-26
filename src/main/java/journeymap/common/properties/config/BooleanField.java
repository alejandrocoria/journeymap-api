package journeymap.common.properties.config;

import journeymap.common.properties.Category;

/**
 * Boolean property field.
 */
public class BooleanField extends ConfigField<Boolean>
{
    public static final String ATTR_CATEGORY_MASTER = "isMaster";

    protected BooleanField()
    {
    }

    public BooleanField(Category category)
    {
        super(category);
    }

    public BooleanField(Category category, String key, boolean defaultValue)
    {
        super(category, key);
        defaultValue(defaultValue);
    }

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
     * @return
     */
    public boolean isCategoryMaster()
    {
        return getBooleanAttr(ATTR_CATEGORY_MASTER);
    }

    /**
     * Whether this field is the master checkbox for the entire category
     *
     * @return this
     */
    public BooleanField categoryMaster(boolean isMaster)
    {
        put(ATTR_CATEGORY_MASTER, isMaster);
        return this;
    }
}
