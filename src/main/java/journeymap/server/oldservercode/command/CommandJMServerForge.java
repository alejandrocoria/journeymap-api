package journeymap.server.oldservercode.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;

import java.util.List;


/**
 * Created by Mysticdrew on 10/27/2014.
 */
public class CommandJMServerForge extends CommandBase {
    private CommandJourneyMapServer jmserver;

    public CommandJMServerForge() {
        this.jmserver = new CommandJourneyMapServer();
    }

    @Override
    public String getName() {
        return "jmserver";
    }


    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/jmserver help|worldid";
    }

    @Override
    public boolean canCommandSenderUse(ICommandSender sender)
    {
        return super.canCommandSenderUse(sender);
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 3;
    }

    @Override
    public void execute(ICommandSender sender, String[] args) throws WrongUsageException
    {
        if (args.length > 0) {
            jmserver.processCommand(
                    sender.getCommandSenderEntity().getName(),
                    sender.getEntityWorld().getWorldInfo().getWorldName(),
                    args);
        } else {
            throw  new WrongUsageException(getCommandUsage(sender));
        }
    }

    public List addTabCompletionOptions(ICommandSender sender, String[] args)
    {
        return jmserver.retrieveTabCompleteValues(args);
    }

}