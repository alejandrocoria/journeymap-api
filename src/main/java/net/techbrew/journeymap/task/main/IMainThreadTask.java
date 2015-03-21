package net.techbrew.journeymap.task.main;

import net.minecraft.client.Minecraft;
import net.techbrew.journeymap.JourneyMap;

/**
 * Created by Mark on 3/21/2015.
 */
public interface IMainThreadTask
{
    public IMainThreadTask perform(Minecraft mc, JourneyMap jm);

    public String getName();
}
