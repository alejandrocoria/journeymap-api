package journeymap.common.properties;

import com.google.gson.*;
import journeymap.common.Journeymap;
import journeymap.common.properties.config.*;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

/**
 * Created by Mark on 3/17/2016.
 */
public abstract class PropertiesSerializer<T extends ConfigField>
{
    // Indicates full serialization should be used
    protected final boolean verbose;

    public PropertiesSerializer(Boolean verbose)
    {
        this.verbose = verbose;
    }
    
    
    public static class BooleanFieldSerializer extends PropertiesSerializer<BooleanField> implements JsonSerializer<BooleanField>, JsonDeserializer<BooleanField>
    {
        public BooleanFieldSerializer(boolean verbose)
        {
            super(verbose);
        }
        
        @Override
        public JsonElement serialize(BooleanField src, Type typeOfSrc, JsonSerializationContext context)
        {
            return baseSerialize(src, typeOfSrc, context);
        }
        
        @Override
        public BooleanField deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            return baseDeserialize(new BooleanField(), json, typeOfT, context);
        }
    }

    public static class IntegerFieldSerializer extends PropertiesSerializer<IntegerField> implements JsonSerializer<IntegerField>, JsonDeserializer<IntegerField>
    {
        public IntegerFieldSerializer(boolean verbose)
        {
            super(verbose);
        }

        @Override
        public JsonElement serialize(IntegerField src, Type typeOfSrc, JsonSerializationContext context)
        {
            return baseSerialize(src, typeOfSrc, context);
        }

        @Override
        public IntegerField deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            return baseDeserialize(new IntegerField(), json, typeOfT, context);
        }
    }

    public static class StringFieldSerializer extends PropertiesSerializer<StringField> implements JsonSerializer<StringField>, JsonDeserializer<StringField>
    {
        public StringFieldSerializer(boolean verbose)
        {
            super(verbose);
        }

        @Override
        public JsonElement serialize(StringField src, Type typeOfSrc, JsonSerializationContext context)
        {
            return baseSerialize(src, typeOfSrc, context);
        }

        @Override
        public StringField deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            return baseDeserialize(new StringField(), json, typeOfT, context);
        }
    }

    public static class EnumFieldSerializer extends PropertiesSerializer<EnumField> implements JsonSerializer<EnumField>, JsonDeserializer<EnumField>
    {
        public EnumFieldSerializer(boolean verbose)
        {
            super(verbose);
        }

        @Override
        public JsonElement serialize(EnumField src, Type typeOfSrc, JsonSerializationContext context)
        {
            return baseSerialize(src, typeOfSrc, context);
        }

        @Override
        public EnumField deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            return baseDeserialize(new EnumField(), json, typeOfT, context);
        }
    }
    
    
    public JsonElement baseSerialize(ConfigField src, Type typeOfSrc, JsonSerializationContext context)
    {
        if(!verbose)
        {
            return context.serialize(src.get());
        }
        else
        {
            JsonObject jsonObject = new JsonObject();
            Map<String,String> attributes = src.getAttributes();
            for(Map.Entry<String, String> attr : attributes.entrySet())
            {
                jsonObject.addProperty(attr.getKey(), attr.getValue());
            }
            return jsonObject;
        }
    }

    protected T baseDeserialize(T result, JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        if(!verbose)
        {
            result.put(ConfigField.ATTR_VALUE, json.getAsString());
        }
        else
        {
            Set<Map.Entry<String, JsonElement>> set = json.getAsJsonObject().entrySet();
            for(Map.Entry<String, JsonElement> entry : set)
            {
                result.put(entry.getKey(), entry.getValue().getAsString());
            }
        }
        return result;
    }
}
