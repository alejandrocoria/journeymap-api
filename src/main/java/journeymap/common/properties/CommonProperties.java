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
import journeymap.common.properties.config.AtomicBooleanSerializer;
import journeymap.common.properties.config.AtomicIntegerSerializer;
import journeymap.common.properties.config.AtomicReferenceSerializer;
import journeymap.common.properties.config.ConfigValidation;
import journeymap.common.version.Version;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Base GSON-backed properties class.
 */
public abstract class CommonProperties
{
    // GSON charset
    protected static final Charset UTF8 = Charset.forName("UTF-8");

    // Flag the serializers can use to signal the file format needs to be updated
    protected static transient final AtomicBoolean configFormatChanged = new AtomicBoolean(false);



    // Gson for file persistence
    protected transient final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(AtomicBoolean.class, new AtomicBooleanSerializer(configFormatChanged))
            .registerTypeAdapter(AtomicInteger.class, new AtomicIntegerSerializer(configFormatChanged))
            .registerTypeAdapter(AtomicReference.class, new AtomicReferenceSerializer(configFormatChanged))
            .create();
    // Whether it's disabled
    protected final AtomicBoolean disabled = new AtomicBoolean(false);
    // Version used to create config
    protected Version configVersion = null;
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
        return disabled.get();
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
            disabled.set(disable);
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

    public boolean toggle(final AtomicBoolean ab)
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
            Journeymap.getLogger().error(String.format("Can't load config file %s: %s", propFile, e.getMessage()));

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
        // Use annotations
        boolean saveNeeded = validateConfigs();

        // Only world configs should be disabled.
        if (!isWorldConfig() && isDisabled())
        {
            disabled.set(false);
            saveNeeded = true;
        }

        if (configFormatChanged.get())
        {
            saveNeeded = true;
            configFormatChanged.set(false);
            Journeymap.getLogger().info("File format will be updated for " + this.getFileName());
        }

        return saveNeeded;
    }

    /**
     * Use @Config annotations to validate value ranges
     *
     * @return
     */
    protected boolean validateConfigs()
    {
        return ConfigValidation.validateConfigs(this);
    }

    public void ensureValid()
    {
        validate();
        save();
    }
}
