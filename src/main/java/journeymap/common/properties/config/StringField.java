package journeymap.common.properties.config;

import com.google.common.base.Joiner;
import journeymap.common.Journeymap;
import journeymap.common.properties.Category;
import org.apache.logging.log4j.core.helpers.Strings;

import java.util.Arrays;
import java.util.List;

/**
 * String property field.
 */
public class StringField extends ConfigField<String>
{
    transient Class<? extends ValuesProvider> valueProviderClass;

    public StringField()
    {
    }

    public StringField(Category category, String key, String[] validValues, String defaultValue)
    {
        super(category, key);
        put(ATTR_VALID_VALUES, Joiner.on(",").join(validValues));
        put(ATTR_DEFAULT, defaultValue);
        put(ATTR_VALUE, defaultValue);
    }

    public StringField(Category category, String key, Class<? extends ValuesProvider> valueProviderClass)
    {
        super(category, key);
        this.valueProviderClass = valueProviderClass;
        try
        {
            ValuesProvider valuesProvider = valueProviderClass.newInstance();
            put(ATTR_VALID_VALUES, Joiner.on(",").join(valuesProvider.getStrings()));
            put(ATTR_DEFAULT, valuesProvider.getDefaultString());
            put(ATTR_VALUE, valuesProvider.getDefaultString());
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error("Couldn't use ValuesProvider: " + valueProviderClass);
        }
    }

    @Override
    public String getDefaultValue()
    {
        return get(ATTR_DEFAULT);
    }

    @Override
    public String get()
    {
        return get(ATTR_VALUE);
    }

    public Class<? extends ValuesProvider> getValuesProviderClass()
    {
        return valueProviderClass;
    }

    @Override
    public boolean isValid()
    {
        boolean valid = super.isValid();
        return valid && Arrays.asList(getValidValues()).contains(getDefaultValue());
    }

    public String[] getValidValues()
    {
        String validValuesString = get(ATTR_VALID_VALUES);
        if(!Strings.isEmpty(validValuesString))
        {
            return validValuesString.split(",");
        }
        return null;
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
