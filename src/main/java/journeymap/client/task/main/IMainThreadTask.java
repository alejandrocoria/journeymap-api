/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.client.task.main;

import journeymap.client.JourneymapClient;
import net.minecraft.client.Minecraft;

/**
 * Created by Mark on 3/21/2015.
 */
public interface IMainThreadTask
{
    public IMainThreadTask perform(Minecraft mc, JourneymapClient jm);

    public String getName();
}
