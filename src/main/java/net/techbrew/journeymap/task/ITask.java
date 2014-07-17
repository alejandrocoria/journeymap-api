package net.techbrew.journeymap.task;

import net.minecraft.client.Minecraft;
import net.techbrew.journeymap.JourneyMap;

import java.io.File;

public interface ITask
{
    public int getMaxRuntime();
    public void performTask(Minecraft mc, JourneyMap jm, File jmWorldDir, boolean threadLogging) throws InterruptedException;
}
