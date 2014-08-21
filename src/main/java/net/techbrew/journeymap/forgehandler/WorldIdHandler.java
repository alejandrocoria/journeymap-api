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
import cpw.mods.fml.common.network.*;
import cpw.mods.fml.common.network.simpleimpl.*;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.techbrew.journeymap.JourneyMap;

/**
 * Sample Forge Client class for handling WorldId custom packets.
 * @author techbrew
 */
public class WorldIdHandler
{
    // Channel name
    public static final String CHANNEL_NAME = "world_id";

    // Packet discriminator
    public static final int PACKET_DISCRIMINATOR = 0;

    // Minimum time in millis that must pass before subsequent requests can be made
    public static final int MIN_DELAY_MS = 1000;

    // Timestamp in millis of the last request by client
    private static long lastRequest;

    // Timestamp in millis of the last response from server
    private static long lastResponse;

    // Network wrapper of the channel for requests/response
    private SimpleNetworkWrapper channel;

    // Handle to Minecraft client
    Minecraft mc = FMLClientHandler.instance().getClient();

    /**
     * Default constructor.
     */
    public WorldIdHandler()
    {
        try
        {
            channel = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL_NAME);
            if (channel != null)
            {
                channel.registerMessage(WorldIdListener.class, WorldIdMessage.class, PACKET_DISCRIMINATOR, Side.CLIENT);
                FMLLog.info("Registered channel: %s", CHANNEL_NAME);
                MinecraftForge.EVENT_BUS.register(this);
            }
        }
        catch(Throwable t)
        {
            FMLLog.severe("Failed to register channel %s: %s", CHANNEL_NAME, t);
        }
    }

    /**
     * Request a World ID from the server by sending a blank WorldUidMessage.
     */
    private void requestWorldUid()
    {
        long now = System.currentTimeMillis();
        if(lastRequest + MIN_DELAY_MS < now && lastResponse + MIN_DELAY_MS < now)
        {
            FMLLog.info("Requesting World ID");
            channel.sendToServer(new WorldIdMessage());
            lastRequest = System.currentTimeMillis();
        }
    }

    /**
     * Use the world Load event as a trigger to request the World ID.
     * @param event
     */
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void on(WorldEvent.Load event)
    {
        if(!mc.isSingleplayer() && mc.thePlayer!=null)
        {
            requestWorldUid();
        }
    }

    /**
     * Use the EntityJoinWorldEvent of the player as a trigger to request the World ID.
     *
     * @param event
     */
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void on(EntityJoinWorldEvent event)
    {
        if(!mc.isSingleplayer() && mc.thePlayer!=null)
        {
            if(event.entity.getUniqueID().equals(mc.thePlayer.getUniqueID()))
            {
                requestWorldUid();
            }
        }
    }

    /**
     * Simple message listener for WorldUidMesssages received from the server.
     */
    public static class WorldIdListener implements IMessageHandler<WorldIdMessage, IMessage>
    {
        @SideOnly(Side.CLIENT)
        @Override
        public IMessage onMessage(WorldIdMessage message, MessageContext ctx)
        {
            lastResponse = System.currentTimeMillis();
            FMLLog.info("Got the worldUid from server: %s" , message.worldUid);
            JourneyMap.getInstance().setCurrentWorldUid(message.worldUid);
            return null;
        }
    }

    /**
     * Simple message to get a World ID from the server.
     * Send a blank one from the client as a request.
     */
    public static class WorldIdMessage implements IMessage
    {
        private String worldUid;

        public WorldIdMessage()
        {
        }

        public String getWorldUid()
        {
            return worldUid;
        }

        @Override
        public void fromBytes(ByteBuf buf)
        {
            worldUid = ByteBufUtils.readUTF8String(buf);
        }

        @Override
        public void toBytes(ByteBuf buf)
        {
            if(worldUid!=null)
            {
                ByteBufUtils.writeUTF8String(buf, worldUid);
            }
        }
    }
}
