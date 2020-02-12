package journeymap.common.feature;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Keeps the server based player radar players thread safe.
 */
public class PlayerRadarManager
{
    private final Map<UUID, EntityPlayer> playersOnServer;
    private static PlayerRadarManager INSTANCE;
    private final Object lock = new Object();

    private PlayerRadarManager()
    {
        playersOnServer = new HashMap<UUID, EntityPlayer>();
    }


    public static PlayerRadarManager getInstance()
    {

        if (INSTANCE == null)
        {
            INSTANCE = new PlayerRadarManager();
        }
        return INSTANCE;
    }

    public Map<UUID, EntityPlayer> getPlayers()
    {
        synchronized (lock)
        {
            return this.playersOnServer;
        }
    }

    public void addPlayer(EntityPlayer player)
    {
        synchronized (lock)
        {
            this.playersOnServer.put(player.getUniqueID(), player);
        }
    }

    public void clearNetworkPlayers()
    {
        synchronized (lock)
        {
            this.playersOnServer.clear();
        }
    }

    private void updateClientPlayer(JsonObject player)
    {
        EntityPlayer clientPlayer = this.getPlayers().get(UUID.fromString(player.get("playerId").getAsString()));

        if (clientPlayer != null)
        {
            updatePlayer(clientPlayer, player);
        }
        else
        {
            this.addPlayer(buildPlayerFromJson(player));
        }
    }

    public void updatePlayers(JsonArray playerList)
    {
        synchronized (lock)
        {
            for (JsonElement p : playerList)
            {
                JsonObject player = p.getAsJsonObject();
                updateClientPlayer(player);
            }
        }
    }

    private void updatePlayer(EntityPlayer player, JsonObject json)
    {
        player.posX = json.get("posX").getAsInt();
        player.posY = json.get("posY").getAsInt();
        player.posZ = json.get("posZ").getAsInt();
        player.chunkCoordX = json.get("chunkX").getAsInt();
        player.chunkCoordY = json.get("chunkY").getAsInt();
        player.chunkCoordZ = json.get("chunkZ").getAsInt();
        player.rotationYawHead = json.get("rotation").getAsFloat();
        player.setSneaking(json.get("sneaking").getAsBoolean());
        player.dimension = json.get("dim").getAsInt();
    }

    /**
     * Create a list of fake players that are not in the client's entity list. The client's list only contains entities that
     * are with in proximity of the player. Any entity that is not in the client list will be created. If they are in the client list, they will not be created.
     * and this method will return null.
     *
     * @param player - The json representation of the player.
     * @return - The EntityPlayer or null.
     */
    private EntityPlayer buildPlayerFromJson(JsonObject player)
    {
        Minecraft mc = FMLClientHandler.instance().getClient();
        EntityPlayer playerMp;

        UUID playerUUID = UUID.fromString(player.get("playerId").getAsString());
        String playerName = player.get("name").getAsString();
        if (!playerUUID.equals(mc.player.getUniqueID()))
        {
            playerMp = new EntityOtherPlayerMP(mc.world, new GameProfile(playerUUID, playerName));
            updatePlayer(playerMp, player);
            playerMp.setUniqueId(playerUUID);
            playerMp.addedToChunk = true;
            return playerMp;
        }
        return null;
    }

}
