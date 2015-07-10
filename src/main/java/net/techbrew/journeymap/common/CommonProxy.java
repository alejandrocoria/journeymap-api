package net.techbrew.journeymap.common;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

/**
 * Proxy prevents servers from using client-side code.
 */
public interface CommonProxy
{
    public static final String MOD_ID = "journeymap";
    public static final String SHORT_MOD_NAME = "JourneyMap";

    public void initialize(FMLInitializationEvent event) throws Throwable;

    public void postInitialize(FMLPostInitializationEvent event) throws Throwable;

}
