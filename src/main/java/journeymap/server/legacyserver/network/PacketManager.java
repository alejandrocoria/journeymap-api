package journeymap.server.legacyserver.network;

/**
 * Created by Mysticdrew on 11/12/2014.
 */
public class PacketManager
{
    public static PacketManager instance;
    private IPacketHandler packetHandler;

    public PacketManager(IPacketHandler packetHandler)
    {
        this.packetHandler = packetHandler;
    }

    public static void init(IPacketHandler packetHandler)
    {
        instance = new PacketManager(packetHandler);
        packetHandler.init();
    }

    public void sendAllPlayersWorldID(String worldID)
    {
        packetHandler.sendAllPlayersWorldID(worldID);
    }

    public void sendPlayerWorldID(String worldID, String playerName)
    {
        packetHandler.sendPlayerWorldID(worldID, playerName);
    }

}
