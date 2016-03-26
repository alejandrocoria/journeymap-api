/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.common.properties;

import com.google.common.base.Objects;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import journeymap.common.properties.config.*;
import journeymap.common.version.Version;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;


/**
 * Base GSON-backed properties class for use on client or server.
 */
public abstract class PropertiesBase
{
    // GSON charset
    protected static final Charset UTF8 = Charset.forName("UTF-8");

    // Version used to create config
    protected Version configVersion = null;

    // Set of all Categories used in fields
    protected CategorySet categories = new CategorySet();

    // Current file reference
    protected transient File sourceFile = null;

    /**
     * Default constructor.
     */
    protected PropertiesBase()
    {
    }

    /**
     * Gets a Gson instance with registered adapters.
     *
     * @param verbose whether to (de)serialize all field attributes. Useful if config is going between client/server.
     * @return
     */
    public static Gson getGson(boolean verbose)
    {
        GsonBuilder gb = new GsonBuilder()
                .registerTypeAdapter(BooleanField.class, new Utils.BooleanFieldSerializer(verbose))
                .registerTypeAdapter(IntegerField.class, new Utils.IntegerFieldSerializer(verbose))
                .registerTypeAdapter(StringField.class, new Utils.StringFieldSerializer(verbose))
                .registerTypeAdapter(EnumField.class, new Utils.EnumFieldSerializer(verbose))
                .registerTypeAdapter(CategorySet.class, new Utils.CategorySetSerializer(verbose));

        return gb.setPrettyPrinting().create();
    }

    /**
     * Gets a Json representation of this object.
     *
     * @param verbose whether to serialize all field attributes. Useful if config is going between client/server.
     * @return json
     */
    public static <T extends PropertiesBase> T fromJsonString(String jsonString, Class<T> propertiesClass, boolean verbose)
    {
        return getGson(verbose).fromJson(jsonString, propertiesClass);
    }

    /**
     * Returns an instance with values loaded from file, or itself if the load failed.
     *
     * @param propertiesClass class to instantiate
     * @param constructorArgs optional arguments needed for constructor
     * @param <T>             properties type
     * @return loaded instance
     */
    public static <T extends PropertiesBase> T load(Class<T> propertiesClass, Object... constructorArgs)
    {
        return load(propertiesClass, false, constructorArgs);
    }

    /**
     * Returns an instance with values loaded from file, or itself if the load failed.
     *
     * @param verbose         whether to serialize all field attributes. Useful if config is going between client/server.
     * @param propertiesClass class to instantiate
     * @param constructorArgs optional arguments needed for constructor
     * @param <T>             properties type
     * @return loaded instance
     */
    public static <T extends PropertiesBase> T load(Class<T> propertiesClass, boolean verbose, Object... constructorArgs)
    {
        boolean saveNeeded = true;
        File propFile = null;
        T instance = null;
        try
        {
            if (constructorArgs.length == 0)
            {
                instance = (T) propertiesClass.newInstance();
            }
            else
            {
                Class[] argTypes = new Class[constructorArgs.length];
                for (int i = 0; i < constructorArgs.length; i++)
                {
                    argTypes[i] = constructorArgs[i].getClass();
                }
                Constructor<T> constructor = propertiesClass.getConstructor(argTypes);
                instance = constructor.newInstance(constructorArgs);
            }

            propFile = instance.getFile();

            if (propFile.canRead())
            {
                String jsonString = Files.toString(propFile, UTF8);
                T jsonInstance = fromJsonString(jsonString, propertiesClass, verbose);
                saveNeeded = !jsonInstance.isCurrent();
                if (saveNeeded)
                {
                    Journeymap.getLogger().info(String.format("Config file needs to be updated: %s", propFile.getName()));
                }

                if (verbose)
                {
                    // Just use whatever was deserialized
                    instance = jsonInstance;
                }
                else
                {
                    // Overlay values from json-deserialized fields
                    instance.getConfigFields().putAll(jsonInstance.getConfigFields());
                }
            }
            else
            {
                instance.newFileInit();
                saveNeeded = true;
            }
        }
        catch (Exception e)
        {
            Journeymap.getLogger().error(String.format("Can't load config file %s: %s", propFile,
                    LogFormatter.toPartialString(e)));

            try
            {
                File badPropFile = new File(propFile.getParentFile(), propFile.getName() + ".bad");
                propFile.renameTo(badPropFile);
            }
            catch (Exception e3)
            {
                Journeymap.getLogger().error(String.format("Can't rename config file %s: %s", propFile, e3.getMessage()));
            }

        }

        // Ensure all fields are initialized
        if (instance != null)
        {
            instance.getConfigFields();
        }

        if (instance != null && (instance.validate() || saveNeeded))
        {
            instance.save();
        }

        return instance;
    }

