package journeymap.common.network;

import com.google.gson.JsonObject;
import journeymap.common.network.impl.MessageProcessor;
import journeymap.common.network.impl.Response;

public class UpdateConfigsService extends MessageProcessor
{
    @Override
    protected JsonObject onServer(Response response)
    {
        JsonObject updatedProperties = response.getAsJson();


        return null;
    }

    @Override
    protected JsonObject onClient(Response response)
    {
        return null;
    }
}
