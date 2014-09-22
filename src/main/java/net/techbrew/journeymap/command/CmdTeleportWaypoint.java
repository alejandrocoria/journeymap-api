/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.command;

import com.mojang.authlib.GameProfile;
import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.log.LogFormatter;
import net.techbrew.journeymap.model.Waypoint;

/**
 * Created by mwoodman on 4/8/2014.
 */
public class CmdTeleportWaypoint
{
    final Minecraft mc = FMLClientHandler.instance().getClient();
    final Waypoint waypoint;

    public CmdTeleportWaypoint(Waypoint waypoint)
    {
        this.waypoint = waypoint;
    }

    public static boolean isPermitted(Minecraft mc)
    {
        if(mc.getIntegratedServer()!=null)
        {
            IntegratedServer mcServer = mc.getIntegratedServer();
            ServerConfigurationManager configurationManager = null;
            GameProfile profile = null;
            try
            {
                profile = new GameProfile(mc.thePlayer.getUniqueID(), mc.thePlayer.getCommandSenderName());
                configurationManager = mcServer.getConfigurationManager();
                return configurationManager.func_152596_g(profile);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                try
                {
                    if (profile != null && configurationManager != null)
                    {
                        return mcServer.isSinglePlayer()
                                && mcServer.worldServers[0].getWorldInfo().areCommandsAllowed()
                                && mcServer.getServerOwner().equalsIgnoreCase(profile.getName());
                    }
                    else
                    {
                        JourneyMap.getLogger().warn("Failed to check teleport permission both ways: " + LogFormatter.toString(e) + ", and profile or configManager were null.");
                    }
                }
                catch (Exception e2)
                {
                    JourneyMap.getLogger().warn("Failed to check teleport permission. Both ways failed: " + LogFormatter.toString(e) + ", and " + LogFormatter.toString(e2));
                }
            }
        }

        return true;
    }

    public void run()
    {
        mc.thePlayer.sendChatMessage(String.format("/tp %s %s %s %s", mc.thePlayer.getCommandSenderName(), waypoint.getX(), waypoint.getY(), waypoint.getZ()));
    }
}
