/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.common.network;

import io.netty.buffer.ByteBuf;
import journeymap.common.Journeymap;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;


/**
 * Just a listener to disable journeymap because the client version is too old for the server to restrict features.
 */
@Deprecated
public class LegacyServerPackets implements IMessage
{
    /**
     * The constant CHANNEL_NAME.
     */
// Channel name
    public static final String CHANNEL_NAME_LOGIN = "jm_init_login";
    public static final String CHANNEL_NAME_PROP = "jm_dim_permission";

    private String packet = "";

    /**
     * Instantiates a new Login packet.
     */
    public LegacyServerPackets()
    {
    }

    /**
     * Instantiates a new Login packet.
     *
     * @param packet the packet
     */
    public LegacyServerPackets(Object packet)
    {
        this.packet = packet.toString();
    }

    /**
     * Gets packet.
     *
     * @return the packet
     */
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

    /**
     * The type Listener.
     */
    public static class Listener implements IMessageHandler<LegacyServerPackets, IMessage>
    {
        @Override
        public IMessage onMessage(LegacyServerPackets message, MessageContext ctx)
        {
            PacketRegistry.getInstance().versionMismatch();
            return null;
        }
    }
}
