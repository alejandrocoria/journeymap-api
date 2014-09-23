/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.properties;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.techbrew.journeymap.Constants;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.io.FileHandler;
import net.techbrew.journeymap.log.LogFormatter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Base GSON-backed properties class.
 */
public abstract class PropertiesBase
{
    protected static final String ADVANCED = "jm.config.category.advanced";
    protected static final String MAPSTYLE = "jm.config.category.mapstyle";

    protected static final Charset UTF8 = Charset.forName("UTF-8");
    private static final String[] HEADERS = {
            "// JourneyMap configuration file. Modify at your own risk!",
            "// To use in all worlds, place here: " + Constants.CONFIG_DIR,
            "// To override configuration for a single world, place here: " + Constants.DATA_DIR + "**" + File.separator + "(worldname)",
            "// To restore the default settings, simply delete this file before starting Minecraft",
            "// For help with this file, see http://journeymap.techbrew.net/help/wiki/Configuration_Files"
    };

    // Gson for file persistence
    protected transient final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // Current file reference
    protected transient File sourceFile = null;

    // Toggles whether save() actually does anything.
    protected transient final AtomicBoolean saveEnabled = new AtomicBoolean(true);

    // Set by subclass
    protected int fileRevision;

    // Whether it's disabled
    protected final AtomicBoolean disabled = new AtomicBoolean(false);

    /**
     * Default constructor.
     */
    protected PropertiesBase()
    {
    }

    /**
     * Name used in property file
     *
     * @return name
     */
    public abstract String getName();

    /**
     * Code base fileRevision of props class
     *
     * @return rev
     */
    public abstract int getCodeRevision();

    /**
     * Gets the property file, looking first in the world config dir,
     * then falling back to look in the standard config dir.
     *
     * @return file
     */
    public File getFile()
    {
        if(sourceFile==null)
        {
            sourceFile = new File(FileHandler.getWorldConfigDir(false), getFileName());
            if(!sourceFile.canRead())
            {
                sourceFile = new File(FileHandler.getStandardConfigDir(), getFileName());
            }
        }
        return sourceFile;
    }

    /**
     * Gets the filename for the instance.
     * @return
     */
    protected String getFileName()
    {
        return String.format("journeymap.%s.config", this.getName());
    }

    /**
     * Whethere the current source file is associated with a specific world.
     * @return
     */
    public boolean isWorldConfig()
    {
        File worldConfigDir = FileHandler.getWorldConfigDir(false);
        return (worldConfigDir!=null && worldConfigDir.equals(getFile().getParentFile()));
    }

    /**
     * Whether this config is disabled and shouldn't be used.
     * @return
     */
    public boolean isDisabled()
    {
        return disabled.get();
    }

