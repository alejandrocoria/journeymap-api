/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
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
 * Teleport to a waypoint
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

                // if on a server that does not have JM on the server.
                return configurationManager.canSendCommands(profile) || Journeymap.getClient().isTeleportEnabled();
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

        // server connection has JMServer
        if (Journeymap.getClient().isJourneyMapServerConnection())
        {
            return Journeymap.getClient().isTeleportEnabled();
        }

        // return true because we are on a server with out JM and have no idea if we can tp or not.
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
        if (Journeymap.getClient().isJourneyMapServerConnection() || FMLClientHandler.instance().getClient().isSingleplayer())
        {
            // This is the logic needed for 1.13+
//            JsonObject object = new JsonObject();
//            object.addProperty(X, x);
//            object.addProperty(Y, waypoint.getY());
//            object.addProperty(Z, z);
//            object.addProperty(DIM, dim.first());
//            new Teleport().send(object);

            // Remove in 1.13+ go to packet system.
            mc.player.sendChatMessage(String.format("/jtp %s %s %s %s", x, waypoint.getY(), z, dim.first()));
        }
        else
        {
            String teleportCommand = Journeymap.getClient().getWaypointProperties().teleportCommand.get();

            teleportCommand = teleportCommand
                    .replace("{name}", mc.player.getName())
                    .replace("{x}", String.valueOf(waypoint.getX()))
                    .replace("{y}", String.valueOf(waypoint.getY()))
                    .replace("{z}", String.valueOf(waypoint.getZ()))
                    .replace("{dim}", String.valueOf(dim.first()));

            mc.player.sendChatMessage(teleportCommand);
        }
    }
}
