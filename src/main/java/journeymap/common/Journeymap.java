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
    /**
     * The constant MOD_ID.
     */
    public static final String MOD_ID = "journeymap";
    /**
     * The constant SHORT_MOD_NAME.
     */
    public static final String SHORT_MOD_NAME = "JourneyMap";
    /**
     * The constant JM_VERSION.
     */
    public static final Version JM_VERSION = Version.from("@MAJOR@", "@MINOR@", "@MICRO@", "@PATCH@", new Version(5, 5, 0, "dev"));
    /**
     * The constant FORGE_VERSION.
     */
    public static final String FORGE_VERSION = "@FORGEVERSION@";
    /**
     * The constant MC_VERSION.
     */
    public static final String MC_VERSION = "@MCVERSION@";
    /**
     * The constant WEBSITE_URL.
     */
    public static final String WEBSITE_URL = "http://journeymap.info/";
    /**
     * The constant DOWNLOAD_URL.
     */
    public static final String DOWNLOAD_URL = "http://minecraft.curseforge.com/projects/journeymap-32274/files/";
    /**
     * The constant VERSION_URL.
     */
    public static final String VERSION_URL = "http://widget.mcf.li/mc-mods/minecraft/journeymap-32274.json";

    /**
     * The constant instance.
     */
    @Mod.Instance(Journeymap.MOD_ID)
    public static Journeymap instance;

    /**
     * The constant proxy.
     */
    @SidedProxy(clientSide = "journeymap.client.JourneymapClient", serverSide = "journeymap.server.JourneymapServer")
    public static CommonProxy proxy;

    /**
     * Get the common logger.
     *
     * @return the logger
     */
    public static Logger getLogger()
    {
        return LogManager.getLogger(MOD_ID);
    }

    /**
     * Whether this side will accept being connected to the other side.
     *
     * @param modList the mod list
     * @param side    the side
     * @return the boolean
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
     * @param event the event
     * @throws Throwable the throwable
     */
    @Mod.EventHandler
    public void preInitialize(FMLPreInitializationEvent event) throws Throwable
    {
        proxy.preInitialize(event);
    }

    /**
     * Initialize the sided proxy.
     *
     * @param event the event
     * @throws Throwable the throwable
     */
    @Mod.EventHandler
    public void initialize(FMLInitializationEvent event) throws Throwable
    {
        proxy.initialize(event);
    }

    /**
     * Post-initialize the sided proxy.
     *
     * @param event the event
     * @throws Throwable the throwable
     */
    @Mod.EventHandler
    public void postInitialize(FMLPostInitializationEvent event) throws Throwable
    {
        proxy.postInitialize(event);

    }

    /**
     * Server starting event.
     *
     * @param event the event
     */
    @Mod.EventHandler
    public void serverStartingEvent(FMLServerStartingEvent event)
    {
//        if (event.getServer().getEntityWorld().isRemote)
//        {
        PropertiesManager.getInstance();
//        }
        event.registerServerCommand(new CommandJTP());
    }

    /**
     * Server started event.
     *
     * @param event the event
     */
    @SideOnly(Side.SERVER)
    @Mod.EventHandler
    public void serverStartedEvent(FMLServerStartedEvent event)
    {

    }

    /**
     * Gets client.
     *
     * @return the client
     */
    @SideOnly(Side.CLIENT)
    public static JourneymapClient getClient()
    {
        return (JourneymapClient) proxy;
    }

    /**
     * Gets server.
     *
     * @return the server
     */
    @SideOnly(Side.SERVER)
    public static JourneymapServer getServer()
    {
        return (JourneymapServer) proxy;
    }
}