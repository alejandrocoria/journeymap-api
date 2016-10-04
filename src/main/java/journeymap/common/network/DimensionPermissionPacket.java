package journeymap.common.network;


import io.netty.buffer.ByteBuf;
import journeymap.client.feature.FeatureManager;
import journeymap.common.Journeymap;
import journeymap.server.properties.DimensionProperties;
import journeymap.server.properties.PermissionProperties;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Created by Mysticdrew on 5/16/2016.
 */
public class DimensionPermissionPacket implements IMessage
{
    // Channel name
    public static final String CHANNEL_NAME = "jm_dim_permission";

    private String prop;

    public DimensionPermissionPacket()
    {
    }

    public DimensionPermissionPacket(PermissionProperties prop)
    {
        this.prop = prop.toJsonString(false);
    }

    public String getProp()
    {
        return prop;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        try
        {
            prop = ByteBufUtils.readUTF8String(buf);
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
            if (prop != null)
            {
                ByteBufUtils.writeUTF8String(buf, prop);
            }
        }
        catch (Throwable t)
        {
            Journeymap.getLogger().error("[toBytes]Failed to read message: " + t);
        }
    }

    public static class Listener implements IMessageHandler<DimensionPermissionPacket, IMessage>
    {
        @Override
        public IMessage onMessage(DimensionPermissionPacket message, MessageContext ctx)
        {
            PermissionProperties prop = new DimensionProperties(0).load(message.getProp(), false);
            FeatureManager.instance().disableDimensionFeature(prop);

            return null;
        }
    }
}
