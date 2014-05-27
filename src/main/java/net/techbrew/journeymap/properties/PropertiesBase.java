package net.techbrew.journeymap.properties;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.io.FileHandler;
import net.techbrew.journeymap.log.LogFormatter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Base GSON-backed properties class.
 */
public abstract class PropertiesBase
{
    // Gson for file persistence
    protected transient final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // Toggles whether save() actually does anything.
    protected transient final AtomicBoolean saveEnabled = new AtomicBoolean(true);

    private static final String[] HEADERS = {
            "// JourneyMap configuration file. Modify at your own risk!",
            "// To restore the default settings, simply delete this file before starting Minecraft",
            "// For help with this file, see http://journeymap.techbrew.net/help/wiki/Configuration_Files"
    };

    /**
     * Name used in property file
     *
     * @return name
     */
    protected abstract String getName();

    /**
     * Code base revision of props class
     *
     * @return rev
     */
    public abstract int getCurrentRevision();

    /**
     * Revision of properties loaded from file
     *
     * @return rev
     */
    public abstract int getRevision();

    /**
     * Gets the property file path.
     *
     * @return file
     */
    public File getFile()
    {
        return new File(FileHandler.getConfigDir(), String.format("journeymap.%s.config", this.getName()));
    }

    /**
     * Whether the code base revision of the properties
     * matches that loaded from the file.
     *
     * @return true if current
     */
    public boolean isCurrent()
    {
        return getCurrentRevision() == getRevision();
    }

    /**
     * Saves the property object to file
     *
     * @return true if saved
     */
    public boolean save()
    {
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
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String json = gson.toJson(this);

                // Write to file
                FileWriter fw = new FileWriter(propFile);
                fw.write(header);
                fw.write(json);
                fw.flush();
                fw.close();

                return true;
            }
            catch (Exception e)
            {
                JourneyMap.getLogger().severe(String.format("Can't save config file %s: %s", propFile, LogFormatter.toString(e)));
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
        FileReader reader = null;
        boolean saveNeeded = true;
        try
        {
            if (propFile.canRead())
            {
                reader = new FileReader(propFile);
                instance = gson.fromJson(reader, (Class<T>) getClass());
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
            JourneyMap.getLogger().severe(String.format("Can't load config file %s: %s", propFile, e.getMessage()));
        }
        finally
        {
            if (reader != null)
            {
                try
                {
                    reader.close();
                }
                catch (Exception e)
                {
                    JourneyMap.getLogger().severe(String.format("Can't close config file %s: %s", propFile, e.getMessage()));
                }
            }
        }

        if (saveNeeded)
        {
            instance.save();
        }

        return instance;
    }

    public <T extends PropertiesBase> T enableSave(boolean enabled)
    {
        synchronized (saveEnabled)
        {
            saveEnabled.set(enabled);
            return (T) this;
        }
    }
}
