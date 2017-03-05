/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.common;

import journeymap.client.JourneymapClient;
import journeymap.common.command.CommandJTP;
import journeymap.common.version.Version;
import journeymap.server.JourneymapServer;
import journeymap.server.properties.PropertiesManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * Forge Mod entry point
 */
@Mod(modid = Journeymap.MOD_ID,
        name = Journeymap.SHORT_MOD_NAME,
        version = "@JMVERSION@",
        canBeDeactivated = true,
        guiFactory = "journeymap.client.ui.dialog.OptionsGuiFactory",
        dependencies = "required-after:Forge@[${@FORGEVERSION@},)")
public class Journeymap
{
    public static final String MOD_ID = "journeymap";
    public static final String SHORT_MOD_NAME = "JourneyMap";
    public static final Version JM_VERSION = Version.from("@MAJOR@", "@MINOR@", "@MICRO@", "@PATCH@", new Version(5, 3, 0, "dev"));
    public static final String FORGE_VERSION = "@FORGEVERSION@";
    public static final String MC_VERSION = "@MCVERSION@";
    public static final String WEBSITE_URL = "http://journeymap.info/";
    public static final String DOWNLOAD_URL = "http://minecraft.curseforge.com/projects/journeymap-32274/files/";
    public static final String VERSION_URL = "http://widget.mcf.li/mc-mods/minecraft/journeymap-32274.json";

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
        if (proxy == null)
        {
            return true;
        }
        else
        {
            return proxy.checkModLists(modList, side);
        }
    }

    /**
     * Pre-initialize the sided proxy.
     *
     * @param event
     * @throws Throwable
     */
    @Mod.EventHandler
    public void preInitialize(FMLPreInitializationEvent event) throws Throwable
    {
        proxy.preInitialize(event);
    }

    /**
     * Initialize the sided proxy.
     *
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
     *
     * @param event
     * @throws Throwable
     */
    @Mod.EventHandler
    public void postInitialize(FMLPostInitializationEvent event) throws Throwable
    {
        proxy.postInitialize(event);

    }

    @Mod.EventHandler
    public void serverStartingEvent(FMLServerStartingEvent event)
    {
//        if (event.getServer().getEntityWorld().isRemote)
//        {
            PropertiesManager.getInstance();
//        }
        event.registerServerCommand(new CommandJTP());
    }

    @SideOnly(Side.SERVER)
    @Mod.EventHandler
    public void serverStartedEvent(FMLServerStartedEvent event)
    {

    }

    @SideOnly(Side.CLIENT)
    public static JourneymapClient getClient()
    {
        return (JourneymapClient) proxy;
    }

    @SideOnly(Side.SERVER)
    public static JourneymapServer getServer()
    {
        return (JourneymapServer) proxy;
    }
}