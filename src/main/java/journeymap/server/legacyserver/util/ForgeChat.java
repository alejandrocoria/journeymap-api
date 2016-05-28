package journeymap.server.legacyserver.util;

import journeymap.common.Journeymap;
import journeymap.server.legacyserver.chat.IChatHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;


/**
 * Created by Mysticdrew on 11/19/2014.
 */
public class ForgeChat implements IChatHandler
{

    @Override
    public void sendChatMessage(String player, String message)
    {
        TextComponentString msg = new TextComponentString(message);
        sendCommandResponse(player, msg);

    }

    private void sendCommandResponse(String sender, TextComponentString text)
    {
        EntityPlayerMP player = ForgePlayerUtil.instance.getPlayerEntityByName(sender);
        if (player != null)
        {
            player.addChatMessage(text);
        }
        else
        {
            Journeymap.getLogger().info(text.getText());
        }
    }
}
