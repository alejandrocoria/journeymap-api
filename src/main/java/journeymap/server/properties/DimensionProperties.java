package journeymap.server.properties;

import journeymap.common.properties.config.BooleanField;
import journeymap.common.properties.config.StringField;

/**
 * Port of oldservercode.config.Configuration
 */
public class DimensionProperties extends ServerPropertiesBase
{
    protected final Integer dimension;

    protected BooleanField enabled = new BooleanField(ServerCategory.General, "Enable Configuration", false).categoryMaster(true);
    protected BooleanField opCaveMapping = new BooleanField(ServerCategory.Cave, "Enable Op cave maps", true);
    protected BooleanField playerCaveMapping = new BooleanField(ServerCategory.Cave, "Enable player cave maps", true);
    protected StringField whiteListCaveMapping = new StringField(ServerCategory.Cave, "Player whitelist").multiline(true);
    protected BooleanField opRadar = new BooleanField(ServerCategory.Radar, "Enable Op radar", true);
    protected BooleanField playerRadar = new BooleanField(ServerCategory.Radar, "Enable player radar", true);
    protected StringField whiteListRadar = new StringField(ServerCategory.Radar, "Player whitelist").multiline(true);

    /**
     * Constructor.
     *
     * @param dimension
     */
    public DimensionProperties(Integer dimension)
    {
        this(dimension, String.format("Dimension %s Configuration", dimension),
                "Overrides the Global Server Configuration for this dimension");
    }

    public DimensionProperties(Integer dimension, String displayName, String description)
    {
        this.dimension = dimension;
        this.displayName = displayName;
        this.description = description;
    }

    @Override
    public String getName()
    {
        return "dim" + dimension;
    }
}
