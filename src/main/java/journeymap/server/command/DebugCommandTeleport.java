package journeymap.server.command;

import journeymap.server.feature.JourneyMapTeleport;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

/**
 * Created by Mysticdrew on 9/15/2016.
 */
public class DebugCommandTeleport extends CommandBase
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

        String x = args[0];
        String y = args[1];
        String z = args[2];
        String dim = args[3];

        EntityPlayerMP player = server.getPlayerList().getPlayerByUsername(sender.getName());
        JourneyMapTeleport.attemptTeleport(player, x, y, z, dim, true);
    }
}
