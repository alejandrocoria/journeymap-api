/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.common.command;

import journeymap.common.feature.JourneyMapTeleport;
import journeymap.common.feature.Location;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;

/**
 * Created by Mysticdrew on 9/15/2018.
 */
// TODO: Remove for 1.13 update and use the packet instead.
@Deprecated
public class CommandJTP extends CommandBase
{

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        return true;
    }

    @Override
    public String getName()
    {
        return "jtp";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "/jtp <x y z dim>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {

        if (args.length < 4)
        {
            throw new CommandException(this.getUsage(sender));
        }
        Entity player = getCommandSenderAsPlayer(sender);
        try
        {
            double x = Double.parseDouble(args[0]);
            double y = Double.parseDouble(args[1]);
            double z = Double.parseDouble(args[2]);
            int dim = Integer.parseInt(args[3]);
            Location location = new Location(x, y, z, dim);
            JourneyMapTeleport.instance().attemptTeleport(player, location);
        }
        catch (NumberFormatException nfe)
        {
            throw new CommandException("Numbers only! Usage: " + this.getUsage(sender) + nfe);
        }
        catch (Exception e)
        {
            throw new CommandException("/jtp failed Usage: " + this.getUsage(sender));
        }
    }
}
