package journeymap.server.oldservercode.events;

import journeymap.common.Journeymap;
import journeymap.server.oldservercode.config.ConfigHandler;
import journeymap.server.oldservercode.mapcontrol.MappingOptionsHandler;
import journeymap.server.oldservercode.network.PacketManager;
import journeymap.server.oldservercode.reference.Codes;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


/**
 * Created by Mysticdrew on 11/10/2014.
 */
public class ForgeEvents
{

    @SideOnly(Side.SERVER)
    @SubscribeEvent
    public void on(EntityJoinWorldEvent event)
    {
        if (event.entity instanceof EntityPlayerMP)
        {
            String worldName = event.world.getWorldInfo().getWorldName();
            new UserJoinWorldThread(
                    (EntityPlayerMP) event.entity,
                    ConfigHandler.getConfigByWorldName(worldName).getWorldID()
            ).start();
        }
    }


    private class UserJoinWorldThread extends Thread
    {
        private String worldID;
        private EntityPlayerMP player;
        private MappingOptionsHandler options;

        public UserJoinWorldThread(EntityPlayerMP player, String worldID)
        {
            this.player = player;
            this.worldID = worldID;
            options = new MappingOptionsHandler(
                    player.getEntityWorld().getWorldInfo().getWorldName());
        }

        @Override
        public void run()
        {
            try
            {
                sleep(500L);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }

            String playerName = player.getName();

            if (options.disableRadar(playerName))
            {
                Journeymap.getLogger().info("Disabling Radar for player: " + playerName);
                player.addChatMessage(new ChatComponentTranslation(Codes.RADAR_CODE));
            }

            if (options.disableCaveMapping(playerName))
            {
                Journeymap.getLogger().info("Disabling CaveMapping for player: " + playerName);
                player.addChatMessage(new ChatComponentTranslation(Codes.CAVE_MAPPING_CODE));
            }

            if (ConfigHandler.getConfigByWorldName(player.getEntityWorld().getWorldInfo().getWorldName()).isUsingWorldID())
            {
                Journeymap.getLogger().info(String.format("Login: Sending WorldID Packet to %s", playerName));
                PacketManager.instance.sendPlayerWorldID(worldID, playerName);
            }
        }
    }
}
