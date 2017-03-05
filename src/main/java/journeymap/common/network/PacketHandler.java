/*
 * JourneyMap Mod <journeymap.info> for Minecraft
 * Copyright (c) 2011-2017  Techbrew Interactive, LLC <techbrew.net>.  All Rights Reserved.
 */

package journeymap.common.network;

/**
 * Created by Mysticdrew on 10/8/2014.
 */

import journeymap.common.Journeymap;
import journeymap.common.network.model.InitLogin;
import journeymap.common.network.model.Location;
import journeymap.server.nbt.WorldNbtIDSaveHandler;
import journeymap.server.properties.PermissionProperties;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;


/**
 * The type Packet handler.
 */
public class PacketHandler
{

    /**
     * The constant WORLD_INFO_CHANNEL.
     */
    public static final SimpleNetworkWrapper WORLD_INFO_CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(WorldIDPacket.CHANNEL_NAME);
    /**
     * The constant DIMENSION_PERMISSIONS_CHANNEL.
     */
    public static final SimpleNetworkWrapper DIMENSION_PERMISSIONS_CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(DimensionPermissionPacket.CHANNEL_NAME);
    /**
     * The constant TELEPORT_CHANNEL.
     */
    public static final SimpleNetworkWrapper TELEPORT_CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(TeleportPacket.CHANNEL_NAME);
    /**
     * The constant INIT_LOGIN_CHANNEL.
     */
    public static final SimpleNetworkWrapper INIT_LOGIN_CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(LoginPacket.CHANNEL_NAME);

    /**
     * Init.
     *
     * @param side the side
     */
    public static void init(Side side)
    {

        WORLD_INFO_CHANNEL.registerMessage(WorldIDPacket.WorldIdListener.class, WorldIDPacket.class, 0, side);
        INIT_LOGIN_CHANNEL.registerMessage(LoginPacket.Listener.class, LoginPacket.class, 0, side);
        TELEPORT_CHANNEL.registerMessage(TeleportPacket.Listener.class, TeleportPacket.class, 0, Side.SERVER);

        if (Side.SERVER == side)
        {

        }

        if (Side.CLIENT == side)
        {
            DIMENSION_PERMISSIONS_CHANNEL.registerMessage(DimensionPermissionPacket.Listener.class, DimensionPermissionPacket.class, 0, side);
        }
    }

    /**
     * Teleport player.
     *
     * @param location the location
     */
    public static void teleportPlayer(Location location)
    {
        TELEPORT_CHANNEL.sendToServer(new TeleportPacket(location));
    }

    /**
     * Send dimension packet to player.
     *
     * @param player   the player
     * @param property the property
     */
    public static void sendDimensionPacketToPlayer(EntityPlayerMP player, PermissionProperties property)
    {
        DIMENSION_PERMISSIONS_CHANNEL.sendTo(new DimensionPermissionPacket(property), player);
    }

    /**
     * Send all players world id.
     *
     * @param worldID the world id
     */
    public static void sendAllPlayersWorldID(String worldID)
    {
        WORLD_INFO_CHANNEL.sendToAll(new WorldIDPacket(worldID));
    }

    /**
     * Send player world id.
     *
     * @param player the player
     */
    public static void sendPlayerWorldID(EntityPlayerMP player)
    {
        if ((player instanceof EntityPlayerMP) && (player != null))
        {
            WorldNbtIDSaveHandler worldSaveHandler = new WorldNbtIDSaveHandler();
            String worldID = worldSaveHandler.getWorldID();
            String playerName = player.getName();

            try
            {
                WORLD_INFO_CHANNEL.sendTo(new WorldIDPacket(worldID), player);
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

    /**
     * Send login packet.
     *
     * @param player     the player
     * @param packetData the packet data
     */
    public static void sendLoginPacket(EntityPlayerMP player, InitLogin packetData)
    {
        if ((player instanceof EntityPlayerMP) && (player != null))
        {
            Journeymap.getLogger().info("Sending log in packet.");
            String playerName = player.getName();

            try
            {
                INIT_LOGIN_CHANNEL.sendTo(new LoginPacket(packetData), player);
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
}
