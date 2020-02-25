package journeymap.common.network;

import com.google.gson.JsonObject;
import journeymap.common.network.impl.MessageProcessor;
import journeymap.common.network.impl.Response;
import journeymap.server.Constants;
import journeymap.server.JourneymapServer;

public class DevModeTest extends MessageProcessor
{
    @Override
    protected JsonObject onServer(Response response)
    {
        if (Constants.isDev(response.getContext().getServerHandler().player))
        {
            JourneymapServer.DEV_MODE = response.getAsJson().get("value").getAsBoolean();
        }

        return null;
    }

    @Override
    protected JsonObject onClient(Response response)
    {
        return null;
    }
}
