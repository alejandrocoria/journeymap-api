/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.server;

import journeymap.common.CommonProxy;
import journeymap.common.Journeymap;
import journeymap.common.network.PacketHandler;
import journeymap.common.network.WorldIDPacket;
import journeymap.server.nbt.WorldNbtIDSaveHandler;
import journeymap.server.oldservercode.chat.ChatHandler;
import journeymap.server.oldservercode.config.ConfigHandler;
import journeymap.server.oldservercode.events.ForgeEvents;
import journeymap.server.oldservercode.network.ForgePacketHandler;
import journeymap.server.oldservercode.network.PacketManager;
import journeymap.server.oldservercode.reference.Controller;
import journeymap.server.oldservercode.util.ForgeChat;
import journeymap.server.oldservercode.util.ForgePlayerUtil;
import journeymap.server.oldservercode.util.PlayerUtil;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
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
    public static String WORLD_NAME;

    private Logger logger;
    /**
     * Constructor.
     */
    public JourneymapServer()
    {
        logger = Journeymap.getLogger();
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
        Controller.setController(Controller.FORGE);
        MinecraftForge.EVENT_BUS.register(new ForgeEvents());
        //FMLCommonHandler.instance().bus().register(new FMLEvents());
        PacketManager.init(new ForgePacketHandler());
        PlayerUtil.init(new ForgePlayerUtil());
        ChatHandler.init(new ForgeChat());
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

        logger.info(side.toString());

        for (String s : modList.keySet()) {
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

    public static void setWorldName(String worldName)
    {
        WORLD_NAME = worldName;
    }

    public static String getWorldName()
    {
        return WORLD_NAME;
    }
}
