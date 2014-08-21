/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.forgehandler;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.techbrew.journeymap.JourneyMap;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

/**
 * Listen for events which are likely to need the map to be updated.
 */
public class WorldUidHandler
{
    public static final String CHANNEL_NAME = "world_uid";
    public static final int PACKET_DISCRIMINATOR = 0;

    private SimpleNetworkWrapper worldUidChannel;

    public WorldUidHandler()
    {
        try
        {
            worldUidChannel = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL_NAME);
            if (worldUidChannel != null)
            {
                worldUidChannel.registerMessage(WorldUidListener.class, WorldUidMessage.class, PACKET_DISCRIMINATOR, Side.CLIENT);
                FMLLog.info("Registered for worldUid channel");
                MinecraftForge.EVENT_BUS.register(this);
            }
        }
        catch(Throwable t)
        {
            FMLLog.severe("Couldn't register worldUid channel: %s", t);
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void invoke(WorldEvent.Load event)
    {
        if(!FMLClientHandler.instance().getClient().isSingleplayer())
        {
            FMLLog.info("Got a %s event, asking server for worldUid", event.getClass().getName());
            worldUidChannel.sendToServer(new WorldUidMessage());
        }
    }

    public static class WorldUidListener implements IMessageHandler<WorldUidMessage, IMessage>
    {
        @SideOnly(Side.CLIENT)
        @Override
        public IMessage onMessage(WorldUidMessage message, MessageContext ctx)
        {
            FMLLog.info("Got the worldUid from server: %s" , message.worldUid);
            JourneyMap.getInstance().setCurrentWorldUid(message.worldUid);
            return null;
        }
    }

    public static class WorldUidMessage implements IMessage
    {
        public String worldUid;

        @Override
        public void fromBytes(ByteBuf buf)
        {
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            try
            {
                worldUid = new String(bytes, "UTF-8");
            }
            catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public void toBytes(ByteBuf buf)
        {
            if(worldUid!=null)
            {
                buf.readBytes(worldUid.getBytes());
            }
        }
    }
}
