package journeymap.client.forge.event;

import journeymap.common.Journeymap;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Detects when player connects to a server.
 */
@SideOnly(Side.CLIENT)
public class PlayerConnectHandler implements EventHandlerManager.EventHandler
{
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onConnect(FMLNetworkEvent.ClientConnectedToServerEvent event)
    {
        try
        {
            if ("MODDED".equals(event.getConnectionType()))
            {
                Journeymap.getClient().setForgeServerConnection(true);
            }
        }
        catch (Exception e)
        {
            Journeymap.getLogger().error("Error handling WorldEvent.Unload", e);
        }
    }
}