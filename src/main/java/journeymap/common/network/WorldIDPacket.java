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
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Created by Mysticdrew on 10/8/2014.
 */
public class WorldIDPacket implements IMessage
{
    // Channel name
    public static final String CHANNEL_NAME = "world_info";

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

    public static class WorldIdListener implements IMessageHandler<WorldIDPacket, IMessage>
    {
        @Override
        public IMessage onMessage(WorldIDPacket message, MessageContext ctx)
        {

            Journeymap.getLogger().info(String.format("Got the World ID from server: %s", message.getWorldID()));
            Journeymap.proxy.handleWorldIdMessage(message.getWorldID(), null);
            return null;

//            EntityPlayerMP player = null;
//            if (ctx.side == Side.SERVER)
//            {
//                player = ctx.getServerHandler().playerEntity;
//                if (ConfigHandler.getConfigByWorldName(player.getEntityWorld().getWorldInfo().getWorldName()).isUsingWorldID())
//                {
//                    Journeymap.proxy.handleWorldIdMessage(message.getWorldID(), player);
//                }
//            }
//            return null;
        }
    }
}
