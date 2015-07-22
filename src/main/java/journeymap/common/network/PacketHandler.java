/*
 * JourneyMap : A mod for Minecraft
 *
 * Copyright (c) 2011-2015 Mark Woodman.  All Rights Reserved.
 * This file may not be altered, file-hosted, re-packaged, or distributed in part or in whole
 * without express written permission by Mark Woodman <mwoodman@techbrew.net>
 */

package journeymap.common.network;

/**
 * Created by Mysticdrew on 10/8/2014.
 */

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;


public class PacketHandler
{

    public static final SimpleNetworkWrapper WORLD_INFO_CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(WorldIDPacket.CHANNEL_NAME);
    //public static final SimpleNetworkWrapper JM_PERMS = NetworkRegistry.INSTANCE.newSimpleChannel("jm_perms");

    public static void sendAllPlayersWorldID(String worldID)
    {
        WORLD_INFO_CHANNEL.sendToAll(new WorldIDPacket(worldID));
    }

    public static void sendPlayerWorldID(String worldID, EntityPlayerMP player) {

        if ((player instanceof EntityPlayerMP) && (player != null)) {
           // LogHelper.info(player.getName() + " is an EntityPlayerMP attempting to send the worldId packet");
            try {
                WORLD_INFO_CHANNEL.sendTo(new WorldIDPacket(worldID), player);
            } catch (RuntimeException rte) {
               // LogHelper.error(player.getName() + " is not a real player. WorldID:" + worldID + " Error: " + rte);
            } catch (Exception e) {
               // LogHelper.error("Unknown Exception - PlayerName:" + player.getName() + " WorldID:" + worldID +" Exception "+ e);
            }
        }
        else
        {
            // LogHelper.info(player.getName() + " is not an EntityPlayerMP");
        }


    }

    private static int toInt(boolean val)
    {
        int intVal;
        if (val)
        {
            intVal = 1;
        } else {
            intVal = 0;
        }
        return intVal;
    }

//    public static void sendPerms(String worldName, EntityPlayerMP player) {
//        MappingOptionsHandler options = new MappingOptionsHandler(worldName);
//        JM_PERMS.sendTo(
//                new PermissionsPacket(
//                        toInt(options.disableRadar(player.getCommandSenderName())),
//                        toInt(options.disableRadar(player.getCommandSenderName())),
//                        toInt(options.disableRadar(player.getCommandSenderName())),
//                        toInt(options.disableRadar(player.getCommandSenderName())),
//                        toInt(options.disableCaveMapping(player.getCommandSenderName()))
//                ), player);
//    }

    public void init(Side side)
    {
        WORLD_INFO_CHANNEL.registerMessage(WorldIDPacket.WorldIdListener.class, WorldIDPacket.class, 0, side);
        //JM_PERMS.registerMessage(PermissionsPacket.PermissionsListener.class, PermissionsPacket.class, 0, Side.SERVER);
    }
}
