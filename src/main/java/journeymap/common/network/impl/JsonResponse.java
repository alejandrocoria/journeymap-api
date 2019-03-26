package journeymap.common.network.impl;

import com.google.gson.JsonObject;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import static journeymap.common.network.impl.MessageProcessor.DATA_KEY;

public class JsonResponse implements Response<JsonObject>
{
    private JsonObject rawResponse;
    private MessageContext messageContext;

    JsonResponse(JsonObject rawResponse, MessageContext messageContext)
    {
        this.rawResponse = rawResponse;
        this.messageContext = messageContext;
    }

    @Override
    public JsonObject getAsJson()
    {
        return rawResponse.get(DATA_KEY).getAsJsonObject();
    }

    @Override
    public String getAsString()
    {
        return rawResponse.get(DATA_KEY).getAsString();
    }

    @Override
    public JsonObject getRawResponse()
    {
        return rawResponse;
    }

    @Override
    public MessageContext getContext()
    {
        return messageContext;
    }
}
