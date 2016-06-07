package journeymap.server.events;

import journeymap.common.Journeymap;
import journeymap.common.network.PacketHandler;
import journeymap.server.properties.DimensionProperties;
import journeymap.server.properties.PropertiesManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.server.FMLServerHandler;

/**
 * Created by Mysticdrew on 5/5/2016.
 */
public class ForgeEvents
{
    @SideOnly(Side.SERVER)
    @SubscribeEvent
    public void on(EntityJoinWorldEvent event)
    {
        if (event.getEntity() instanceof EntityPlayerMP)
        {
            EntityPlayerMP player = (EntityPlayerMP) event.getEntity();

            //TODO: sendplayer permission packets
            Journeymap.getLogger().info(((EntityPlayerMP) event.getEntity()).getDisplayNameString() + " joining dimension " + event.getEntity().dimension);
            DimensionProperties prop = PropertiesManager.getInstance().getDimProperties(player.dimension);
            System.out.println(prop.toString());
            if (prop.enabled.get())
            {
                PacketHandler.sendDimensionPacketToPlayer(player, prop);
            }
            else
            {
                PacketHandler.sendDimensionPacketToPlayer(player, PropertiesManager.getInstance().getGlobalProperties());
            }



        }
    }

    @SideOnly(Side.SERVER)
    @SubscribeEvent
    public void playerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (event.player instanceof EntityPlayerMP)
        {
            if (isOp(event.player.getName()))
            {
                //TODO: send op ui packet
                System.out.println("PLAYER IS OPS");
            }
            else
            {
                //TODO: sendplayer logged in packet(worldid)
                System.out.println("NOT OP");
            }
        }
    }

    private boolean isOp(String playerName)
    {
        String[] ops = FMLServerHandler.instance().getServer().getPlayerList().getOppedPlayerNames();
        for (String opName : ops)
        {
            if (playerName.equalsIgnoreCase(opName))
            {
                return true;
            }
        }
        return false;
    }
}
