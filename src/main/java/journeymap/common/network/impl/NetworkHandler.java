/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.common.network.impl;

/**
 * Created by Mysticdrew on 10/8/2014.
 */

import journeymap.common.Journeymap;
import journeymap.common.network.WorldIDPacket;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;


public class NetworkHandler
{
    private static NetworkHandler INSTANCE;

    static final SimpleNetworkWrapper JOURNEYMAP_NETWORK_CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(Message.CHANNEL_NAME);

    // TODO: REMOVE world id packet for the 1.13+ update if bukkit/spiggot does not migrate.
    // this is only a listener now for bukkit servers.
    private static final SimpleNetworkWrapper WORLD_INFO_CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(WorldIDPacket.CHANNEL_NAME);

    public static void init()
    {
        int d = 0;
        INSTANCE = new NetworkHandler();
        JOURNEYMAP_NETWORK_CHANNEL.registerMessage(MessageListener.class, Message.class, d++, Side.SERVER);
        JOURNEYMAP_NETWORK_CHANNEL.registerMessage(MessageListener.class, Message.class, d++, Side.CLIENT);
        WORLD_INFO_CHANNEL.registerMessage(WorldIDPacket.Listener.class, WorldIDPacket.class, d++, Side.CLIENT);
    }

    public static NetworkHandler getInstance()
    {
        if (INSTANCE != null)
        {
            return INSTANCE;
        }
        else
        {
            Journeymap.getLogger().error("Packet Handler not initialized before use.");
            throw new UnsupportedOperationException("Packet Handler not Initialized");
        }
    }
}
