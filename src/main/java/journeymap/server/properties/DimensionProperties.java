/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.server.properties;

import journeymap.common.properties.config.BooleanField;

/**
 * Permissions which can be applied to a specific dimension.
 */
public class DimensionProperties extends PermissionProperties
{
    /**
     * The Enabled.
     */
// Whether or not these properties should override GlobalProperties
    public final BooleanField enabled = new BooleanField(ServerCategory.General, "Enable Configuration", false).categoryMaster(true);
    /**
     * The Dimension.
     */
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

    /**
     * Gets dimension.
     *
     * @return the dimension
     */
    public Integer getDimension()
    {
        return dimension;
    }

    /**
     * Build dimension properties.
     *
     * @return the dimension properties
     */
    public DimensionProperties build()
    {
        GlobalProperties gProp = PropertiesManager.getInstance().getGlobalProperties();
        this.opCaveMappingEnabled.set(gProp.opCaveMappingEnabled.get());
        this.caveMappingEnabled.set(gProp.caveMappingEnabled.get());
        this.opRadarEnabled.set(gProp.opRadarEnabled.get());
        this.radarEnabled.set(gProp.radarEnabled.get());
        this.playerRadarEnabled.set(gProp.playerRadarEnabled.get());
        this.villagerRadarEnabled.set(gProp.villagerRadarEnabled.get());
        this.animalRadarEnabled.set(gProp.animalRadarEnabled.get());
        this.mobRadarEnabled.set(gProp.mobRadarEnabled.get());
        this.save();
        return this;
    }
}