    /**
     * Save properties to file and reload.
     *
     * @param properties      Current instance, can be null
     * @param propertiesClass Properties class
     * @param constructorArgs optional arguments needed for constructor
     * @param <T>             extends PropertiesBase
     * @return newly-loaded instance
     */
    public static <T extends PropertiesBase> T reload(T properties, Class<T> propertiesClass, Object... constructorArgs)
    {
        if (properties != null)
        {
            properties.save();
        }

        T reloadedProperties = null;
        try
        {
            reloadedProperties = load(propertiesClass, false, constructorArgs);
            if (properties == null)
            {
                Journeymap.getLogger().info("Loaded " + propertiesClass.getSimpleName() + " from " + reloadedProperties.getFile());
            }
            return reloadedProperties;
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error("Failed to reload " + propertiesClass.getName(), t);
            return (properties != null) ? properties : reloadedProperties;
        }
    }

    /**
     * Name used in property file
     *
     * @return name
     */
    public abstract String getName();

    /**
     * Gets the property file.
     *
     * @return file
     */
    public abstract File getFile();

    /**
     * Gets an array of headers to prepend to the property file when saved.
     *
     * @return
     */
    public abstract String[] getHeaders();

    /**
     * Gets the filename for the instance.
     *
     * @return
     */
    public abstract String getFileName();

    /**
     * Whether the file has the same config version as the current JourneyMap version
     *
     * @return true if current
     */
    public boolean isCurrent()
    {
        return Journeymap.JM_VERSION.equals(configVersion);
    }

    /**
     * Saves the property object to file.
     *
     * @return true if saved
     */
    public boolean save()
    {
        File propFile = null;
        try
        {
            // Write to file
            propFile = getFile();

            if (!propFile.exists())
            {
                Journeymap.getLogger().info(String.format("Creating config file: %s", propFile));
                if (!propFile.getParentFile().exists())
                {
                    propFile.getParentFile().mkdirs();
                }
            }
            else if (!isCurrent())
            {
                Journeymap.getLogger().info(String.format("Updating config file from version \"%s\" to \"%s\": %s", configVersion, Journeymap.JM_VERSION, propFile));
                configVersion = Journeymap.JM_VERSION;
            }

            // Header
            String lineEnding = System.getProperty("line.separator");
            StringBuilder sb = new StringBuilder();
            for (String line : getHeaders())
            {
                sb.append(line).append(lineEnding);
            }
            String header = sb.toString();

            // Json body
            String json = toJsonString(false);

            // Write to file
            Files.write(header + json, propFile, UTF8);

            Journeymap.getLogger().debug("Saved " + getFileName());

            return true;
        }
        catch (Exception e)
        {
            Journeymap.getLogger().error(String.format("Can't save config file %s: %s", propFile, e), e);
            return false;
        }
    }

    /**
     * Gets a Json representation of this object.
     *
     * @param verbose whether to serialize all field attributes. Useful if config is going between client/server.
     * @return json
     */
    public String toJsonString(boolean verbose)
    {
        return getGson(verbose).toJson(this);
    }

