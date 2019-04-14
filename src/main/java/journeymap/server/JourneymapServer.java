/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.server;

import com.mojang.authlib.GameProfile;
import journeymap.common.CommonProxy;
import journeymap.common.Journeymap;
import journeymap.common.version.Version;
import journeymap.server.config.ForgeConfig;
import journeymap.server.events.ForgeEvents;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.server.FMLServerHandler;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.UUID;


/**
 * Coming soon to a codebase near you.
 */
public class JourneymapServer implements CommonProxy
{
    private Logger logger;
    public static boolean DEV_MODE = false;

    private static final Version MINIMUM_ACCEPTABLE_VERSION = new Version(5, 5, 5, "b2");


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
        MinecraftForge.EVENT_BUS.register(new ForgeConfig());
    }

    /**
     * Initialize the server.
     *
     * @param event
     */
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
//            logger.info("MOD Key: " + s + " MOD Value: " + modList.get(s));
            if (s.toLowerCase().equals("journeymap") || s.toLowerCase().equals("journeymap@"))
            {
                if (modList.get(s).contains("@"))
                {
                    // Dev Env
                    logger.info("Mod check = dev environment");
                    DEV_MODE = true;
                    return true;
                }

                String version = modList.get(s).split("-")[1];
                Version userLoggedInVersion = Version.from(version, null);

                if (MINIMUM_ACCEPTABLE_VERSION.isNewerThan(userLoggedInVersion))
                {
                    logger.info("Version Mismatch need " + MINIMUM_ACCEPTABLE_VERSION.toString() + " or higher. Current version attempt -> " + modList.get(s));
                    return false;
                }
                return true;
            }
        }
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

    public static boolean isOp(EntityPlayer player)
    {
        if (Side.CLIENT.equals(FMLCommonHandler.instance().getSide()))
        {
            MinecraftServer mcServer = FMLCommonHandler.instance().getMinecraftServerInstance();
            boolean creative = ((EntityPlayerMP) player).capabilities.isCreativeMode;
            boolean cheatMode = mcServer.getPlayerList().canSendCommands(new GameProfile(player.getUniqueID(), player.getName()));
            return creative || cheatMode;
        }

        String[] ops = FMLServerHandler.instance().getServer().getPlayerList().getOppedPlayerNames();
        for (String opName : ops)
        {
            // Checking by UUID in case player name changes.
            UUID opId = FMLServerHandler.instance().getServer().getPlayerList().getOppedPlayers().getGameProfileFromName(opName).getId();
            if (player.getDisplayNameString().equalsIgnoreCase(opName) || player.getUniqueID().equals(opId) || Constants.debugOverride(player))
            {
                return true;
            }
        }
        return false;
    }
}
