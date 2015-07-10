/*
 * JourneyMap mod for Minecraft
 *
 * Copyright (C) 2011-2014 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>.
 */

package net.techbrew.journeymap.forge.event;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.forge.helper.ForgeHelper;

/**
 * Sample Forge Client class for handling World Info custom packets.
 *
 * @author techbrew
 */
public class WorldInfoHandler
{
    // Channel name
    public static final String CHANNEL_NAME = "world_info";

    // Packet discriminator for World ID message
    public static final int PACKET_WORLDID = 0;

    // Minimum time in millis that must pass before subsequent requests can be made
    public static final int MIN_DELAY_MS = 1000;

    // Timestamp in millis of the last request by client
    private static long lastRequest;

    // Timestamp in millis of the last response from server
    private static long lastResponse;
    // Network wrapper of the channel for requests/response
    private static SimpleNetworkWrapper channel;
    // Handle to Minecraft client
    Minecraft mc = ForgeHelper.INSTANCE.getClient();

    /**
     * Default constructor.
     */
    public WorldInfoHandler()
    {
        try
        {
            channel = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL_NAME);
            if (channel != null)
            {
                channel.registerMessage(WorldIdListener.class, WorldIdMessage.class, PACKET_WORLDID, Side.CLIENT);
                JourneyMap.getLogger().info(String.format("Registered channel: %s", CHANNEL_NAME));
                MinecraftForge.EVENT_BUS.register(this);
            }
        }
        catch (Throwable t)
        {
            JourneyMap.getLogger().error(String.format("Failed to register channel %s: %s", CHANNEL_NAME, t));
        }
    }

    /**
     * Request a World ID from the server by sending a blank WorldUidMessage.
     */
    public static void requestWorldID()
    {
        if (channel != null)
        {
            long now = System.currentTimeMillis();
            if (lastRequest + MIN_DELAY_MS < now && lastResponse + MIN_DELAY_MS < now)
            {
                JourneyMap.getLogger().info("Requesting World ID");
                channel.sendToServer(new WorldIdMessage());
                lastRequest = System.currentTimeMillis();
            }
        }
    }

    @SubscribeEvent
    public void onConnected(FMLNetworkEvent.ClientConnectedToServerEvent event)
    {
        // TODO: Check the timing on this?
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
        if (!mc.isSingleplayer() && mc.thePlayer != null && !mc.thePlayer.isDead)
        {
            if (ForgeHelper.INSTANCE.getEntityName(event.entity).equals(ForgeHelper.INSTANCE.getEntityName(mc.thePlayer)))
            {
                requestWorldID();
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
            JourneyMap.getLogger().info(String.format("Got the World ID from server: %s", message.worldUid));
            JourneyMap.getInstance().setCurrentWorldId(message.worldUid);
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
            try
            {
                worldUid = ByteBufUtils.readUTF8String(buf);
            }
            catch (Throwable t)
            {
                JourneyMap.getLogger().error(String.format("Failed to read message: %s", t));
            }
        }

        @Override
        public void toBytes(ByteBuf buf)
        {
            try
            {
                if (worldUid != null)
                {
                    ByteBufUtils.writeUTF8String(buf, worldUid);
                }
            }
            catch (Throwable t)
            {
                JourneyMap.getLogger().error(String.format("Failed to read message: %s", t));
            }
        }
    }
}
