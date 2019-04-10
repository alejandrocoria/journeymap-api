/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

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
                "Overrides the Global Server Configuration for this dimension - sent enable true to override global settings for this dim");
        this.dimension = dimension;
    }

    @Override
    public String getName()
    {
        return "dim" + dimension;
    }

    public Integer getDimension()
    {
        return dimension;
    }

    public DimensionProperties build()
    {
        DefaultDimensionProperties defaultProp = PropertiesManager.getInstance().getDefaultDimensionProperties();
        this.teleportEnabled.set(defaultProp.teleportEnabled.get());
        this.enabled.set(defaultProp.enabled.get());
        this.opCaveMappingEnabled.set(defaultProp.opCaveMappingEnabled.get());
        this.caveMappingEnabled.set(defaultProp.caveMappingEnabled.get());
        this.opSurfaceMappingEnabled.set(defaultProp.opSurfaceMappingEnabled.get());
        this.surfaceMappingEnabled.set(defaultProp.surfaceMappingEnabled.get());
        this.opTopoMappingEnabled.set(defaultProp.opTopoMappingEnabled.get());
        this.topoMappingEnabled.set(defaultProp.topoMappingEnabled.get());
        this.opRadarEnabled.set(defaultProp.opRadarEnabled.get());
        this.radarEnabled.set(defaultProp.radarEnabled.get());
        this.playerRadarEnabled.set(defaultProp.playerRadarEnabled.get());
        this.villagerRadarEnabled.set(defaultProp.villagerRadarEnabled.get());
        this.animalRadarEnabled.set(defaultProp.animalRadarEnabled.get());
        this.mobRadarEnabled.set(defaultProp.mobRadarEnabled.get());
        this.save();
        return this;
    }
}
