package journeymap.common.network;

import journeymap.common.Journeymap;
import journeymap.common.network.impl.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;

public class PacketRegistry
{
    private static PacketRegistry INSTANCE;

    public static void init()
    {
        INSTANCE = new PacketRegistry();
        NetworkHandler handler = new NetworkHandler(Journeymap.MOD_ID);

        // custom listeners.
        NetworkRegistry.INSTANCE.newSimpleChannel(WorldIDPacket.CHANNEL_NAME).registerMessage(WorldIDPacket.Listener.class, WorldIDPacket.class, 0, Side.CLIENT);
        NetworkRegistry.INSTANCE.newSimpleChannel(LegacyServerPackets.CHANNEL_NAME_PROP).registerMessage(LegacyServerPackets.Listener.class, LegacyServerPackets.class, 0, Side.CLIENT);
        NetworkRegistry.INSTANCE.newSimpleChannel(LegacyServerPackets.CHANNEL_NAME_LOGIN).registerMessage(LegacyServerPackets.Listener.class, LegacyServerPackets.class, 0, Side.CLIENT);

        // register the new handler
        handler.register();

    }

    public static PacketRegistry getInstance()
    {
        if (INSTANCE != null)
        {
            return INSTANCE;
        }
        else
        {
            Journeymap.getLogger().error("Packet Handler not initialized before use.");
            throw new UnsupportedOperationException("Packet Handler not Initialized");
        }
    }

    public void versionMismatch()
    {
        try
        {
            ITextComponent text1 = new TextComponentString("Disabling Journeymap for this server.");
            ITextComponent text2 = new TextComponentString("This client cannot connect to servers running versions older than Journeymap 5.5.5");
            ITextComponent text3 = new TextComponentString("Please downgrade to Journeymap 5.5.4 to connect to this server or ask the server admin to update Journeymap.");
            Minecraft.getMinecraft().player.sendMessage(text1);
            Minecraft.getMinecraft().player.sendMessage(text2);
            Minecraft.getMinecraft().player.sendMessage(text3);
        } catch (Exception e) {
            // do nothing we will still disable journeymap.
        }
        Journeymap.getClient().disable();
    }
}
