package journeymap.common.properties.config;

import com.google.gson.*;
import journeymap.common.Journeymap;
import journeymap.common.properties.Category;
import journeymap.common.properties.CategorySet;
import journeymap.common.version.Version;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

/**
 * Serializers for Config classes.
 */
public abstract class JsonHelper<T extends ConfigField>
{
    // Indicates full serialization should be used
    protected final boolean verbose;

    public JsonHelper(Boolean verbose)
    {
        this.verbose = verbose;
    }

    /**
     * Serialize attributes as Strings.
     */
    public JsonElement serializeAttributes(ConfigField<?> src, Type typeOfSrc, JsonSerializationContext context)
    {
        if (!verbose)
        {
            // Just serialize ATTR_VALIUE
            return context.serialize(src.getStringAttr(ConfigField.ATTR_VALUE));
        }
        else
        {
            // Serialize all attributes
            JsonObject jsonObject = new JsonObject();
            for (String attrName : src.getAttributeNames())
            {
                jsonObject.addProperty(attrName, src.getStringAttr(attrName));
            }
            return jsonObject;
        }
    }

    /**
     * Deserialize attributes as Strings.
     */
    protected T deserializeAttributes(T result, JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        if (!verbose || !json.isJsonObject())
        {
            // Just deserialize ATTR_VALUE
            result.put(ConfigField.ATTR_VALUE, json.getAsString());
        }
        else
        {
            // Deserialize all attributes
            Set<Map.Entry<String, JsonElement>> set = json.getAsJsonObject().entrySet();
            for (Map.Entry<String, JsonElement> entry : set)
            {
                result.put(entry.getKey(), entry.getValue().getAsString());
            }
        }
        return result;
    }

    /**
     * Handles CategorySet instances.
     */
    public static class CategorySetSerializer implements JsonSerializer<CategorySet>, JsonDeserializer<CategorySet>
    {
        // Indicates full serialization should be used
        protected final boolean verbose;

        public CategorySetSerializer(boolean verbose)
        {
            this.verbose = verbose;
        }

        @Override
        public JsonElement serialize(CategorySet src, Type typeOfSrc, JsonSerializationContext context)
        {
            if (!verbose)
            {
                return null;
            }
            Category[] array = new Category[src.size()];
            return context.serialize(src.toArray(array));
        }

        @Override
        public CategorySet deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            CategorySet categorySet = new CategorySet();
            if (verbose)
            {
                JsonArray jsonArray = json.getAsJsonArray();
                for (JsonElement jsonElement : jsonArray)
                {
                    categorySet.add((Category) context.deserialize(jsonElement, Category.class));
                }
            }
            return categorySet;
        }
    }

    /**
     * Handles CategorySet instances.
     */
    public static class VersionSerializer implements JsonSerializer<Version>, JsonDeserializer<Version>
    {
        public VersionSerializer(boolean verbose)
        {
        }

        @Override
        public JsonElement serialize(Version src, Type typeOfSrc, JsonSerializationContext context)
        {
            return context.serialize(src.toString());
        }

        @Override
        public Version deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            return Version.from(json.getAsString(), Journeymap.JM_VERSION);
        }
    }

    /**
     * Handles BooleanField instances.
     */
    public static class BooleanFieldSerializer extends JsonHelper<BooleanField> implements JsonSerializer<BooleanField>, JsonDeserializer<BooleanField>
    {
        public BooleanFieldSerializer(boolean verbose)
        {
            super(verbose);
        }

        @Override
        public JsonElement serialize(BooleanField src, Type typeOfSrc, JsonSerializationContext context)
        {
            return serializeAttributes(src, typeOfSrc, context);
        }

        @Override
        public BooleanField deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            return deserializeAttributes(new BooleanField(), json, typeOfT, context);
        }
    }

    /**
     * Handles IntegerField instances.
     */
    public static class IntegerFieldSerializer extends JsonHelper<IntegerField> implements JsonSerializer<IntegerField>, JsonDeserializer<IntegerField>
    {
        public IntegerFieldSerializer(boolean verbose)
        {
            super(verbose);
        }

        @Override
        public JsonElement serialize(IntegerField src, Type typeOfSrc, JsonSerializationContext context)
        {
            return serializeAttributes(src, typeOfSrc, context);
        }

        @Override
        public IntegerField deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            return deserializeAttributes(new IntegerField(), json, typeOfT, context);
        }
    }

    /**
     * Handles StringField instances.
     */
    public static class StringFieldSerializer extends JsonHelper<StringField> implements JsonSerializer<StringField>, JsonDeserializer<StringField>
    {
        public StringFieldSerializer(boolean verbose)
        {
            super(verbose);
        }

        @Override
        public JsonElement serialize(StringField src, Type typeOfSrc, JsonSerializationContext context)
        {
            return serializeAttributes(src, typeOfSrc, context);
        }

        @Override
        public StringField deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            return deserializeAttributes(new StringField(), json, typeOfT, context);
        }
    }

    /**
     * Handles EnumField instances.
     */
    public static class EnumFieldSerializer extends JsonHelper<EnumField> implements JsonSerializer<EnumField>, JsonDeserializer<EnumField>
    {
        public EnumFieldSerializer(boolean verbose)
        {
            super(verbose);
        }

        @Override
        public JsonElement serialize(EnumField src, Type typeOfSrc, JsonSerializationContext context)
        {
            return serializeAttributes(src, typeOfSrc, context);
        }

        @Override
        public EnumField deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            return deserializeAttributes(new EnumField(), json, typeOfT, context);
        }
    }
}
