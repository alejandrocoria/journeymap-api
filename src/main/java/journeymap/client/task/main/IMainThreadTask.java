/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.task.main;

import journeymap.client.JourneymapClient;
import net.minecraft.client.Minecraft;

/**
 * Interface for a task that must be performed on the main thread.
 */
public interface IMainThreadTask
{
    public IMainThreadTask perform(Minecraft mc, JourneymapClient jm);

    public String getName();
}
