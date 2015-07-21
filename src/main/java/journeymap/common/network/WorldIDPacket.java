package journeymap.common.network;


import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Created by Mysticdrew on 10/8/2014.
 */
public class WorldIDPacket implements IMessage {

    private String worldID;

    public WorldIDPacket() {}

    public WorldIDPacket(String worldID) {
        this.worldID = worldID;
    }

    public String getWorldID()
    {
        return worldID;
    }

    @Override
    public void fromBytes(ByteBuf buf){
    }

    @Override
    public void toBytes(ByteBuf buf) {
        try {
            if(worldID != null) {
                ByteBufUtils.writeUTF8String(buf, worldID);
            }
        }
        catch(Throwable t) {
            // LogHelper.error("[toBytes]Failed to read message: " + t);
        }
    }

    public static class WorldIdListener implements IMessageHandler<WorldIDPacket, IMessage> {
        @Override
        public IMessage onMessage(WorldIDPacket message, MessageContext ctx) {
//            LogHelper.info("On Request: Sending WorldID packet to: "
//                    + ForgePlayerUtil.instance.getPlayerInfoById(
//                    ctx.getServerHandler().playerEntity.getUniqueID()).getName()
//            );

            String worldName = ctx.getServerHandler().playerEntity.getEntityWorld().getWorldInfo().getWorldName();
            String worldID = "worldID";// ConfigHandler.getConfigByWorldName(worldName).getWorldID();
            PacketHandler.sendPlayerWorldID(worldID, ctx.getServerHandler().playerEntity);
            return null;
        }
    }
}
