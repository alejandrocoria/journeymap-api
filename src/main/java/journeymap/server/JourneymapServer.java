/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.server;

import journeymap.common.CommonProxy;
import journeymap.common.network.PacketHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Coming soon to a codebase near you.
 */
@SideOnly(Side.SERVER)
public class JourneymapServer implements CommonProxy
{
    /**
     * Constructor.
     */
    public JourneymapServer()
    {
    }

    /**
     * Initialize the server.
     * @param event
     */
    @SideOnly(Side.SERVER)
    @Override
    public void initialize(FMLInitializationEvent event)
    {
//        PacketHandler packetHandler = new PacketHandler();
//        packetHandler.init(Side.SERVER);
    }

    /**
     * Post-initialize the server
     * @param event
     */
    @SideOnly(Side.SERVER)
    @Override
    public void postInitialize(FMLPostInitializationEvent event)
    {
    }
}
