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
 * Created by Mysticdrew on 10/8/2014.
 */
public class WorldIDPacket implements IMessage
{
    /**
     * The constant CHANNEL_NAME.
     */
// Channel name
    public static final String CHANNEL_NAME = "world_info";

    private String worldID;

    /**
     * Instantiates a new World id packet.
     */
    public WorldIDPacket()
    {
    }

    /**
     * Instantiates a new World id packet.
     *
     * @param worldID the world id
     */
    public WorldIDPacket(String worldID)
    {
        this.worldID = worldID;
    }

    /**
     * Gets world id.
     *
     * @return the world id
     */
    public String getWorldID()
    {
        return worldID;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        try
        {
            worldID = ByteBufUtils.readUTF8String(buf);
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

    /**
     * The type World id listener.
     */
    public static class WorldIdListener implements IMessageHandler<WorldIDPacket, IMessage>
    {
        @Override
        public IMessage onMessage(WorldIDPacket message, MessageContext ctx)
        {
            Journeymap.getLogger().info(String.format("Got the World ID from server: %s", message.getWorldID()));
            Journeymap.proxy.handleWorldIdMessage(message.getWorldID(), null);
//            Journeymap.getClient().setServerEnabled(true);
            return null;
        }
    }
}
