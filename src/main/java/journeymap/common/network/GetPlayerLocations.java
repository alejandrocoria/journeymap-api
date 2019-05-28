package journeymap.common.network;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import journeymap.common.network.impl.MessageProcessor;
import journeymap.common.network.impl.Response;
import journeymap.server.properties.GlobalProperties;
import journeymap.server.properties.PropertiesManager;
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

        if ((sendToEveryone && playerRadarEnabled) || (sendToOps && isOp(player)))
        {
            try
            {
                return getPlayerList(player);
            }
            catch (ConcurrentModificationException cme)
            {
                // do nothing.
                return null;
            }
        }

        return null;
    }

    // Does this action when the message is received on the client.
    @Override
    protected JsonObject onClient(Response response)
    {
        return null;
    }

    private synchronized JsonObject getPlayerList(EntityPlayerMP entityPlayerMP) throws ConcurrentModificationException
    {
        int receiverDimension = entityPlayerMP.dimension;
        boolean receiverOp = isOp(entityPlayerMP);
        JsonArray playerList = new JsonArray();
        List<EntityPlayerMP> serverPlayers = new ArrayList<>(FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers());
        JsonObject players = new JsonObject();

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
        int updateTime = PropertiesManager.getInstance().getGlobalProperties().playerTrackingUpdateTime.get();
        boolean userTrack = PropertiesManager.getInstance().getGlobalProperties().playerTrackingEnabled.get();
        boolean opTrack = PropertiesManager.getInstance().getGlobalProperties().opPlayerTrackingEnabled.get();

        players.addProperty(TRACKING_UPDATE_TIME, updateTime);
        players.addProperty(TRACKING, (userTrack || (receiverOp && opTrack)));
        players.add("players", playerList);
        return players;
    }

}
