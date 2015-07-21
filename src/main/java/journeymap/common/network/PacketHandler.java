package journeymap.common.network;

/**
 * Created by Mysticdrew on 10/8/2014.
 */

import journeymap.common.Constants;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;


public class PacketHandler {

    public static final SimpleNetworkWrapper WORLD_INFO_CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(Constants.WORLD_ID_CHANNEL);
    //public static final SimpleNetworkWrapper JM_PERMS = NetworkRegistry.INSTANCE.newSimpleChannel("jm_perms");

    public void init() {
        WORLD_INFO_CHANNEL.registerMessage(WorldIDPacket.WorldIdListener.class, WorldIDPacket.class, 0, Side.SERVER);
        //JM_PERMS.registerMessage(PermissionsPacket.PermissionsListener.class, PermissionsPacket.class, 0, Side.SERVER);
    }

    public static void sendAllPlayersWorldID(String worldID) {
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
        } else {
           // LogHelper.info(player.getName() + " is not an EntityPlayerMP");
        }


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

    private static int toInt(boolean val) {
        int intVal;
        if (val) {
            intVal = 1;
        } else {
            intVal = 0;
        }
        return intVal;
    }
}
