package journeymap.client.properties;

import com.google.common.io.Files;
import journeymap.client.Constants;
import journeymap.client.io.FileHandler;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import journeymap.common.properties.Category;
import journeymap.common.properties.PropertiesBase;
import journeymap.common.properties.config.BooleanField;

import java.io.File;
import java.io.IOException;

/**
 * Base class for Client Property Files.
 */
public abstract class ClientPropertiesBase extends PropertiesBase
{
    // Headers to output before file
    private static final String[] HEADERS = {
            "// " + Constants.getString("jm.config.file_header_1"),
            "// " + Constants.getString("jm.config.file_header_2", Constants.CONFIG_DIR),
            // "// " + Constants.getString("jm.config.file_header_3", Constants.DATA_DIR + File.separator + "**" + File.separator),
            // "// " + Constants.getString("jm.config.file_header_4"),
            "// " + Constants.getString("jm.config.file_header_5", "http://journeymap.info/Options_Manager")
    };

    // Whether it's disabled
    public BooleanField disabled = new BooleanField(ClientCategory.Hidden, "", false);

    /**
     * Gets the filename for the instance.
     *
     * @return
     */
    @Override
    public String getFileName()
    {
        return String.format("journeymap.%s.config", this.getName());
    }

    /**
     * Gets the property file, looking first in the world config dir,
     * then falling back to look in the standard config dir.
     *
     * @return file
     */
    @Override
    public File getFile()
    {
        if (sourceFile == null)
        {
            sourceFile = new File(FileHandler.getWorldConfigDir(false), getFileName());
            if (!sourceFile.canRead())
            {
                sourceFile = new File(FileHandler.StandardConfigDirectory, getFileName());
            }
        }
        return sourceFile;
    }

    /**
     * Whether the current source file is associated with a specific world.
     *
     * @return
     */
    public boolean isWorldConfig()
    {
        File worldConfigDir = FileHandler.getWorldConfigDir(false);
        return (worldConfigDir != null && worldConfigDir.equals(getFile().getParentFile()));
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
     * Validate fields, return whether they were all valid.
     *
     * @return true if all valid
     */
    protected boolean validate()
    {
        // Check fields
        boolean valid = validateFields();

        // Only world configs should be disabled.
        if (!isWorldConfig() && isDisabled())
        {
            disabled.set(false);
            valid = false;
        }

        return valid;
    }

    @Override
    public String[] getHeaders()
    {
        return HEADERS;
    }

    /**
     * Get a Category known to this Properties instance (used in a field)
     * by name.
     *
     * @param name category name
     * @return category or null
     */
    @Override
    public Category getCategoryByName(String name)
    {
        Category category = super.getCategoryByName(name);
        if (category == null)
        {
            category = ClientCategory.valueOf(name);
        }
        return category;
    }

    /**
     * Copies world config over standard config
     *
     * @return
     */
    public boolean copyToStandardConfig()
    {
        if (isWorldConfig())
        {
            try
            {
                save();
                File standardConfig = new File(FileHandler.StandardConfigDirectory, getFileName());
                Files.copy(sourceFile, standardConfig);
                return standardConfig.canRead();
            }
            catch (IOException e)
            {
                Journeymap.getLogger().error("Couldn't copy config to world config: " + LogFormatter.toString(e));
                return false;
            }
        }
        else
        {
            throw new IllegalStateException("Can't replace standard config with itself.");
        }
    }
}
