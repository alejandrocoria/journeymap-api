package net.techbrew.journeymap.properties;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.io.FileHandler;
import net.techbrew.journeymap.log.LogFormatter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * Base GSON-backed properties class.
 */
public abstract class PropertiesBase
{
    // Gson for file persistence
    protected transient final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    protected transient boolean saveEnabled = true;

    /**
     * Name used in property file
     * @return name
     */
    protected abstract String getName();

    /**
     * Code base revision of props class
     * @return rev
     */
    public abstract int getCurrentRevision();

    /**
     * Revision of properties loaded from file
     * @return rev
     */
    public abstract int getRevision();

    /**
     * Gets the property file path.
     * @return file
     */
    public File getFile()
    {
        return new File(FileHandler.getJourneyMapDir(), String.format("journeymap.%s.properties", this.getName()));
    }

    /**
     * Whether the code base revision of the properties
     * matches that loaded from the file.
     * @return true if current
     */
    public boolean isCurrent()
    {
        return getCurrentRevision()==getRevision();
    }

    /**
     * Saves the property object to file
     * @return true if saved
     */
    public synchronized boolean save()
    {
        if(!saveEnabled) return false;

        File propFile = null;
        try
        {
            // Write to file
            propFile = getFile();
            FileWriter fw = new FileWriter(propFile);
            fw.write(gson.toJson(this));
            fw.flush();
            fw.close();
            return true;
        }
        catch (Exception e)
        {
            JourneyMap.getLogger().severe(String.format("Can't save properties file %s: %s", propFile, LogFormatter.toString(e)));
            return false;
        }
    }

    /**
     * Returns an instance with values loaded
     * from file, or itself if the load failed.
     * @param <T> properties default instance
     * @return  loaded instance
     */
    public <T extends PropertiesBase> T load()
    {
        T instance = (T) this;
        File propFile = getFile();
        FileReader reader = null;
        boolean saveNeeded = true;
        try
        {
            if(propFile.canRead())
            {
                reader = new FileReader(propFile);
                instance = gson.fromJson(reader, (Class<T>) getClass());
                saveNeeded = !instance.isCurrent();
                if(saveNeeded)
                {
                    JourneyMap.getLogger().info(String.format("Properties file needs to be updated: %s", propFile.getName()));
                }
            }
            else
            {
                JourneyMap.getLogger().info(String.format("Properties file not found: %s", propFile));
            }
        }
        catch (Exception e)
        {
            JourneyMap.getLogger().severe(String.format("Can't load properties file %s: %s", propFile, e.getMessage()));
        }
        finally
        {
            if(reader!=null)
            {
                try
                {
                    reader.close();
                }
                catch (Exception e)
                {
                    JourneyMap.getLogger().severe(String.format("Can't close properties file %s: %s", propFile, e.getMessage()));
                }
            }
        }

        if(saveNeeded)
        {
            instance.save();
        }

        return instance;
    }

    public <T extends PropertiesBase> T enableSave(boolean enabled)
    {
        saveEnabled = enabled;
        return (T) this;
    }

    /**
     * Generates Json string of properties.
     * @return String, or null on error.
     */
    public String toJsonString()
    {
        try
        {
            return gson.toJson(this);
        }
        catch (Exception e)
        {
            JourneyMap.getLogger().severe(String.format("Can't generate Json string of properties %s", getName()));
            return null;
        }
    }
}
