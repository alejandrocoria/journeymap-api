package journeymap.common.network;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import journeymap.common.feature.PlayerRadarManager;
import journeymap.common.network.impl.MessageProcessor;
import journeymap.common.network.impl.Response;
import journeymap.server.properties.GlobalProperties;
import journeymap.server.properties.PropertiesManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.UUID;

import static journeymap.common.network.Constants.TRACKING;
import static journeymap.common.network.Constants.TRACKING_UPDATE_TIME;
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
        boolean playerRadarEnabled = PropertiesManager.getInstance().getDimProperties(player.dimension).playerRadarEnabled.get();
        boolean receiverOp = isOp(player);
        if ((sendToEveryone && playerRadarEnabled) || (sendToOps && receiverOp))
        {
            try
            {
                JsonObject update = new JsonObject();
                int updateTime = PropertiesManager.getInstance().getGlobalProperties().playerTrackingUpdateTime.get();
                boolean userTrack = PropertiesManager.getInstance().getGlobalProperties().playerTrackingEnabled.get();
                boolean opTrack = PropertiesManager.getInstance().getGlobalProperties().opPlayerTrackingEnabled.get();

                update.addProperty(TRACKING, (userTrack || (receiverOp && opTrack)));
                update.addProperty(TRACKING_UPDATE_TIME, updateTime);

                sendPlayerTrackingData(player);
                return update;
            }
            catch (ConcurrentModificationException cme)
            {
                // do nothing.
                return null;
            }
        }

        return null;
    }

    private void sendPlayerTrackingData(EntityPlayerMP entityPlayerMP)
    {
        int receiverDimension = entityPlayerMP.dimension;
        boolean receiverOp = isOp(entityPlayerMP);
        List<EntityPlayerMP> serverPlayers = new ArrayList<>(FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers());
        List<JsonObject> playerList = new ArrayList<>();
        if (serverPlayers != null || serverPlayers.size() > 1)
        {
            for (EntityPlayerMP playerMp : serverPlayers)
            {
                boolean sneaking = playerMp.isSneaking();
                int dimension = playerMp.dimension;
                UUID playerId = playerMp.getUniqueID();

                if (!entityPlayerMP.getUniqueID().equals(playerId) && !sneaking && receiverDimension == dimension)
                {
                    playerList.add(buildJsonPlayer(playerMp, receiverOp));
                }
            }

            if (serverPlayers.size() < 10)
            {
                sendPlayerList(playerList);
            }
        }
    }

    /**
     * Sends 10 players at a time due to limitations in the packet size.
     *
     * @param allPlayers - The all players list.
     */
    private void sendPlayerList(List<JsonObject> allPlayers)
    {
        List<List<JsonObject>> partitionedPlayerList = Lists.partition(allPlayers, 10);
        for (List<JsonObject> playerList : partitionedPlayerList)
        {
            JsonArray playerArray = new JsonArray();
            for (JsonObject playerJsonObject : playerList)
            {
                playerArray.add(playerJsonObject);
            }
            JsonObject payload = new JsonObject();
            payload.add("players", playerArray);
            new GetPlayerLocations().sendToPlayer(payload, (EntityPlayerMP) player);
        }
    }

    private JsonObject buildJsonPlayer(EntityPlayer playerMp, boolean receiverOp)
    {
        boolean sneaking = playerMp.isSneaking();
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
        return player;
    }

    // Does this action when the message is received on the client.
    @Override
    protected JsonObject onClient(Response result)
    {
        if (result.getAsJson().get("players") != null)
        {
            JsonArray playerList = result.getAsJson().get("players").getAsJsonArray();
            PlayerRadarManager.getInstance().updatePlayers(playerList);
        }
        return null;
    }


}
