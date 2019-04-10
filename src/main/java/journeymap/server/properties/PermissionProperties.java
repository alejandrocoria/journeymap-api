/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.server.properties;

import journeymap.common.properties.config.BooleanField;

/**
 * Base class for permission-related properties.
 */
public abstract class PermissionProperties extends ServerPropertiesBase
{
    /**
     * Teleport enabled to, from, inside dimension.
     */
    public final BooleanField teleportEnabled = new BooleanField(ServerCategory.General, "Enable Players to teleport", false);
    /**
     * Op surface mapping enabled.
     */
    public final BooleanField opSurfaceMappingEnabled = new BooleanField(ServerCategory.Surface, "Enable Op surface maps", true);
    /**
     * Cave mapping enabled.
     */
    public final BooleanField surfaceMappingEnabled = new BooleanField(ServerCategory.Surface, "Enable surface maps", true);
    /**
     * Op topo mapping enabled.
     */
    public final BooleanField opTopoMappingEnabled = new BooleanField(ServerCategory.Topo, "Enable Op topo maps", true);
    /**
     * Cave mapping enabled.
     */
    public final BooleanField topoMappingEnabled = new BooleanField(ServerCategory.Topo, "Enable topo maps", true);
    /**
     * Op cave mapping enabled.
     */
    public final BooleanField opCaveMappingEnabled = new BooleanField(ServerCategory.Cave, "Enable Op cave maps", true);
    /**
     * Cave mapping enabled.
     */
    public final BooleanField caveMappingEnabled = new BooleanField(ServerCategory.Cave, "Enable cave maps", true);
    /**
     * Op radar enabled.
     */
    public final BooleanField opRadarEnabled = new BooleanField(ServerCategory.Radar, "Enable Op radar", true);
    /**
     * Radar enabled.
     */
    public final BooleanField radarEnabled = new BooleanField(ServerCategory.Radar, "Enable radar", true);
    /**
     * Player radar enabled.
     */
    public final BooleanField playerRadarEnabled = new BooleanField(ServerCategory.Radar, "Enable player radar", true);
    /**
     * Villager radar enabled.
     */
    public final BooleanField villagerRadarEnabled = new BooleanField(ServerCategory.Radar, "Enable villager radar", true);
    /**
     * Animal radar enabled.
     */
    public final BooleanField animalRadarEnabled = new BooleanField(ServerCategory.Radar, "Enable animal radar", true);
    /**
     * Mob radar enabled.
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
