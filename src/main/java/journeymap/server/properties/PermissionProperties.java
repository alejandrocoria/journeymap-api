package journeymap.server.properties;

import journeymap.common.properties.config.BooleanField;

/**
 * Base class for permission-related properties.
 */
public abstract class PermissionProperties extends ServerPropertiesBase
{
    public final BooleanField opCaveMappingEnabled = new BooleanField(ServerCategory.Cave, "Enable Op cave maps", true);
    public final BooleanField caveMappingEnabled = new BooleanField(ServerCategory.Cave, "Enable cave maps", true);
    public final BooleanField opRadarEnabled = new BooleanField(ServerCategory.Radar, "Enable Op radar", true);
    public final BooleanField radarEnabled = new BooleanField(ServerCategory.Radar, "Enable radar", true);
    public final BooleanField playerRadarEnabled = new BooleanField(ServerCategory.Radar, "Enable player radar", true);
    public final BooleanField villagerRadarEnabled = new BooleanField(ServerCategory.Radar, "Enable villager radar", true);
    public final BooleanField animalRadarEnabled = new BooleanField(ServerCategory.Radar, "Enable animal radar", true);
    public final BooleanField mobRadarEnabled = new BooleanField(ServerCategory.Radar, "Enable mob radar", true);
    public final BooleanField creeperRadarEnabled = new BooleanField(ServerCategory.Radar, "Enable creeper radar", true);


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
