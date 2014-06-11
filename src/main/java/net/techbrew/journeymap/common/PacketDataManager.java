package net.techbrew.journeymap.common;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.feature.FeatureManager;
import net.techbrew.journeymap.log.LogFormatter;

/**
 * Manages Packet250CustomPayload packets, converting and processing them.
 */
public class PacketDataManager
{
    @SideOnly(Side.CLIENT)
    public static void process(INetworkManager manager, Packet250CustomPayload customPayload, Player playerEntity)
    {
        PacketData packetData = null;

        try
        {
            packetData = PacketData.fromCustomPayload(customPayload);
        }
        catch(Throwable t)
        {
            JourneyMap.getLogger().severe(String.format("Error creating PacketData from Packet250CustomPayload %s, Error=%s", customPayload, LogFormatter.toString(t)));
            return;
        }

        try
        {
            if(packetData!=null)
            {
                if(packetData instanceof ServerInfoData)
                {
                    ServerInfoData serverInfoData = (ServerInfoData) packetData;

                    JourneyMap.getInstance().setCurrentWorldHash(serverInfoData.hash);
                    FeatureManager.instance().overrideFeatures(serverInfoData.features);
                }
                else if(packetData instanceof ClientInfoData)
                {
                    if (playerEntity instanceof EntityPlayerMP && MinecraftServer.getServer()!=null)
                    {
                        EntityPlayerMP player = (EntityPlayerMP) playerEntity;
                        //ClientInfoData clientInfoData = (ClientInfoData) packetData;
                        PacketDispatcher.sendPacketToPlayer(new ServerInfoData(player.getEntityWorld(), null).toCustomPayload(), playerEntity);
                    }
                }
            }
        }
        catch (Throwable t)
        {
            JourneyMap.getLogger().severe(String.format("Error handling PacketData from %s, Error=%s", customPayload, LogFormatter.toString(t)));
            return;
        }
    }

    @SideOnly(Side.CLIENT)
    public static void sendClientInfoData()
    {
        try
        {
            PacketDispatcher.sendPacketToServer(ClientInfoData.create().toCustomPayload());
            JourneyMap.getLogger().info("Sent ClientInfoData to server");
        }
        catch(Throwable t)
        {
            JourneyMap.getLogger().severe(String.format("Error sending ClientInfoData to server. Error=%s", LogFormatter.toString(t)));
        }
    }
}
