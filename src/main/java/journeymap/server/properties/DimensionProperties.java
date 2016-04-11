package journeymap.server.properties;

import journeymap.common.properties.config.BooleanField;

/**
 * Permissions which can be applied to a specific dimension.
 */
public class DimensionProperties extends PermissionProperties
{
    // Whether or not these properties should override GlobalProperties
    public final BooleanField enabled = new BooleanField(ServerCategory.General, "Enable Configuration", false).categoryMaster(true);
    protected final Integer dimension;

    /**
     * Constructor.
     *
     * @param dimension the dimension id this applies to
     */
    public DimensionProperties(Integer dimension)
    {
        super(String.format("Dimension %s Configuration", dimension),
                "Overrides the Global Server Configuration for this dimension");
        this.dimension = dimension;
    }

    @Override
    public String getName()
    {
        return "dim" + dimension;
    }
}
