/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.command;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.command.server.CommandTeleport;
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

    public boolean isPermitted()
    {
        if (!waypoint.isTeleportReady())
        {
            return false;
        }

        if (mc.thePlayer.capabilities.isCreativeMode)
        {
            return true;
        }

        CommandTeleport command = new CommandTeleport();
        return command.canCommandSenderUseCommand(mc.thePlayer);
    }

    public void run()
    {
        Vec3 waypointPos = waypoint.getPosition();
        mc.thePlayer.sendChatMessage(String.format("/tp %s %s %s %s", mc.thePlayer.getCommandSenderName(), waypointPos.xCoord, waypointPos.yCoord, waypointPos.zCoord));
    }
}
