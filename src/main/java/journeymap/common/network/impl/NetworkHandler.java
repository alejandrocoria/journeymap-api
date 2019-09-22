/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.common.network.impl;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NetworkHandler
{
    private final String MOD_ID;
    private static NetworkHandler INSTANCE;
    private final SimpleNetworkWrapper NETWORK_CHANNEL;

    public NetworkHandler(String modid)
    {
        INSTANCE = this;
        this.MOD_ID = modid;
        NETWORK_CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(MOD_ID + "_channel");
    }

    public int register()
    {
        return register(0);
    }

    public int register(int discriminator)
    {
        NETWORK_CHANNEL.registerMessage(MessageListener.class, Message.class, discriminator++, Side.SERVER);
        NETWORK_CHANNEL.registerMessage(MessageListener.class, Message.class, discriminator++, Side.CLIENT);
        return discriminator;
    }

    public static NetworkHandler getInstance()
    {
        if (INSTANCE != null)
        {
            return INSTANCE;
        }
        else
        {
            getLogger().error("Packet Handler not initialized before use.");
            throw new UnsupportedOperationException("Packet Handler not Initialized");
        }
    }

    public static Logger getLogger()
    {
        return LogManager.getLogger(INSTANCE.MOD_ID);
    }

    public void sendToServer(Message message)
    {
        NETWORK_CHANNEL.sendToServer(message);
    }

    public void sendTo(Message message, EntityPlayerMP player)
    {
        NETWORK_CHANNEL.sendTo(message, player);
    }

    public void sendToAll(Message message)
    {
        NETWORK_CHANNEL.sendToAll(message);
    }

    public void sendToAllAround(Message message, NetworkRegistry.TargetPoint point)
    {
        NETWORK_CHANNEL.sendToAllAround(message, point);
    }

    public void sendToAllTracking(IMessage message, NetworkRegistry.TargetPoint point)
    {
        NETWORK_CHANNEL.sendToAllTracking(message, point);
    }

    public void sendToAllTracking(IMessage message, Entity entity)
    {
        NETWORK_CHANNEL.sendToAllTracking(message, entity);
    }

    public void sendToDimension(IMessage message, int dimensionId)
    {
        NETWORK_CHANNEL.sendToDimension(message, dimensionId);
    }
}
