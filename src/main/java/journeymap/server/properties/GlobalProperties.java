/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.server.properties;

import journeymap.common.properties.config.BooleanField;

/**
 * Properties which can be applied globally (unless overridden by a specific DimensionProperties.)
 */
public class GlobalProperties extends PermissionProperties
{
    /**
     * The Teleport enabled.
     */
    public final BooleanField teleportEnabled = new BooleanField(ServerCategory.General, "Enable Players to teleport", false);
    /**
     * The Use world id.
     */
    public final BooleanField useWorldId = new BooleanField(ServerCategory.General, "Use world id", false);

    /**
     * Constructor.
     */
    public GlobalProperties()
    {
        super("Global Server Configuration", "Applies to all dimensions unless overridden. 'WorldID is Read Only'");
    }


    public String getName()
    {
        return "global";
    }

    @Override
    protected void postLoad(boolean isNew)
    {
        super.postLoad(isNew);
    }

    @Override
    protected void preSave()
    {
        super.preSave();
    }
}
