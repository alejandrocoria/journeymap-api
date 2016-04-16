package journeymap.client.command;

import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import org.apache.logging.log4j.core.helpers.Strings;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Delegates /jm commands
 */
public class ClientCommandInvoker implements ICommand
{
    Map<String, ICommand> commandMap = new HashMap<String, ICommand>();

    public ClientCommandInvoker register(ICommand command)
    {
        commandMap.put(command.getCommandName().toLowerCase(), command);
        return this;
    }

    @Override
    public String getCommandName()
    {
        return "jm";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        StringBuffer sb = new StringBuffer();
        for (ICommand command : commandMap.values())
        {
            String usage = command.getCommandUsage(sender);
            if (!Strings.isEmpty(usage))
            {
                if (sb.length() > 0)
                {
                    sb.append("\n");
                }
                sb.append("/jm ").append(usage);
            }
        }
        return sb.toString();
    }

    @Override
    public List<String> getCommandAliases()
    {
        return Arrays.asList("journeymap");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        try
        {
            if (args.length > 0)
            {
                ICommand command = getSubCommand(args);
                if (command != null)
                {
                    String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                    command.processCommand(sender, subArgs);
                }
            }
            else
            {
                sender.addChatMessage(new ChatComponentText(getCommandUsage(sender)));
            }
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error(LogFormatter.toPartialString(t));
            // todo i18n
            throw new CommandException("Error in /jm: " + t);
        }
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender)
    {
        return true;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        try
        {
            ICommand command = getSubCommand(args);
            if (command != null)
            {
                String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                return command.addTabCompletionOptions(sender, subArgs, pos);
            }
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error("Error in addTabCompletionOptions: " + LogFormatter.toPartialString(t));
        }
        return null;
    }

    public ICommand getSubCommand(String[] args)
    {
        if (args.length > 0)
        {
            ICommand command = commandMap.get(args[0].toLowerCase());
            if (command != null)
            {
                return command;
            }
        }
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index)
    {
        return false;
    }

    @Override
    public int compareTo(ICommand o)
    {
        return 0;
    }
}
