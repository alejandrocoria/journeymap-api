package net.techbrew.journeymap.common;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;

/**
 * Does nothing.
 */
public class NoOp implements CommonProxy
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
