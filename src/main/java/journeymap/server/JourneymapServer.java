/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.server;

import journeymap.common.CommonProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

/**
 * Does nothing.
 */
public class JourneymapServer implements CommonProxy
{
    public JourneymapServer()
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
