/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.task.main;

import journeymap.client.JourneymapClient;
import net.minecraft.client.Minecraft;

/**
 * @author techbrew 3/21/2016.
 */
public interface IMainThreadTask
{
    /**
     * Perform main thread task.
     *
     * @param mc the mc
     * @param jm the jm
     * @return the main thread task
     */
    public IMainThreadTask perform(Minecraft mc, JourneymapClient jm);

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName();
}
