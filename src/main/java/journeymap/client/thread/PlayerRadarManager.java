package journeymap.client.thread;

import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Keeps the server based player radar players thread safe.
 */
public class PlayerRadarManager
{
    private final List<EntityPlayer> playersOnServer;
    private static PlayerRadarManager INSTANCE;

    private PlayerRadarManager()
    {
        playersOnServer = new ArrayList<>();
    }


    public static PlayerRadarManager getInstance()
    {

        if (INSTANCE == null)
        {
            INSTANCE = new PlayerRadarManager();
        }
        return INSTANCE;
    }

    public List<EntityPlayer> getPlayers()
    {
        synchronized (playersOnServer)
        {
            return this.playersOnServer;
        }
    }

    public void addPlayer(EntityPlayer player)
    {
        synchronized (playersOnServer)
        {
            this.playersOnServer.add(player);
        }
    }

    public void removePlayer(EntityPlayer player)
    {
        synchronized (playersOnServer)
        {
            this.playersOnServer.removeIf(players -> player.getUniqueID().equals(players.getUniqueID()));
        }
    }
}
