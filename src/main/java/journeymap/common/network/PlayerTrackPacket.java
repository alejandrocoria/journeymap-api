/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.common.network;

import io.netty.buffer.ByteBuf;
import journeymap.common.Journeymap;
import journeymap.common.network.model.PlayersInWorld;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Created by Mysticdrew on 11/19/2018.
 */
public class PlayerTrackPacket implements IMessage
{
    // Channel name
    public static final String CHANNEL_NAME = "jm_player_track";

    private String packet;

    public PlayerTrackPacket()
    {
    }

    public PlayerTrackPacket(PlayersInWorld packet)
    {
        this.packet = PlayersInWorld.GSON.toJson(packet);
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

    public static class Listener implements IMessageHandler<PlayerTrackPacket, IMessage>
    {
        @Override
        public IMessage onMessage(PlayerTrackPacket message, MessageContext ctx)
        {
            PlayersInWorld playersInWorld = PlayersInWorld.GSON.fromJson(message.getPacket(), PlayersInWorld.class);
            Journeymap.getClient().playersOnServer.clear();
            for (PlayersInWorld.PlayerWorld player : playersInWorld.get())
            {
                Journeymap.getClient().playersOnServer.put(player.getUuid(), player);
            }
            return null;
        }
    }
}

