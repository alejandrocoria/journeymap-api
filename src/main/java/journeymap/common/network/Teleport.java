package journeymap.common.network;

import com.google.gson.JsonObject;
import journeymap.common.feature.JourneyMapTeleport;
import journeymap.common.feature.Location;
import journeymap.common.network.impl.MessageProcessor;
import journeymap.common.network.impl.Response;

public class Teleport extends MessageProcessor
{
    @Override
    protected JsonObject onServer(Response response)
    {
        JsonObject jsonObject = response.getAsJson();

        Location location = new Location(
                jsonObject.get("x").getAsDouble(),
                jsonObject.get("y").getAsDouble(),
                jsonObject.get("z").getAsDouble(),
                jsonObject.get("dim").getAsInt()
        );
        JourneyMapTeleport.instance().attemptTeleport(response.getContext().getServerHandler().player, location);
        return null;
    }

    @Override
    protected JsonObject onClient(Response response)
    {
        return null;
    }
}
