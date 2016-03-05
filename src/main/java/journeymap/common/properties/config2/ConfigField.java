package journeymap.common.properties.config2;

import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * Created by Mark on 3/2/2016.
 */
public class ConfigField<T>
{
    String key;
    String category;
    boolean isMaster;
    int sortOrder = 100;

    T defaultValue;
    T value;

    protected ConfigField()
    {
    }

    public ConfigField(String key, String category, T defaultValue)
    {
        this.key = key;
        this.category = category;
        this.defaultValue = defaultValue;
    }

    public String getKey()
    {
        return key;
    }

    public String getCategory()
    {
        return category;
    }

    public T getDefaultValue()
    {
        return defaultValue;
    }

    public ConfigField setValue(T value)
    {
        this.value = value;
        return this;
    }

    public T getValue()
    {
        return value;
    }

    public int getSortOrder()
    {
        return sortOrder;
    }

    public ConfigField setSortOrder(int sortOrder)
    {
        this.sortOrder = sortOrder;
        return this;
    }

    public boolean isMaster()
    {
        return isMaster;
    }

    public ConfigField setMaster(boolean master)
    {
        isMaster = master;
        return this;
    }

    protected ConfigField<T> deserialize(JsonObject jsonObject)
    {
        this.key = getString("key", jsonObject);
        this.category = getString("category", jsonObject);
        this.isMaster = getBoolean("isMaster", jsonObject);
        this.sortOrder = getInteger("sortOrder", jsonObject);
        return this;
    }

    protected JsonObject serialize()
    {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("key", this.key);
        jsonObject.addProperty("category", this.category);
        jsonObject.addProperty("isMaster", this.isMaster);
        jsonObject.addProperty("sortOrder", this.sortOrder);
        return jsonObject;
    }

    protected static String getString(String name, JsonObject jsonObject)
    {
        JsonElement element = jsonObject.get(name);
        if(element==null)
        {
            return null;
        }
        else
        {
            return element.getAsString();
        }
    }

    protected static boolean getBoolean(String name, JsonObject jsonObject)
    {
        JsonElement element = jsonObject.get(name);
        if(element==null)
        {
            return false;
        }
        else
        {
            return element.getAsBoolean();
        }
    }

    protected static int getInteger(String name, JsonObject jsonObject)
    {
        JsonElement element = jsonObject.get(name);
        if(element==null)
        {
            return 0;
        }
        else
        {
            return element.getAsInt();
        }
    }

}
