package journeymap.common.properties.config2;

import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * Integer property field.
 */
public class IntegerField extends ConfigField<Integer>
{
    int minValue;
    int maxValue;

    protected IntegerField()
    {
    }

    public IntegerField(String key, String category, Integer defaultValue)
    {
        super(key, category, defaultValue);
    }

    public int getMinValue()
    {
        return minValue;
    }

    public int getMaxValue()
    {
        return maxValue;
    }

    @Override
    protected IntegerField deserialize(JsonObject jsonObject)
    {
        super.deserialize(jsonObject);
        this.defaultValue = getInteger("defaultValue", jsonObject);
        this.value = getInteger("value", jsonObject);
        this.minValue = getInteger("minValue", jsonObject);
        this.maxValue = getInteger("maxValue", jsonObject);
        return this;
    }

    @Override
    protected JsonObject serialize()
    {
        final JsonObject jsonObject = super.serialize();
        jsonObject.addProperty("defaultValue", this.defaultValue);
        jsonObject.addProperty("value", this.value);
        jsonObject.addProperty("minValue", this.minValue);
        jsonObject.addProperty("maxValue", this.maxValue);
        return jsonObject;
    }

    public static class Serializer implements JsonSerializer<IntegerField>, JsonDeserializer<IntegerField>
    {
        @Override
        public JsonElement serialize(IntegerField src, Type typeOfSrc, JsonSerializationContext context)
        {
            return src.serialize();
        }

        @Override
        public IntegerField deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            return new IntegerField().deserialize(json.getAsJsonObject());
        }
    }
}
