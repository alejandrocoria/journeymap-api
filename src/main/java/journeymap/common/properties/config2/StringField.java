package journeymap.common.properties.config2;

import com.google.common.base.Joiner;
import com.google.gson.*;
import journeymap.common.Journeymap;
import journeymap.common.properties.config.StringListProvider;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * String property field.
 */
public class StringField extends ConfigField<String>
{
    String[] validValues;

    Class validValuesProviderClass;

    protected StringField()
    {
    }

    public StringField(String key, String category, String defaultValue)
    {
        super(key, category, defaultValue);
    }

    public String[] getValidValues()
    {
        return validValues;
    }

    @Override
    protected StringField deserialize(JsonObject jsonObject)
    {
        super.deserialize(jsonObject);
        this.defaultValue = getString("defaultValue", jsonObject);
        this.value = getString("value", jsonObject);

        String validValuesStr = getString("validValues", jsonObject);
        if (validValuesStr != null && !validValuesStr.isEmpty())
        {
            this.validValues = validValuesStr.split(",");
        }

        String validValuesProviderClassStr = getString("validValuesProviderClass", jsonObject);
        if (validValuesProviderClassStr != null && !validValuesProviderClassStr.isEmpty())
        {
            try
            {
                this.validValuesProviderClass = Class.forName(validValuesProviderClassStr);
                List<String> validValuesList = new ArrayList<String>();

                if (StringListProvider.class.isAssignableFrom(validValuesProviderClass))
                {
                    StringListProvider provider = (StringListProvider) validValuesProviderClass.newInstance();
                    this.defaultValue = provider.getDefaultString();
                    validValuesList = provider.getStrings();
                }
                else if (Enum.class.isAssignableFrom(validValuesProviderClass))
                {
                    EnumSet<? extends Enum> enumSet = EnumSet.allOf(validValuesProviderClass);
                    for (Enum enumVal : enumSet)
                    {
                        validValuesList.add(enumVal.name());
                    }
                }

                if (!validValuesList.isEmpty())
                {
                    this.validValues = validValuesList.toArray(new String[validValuesList.size()]);
                }

            }
            catch (Throwable t)
            {
                Journeymap.getLogger().error("Couldn't get stringListProviderClassStr: " + validValuesProviderClassStr);
            }
        }
        return this;
    }

    @Override
    protected JsonObject serialize()
    {
        final JsonObject jsonObject = super.serialize();
        jsonObject.addProperty("defaultValue", this.defaultValue);
        jsonObject.addProperty("value", this.value);
        if (validValues != null && validValues.length > 0)
        {
            jsonObject.addProperty("validValues", Joiner.on(",").join(validValues));
        }
        if (validValuesProviderClass != null)
        {
            jsonObject.addProperty("validValuesProviderClass", this.validValuesProviderClass.getName());
        }
        return jsonObject;
    }

    public static class Serializer implements JsonSerializer<StringField>, JsonDeserializer<StringField>
    {
        @Override
        public JsonElement serialize(StringField src, Type typeOfSrc, JsonSerializationContext context)
        {
            return src.serialize();
        }

        @Override
        public StringField deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            return new StringField().deserialize(json.getAsJsonObject());
        }
    }
}
