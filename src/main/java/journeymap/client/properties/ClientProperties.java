package journeymap.client.properties;

import com.google.common.io.Files;
import journeymap.client.Constants;
import journeymap.client.io.FileHandler;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import journeymap.common.properties.CommonProperties;

import java.io.File;
import java.io.IOException;

/**
 * Base class for Client Property Files.
 */
public abstract class ClientProperties extends CommonProperties
{
    // Headers to output before file
    private static final String[] HEADERS = {
            "// " + Constants.getString("jm.config.file_header_1"),
            "// " + Constants.getString("jm.config.file_header_2", Constants.CONFIG_DIR),
            // "// " + Constants.getString("jm.config.file_header_3", Constants.DATA_DIR + File.separator + "**" + File.separator),
            // "// " + Constants.getString("jm.config.file_header_4"),
            "// " + Constants.getString("jm.config.file_header_5", "http://journeymap.info/help/wiki/Options_Manager")
    };

    @Override
    /**
     * Gets the property file, looking first in the world config dir,
     * then falling back to look in the standard config dir.
     *
     * @return file
     */
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
     * Whethere the current source file is associated with a specific world.
     *
     * @return
     */
    @Override
    public boolean isWorldConfig()
    {
        File worldConfigDir = FileHandler.getWorldConfigDir(false);
        return (worldConfigDir != null && worldConfigDir.equals(getFile().getParentFile()));
    }

    @Override
    public String[] getHeaders()
    {
        return HEADERS;
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
