package journeymap.server.oldservercode.network;


import io.netty.buffer.ByteBuf;
import journeymap.common.Journeymap;
import journeymap.server.oldservercode.config.ConfigHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Created by Mysticdrew on 10/8/2014.
 */
public class WorldIDPacket implements IMessage
{

    private String worldID;

    public WorldIDPacket()
    {
    }

    public WorldIDPacket(String worldID)
    {
        this.worldID = worldID;
    }

    public String getWorldID()
    {
        return worldID;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        try
        {
            if (worldID != null)
            {
                ByteBufUtils.writeUTF8String(buf, worldID);
            }
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error("[toBytes]Failed to read message: " + t);
        }
    }

    public static class WorldIdListener implements IMessageHandler<WorldIDPacket, IMessage>
    {
        @Override
        public IMessage onMessage(WorldIDPacket message, MessageContext ctx)
        {
            if (ctx.side == Side.SERVER)
            {
                if (ConfigHandler.getConfigByWorldName(ctx.getServerHandler().playerEntity.getEntityWorld().getWorldInfo().getWorldName()).isUsingWorldID())
                {
                    String worldName = ctx.getServerHandler().playerEntity.getEntityWorld().getWorldInfo().getWorldName();
                    String worldID = ConfigHandler.getConfigByWorldName(worldName).getWorldID();
                    PacketManager.instance.sendPlayerWorldID(worldID, ctx.getServerHandler().playerEntity.getName());
                }
            }

            return null;
        }
    }
}
