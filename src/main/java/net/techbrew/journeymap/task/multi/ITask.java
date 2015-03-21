/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.task.multi;

import net.minecraft.client.Minecraft;
import net.techbrew.journeymap.JourneyMap;

import java.io.File;

public interface ITask
{
    public int getMaxRuntime();

    public void performTask(Minecraft mc, JourneyMap jm, File jmWorldDir, boolean threadLogging) throws InterruptedException;
}
