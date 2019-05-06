package journeymap.common.properties.config;

import journeymap.common.properties.Category;
import org.apache.logging.log4j.util.Strings;

/**
 * Custom field, stores items as free form text. Options Factory displays as a textbox.
 */
public class CustomField extends ConfigField<String>
{
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
    public CustomField set(String value)
    {
        super.set(value);
        return this;
    }
}
