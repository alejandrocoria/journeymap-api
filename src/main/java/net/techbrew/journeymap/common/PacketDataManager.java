package net.techbrew.journeymap.common;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.techbrew.journeymap.JourneyMap;
import net.techbrew.journeymap.feature.FeatureManager;

/**
 * Manages data packets.
 */
public class PacketDataManager
{
    public final static String CHANNEL_JOURNEYMAP = "journeymap";

    public static class ServerInfoHandler implements IMessageHandler<ServerInfoData, IMessage>
    {
        @Override
        public IMessage onMessage(ServerInfoData message, MessageContext ctx)
        {
            JourneyMap.getInstance().setCurrentWorldHash(message.hash);
            FeatureManager.instance().overrideFeatures(message.features);
            return null;
        }
    }

    public static class ClientInfoHandler implements IMessageHandler<ClientInfoData,ServerInfoData>
    {
        @Override
        public ServerInfoData onMessage(ClientInfoData message, MessageContext ctx)
        {
            return new ServerInfoData(ctx.getServerHandler().playerEntity.getEntityWorld(), null);
        }
    }
}
