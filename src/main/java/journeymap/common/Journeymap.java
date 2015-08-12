/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.common;

import journeymap.common.version.Version;
import journeymap.server.JourneymapServer;
import journeymap.server.oldservercode.command.CommandJMServerForge;
import journeymap.server.oldservercode.config.ConfigHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Map;

/**
 * Forge Mod entry point
 */
@Mod(modid = Journeymap.MOD_ID, name = Journeymap.SHORT_MOD_NAME, version = "@JMVERSION@", canBeDeactivated = true, dependencies = "Forge@[${@FORGEVERSION@},)")
public class Journeymap
{
    public static final String MOD_ID = "journeymap";
    public static final String SHORT_MOD_NAME = "JourneyMap";
    public static final Version JM_VERSION = Version.from("@MAJOR@", "@MINOR@", "@MICRO@", "@PATCH@", new Version(5, 1, 1, "dev"));
    public static final String FORGE_VERSION = "@FORGEVERSION@";
    public static final String WEBSITE_URL = "http://journeymap.info/";
    public static final String DOWNLOAD_URL = WEBSITE_URL + "download";
    public static final String VERSION_URL = "https://dl.dropboxusercontent.com/u/38077766/JourneyMap/journeymap-versions.json";

    @Mod.Instance(Journeymap.MOD_ID)
    public static Journeymap instance;

    @SidedProxy(clientSide = "journeymap.client.JourneymapClient", serverSide = "journeymap.server.JourneymapServer")
    public static CommonProxy proxy;

    /**
     * Get the common logger.
     */
    public static Logger getLogger()
    {
        return LogManager.getLogger(MOD_ID);
    }

    /**
     * Whether this side will accept being connected to the other side.
     */
    @NetworkCheckHandler
    public boolean checkModLists(Map<String, String> modList, Side side)
    {
        if(proxy==null)
        {
            return true;
        }
        else
        {
            return proxy.checkModLists(modList, side);
        }
    }

    /**
     * Initialize the sided proxy.
     * @param event
     * @throws Throwable
     */
    @Mod.EventHandler
    public void initialize(FMLInitializationEvent event) throws Throwable
    {
        proxy.initialize(event);
    }

    /**
     * Post-initialize the sided proxy.
     * @param event
     * @throws Throwable
     */
    @Mod.EventHandler
    public void postInitialize(FMLPostInitializationEvent event) throws Throwable
    {
        proxy.postInitialize(event);
    }

    @SideOnly(Side.SERVER)
    @Mod.EventHandler
    public void preInitEvent(FMLPreInitializationEvent event)
    {
        ConfigHandler.init(new File(event.getModConfigurationDirectory() + "/JourneyMapServer/"));
    }

    @SideOnly(Side.SERVER)
    @Mod.EventHandler
    public void serverStartingEvent(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new CommandJMServerForge());
    }

    @SideOnly(Side.SERVER)
    @Mod.EventHandler
    public void serverStartedEvent(FMLServerStartedEvent event)
    {
        MinecraftServer server = MinecraftServer.getServer();
        JourneymapServer.setWorldName(server.getEntityWorld().getWorldInfo().getWorldName());
        getLogger().info("World ID: " + ConfigHandler.getConfigByWorldName(JourneymapServer.getWorldName()).getWorldID());
    }
}