    /**
     * Set disabled - only works for world configs.
     * Saves after the set.
     * @param disable
     */
    public void setDisabled(boolean disable)
    {
        if(isWorldConfig())
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
     * @param overwrite true if current world config should be overwritten
     * @return true if copy succeeded
     */
    public boolean copyToWorldConfig(boolean overwrite)
    {
        if(!isWorldConfig())
        {
            try
            {
                File worldConfig = getFile();
                if(overwrite || !worldConfig.exists())
                {
                    save();
                    Files.copy(sourceFile, worldConfig);
                    return worldConfig.canRead();
                }
            }
            catch (IOException e)
            {
                JourneyMap.getLogger().error("Couldn't copy config to world config: " + LogFormatter.toString(e));
            }
            return false;
        }
        else
        {
            throw new IllegalStateException("Can't create World config from itself.");
        }
    }

    /**
     * Copies world config over standard config
     * @return
     */
    public boolean copyToStandardConfig()
    {
        if(isWorldConfig())
        {
            try
            {
                save();
                File standardConfig = new File(FileHandler.getStandardConfigDir(), getFileName());
                Files.copy(sourceFile, standardConfig);
                return standardConfig.canRead();
            }
            catch (IOException e)
            {
                JourneyMap.getLogger().error("Couldn't copy config to world config: " + LogFormatter.toString(e));
                return false;
            }
        }
        else
        {
            throw new IllegalStateException("Can't replace standard config with itself.");
        }
    }

    /**
     * Whether the code base fileRevision of the properties
     * matches that loaded from the file.
     *
     * @return true if current
     */
    public boolean isCurrent()
    {
        return getCodeRevision() == fileRevision;
    }

    /**
     * Saves the property object to file
     *
     * @return true if saved
     */
    public boolean save()
    {
        fileRevision = getCodeRevision();

        synchronized (saveEnabled)
        {
            if (!saveEnabled.get())
            {
                return false;
            }

            File propFile = null;
            try
            {
                // Write to file
                propFile = getFile();

                if (!propFile.exists())
                {
                    JourneyMap.getLogger().info(String.format("Creating config file: %s", propFile));
                    if (!propFile.getParentFile().exists())
                    {
                        propFile.getParentFile().mkdirs();
                    }
                }

                // Header
                String lineEnding = System.getProperty("line.separator");
                StringBuilder sb = new StringBuilder();
                for (String line : HEADERS)
                {
                    sb.append(line).append(lineEnding);
                }
                String header = sb.toString();

                // Json body
                String json = gson.toJson(this);

                // Write to file
                Files.write(header + json, propFile, UTF8);

                return true;
            }
            catch (Exception e)
            {
                JourneyMap.getLogger().error(String.format("Can't save config file %s: %s", propFile, LogFormatter.toString(e)));
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
    public <T extends PropertiesBase> T load()
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
                    JourneyMap.getLogger().info(String.format("Config file needs to be updated: %s", propFile.getName()));
                }
            }
            else
            {
                JourneyMap.getLogger().info(String.format("Config file not found: %s", propFile));
            }
        }
        catch (Exception e)
        {
            JourneyMap.getLogger().error(String.format("Can't load config file %s: %s", propFile, e.getMessage()));

            try
            {
                File badPropFile = new File(propFile.getParentFile(), propFile.getName() + ".bad");
                propFile.renameTo(badPropFile);
            }
            catch(Exception e3)
            {
                JourneyMap.getLogger().error(String.format("Can't rename config file %s: %s", propFile, e3.getMessage()));
            }

        }

        if(instance==null)
        {
            try
            {
                instance = (T) getClass().newInstance();
                saveNeeded = true;
            }
            catch (Exception e)
            {
                // This isn't really the reason for this exception, just the root cause of the trouble.
                throw new RuntimeException("Config file corrupted.  Please fix or remove: " + propFile);
            }
        }

        if (instance!=null && (instance.validate() || saveNeeded))
        {
            instance.save();
        }

        return instance;
    }

    /**
     * Should return true if save needed after validation.
     *
     * @return
     */
    protected boolean validate()
    {
        // Only world configs should be disabled.
        if(!isWorldConfig() && isDisabled())
        {
            disabled.set(false);
            return true;
        }
        return false;
    }

    public <T extends PropertiesBase> T enableSave(boolean enabled)
    {
        synchronized (saveEnabled)
        {
            saveEnabled.set(enabled);
            return (T) this;
        }
    }

    public static <T extends PropertiesBase> T reload(T properties, Class<T> propertiesClass)
    {
        if(properties!=null)
        {
            properties.save();
        }

        T reloadedProperties = null;
        try
        {
            reloadedProperties = propertiesClass.newInstance().load();
            boolean sourceChanged = (properties==null) || properties.isWorldConfig() != reloadedProperties.isWorldConfig();
            if(sourceChanged)
            {
                JourneyMap.getLogger().info("Loaded " + propertiesClass.getSimpleName() + " from " + reloadedProperties.getFile());
            }
            return reloadedProperties;
        }
        catch (Throwable t)
        {
            JourneyMap.getLogger().error("Failed to reload " + propertiesClass.getName() + ": " + LogFormatter.toString(t));
            return (properties!=null) ? properties : reloadedProperties;
        }
    }
}
