/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2016 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
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
    // Channel name
    public static final String CHANNEL_NAME = "jtp";

    private String location;

    public TeleportPacket()
    {
    }

    public TeleportPacket(Location location)
    {
        this.location = Location.GSON.toJson(location);
    }

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
