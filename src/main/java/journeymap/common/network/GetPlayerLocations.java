package journeymap.common.network;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import journeymap.common.Journeymap;
import journeymap.common.feature.PlayerRadarManager;
import journeymap.common.network.impl.MessageProcessor;
import journeymap.common.network.impl.Response;


/**
 * Sends a list of players to the client for tracking on the map.
 */
public class GetPlayerLocations extends MessageProcessor
{
    // Does this action when the message is received on the server.
    @Override
    protected JsonObject onServer(Response response)
    {
        return null;
    }

    // Does this action when the message is received on the client.
    @Override
    protected JsonObject onClient(Response result)
    {

        Journeymap.getClient().setPlayerTrackingEnabled(true);
        if (result.getAsJson().get("players") != null)
        {
            JsonArray playerList = result.getAsJson().get("players").getAsJsonArray();
            PlayerRadarManager.getInstance().updatePlayers(playerList);
        }
        return null;
    }
}
