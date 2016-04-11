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
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import journeymap.client.model.GridSpec;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import journeymap.common.properties.config.*;
import journeymap.common.version.Version;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.*;


/**
 * Base GSON-backed properties class for use on client or server. Not threadsafe.
 * <p/>
 * The basics of its design:
 * 1. A subclass of PropertiesBase declares fields that need to be persisted to a JSON file.
 * - Declare public final fields as ConfigFields for anything that needs to be displayed in Options Manager, bound to a UI element, or validated.
 * - Declare fields of other types if needed, but you'll need to include them in updateFrom() and isValid()
 * 2. Use load() to update the values from a JSON file.  Subclasses may need constructor args.
 * 3. Use save() to write the values to a JSON file.
 */
public abstract class PropertiesBase
{
    // GSON charset
    protected static final Charset UTF8 = Charset.forName("UTF-8");

    // State enum for debugging
    protected enum State
    {
        New, Initialized, FirstLoaded, FileLoaded, Valid, Invalid, SavedOk, SavedError
    }

    // Version used to create config
    protected Version configVersion = null;

    // Set of all Categories used in fields
    protected CategorySet categories = new CategorySet();

    // Current file reference
    protected transient File sourceFile = null;

    // Reflection-generated map of all ConfigFields
    private transient Map<String, ConfigField<?>> configFields;

    // Current state, just used for debugging.
    protected State currentState;

    /**
     * Default constructor.
     */
    protected PropertiesBase()
    {
        currentState = State.New;
    }

    /**
     * Gets a Gson instance with registered adapters.
     *
     * @param verbose whether to (de)serialize all field attributes. Useful if config is going between client/server.
     * @return
     */
    public Gson getGson(boolean verbose)
    {
        GsonBuilder gb = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .registerTypeAdapter(BooleanField.class, new GsonHelper.BooleanFieldSerializer(verbose))
                .registerTypeAdapter(IntegerField.class, new GsonHelper.IntegerFieldSerializer(verbose))
                .registerTypeAdapter(StringField.class, new GsonHelper.StringFieldSerializer(verbose))
                .registerTypeAdapter(EnumField.class, new GsonHelper.EnumFieldSerializer(verbose))
                .registerTypeAdapter(CategorySet.class, new GsonHelper.CategorySetSerializer(verbose))
                .registerTypeAdapter(Version.class, new GsonHelper.VersionSerializer(verbose))
                .registerTypeAdapter(GridSpec.class, new GsonHelper.GridSpecSerializer(verbose));

        List<ExclusionStrategy> exclusionStrategies = getExclusionStrategies(verbose);
        if (exclusionStrategies != null && !exclusionStrategies.isEmpty())
        {
            gb.setExclusionStrategies(exclusionStrategies.toArray(new ExclusionStrategy[exclusionStrategies.size()]));
        }

        return gb.create();
    }

