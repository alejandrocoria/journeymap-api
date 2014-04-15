package net.techbrew.journeymap.command;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandServerTp;
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

        CommandServerTp command = new CommandServerTp();
        return command.canCommandSenderUseCommand(mc.thePlayer);
    }

    public void run()
    {
        mc.thePlayer.sendChatMessage(String.format("/tp %s %s %s %s", mc.thePlayer.getEntityName(), waypoint.getX(), waypoint.getY(), waypoint.getZ()));
    }
}
