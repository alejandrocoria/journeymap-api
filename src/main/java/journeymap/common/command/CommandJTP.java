package journeymap.common.command;

import journeymap.common.feature.JourneyMapTeleport;
import journeymap.common.network.model.Location;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * Created by Mysticdrew on 9/15/2016.
 */
public class CommandJTP extends CommandBase
{

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        return true;
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
        Entity player = getCommandSenderAsPlayer(sender);
        try
        {
            double x = Double.parseDouble(args[0]);
            double y = Double.parseDouble(args[1]);
            double z = Double.parseDouble(args[2]);
            int dim = Integer.parseInt(args[3]);
            Location location = new Location(x, y, z, dim);
            JourneyMapTeleport.attemptTeleport(player, location);
        }
        catch (NumberFormatException nfe)
        {
            throw new CommandException("Numbers only! Usage: " + this.getCommandUsage(sender) + nfe);
        }
        catch (Exception e)
        {
            throw new CommandException("/jtp failed Usage: " + this.getCommandUsage(sender));
        }
    }
}
