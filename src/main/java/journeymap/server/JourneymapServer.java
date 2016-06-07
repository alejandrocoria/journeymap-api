/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.server;

import journeymap.common.CommonProxy;
import journeymap.common.Journeymap;
import journeymap.common.network.PacketHandler;
import journeymap.server.events.ForgeEvents;
import journeymap.server.legacyserver.config.ConfigHandler;
import journeymap.server.nbt.WorldNbtIDSaveHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Map;


/**
 * Coming soon to a codebase near you.
 */
@SideOnly(Side.SERVER)
public class JourneymapServer implements CommonProxy
{
    private Logger logger;

    /**
     * Constructor.
     */
    public JourneymapServer()
    {
        logger = Journeymap.getLogger();
    }

    /**
     * Pre-initialize the server.
     *
     * @param event
     */
    @SideOnly(Side.SERVER)
    @Override
    @Mod.EventHandler
    public void preInitialize(FMLPreInitializationEvent event)
    {
        ConfigHandler.init(new File(event.getModConfigurationDirectory() + "/JourneyMapServer/"));
    }

    /**
     * Initialize the server.
     *
     * @param event
     */
    @SideOnly(Side.SERVER)
    @Override
    public void initialize(FMLInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(new ForgeEvents());
    }

    /**
     * Post-initialize the server
     *
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

        logger.info(side.toString());

        for (String s : modList.keySet())
        {
            //logger.info("MOD Key: " + s + " MOD Value: " + modList.get(s));
        }
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

    @Override
    public void handleWorldIdMessage(String message, EntityPlayerMP playerEntity)
    {
        WorldNbtIDSaveHandler nbt = new WorldNbtIDSaveHandler();
        PacketHandler.sendPlayerWorldID(nbt.getWorldID(), playerEntity);
    }
}
