package journeymap.server.legacyserver.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;

import java.util.List;


/**
 * Created by Mysticdrew on 10/27/2014.
 */
public class CommandJMServerForge extends CommandBase
{
    private CommandJourneyMapServer jmserver;

    public CommandJMServerForge()
    {
        this.jmserver = new CommandJourneyMapServer();
    }

    @Override
    // 1.7.10, 1.8
    // public String getName()
    // 1.8.8
    public String getCommandName()
    {
        return "jmserver";
    }


    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/jmserver help|worldid";
    }


    @Override
    // 1.7.10, 1.8
    // public String canCommandSenderUse()
    // 1.8.8
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        return super.checkPermission(server, sender);
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 3;
    }

    @Override
    // 1.7.10, 1.8
    // public String execute()
    // 1.8.8
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length > 0)
        {
            jmserver.processCommand(
                    sender.getCommandSenderEntity().getName(),
                    sender.getEntityWorld().getWorldInfo().getWorldName(),
                    args);
        }
        else
        {
            throw new WrongUsageException(getCommandUsage(sender));
        }
    }

    public List addTabCompletionOptions(ICommandSender sender, String[] args)
    {
        return jmserver.retrieveTabCompleteValues(args);
    }

}
