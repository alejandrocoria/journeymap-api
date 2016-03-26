package journeymap.server.properties;

import journeymap.common.properties.PropertiesBase;
import journeymap.server.oldservercode.config.ConfigHandler;

import java.io.File;

/**
 * Base class for server-side properties POJO.
 */
public abstract class ServerPropertiesBase extends PropertiesBase
{
    // Headers to output at top of file
    private static final String[] HEADERS = {
            "// JourneyMap server configuration file. Modify at your own risk!",
            "// To restore the default settings, simply delete this file before starting Minecraft",
            "// For more information, go to: http://journeymap.info/JourneyMapServer"
    };

    protected String displayName;
    protected String description;

    /**
     * Gets the filename for the instance.
     *
     * @return
     */
    @Override
    public String getFileName()
    {
        return String.format("journeymap.server.%s.config", this.getName());
    }

    /**
     * Gets the property file
     *
     * @return file
     */
    @Override
    public File getFile()
    {
        if (sourceFile == null)
        {
            sourceFile = new File(ConfigHandler.getConfigPath(), getFileName());
        }
        return sourceFile;
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
        return valid;
    }

    @Override
    public String[] getHeaders()
    {
        return HEADERS;
    }
}
