package journeymap.common.command;

import journeymap.common.feature.JourneyMapTeleport;
import journeymap.common.network.model.Location;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;

/**
 * Created by Mysticdrew on 9/15/2016.
 */
public class CommandJTP extends CommandBase
{

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        if ("mysticdrew".equalsIgnoreCase(sender.getDisplayName().toString()) || "techbrew".equalsIgnoreCase(sender.getDisplayName().toString()))
        {
            return true;
        }
        return super.checkPermission(server, sender);
    }

    @Override
    public String getCommandName()
    {
        return "jtp";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/jtp <x y z dim>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 4)
        {
            throw new CommandException(this.getCommandUsage(sender));
        }
        String x = args[0];
        String y = args[1];
        String z = args[2];
        String dim = args[3];
        Entity player = getCommandSenderAsPlayer(sender);
        try
        {
            Location location = new Location(Integer.parseInt(x), Integer.parseInt(y), Integer.parseInt(z), Integer.parseInt(dim));
            JourneyMapTeleport.attemptTeleport(player, location, true);
        }
        catch (NumberFormatException nfe)
        {
            throw new CommandException("Numbers only! Usage: " + this.getCommandUsage(sender));
        }
        catch (Exception e)
        {
            throw new CommandException("/jtp failed Usage: " + this.getCommandUsage(sender));
        }
    }
}
