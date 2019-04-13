package journeymap.common.network;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import journeymap.common.Journeymap;
import journeymap.common.network.impl.MessageProcessor;
import journeymap.common.network.impl.Response;
import journeymap.server.properties.GlobalProperties;
import journeymap.server.properties.PropertiesManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.List;
import java.util.UUID;

import static journeymap.server.JourneymapServer.isOp;

/**
 * Sends a list of players to the client for tracking on the map.
 */
public class GetPlayerLocations extends MessageProcessor
{
    // Does this action when the message is received on the server.
    @Override
    protected JsonObject onServer(Response response)
    {
        GlobalProperties prop = PropertiesManager.getInstance().getGlobalProperties();
        boolean sendToEveryone = prop.playerTrackingEnabled.get();
        boolean sendToOps = prop.opPlayerTrackingEnabled.get();
        EntityPlayerMP player = response.getContext().getServerHandler().player;
        if (sendToEveryone || (sendToOps && isOp(player)))
        {
            return getPlayerList(player);
        }

        return null;
    }

    // Does this action when the message is received on the client.
    @Override
    protected JsonObject onClient(Response response)
    {
        JsonArray playerList = response.getAsJson().get("players").getAsJsonArray();
        Journeymap.getClient().playersOnServer.clear();
        for (JsonElement p : playerList)
        {
            JsonObject player = p.getAsJsonObject();
            Journeymap.getClient().playersOnServer.put(UUID.fromString(player.get("playerId").getAsString()), player);
        }
        return null;
    }

    private JsonObject getPlayerList(EntityPlayerMP entityPlayerMP)
    {
        int receiverDimension = entityPlayerMP.dimension;
        boolean receiverOp = isOp(entityPlayerMP);
        JsonArray playerList = new JsonArray();
        List<EntityPlayerMP> serverPlayers = null;
        JsonObject players = new JsonObject();
        serverPlayers = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers();
        if (serverPlayers != null || serverPlayers.size() > 1)
        {
            for (EntityPlayerMP playerMp : serverPlayers)
            {
                boolean sneaking = playerMp.isSneaking();
                int dimension = playerMp.dimension;
                UUID playerId = playerMp.getUniqueID();
                if (receiverOp)
                {
                    sneaking = false;
                }
                JsonObject player = new JsonObject();
                player.addProperty("name", playerMp.getName());
                player.addProperty("posX", playerMp.getPosition().getX());
                player.addProperty("posY", playerMp.getPosition().getY());
                player.addProperty("posZ", playerMp.getPosition().getZ());
                player.addProperty("chunkX", playerMp.chunkCoordX);
                player.addProperty("chunkY", playerMp.chunkCoordY);
                player.addProperty("chunkZ", playerMp.chunkCoordZ);
                player.addProperty("rotation", playerMp.rotationYawHead);
                player.addProperty("sneaking", sneaking);
                player.addProperty("playerId", playerId.toString());

                // Don't send the player to them self and don't send sneaking players unless op is receiving.
                if (!entityPlayerMP.getUniqueID().equals(playerId) && !sneaking && receiverDimension == dimension)
                {
                    playerList.add(player);
                }
            }
        }
        players.add("players", playerList);
        return players;
    }

}
