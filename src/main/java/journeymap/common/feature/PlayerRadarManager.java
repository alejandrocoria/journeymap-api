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

    private void updateClientPlayer(EntityPlayer player)
    {

        EntityPlayer clientPlayer = this.getPlayers().get(player.getUniqueID());

        if (clientPlayer != null)
        {
            clientPlayer.posX = player.posX;
            clientPlayer.posY = player.posY;
            clientPlayer.posZ = player.posZ;
            clientPlayer.chunkCoordX = player.chunkCoordX;
            clientPlayer.chunkCoordY = player.chunkCoordY;
            clientPlayer.chunkCoordZ = player.chunkCoordZ;
            clientPlayer.rotationYawHead = player.rotationYawHead;
            clientPlayer.setSneaking(player.isSneaking());
        }
        else
        {
            this.addPlayer(player);
        }
    }

    public void updatePlayers(JsonArray playerList)
    {
        synchronized (lock)
        {
            for (JsonElement p : playerList)
            {
                JsonObject player = p.getAsJsonObject();
                EntityPlayer entityPlayer = buildPlayerFromJson(player);
                if (entityPlayer != null)
                {
                    updateClientPlayer(entityPlayer);
                }
            }
        }
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
            playerMp.posX = player.get("posX").getAsInt();
            playerMp.posY = player.get("posY").getAsInt();
            playerMp.posZ = player.get("posZ").getAsInt();
            playerMp.chunkCoordX = player.get("chunkX").getAsInt();
            playerMp.chunkCoordY = player.get("chunkY").getAsInt();
            playerMp.chunkCoordZ = player.get("chunkZ").getAsInt();
            playerMp.rotationYawHead = player.get("rotation").getAsFloat();
            playerMp.setSneaking(player.get("sneaking").getAsBoolean());
            playerMp.setUniqueId(playerUUID);
            playerMp.addedToChunk = true;
            return playerMp;
        }
        return null;
    }
}
