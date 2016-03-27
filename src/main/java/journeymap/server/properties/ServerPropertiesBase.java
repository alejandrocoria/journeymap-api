package journeymap.server.properties;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import journeymap.common.properties.PropertiesBase;
import journeymap.server.oldservercode.config.ConfigHandler;

import java.io.File;
import java.util.List;

/**
 * Base class for server-side properties POJO.
 */
public abstract class ServerPropertiesBase extends PropertiesBase
{
    protected final String displayName;
    protected final String description;

    /**
     * Constructor.
     *
     * @param displayName display name for client GUI and file headers
     * @param description description for client GUI and file headers
     */
    protected ServerPropertiesBase(String displayName, String description)
    {
        this.displayName = displayName;
        this.description = description;
    }

    @Override
    public String[] getHeaders()
    {
        return new String[]{
                "// JourneyMap server configuration file. Modify at your own risk!",
                "// To restore the default settings, simply delete this file before starting Minecraft server",
                "// For more information, go to: http://journeymap.info/JourneyMapServer",
                "//",
                String.format("// %s : %s ", displayName, description)
        };
    }

    /**
     * Copies values from another instance into this one.
     * Override this to include non-ConfigField members if necessary.
     *
     * @param otherInstance other
     * @param <T>           properties type
     */
    @Override
    protected <T extends PropertiesBase> void updateFrom(T otherInstance)
    {
        super.updateFrom(otherInstance);
    }

    /**
     * Override this to provide a customized way to exclude fields from serialization.
     *
     * @param verbose true for verbose serialization
     * @return list of all strategies
     */
    public List<ExclusionStrategy> getExclusionStrategies(boolean verbose)
    {
        List<ExclusionStrategy> strategies = super.getExclusionStrategies(verbose);

        if (!verbose)
        {
            // Don't serialize displayName and description when not verbose.
            // Those will already be in the file headers.
            strategies.add(new ExclusionStrategy()
            {
                @Override
                public boolean shouldSkipField(FieldAttributes f)
                {
                    if (f.getDeclaringClass().equals(ServerPropertiesBase.class))
                    {
                        return f.getName().equals("displayName") || f.getName().equals("description");
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
     * Whether current state is valid.
     * Override this in a subclass to include non-ConfigField members if necessary.
     *
     * @param fix true to try to fix validation problems, false to just report them
     * @return true if valid
     */
    @Override
    protected boolean isValid(boolean fix)
    {
        boolean valid = super.isValid(fix);
        // new checks go here
        return valid;
    }

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
}
