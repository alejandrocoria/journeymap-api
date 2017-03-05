/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.task.multi;

import journeymap.client.JourneymapClient;
import net.minecraft.client.Minecraft;

import java.io.File;

/**
 * The interface Task.
 */
public interface ITask
{
    /**
     * Gets max runtime.
     *
     * @return the max runtime
     */
    public int getMaxRuntime();

    /**
     * Perform task.
     *
     * @param mc            the mc
     * @param jm            the jm
     * @param jmWorldDir    the jm world dir
     * @param threadLogging the thread logging
     * @throws InterruptedException the interrupted exception
     */
    public void performTask(Minecraft mc, JourneymapClient jm, File jmWorldDir, boolean threadLogging) throws InterruptedException;
}
