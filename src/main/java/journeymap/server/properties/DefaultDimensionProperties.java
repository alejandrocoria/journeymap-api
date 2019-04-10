package journeymap.server.properties;

import journeymap.common.properties.config.BooleanField;

public class DefaultDimensionProperties extends PermissionProperties
{
    public final BooleanField enabled = new BooleanField(ServerCategory.General, "Enable Configuration", false).categoryMaster(true);
    protected DefaultDimensionProperties()
    {
        super("default", "New Dimension properties will be based on this file.");
    }

    @Override
    public String getName()
    {
        return "default";
    }
}
