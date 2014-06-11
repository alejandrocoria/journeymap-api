package net.techbrew.journeymap.forgehandler;

import cpw.mods.fml.common.network.IConnectionHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetLoginHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet1Login;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.common.PacketData;
import net.techbrew.journeymap.common.PacketDataManager;
import net.techbrew.journeymap.feature.FeatureManager;

import java.util.EnumSet;

/**
 * Handle server-sent packets
 */
public class NetworkHandler implements EventHandlerManager.EventHandler, EventHandlerManager.SelfRegister, IPacketHandler, IConnectionHandler
{
    /**
     * Does own registration
     */
    public void register()
    {
        NetworkRegistry.instance().registerChannel(this, PacketData.CHANNEL_JOURNEYMAP);
        NetworkRegistry.instance().registerConnectionHandler(this);
        JourneyMap.getLogger().fine("Registered packet channel: " + PacketData.CHANNEL_JOURNEYMAP);
    }

    /**
     * Does own unregistration
     */
    public void unregister()
    {
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player playerEntity)
    {
        if (packet.channel.equals(PacketData.CHANNEL_JOURNEYMAP))
        {
            PacketDataManager.process(manager, packet, playerEntity);
        }
    }

    /**
     * Only needed for 1.6.4
     */
    @Override
    public EnumSet<EventHandlerManager.BusType> getBus()
    {
        return EnumSet.of(EventHandlerManager.BusType.NetworkRegistry);
    }

    @Override
    public void playerLoggedIn(Player player, NetHandler netHandler, INetworkManager manager)
    {
        // Server side only
    }

    @Override
    public String connectionReceived(NetLoginHandler netHandler, INetworkManager manager)
    {
        // Server side only
        return null;
    }

    @Override
    public void connectionOpened(NetHandler netClientHandler, String server, int port, INetworkManager manager)
    {
        // Client side
    }

    @Override
    public void connectionOpened(NetHandler netClientHandler, MinecraftServer server, INetworkManager manager)
    {
        // Client side, Local server
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void connectionClosed(INetworkManager manager)
    {
        // All sides
        JourneyMap.getInstance().stopMapping();
        FeatureManager.instance().resetOverrides();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void clientLoggedIn(NetHandler clientHandler, INetworkManager manager, Packet1Login login)
    {
        // Client side, Remote Server
        PacketDataManager.sendClientInfoData();
    }
}
