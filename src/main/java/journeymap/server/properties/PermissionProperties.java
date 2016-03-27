package journeymap.server.properties;

import journeymap.common.properties.config.BooleanField;
import journeymap.common.properties.config.StringField;

/**
 * Base class for permission-related properties.
 */
public abstract class PermissionProperties extends ServerPropertiesBase
{
    public final BooleanField opCaveMapping = new BooleanField(ServerCategory.Cave, "Enable Op cave maps", true);
    public final BooleanField playerCaveMapping = new BooleanField(ServerCategory.Cave, "Enable player cave maps", true);
    public final StringField whiteListCaveMapping = new StringField(ServerCategory.Cave, "Player whitelist").multiline(true);
    public final BooleanField opRadar = new BooleanField(ServerCategory.Radar, "Enable Op radar", true);
    public final BooleanField playerRadar = new BooleanField(ServerCategory.Radar, "Enable player radar", true);
    public final StringField whiteListRadar = new StringField(ServerCategory.Radar, "Player whitelist").multiline(true);

    /**
     * Constructor.
     *
     * @param displayName display name for client GUI and file headers
     * @param description description for client GUI and file headers
     */
    protected PermissionProperties(String displayName, String description)
    {
        super(displayName, description);
    }

}
