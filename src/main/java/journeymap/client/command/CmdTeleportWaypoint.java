/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.client.command;

import com.mojang.authlib.GameProfile;
import journeymap.client.model.Waypoint;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.management.PlayerList;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.util.TreeSet;

/**
 * @author techbrew 4/8/2014.
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
        if (mc.getIntegratedServer() != null)
        {
            IntegratedServer mcServer = mc.getIntegratedServer();
            PlayerList configurationManager = null;
            GameProfile profile = null;
            try
            {
                profile = new GameProfile(mc.player.getUniqueID(), mc.player.getName());
                configurationManager = mcServer.getPlayerList();

                // 1.7
                //return configurationManager.func_152596_g(profile);

                // 1.8
                return configurationManager.canSendCommands(profile);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                try
                {
                    if (profile != null && configurationManager != null)
                    {
                        return mcServer.isSinglePlayer()
                                && mcServer.worlds[0].getWorldInfo().areCommandsAllowed()
                                && mcServer.getServerOwner().equalsIgnoreCase(profile.getName());
                    }
                    else
                    {
                        Journeymap.getLogger().warn("Failed to check teleport permission both ways: " + LogFormatter.toString(e) + ", and profile or configManager were null.");
                    }
                }
                catch (Exception e2)
                {
                    Journeymap.getLogger().warn("Failed to check teleport permission. Both ways failed: " + LogFormatter.toString(e) + ", and " + LogFormatter.toString(e2));
                }
            }
        }

        return true;
    }

    public void run()
    {
        double x = waypoint.getBlockCenteredX();
        double z = waypoint.getBlockCenteredZ();
        TreeSet<Integer> dim = (TreeSet<Integer>) waypoint.getDimensions();

        if (dim.first() == -1 && mc.player.dimension != -1)
        {
            x = x / 8;
            z = z / 8;
        }
        else if (dim.first() != -1 && mc.player.dimension == -1)
        {
            x = (x * 8);
            z = (z * 8);
        }
        if (Journeymap.getClient().isServerEnabled() || FMLClientHandler.instance().getClient().isSingleplayer()) {
            mc.player.sendChatMessage(String.format("/jtp %s %s %s %s", x, waypoint.getY(), z, dim.first()));
        } else {
            mc.player.sendChatMessage(String.format("/tp %s %s %s %s", mc.player.getName(), waypoint.getX(), waypoint.getY(), waypoint.getZ()));
        }
    }
}
