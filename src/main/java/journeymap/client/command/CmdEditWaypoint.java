package journeymap.client.command;

import com.google.common.base.Joiner;
import journeymap.client.JourneymapClient;
import journeymap.client.log.ChatLog;
import journeymap.client.model.Waypoint;
import journeymap.client.task.main.IMainThreadTask;
import journeymap.client.ui.UIManager;
import journeymap.client.waypoint.WaypointParser;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import org.lwjgl.input.Keyboard;

import java.util.List;

/**
 * Subcommand assumes /jm invokes this
 */
public class CmdEditWaypoint implements ICommand
{

    @Override
    public String getCommandName()
    {
        return "wpedit";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        // Users don't need this, it only exists for ClickEvents to work.
        return null;
    }

    @Override
    public List<String> getCommandAliases()
    {
        return null;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        String text = Joiner.on(" ").skipNulls().join(args);
        final Waypoint waypoint = WaypointParser.parse(text);
        if (waypoint != null)
        {
            final boolean controlDown = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
            JourneymapClient.getInstance().queueMainThreadTask(new IMainThreadTask()
            {
                @Override
                public IMainThreadTask perform(Minecraft mc, JourneymapClient jm)
                {
                    if (controlDown)
                    {
                        if (waypoint.isInPlayerDimension())
                        {
                            waypoint.setPersistent(false);
                            UIManager.getInstance().openFullscreenMap(waypoint);
                        }
                        else
                        {
                            // TODO: i18n
                            ChatLog.announceError("Location is not in your dimension");
                        }
                    }
                    else
                    {
                        UIManager.getInstance().openWaypointEditor(waypoint, true, null);
                    }
                    return null;
                }

                @Override
                public String getName()
                {
                    return "Edit Waypoint";
                }
            });
        }
        else
        {
            // TODO i18n
            ChatLog.announceError("Not a valid waypoint. Use: 'x:3, z:70', etc. : " + text);
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