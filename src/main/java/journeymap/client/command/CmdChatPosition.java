package journeymap.client.command;

import com.google.common.base.Joiner;
import journeymap.client.JourneymapClient;
import journeymap.client.task.main.IMainThreadTask;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.util.List;

/**
 * Subcommand assumes /jm invokes this
 */
public class CmdChatPosition implements ICommand
{

    @Override
    public String getCommandName()
    {
        return "~";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        // todo i18n
        return TextFormatting.AQUA + "~" + TextFormatting.RESET + " : Copy your location into Text";
    }

    @Override
    public List<String> getCommandAliases()
    {
        return null;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        String text;
        if (args.length > 1)
        {
            text = Joiner.on("").skipNulls().join(args);
        }
        else
        {
            final BlockPos pos = sender.getPosition();
            text = String.format("[x:%s, y:%s, z:%s]", pos.getX(), pos.getY(), pos.getZ());
        }

        final String pos = text;

        JourneymapClient.getInstance().queueMainThreadTask(new IMainThreadTask()
        {
            @Override
            public IMainThreadTask perform(Minecraft mc, JourneymapClient jm)
            {
                FMLClientHandler.instance().getClient().displayGuiScreen(new GuiChat(pos));
                return null;
            }

            @Override
            public String getName()
            {
                return "Edit Waypoint";
            }
        });
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        return true;
    }


    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
    {
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
