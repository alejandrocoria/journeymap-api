/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.task.main;

import net.minecraft.client.Minecraft;
import journeymap.client.JourneyMap;

/**
 * Created by Mark on 3/21/2015.
 */
public interface IMainThreadTask
{
    public IMainThreadTask perform(Minecraft mc, JourneyMap jm);

    public String getName();
}
