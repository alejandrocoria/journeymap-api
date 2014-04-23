package net.techbrew.journeymap.command;

import net.minecraft.client.Minecraft;
import net.minecraft.command.server.CommandTeleport;
import net.techbrew.journeymap.model.Waypoint;

/**
 * Created by mwoodman on 4/8/2014.
 */
public class CmdTeleportWaypoint
{
    final Minecraft mc = Minecraft.getMinecraft();
    final Waypoint waypoint;

    public CmdTeleportWaypoint(Waypoint waypoint)
    {
        this.waypoint = waypoint;
    }

    public boolean isPermitted()
    {
        if(!waypoint.isTeleportReady())
        {
            return false;
        }

        if(mc.thePlayer.capabilities.isCreativeMode)
        {
            return true;
        }

        CommandTeleport command = new CommandTeleport();
        return command.canCommandSenderUseCommand(mc.thePlayer);
    }

    public void run()
    {
        int dimension = mc.thePlayer.dimension;
        mc.thePlayer.sendChatMessage(String.format("/tp %s %s %s %s", mc.thePlayer.getCommandSenderName(), waypoint.getX(dimension), waypoint.getY(dimension), waypoint.getZ(dimension)));
    }
}
