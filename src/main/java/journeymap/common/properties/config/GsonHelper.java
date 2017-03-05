/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.common.properties.config;

import com.google.common.base.Joiner;
import com.google.gson.*;
import journeymap.client.cartography.RGB;
import journeymap.client.model.GridSpec;
import journeymap.common.Journeymap;
import journeymap.common.properties.Category;
import journeymap.common.properties.CategorySet;
import journeymap.common.version.Version;

import java.awt.*;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

/**
 * Serializers for Config classes.
 */
public abstract class GsonHelper<T extends ConfigField>
{
    // Indicates full serialization should be used
    protected final boolean verbose;

    public GsonHelper(Boolean verbose)
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
                try
                {
                    result.put(entry.getKey(), entry.getValue().getAsString());
                }
                catch (Throwable t)
                {
                    Journeymap.getLogger().warn("Error deserializing %s in %s: %s", entry, json, t);
                }
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
     * Handles Version instances for both 5.1 and 5.2 configs
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
            if (json.isJsonObject())
            {
                // Use basic deserialization (from 5.1)
                JsonObject jo = json.getAsJsonObject();
                return Version.from(
                        jo.get("major").getAsString(),
                        jo.get("minor").getAsString(),
                        jo.get("micro").getAsString(),
                        jo.get("patch").getAsString(),
                        Journeymap.JM_VERSION
                );
            }
            else
            {
                // Use string deserialization (from 5.2)
                return Version.from(json.getAsString(), Journeymap.JM_VERSION);
            }
        }
    }

    /**
     "day": {
     "style": "Squares",
     "red": 0.5,
     "green": 0.5,
     "blue": 0.5,
     "alpha": 0.5,
     "colorX": -1,
     "colorY": -1
     }
     */

    /**
     * Handles Version instances for both 5.1 and 5.2 configs
     */
    public static class GridSpecSerializer implements JsonSerializer<GridSpec>, JsonDeserializer<GridSpec>
    {
        public GridSpecSerializer(boolean verbose)
        {
        }

        @Override
        public JsonElement serialize(GridSpec src, Type typeOfSrc, JsonSerializationContext context)
        {
            String string = Joiner.on(",").join(src.style, RGB.toHexString(src.getColor()), src.alpha, src.getColorX(), src.getColorY());
            return context.serialize(string);
        }

        @Override
        public GridSpec deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            if (json.isJsonObject())
            {
                // Use basic deserialization (from 5.1)
                JsonObject jo = json.getAsJsonObject();
                GridSpec gridSpec = new GridSpec(
                        GridSpec.Style.valueOf(GridSpec.Style.class, jo.get("style").getAsString()),
                        jo.get("red").getAsFloat(),
                        jo.get("green").getAsFloat(),
                        jo.get("blue").getAsFloat(),
                        jo.get("alpha").getAsFloat()
                );
                gridSpec.setColorCoords(jo.get("colorX").getAsInt(), jo.get("colorY").getAsInt());
                return gridSpec;
            }
            else
            {
                // Use string deserialization (from 5.2)
                String[] parts = json.getAsString().split(",");
                GridSpec gridSpec = new GridSpec(
                        GridSpec.Style.valueOf(GridSpec.Style.class, parts[0]),
                        new Color(RGB.hexToInt(parts[1])),
                        Float.parseFloat(parts[2])
                );

                gridSpec.setColorCoords(Integer.parseInt(parts[3]), Integer.parseInt(parts[4]));
                return gridSpec;
            }
        }
    }

    /**
     * Handles BooleanField instances.
     */
    public static class BooleanFieldSerializer extends GsonHelper<BooleanField> implements JsonSerializer<BooleanField>, JsonDeserializer<BooleanField>
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
    public static class IntegerFieldSerializer extends GsonHelper<IntegerField> implements JsonSerializer<IntegerField>, JsonDeserializer<IntegerField>
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
    public static class StringFieldSerializer extends GsonHelper<StringField> implements JsonSerializer<StringField>, JsonDeserializer<StringField>
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
    public static class EnumFieldSerializer extends GsonHelper<EnumField> implements JsonSerializer<EnumField>, JsonDeserializer<EnumField>
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
