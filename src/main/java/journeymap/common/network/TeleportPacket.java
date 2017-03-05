/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.common.network;


import io.netty.buffer.ByteBuf;
import journeymap.common.Journeymap;
import journeymap.common.network.model.Location;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Created by Mysticdrew on 10/8/2014.
 */
public class TeleportPacket implements IMessage
{
    /**
     * The constant CHANNEL_NAME.
     */
// Channel name
    public static final String CHANNEL_NAME = "jtp";

    private String location;

    /**
     * Instantiates a new Teleport packet.
     */
    public TeleportPacket()
    {
    }

    /**
     * Instantiates a new Teleport packet.
     *
     * @param location the location
     */
    public TeleportPacket(Location location)
    {
        this.location = Location.GSON.toJson(location);
    }

    /**
     * Gets location.
     *
     * @return the location
     */
    public String getLocation()
    {
        return location;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {

        try
        {
            location = ByteBufUtils.readUTF8String(buf);
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
            if (location != null)
            {
                ByteBufUtils.writeUTF8String(buf, location);
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
    public static class Listener implements IMessageHandler<TeleportPacket, IMessage>
    {
        @Override
        public IMessage onMessage(TeleportPacket message, MessageContext ctx)
        {
            Entity player = null;
            player = ctx.getServerHandler().player;
            Location location = Location.GSON.fromJson(message.getLocation(), Location.class);
//            JourneyMapTeleport.attemptTeleport(player, location, false);
            return null;
        }
    }
}