    /**
     * Override if a new file needs to have initial configuration.
     */
    protected void newFileInit()
    {
    }

    /**
     * Whether properties are valid.
     *
     * @return
     */
    protected boolean validate()
    {
        return validateFields();
    }

    /**
     * Map (keyed by field name) of all ConfigFields defined on the class via reflection.
     * If the owning class isn't set yet, it's done so here.
     *
     * @return
     */
    protected HashMap<String, ConfigField<?>> getConfigFields()
    {
        HashMap<String, ConfigField<?>> configFields = new HashMap<String, ConfigField<?>>();
        try
        {
            Class<?> theClass = getClass();
            while (PropertiesBase.class.isAssignableFrom(theClass))
            {
                for (Field field : getClass().getDeclaredFields())
                {
                    Class<?> fieldType = field.getType();

                    if (ConfigField.class.isAssignableFrom(fieldType))
                    {
                        ConfigField configField = (ConfigField) field.get(this);
                        if (configField.getOwner() != this)
                        {
                            // Hasn't been initialized yet
                            configField.setOwner(this);
                            Category category = configField.getCategory();
                            if (category != null)
                            {
                                this.categories.add(category);
                            }
                        }
                        configFields.put(field.getName(), configField);
                    }
                }

                // Look for fields on the parent class too
                theClass = theClass.getSuperclass();
            }
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error("Unexpected error getting fields: " + LogFormatter.toPartialString(t));
        }
        return configFields;
    }

    /**
     * Get a Category known to this Properties instance (used in a field)
     * by name.
     *
     * @param name category name
     * @return category or null
     */
    public Category getCategoryByName(String name)
    {
        for (Category category : categories)
        {
            if (category.getName().equalsIgnoreCase(name))
            {
                return category;
            }
        }
        return null;
    }

    /**
     * Validate all ConfigFields on the class, logging warnings where there are problems.
     *
     * @return true if all valid
     */
    protected boolean validateFields()
    {
        try
        {
            boolean valid = true;

            for (Map.Entry<String, ConfigField<?>> entry : getConfigFields().entrySet())
            {
                ConfigField<?> configField = entry.getValue();
                if (configField == null)
                {
                    Journeymap.getLogger().warn(String.format("%s.%s is null",
                            getClass().getSimpleName(),
                            entry.getKey()));
                    valid = false;
                }
                else
                {
                    boolean fieldValid = configField.isValid();
                    if (!fieldValid)
                    {
                        Journeymap.getLogger().warn(String.format("%s.%s has invalid %s",
                                getClass().getSimpleName(),
                                entry.getKey(),
                                configField.getClass().getSimpleName()));
                        valid = false;
                    }
                }
            }

            return valid;
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error("Unexpected error in validateFields: " + LogFormatter.toPartialString(t));
            return false;
        }
    }

    /**
     * Validate and save.
     */
    public void ensureValid()
    {
        validate();
        save();
    }

    protected Objects.ToStringHelper toStringHelper()
    {
        Objects.ToStringHelper toStringHelper = Objects.toStringHelper(this)
                .add("file", getFileName())
                .add("configVersion", configVersion);
        return toStringHelper;
    }

    @Override
    public String toString()
    {
        Objects.ToStringHelper toStringHelper = toStringHelper();

        for (Map.Entry<String, ConfigField<?>> entry : getConfigFields().entrySet())
        {
            ConfigField<?> configField = entry.getValue();
            toStringHelper.add(entry.getKey(), configField.get());
        }

        return toStringHelper.toString();
    }

    @Override
    public final boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof PropertiesBase))
        {
            return false;
        }
        PropertiesBase that = (PropertiesBase) o;
        return Objects.equal(getFileName(), that.getFileName());
    }

    @Override
    public final int hashCode()
    {
        return Objects.hashCode(getConfigFields());
    }
}
