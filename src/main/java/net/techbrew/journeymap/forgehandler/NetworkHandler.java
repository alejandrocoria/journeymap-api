package net.techbrew.journeymap.forgehandler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.common.ClientInfoData;
import net.techbrew.journeymap.common.PacketDataManager;
import net.techbrew.journeymap.common.ServerInfoData;
import net.techbrew.journeymap.feature.FeatureManager;

import java.util.EnumSet;

/**
 * Handle server-sent packets
 */
public class NetworkHandler implements EventHandlerManager.EventHandler
{

    private final SimpleNetworkWrapper simpleNetworkWrapper;

    /**
     * Constructor.  Delegates message handling to PacketDataManager classes.
     */
    public NetworkHandler()
    {
        simpleNetworkWrapper = new SimpleNetworkWrapper(PacketDataManager.CHANNEL_JOURNEYMAP);

        try
        {
            simpleNetworkWrapper.registerMessage(PacketDataManager.ServerInfoHandler.class, ServerInfoData.class, 0, Side.CLIENT);
        }
        catch (Exception e)
        {
            JourneyMap.getLogger().severe("Could not register " + PacketDataManager.ServerInfoHandler.class);
        }

        try
        {
            simpleNetworkWrapper.registerMessage(PacketDataManager.ClientInfoHandler.class, ClientInfoData.class, 1, Side.SERVER);
        }
        catch (Exception e)
        {
            JourneyMap.getLogger().severe("Could not register " + PacketDataManager.ServerInfoHandler.class);
        }

        JourneyMap.getLogger().fine("Registered messages for channel: " + PacketDataManager.CHANNEL_JOURNEYMAP);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onClientConnectedToServer(FMLNetworkEvent.ClientConnectedToServerEvent event)
    {
        simpleNetworkWrapper.sendToServer(ClientInfoData.create());
        JourneyMap.getLogger().info("Sent ClientInfoData to server");
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onClientDisconnectionFromServer(FMLNetworkEvent.ClientDisconnectionFromServerEvent event)
    {
        JourneyMap.getInstance().stopMapping();
        FeatureManager.instance().resetOverrides();
    }


    /**
     * Only needed for 1.6.4
     */
    @Override
    public EnumSet<EventHandlerManager.BusType> getBus()
    {
        return EnumSet.of(EventHandlerManager.BusType.FMLCommonHandlerBus);
    }

}
