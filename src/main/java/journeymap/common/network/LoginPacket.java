package journeymap.common.network;

import io.netty.buffer.ByteBuf;
import journeymap.common.Journeymap;
import journeymap.common.network.model.InitLogin;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Created by Mysticdrew on 11/19/2016.
 */
public class LoginPacket implements IMessage
{
    // Channel name
    public static final String CHANNEL_NAME = "jm_init_login";

    private String packet;

    public LoginPacket()
    {
    }

    public LoginPacket(InitLogin packet)
    {
        this.packet = InitLogin.GSON.toJson(packet);
    }

    public String getPacket()
    {
        return packet;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        try
        {
            packet = ByteBufUtils.readUTF8String(buf);
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error(String.format("Failed to read message: %s", t));
        }
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        try
        {
            if (packet != null)
            {
                ByteBufUtils.writeUTF8String(buf, packet);
            }
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error("[toBytes]Failed to read message: " + t);
        }
    }

    public static class Listener implements IMessageHandler<LoginPacket, IMessage>
    {
        @Override
        public IMessage onMessage(LoginPacket message, MessageContext ctx)
        {
            Journeymap.getLogger().info("Login Packet received");
            InitLogin packet = InitLogin.GSON.fromJson(message.getPacket(), InitLogin.class);
            Journeymap.getClient().setServerTeleportEnabled(packet.isTeleportEnabled());
            Journeymap.getClient().setServerEnabled(true);
            return null;
        }
    }
}
