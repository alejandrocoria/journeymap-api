package journeymap.common.network;

import com.google.gson.JsonObject;
import journeymap.common.feature.JourneyMapTeleport;
import journeymap.common.feature.Location;
import journeymap.common.network.impl.MessageProcessor;
import journeymap.common.network.impl.Response;

import static journeymap.common.Constants.DIM;
import static journeymap.common.Constants.X;
import static journeymap.common.Constants.Y;
import static journeymap.common.Constants.Z;

public class TeleportPlayer extends MessageProcessor
{
    @Override
    protected JsonObject onServer(Response response)
    {
        JsonObject jsonObject = response.getAsJson();

        Location location = new Location(
                jsonObject.get(X).getAsDouble(),
                jsonObject.get(Y).getAsDouble(),
                jsonObject.get(Z).getAsDouble(),
                jsonObject.get(DIM).getAsInt()
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
