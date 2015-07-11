/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.common;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

/**
 * Proxy to provide a common interface for initializing client-side or server-side.
 */
public interface CommonProxy
{
    /**
     * Initialize the side.
     * @param event
     * @throws Throwable
     */
    public void initialize(FMLInitializationEvent event) throws Throwable;

    /**
     * Post-initialize the side.
     * @param event
     * @throws Throwable
     */
    public void postInitialize(FMLPostInitializationEvent event) throws Throwable;

}
