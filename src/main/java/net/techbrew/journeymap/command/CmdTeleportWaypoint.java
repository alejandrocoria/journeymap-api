package net.techbrew.journeymap.command;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Vec3;
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
            return mc.getIntegratedServer().getConfigurationManager().isPlayerOpped(mc.thePlayer.getCommandSenderName());
        }
        else
        {
            return true;
        }
    }

    public void run()
    {
        Vec3 waypointPos = waypoint.getPosition();
        mc.thePlayer.sendChatMessage(String.format("/tp %s %s %s %s", mc.thePlayer.getCommandSenderName(), waypointPos.xCoord, waypointPos.yCoord, waypointPos.zCoord));
    }
}
