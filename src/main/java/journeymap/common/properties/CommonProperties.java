/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.common.properties;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import journeymap.common.properties.config.*;
import journeymap.common.version.Version;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;


/**
 * Base GSON-backed properties class.
 */
public abstract class CommonProperties
{
    // GSON charset
    protected static final Charset UTF8 = Charset.forName("UTF-8");

    // Gson for file persistence
    protected transient final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(BooleanField.class, new PropertiesSerializer.BooleanFieldSerializer(false))
            .registerTypeAdapter(IntegerField.class, new PropertiesSerializer.IntegerFieldSerializer(false))
            .registerTypeAdapter(StringField.class, new PropertiesSerializer.StringFieldSerializer(false))
            .registerTypeAdapter(EnumField.class, new PropertiesSerializer.EnumFieldSerializer(false))
            .create();

    // Whether it's disabled
    public Boolean disabled = false;

    // Version used to create config
    public Version configVersion = null;

    // Current file reference
    protected transient File sourceFile = null;

    /**
     * Default constructor.
     */
    protected CommonProperties()
    {
    }

    public static <T extends CommonProperties> T reload(T properties, Class<T> propertiesClass)
    {
        if (properties != null)
        {
            properties.save();
        }

        T reloadedProperties = null;
        try
        {
            reloadedProperties = propertiesClass.newInstance().load();
            boolean sourceChanged = (properties == null) || properties.isWorldConfig() != reloadedProperties.isWorldConfig();
            if (sourceChanged)
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
     * Whethere the current source file is associated with a specific world.
     *
     * @return
     */
    public abstract boolean isWorldConfig();

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
    protected String getFileName()
    {
        return String.format("journeymap.%s.config", this.getName());
    }

    /**
     * Whether this config is disabled and shouldn't be used.
     *
     * @return
     */
    public boolean isDisabled()
    {
        return disabled;
    }

    /**
     * Set disabled - only works for world configs.
     * Saves after the set.
     *
     * @param disable
     */
    public void setDisabled(boolean disable)
    {
        if (isWorldConfig())
        {
            disabled = (disable);
            save();
        }
        else
        {
            throw new IllegalStateException("Can't disable standard config.");
        }
    }

    /**
     * Copies standard config to world config.
     *
     * @param overwrite true if current world config should be overwritten
     * @return true if copy succeeded
     */
    public boolean copyToWorldConfig(boolean overwrite)
    {
        if (!isWorldConfig())
        {
            try
            {
                File worldConfig = getFile();
                if (overwrite || !worldConfig.exists())
                {
                    save();
                    Files.copy(sourceFile, worldConfig);
                    return worldConfig.canRead();
                }
            }
            catch (IOException e)
            {
                Journeymap.getLogger().error("Couldn't copy config to world config: " + e, e);
            }
            return false;
        }
        else
        {
            throw new IllegalStateException("Can't create World config from itself.");
        }
    }

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
     * Saves the property object to file
     *
     * @return true if saved
     */
    public boolean save()
    {
        synchronized (gson)
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
                String json = gson.toJson(this);

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
    }

    public boolean toggle(final BooleanField ab)
    {
        ab.set(!ab.get());
        save();
        return ab.get();
    }

    /**
     * Returns an instance with values loaded
     * from file, or itself if the load failed.
     *
     * @param <T> properties default instance
     * @return loaded instance
     */
    public <T extends CommonProperties> T load()
    {
        T instance = (T) this;
        File propFile = getFile();
        boolean saveNeeded = true;
        try
        {
            if (propFile.canRead())
            {
                instance = gson.fromJson(Files.toString(propFile, UTF8), (Class<T>) getClass());
                saveNeeded = !instance.isCurrent();
                if (saveNeeded)
                {
                    Journeymap.getLogger().info(String.format("Config file needs to be updated: %s", propFile.getName()));
                }
            }
            else
            {
                instance.newFileInit();
            }
        }
        catch (Exception e)
        {
            Journeymap.getLogger().error(String.format("Can't load config file %s: %s", propFile, LogFormatter.toPartialString(e)));

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

        if (instance == null)
        {
            try
            {
                instance = (T) getClass().newInstance();
                instance.newFileInit();
                saveNeeded = true;
            }
            catch (Exception e)
            {
                // This isn't really the reason for this exception, just the root cause of the trouble.
                throw new RuntimeException("Config file corrupted.  Please fix or remove: " + propFile);
            }
        }

        if (instance != null && (instance.validate() || saveNeeded))
        {
            instance.save();
        }

        return instance;
    }

    /**
     * Override if a new file should have special configuration.
     */
    protected void newFileInit()
    {

    }

    /**
     * Should return true if save needed after validation.
     *
     * @return
     */
    protected boolean validate()
    {
        // Check fields
        boolean saveNeeded = validateFields();

        // Only world configs should be disabled.
        if (!isWorldConfig() && isDisabled())
        {
            disabled = (false);
            saveNeeded = true;
        }

        return saveNeeded;
    }

    /**
     * Use reflection to validate all fields
     *
     * @return
     */
    protected boolean validateFields()
    {
        try
        {
            boolean saveNeeded = false;

            Class<?> theClass = getClass();

            while(theClass.isAssignableFrom(CommonProperties.class))
            {
                for (Field field : getClass().getDeclaredFields())
                {
                    Class<?> fieldType = field.getType();

                    if (ConfigField.class.isAssignableFrom(fieldType))
                    {
                        ConfigField configField = (ConfigField) field.get(this);
                        if (configField == null)
                        {
                            Journeymap.getLogger().warn(theClass.getSimpleName() + " had null ConfigField for " + field.getName());
                            saveNeeded = true;
                        }
                        else
                        {
                            boolean fieldSaveNeeded = configField.saveNeeded();
                            if(fieldSaveNeeded)
                            {
                                Journeymap.getLogger().warn(theClass.getSimpleName() + " had problematic ConfigField for " + field.getName());
                            }

                            saveNeeded = fieldSaveNeeded || saveNeeded;
                        }
                    }
                }

                // Look for fields on the parent class too
                theClass = theClass.getSuperclass();
            }

            return saveNeeded;
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error("Unexpected error in validateFields: " + LogFormatter.toString(t));
            return false;
        }
    }

    public void ensureValid()
    {
        validate();
        save();
    }
}
