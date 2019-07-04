/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.server.properties;

import journeymap.common.properties.config.BooleanField;
import journeymap.common.properties.config.IntegerField;

import static journeymap.common.network.Constants.TRACKING_DEFUALT;
import static journeymap.common.network.Constants.TRACKING_MAX;
import static journeymap.common.network.Constants.TRACKING_MIN;

/**
 * Properties which can be applied globally (unless overridden by a specific DimensionProperties.)
 */
public class GlobalProperties extends PermissionProperties
{
    public final BooleanField useWorldId = new BooleanField(ServerCategory.General, "Use world id", false);
    public final BooleanField playerTrackingEnabled = new BooleanField(ServerCategory.General, "Enable player tracking", true);
    public final BooleanField opPlayerTrackingEnabled = new BooleanField(ServerCategory.General, "Enable player tracking by Ops and Admins", true);
    public final IntegerField playerTrackingUpdateTime = new IntegerField(ServerCategory.General, "Player tracking update time in ticks, 1-20", TRACKING_MIN, TRACKING_MAX, TRACKING_DEFUALT);

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
