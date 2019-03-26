/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.server.properties;

import journeymap.common.properties.config.BooleanField;
import journeymap.common.properties.config.IntegerField;

/**
 * Properties which can be applied globally (unless overridden by a specific DimensionProperties.)
 */
public class GlobalProperties extends PermissionProperties
{
    public final BooleanField teleportEnabled = new BooleanField(ServerCategory.General, "Enable Players to teleport", false);
    public final BooleanField useWorldId = new BooleanField(ServerCategory.General, "Use world id", false);
    public final BooleanField playerTrackingEnabled = new BooleanField(ServerCategory.General, "Enable player tracking", true);
    public final BooleanField opPlayerTrackingEnabled = new BooleanField(ServerCategory.General, "Enable player tracking by Ops and Admins", true);
    public final IntegerField playerTrackingUpdateTime = new IntegerField(ServerCategory.General, "Player tracking update time in milliseconds", 100, 60000, 1000);

    /**
     * Constructor.
     */
    public GlobalProperties()
    {
        super("Global Server Configuration", "Applies to all dimensions unless overridden.");
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
