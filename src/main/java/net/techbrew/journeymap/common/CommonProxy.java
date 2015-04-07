package net.techbrew.journeymap.common;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;

/**
 * Proxy prevents servers from using client-side code.
 */
public interface CommonProxy
{
    public static final String MOD_ID = "journeymap";
    public static final String SHORT_MOD_NAME = "JourneyMap";

    public void initialize(FMLInitializationEvent event) throws Throwable;

    public void postInitialize(FMLPostInitializationEvent event) throws Throwable;

    public static class NoOp implements CommonProxy
    {
        public NoOp()
        {
        }

        @Override
        public void initialize(FMLInitializationEvent event)
        {
        }

        @Override
        public void postInitialize(FMLPostInitializationEvent event)
        {
        }
    }
}