    /**
     * Gets a Json representation of this object.
     *
     * @param verbose whether to serialize all field attributes. Useful if config is going between client/server.
     * @return json
     */
    public <T extends PropertiesBase> T fromJsonString(String jsonString, Class<T> propertiesClass, boolean verbose)
    {
        return getGson(verbose).fromJson(jsonString, propertiesClass);
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
     * Returns an instance with values loaded from file
     *
     * @param <T> properties type
     * @return loaded instance
     */
    public <T extends PropertiesBase> T load()
    {
        return load(this.getFile(), false);
    }

    /**
     * Returns an instance with values loaded from file, or itself if the load failed.
     *
     * @param configFile file to load config from
     * @param verbose    whether to deserialize all field attributes.
     * @param <T>        properties type
     * @return loaded instance
     */
    public <T extends PropertiesBase> T load(File configFile, boolean verbose)
    {
        ensureInit();
        boolean saveNeeded = false;

        if (!configFile.canRead())
        {
            this.postLoad(true);
            this.currentState = State.FirstLoaded;
            saveNeeded = true;
        }
        else
        {
            try
            {
                String jsonString = Files.toString(configFile, UTF8);
                T jsonInstance = fromJsonString(jsonString, (Class<T>) this.getClass(), verbose);
                this.updateFrom(jsonInstance);
                this.postLoad(false);
                this.currentState = State.FileLoaded;
                saveNeeded = !this.isValid(false);
            }
            catch (Exception e)
            {
                error(String.format("Can't load config file %s", configFile), e);

                try
                {
                    File badPropFile = new File(configFile.getParentFile(), configFile.getName() + ".bad");
                    configFile.renameTo(badPropFile);
                }
                catch (Exception e3)
                {
                    error(String.format("Can't rename config file %s: %s", configFile, e3.getMessage()));
                }

            }
        }

        // Ensure all fields are initialized
        if (saveNeeded)
        {
            this.save(configFile, verbose);
        }

        return (T) this;
    }

    /**
     * Override if a file needs to have initial configuration after being loaded.
     *
     * @param isNew whether the file is being created the first time
     */
    protected void postLoad(boolean isNew)
    {
        ensureInit();
    }

    /**
     * Copies values from another instance into this one.
     * Override this to include non-ConfigField members if necessary.
     *
     * @param otherInstance other
     * @param <T>           properties type
     */
    public <T extends PropertiesBase> void updateFrom(T otherInstance)
    {
        for (Map.Entry<String, ConfigField<?>> otherEntry : otherInstance.getConfigFields().entrySet())
        {
            String fieldName = otherEntry.getKey();
            ConfigField<?> otherField = otherEntry.getValue();
            ConfigField<?> myField = this.getConfigField(fieldName);
            if (myField != null)
            {
                myField.getAttributeMap().putAll(otherField.getAttributeMap());
            }
            else
            {
                warn("Missing field during updateFrom(): " + fieldName);
            }
        }
        this.configVersion = otherInstance.configVersion;
    }

    /**
     * Ensure state completely initialized.
     */
    protected void ensureInit()
    {
        if (configFields == null)
        {
            getConfigFields();
            this.currentState = State.Initialized;
        }
    }

    /**
     * Override if a file needs to do custom work just prior to saving.
     */
    protected void preSave()
    {
        ensureInit();
    }

    /**
     * Saves the property object to file.
     *
     * @return true if saved
     */
    public boolean save()
    {
        return save(getFile(), false);
    }

    /**
     * Saves the property object to file.
     *
     * @param configFile file to save config to
     * @param verbose    whether to serialize all field attributes.
     * @return true if saved
     */
    public boolean save(File configFile, boolean verbose)
    {
        preSave();
        boolean saved = false;
        boolean canSave = isValid(true);
        if (!canSave)
        {
            error(String.format("Can't save invalid config to file: %s", this.getFileName()));
        }
        else
        {
            try
            {
                // Check for existing file
                if (!configFile.exists())
                {
                    info(String.format("Creating config file: %s", configFile));
                    if (!configFile.getParentFile().exists())
                    {
                        configFile.getParentFile().mkdirs();
                    }
                }
                else if (!isCurrent())
                {
                    if (configVersion != null)
                    {
                        info(String.format("Updating config file from version \"%s\" to \"%s\": %s", configVersion, Journeymap.JM_VERSION, configFile));
                    }
                    configVersion = Journeymap.JM_VERSION;
                }

                StringBuilder sb = new StringBuilder();

                // Add Headers
                String lineEnding = System.getProperty("line.separator");
                for (String line : getHeaders())
                {
                    sb.append(line).append(lineEnding);
                }
                String header = sb.toString();

                // Add Json body
                String json = toJsonString(verbose);

                // Write to file
                Files.write(header + json, configFile, UTF8);

                saved = true;
            }
            catch (Exception e)
            {
                error(String.format("Can't save config file %s: %s", configFile, e), e);
            }
        }

        this.currentState = saved ? State.SavedOk : State.SavedError;
        return saved;
    }

    /**
     * Gets a Json representation of this object.
     *
     * @param verbose whether to serialize all field attributes. Useful if config is going between client/server.
     * @return json
     */
    public String toJsonString(boolean verbose)
    {
        ensureInit();
        return getGson(verbose).toJson(this);
    }

    /**
     * Whether state is valid
     *
     * @param fix whether to try to fix validation problems
     * @return true if valid
     */
    public boolean isValid(boolean fix)
    {
        ensureInit();
        boolean valid = validateFields(fix);
        if (!isCurrent())
        {
            if (fix)
            {
                configVersion = Journeymap.JM_VERSION;
                info(String.format("Setting config file to version \"%s\": %s", configVersion, getFileName()));
            }
            else
            {
                valid = false;
                info(String.format("Config file isn't current, has version \"%s\": %s", configVersion, getFileName()));
            }
        }

        this.currentState = valid ? State.Valid : State.Invalid;
        return valid;
    }

    /**
     * Get a ConfigField by its field name
     *
     * @param fieldName name
     * @return field or null
     */
    protected ConfigField<?> getConfigField(String fieldName)
    {
        return getConfigFields().get(fieldName);
    }

    /**
     * Map (keyed by field name) of all ConfigFields defined on the class via reflection.
     * If the owning class isn't set yet, it's done so here.
     */
    public Map<String, ConfigField<?>> getConfigFields()
    {
        if (this.configFields == null)
        {
            HashMap<String, ConfigField<?>> map = new HashMap<String, ConfigField<?>>();
            try
            {
                for (Field field : getClass().getFields())
                {
                    Class<?> fieldType = field.getType();

                    if (ConfigField.class.isAssignableFrom(fieldType))
                    {
                        ConfigField configField = (ConfigField) field.get(this);
                        if (configField != null)
                        {
                            configField.setOwner(field.getName(), this);
                            Category category = configField.getCategory();
                            if (category != null)
                            {
                                this.categories.add(category);
                            }
                        }
                        map.put(field.getName(), configField);
                    }
                }
            }
            catch (Throwable t)
            {
                error("Unexpected error getting fields: " + LogFormatter.toString(t));
            }

            this.configFields = Collections.unmodifiableMap(map);
        }

        return this.configFields;
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
     * @param fix set to true to try to correct the problems
     * @return true if all valid
     */
    protected boolean validateFields(boolean fix)
    {
        try
        {
            boolean valid = true;

            for (Map.Entry<String, ConfigField<?>> entry : getConfigFields().entrySet())
            {
                ConfigField<?> configField = entry.getValue();
                if (configField == null)
                {
                    warn(String.format("%s.%s is null",
                            getClass().getSimpleName(),
                            entry.getKey()));
                    valid = false;
                }
                else
                {
                    boolean fieldValid = configField.validate(fix);
                    if (!fieldValid)
                    {
                        valid = false;
                    }
                }
            }

            return valid;
        }
        catch (Throwable t)
        {
            error("Unexpected error in validateFields: " + LogFormatter.toPartialString(t));
            return false;
        }
    }

    /**
     * Override this to provide a customized way to exclude fields from serialization.
     *
     * @param verbose true for verbose serialization
     * @return strategy impl or null
     */
    public List<ExclusionStrategy> getExclusionStrategies(boolean verbose)
    {
        ArrayList strategies = new ArrayList<ExclusionStrategy>();
        if (!verbose)
        {
            // Don't serialize categories in compact form
            strategies.add(new ExclusionStrategy()
            {
                @Override
                public boolean shouldSkipField(FieldAttributes f)
                {
                    if (f.getDeclaringClass().equals(PropertiesBase.class))
                    {
                        return f.getName().equals("categories");
                    }
                    return false;
                }

                @Override
                public boolean shouldSkipClass(Class<?> clazz)
                {
                    return false;
                }
            });
        }
        return strategies;
    }

    /**
     * If the file exists, returns the lastModified timestamp.
     */
    public long lastModified()
    {
        File file = getFile();
        if (file.canRead())
        {
            return file.lastModified();
        }
        else
        {
            return 0;
        }
    }

    protected Objects.ToStringHelper toStringHelper()
    {
        Objects.ToStringHelper toStringHelper = Objects.toStringHelper(this)
                .add("state", currentState)
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

    /**
     * Todo: Implement true deep equivalency?
     *
     * @param o
     * @return
     */
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

    /**
     * Logs the message with the properties name and current state
     *
     * @param message
     */
    protected void info(String message)
    {
        Journeymap.getLogger().info(String.format("%s (%s) %s", getName(), currentState, message));
    }

    /**
     * Logs the message with the properties name and current state
     *
     * @param message
     */
    protected void warn(String message)
    {
        Journeymap.getLogger().warn(String.format("%s (%s) %s", getName(), currentState, message));
    }

    /**
     * Logs the message with the properties name and current state
     *
     * @param message
     */
    protected void error(String message)
    {
        Journeymap.getLogger().error(String.format("%s (%s) %s", getName(), currentState, message));
    }

    /**
     * Logs the message with the properties name and current state
     *
     * @param message
     */
    protected void error(String message, Throwable throwable)
    {
        Journeymap.getLogger().error(String.format("%s (%s) %s : %s", getName(), currentState, message, LogFormatter.toString(throwable)));
    }
}
