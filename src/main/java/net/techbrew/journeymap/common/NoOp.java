package net.techbrew.journeymap.common;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

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
