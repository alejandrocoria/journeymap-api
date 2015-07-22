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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Map;

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

    /**
     * Accept any modlist on client
     *
     * @param modList
     * @param side
     * @return
     */
    @Override
    public boolean checkModLists(Map<String, String> modList, Side side)
    {
        // TODO: Check for JM client and enable/disable worldid checking, etc.
        return true;
    }

    /**
     * Whether the update check is enabled.
     *
     * @return
     */
    @Override
    public boolean isUpdateCheckEnabled()
    {
        // TODO: Make this configurable
        return false;
    }
}
