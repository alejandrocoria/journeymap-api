/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2018  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.common.network.impl;

/**
 * Created by Mysticdrew on 10/8/2014.
 */

import journeymap.common.Journeymap;
import journeymap.common.network.DimensionPermissionPacket;
import journeymap.common.network.LoginPacket;
import journeymap.common.network.TeleportPacket;
import journeymap.common.network.WorldIDPacket;
import journeymap.common.network.model.InitLogin;
import journeymap.common.network.model.Location;
import journeymap.server.nbt.WorldNbtIDSaveHandler;
import journeymap.server.properties.DimensionProperties;
import journeymap.server.properties.GlobalProperties;
import journeymap.server.properties.PermissionProperties;
import journeymap.server.properties.PropertiesManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import static journeymap.server.JourneymapServer.isOp;


public class NetworkHandler
{
    private static NetworkHandler INSTANCE;

    static final SimpleNetworkWrapper JOURNEYMAP_NETWORK_CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(Message.CHANNEL_NAME);
    public static final SimpleNetworkWrapper WORLD_INFO_CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(WorldIDPacket.CHANNEL_NAME);
    public static final SimpleNetworkWrapper DIMENSION_PERMISSIONS_CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(DimensionPermissionPacket.CHANNEL_NAME);
    public static final SimpleNetworkWrapper TELEPORT_CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(TeleportPacket.CHANNEL_NAME);
    public static final SimpleNetworkWrapper INIT_LOGIN_CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(LoginPacket.CHANNEL_NAME);

    public static void init(Side side)
    {
        INSTANCE = new NetworkHandler();
        JOURNEYMAP_NETWORK_CHANNEL.registerMessage(MessageListener.class, Message.class, 0, side);
        WORLD_INFO_CHANNEL.registerMessage(WorldIDPacket.Listener.class, WorldIDPacket.class, 0, side);
        INIT_LOGIN_CHANNEL.registerMessage(LoginPacket.Listener.class, LoginPacket.class, 0, side);
        TELEPORT_CHANNEL.registerMessage(TeleportPacket.Listener.class, TeleportPacket.class, 0, Side.SERVER);
        DIMENSION_PERMISSIONS_CHANNEL.registerMessage(DimensionPermissionPacket.Listener.class, DimensionPermissionPacket.class, 0, side);
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

    public void requestPermissions()
    {
        DIMENSION_PERMISSIONS_CHANNEL.sendToServer(new DimensionPermissionPacket());
    }

    public void requestTeleportFromServer(Location location)
    {
        TELEPORT_CHANNEL.sendToServer(new TeleportPacket(location));
    }

    private void sendDimensionPacketToPlayer(EntityPlayerMP player, PermissionProperties property)
    {
        if (validClient(player))
        {
            DimensionPermissionPacket prop = new DimensionPermissionPacket(property);
            DIMENSION_PERMISSIONS_CHANNEL.sendTo(prop, player);
        }
    }

    private void sendWorldIdToPlayer(EntityPlayerMP player, String worldId) throws Exception
    {
        if (validClient(player))
        {
            WORLD_INFO_CHANNEL.sendTo(new WorldIDPacket(worldId), player);
        }
    }

    private void sendLoginPacketToPlayer(EntityPlayerMP player, InitLogin packetData) throws Exception
    {
        if (validClient(player))
        {
            INIT_LOGIN_CHANNEL.sendTo(new LoginPacket(packetData), player);
        }
    }

    public void sendAllPlayersWorldID(String worldID)
    {
        WORLD_INFO_CHANNEL.sendToAll(new WorldIDPacket(worldID));
    }

    public void sendPlayerWorldID(EntityPlayerMP player)
    {
        if ((player != null) && (player instanceof EntityPlayerMP))
        {
            WorldNbtIDSaveHandler worldSaveHandler = new WorldNbtIDSaveHandler();
            String worldID = worldSaveHandler.getWorldID();
            String playerName = player.getName();

            try
            {
                sendWorldIdToPlayer(player, worldID);
            }
            catch (RuntimeException rte)
            {
                Journeymap.getLogger().error(playerName + " is not a real player. WorldID:" + worldID + " Error: " + rte);
            }
            catch (Exception e)
            {
                Journeymap.getLogger().error("Unknown Exception - PlayerName:" + playerName + " WorldID:" + worldID + " Exception " + e);
            }
        }
    }

    public void sendLoginPacket(EntityPlayerMP player, InitLogin packetData)
    {
        if ((player != null) && (player instanceof EntityPlayerMP))
        {
            Journeymap.getLogger().info("Sending log in packet.");
            String playerName = player.getName();

            try
            {
                sendLoginPacketToPlayer(player, packetData);
            }
            catch (RuntimeException rte)
            {
                Journeymap.getLogger().error(playerName + " is not a real player. Error: " + rte);
            }
            catch (Exception e)
            {
                Journeymap.getLogger().error("Unknown Exception - PlayerName:" + playerName + " Exception " + e);
            }
        }
    }

    public void sendPermissionsPacket(EntityPlayerMP player)
    {
        Journeymap.getLogger().info(player.getDisplayNameString() + " joining dimension " + player.dimension);
        PermissionProperties prop;
        DimensionProperties dimensionProperties = PropertiesManager.getInstance().getDimProperties(player.dimension);

        try
        {
            /*
             * Cloning since we do not want to modify the permission properties,
             * We want a brand new copy to send to the client
             */
            if (dimensionProperties.enabled.get())
            {
                prop = (DimensionProperties) dimensionProperties.clone();
            }
            else
            {
                prop = (GlobalProperties) PropertiesManager.getInstance().getGlobalProperties().clone();
            }

            /*
             * If player is op, set the cave and radar options on the packet to send.
             * The client only reads radarEnabled and caveMappingEnabled, it ignores the
             */
            if (isOp(player))
            {
                prop.radarEnabled.set(prop.opRadarEnabled.get());
                prop.caveMappingEnabled.set(prop.opCaveMappingEnabled.get());
                prop.surfaceMappingEnabled.set(prop.opSurfaceMappingEnabled.get());
                prop.topoMappingEnabled.set(prop.opTopoMappingEnabled.get());
            }

            sendDimensionPacketToPlayer(player, prop);
        }
        catch (CloneNotSupportedException e)
        {
            Journeymap.getLogger().error("CloneNotSupportedException: ", e);
        }
    }

    private boolean validClient(EntityPlayerMP player)
    {
        return player.connection.getNetworkManager().channel().attr(NetworkRegistry.FML_MARKER).get();
    }
}
