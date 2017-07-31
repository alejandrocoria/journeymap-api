/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.server.properties;

import journeymap.common.properties.config.BooleanField;

/**
 * Base class for permission-related properties.
 */
public abstract class PermissionProperties extends ServerPropertiesBase
{
    /**
     * The Op cave mapping enabled.
     */
    public final BooleanField opCaveMappingEnabled = new BooleanField(ServerCategory.Cave, "Enable Op cave maps", true);
    /**
     * The Cave mapping enabled.
     */
    public final BooleanField caveMappingEnabled = new BooleanField(ServerCategory.Cave, "Enable cave maps", true);
    /**
     * The Op radar enabled.
     */
    public final BooleanField opRadarEnabled = new BooleanField(ServerCategory.Radar, "Enable Op radar", true);
    /**
     * The Radar enabled.
     */
    public final BooleanField radarEnabled = new BooleanField(ServerCategory.Radar, "Enable radar", true);
    /**
     * The Player radar enabled.
     */
    public final BooleanField playerRadarEnabled = new BooleanField(ServerCategory.Radar, "Enable player radar", true);
    /**
     * The Villager radar enabled.
     */
    public final BooleanField villagerRadarEnabled = new BooleanField(ServerCategory.Radar, "Enable villager radar", true);
    /**
     * The Animal radar enabled.
     */
    public final BooleanField animalRadarEnabled = new BooleanField(ServerCategory.Radar, "Enable animal radar", true);
    /**
     * The Mob radar enabled.
     */
    public final BooleanField mobRadarEnabled = new BooleanField(ServerCategory.Radar, "Enable mob radar", true);

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