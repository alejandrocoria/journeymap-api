package journeymap.common.network.impl;


import com.google.gson.JsonObject;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public interface Response<T>
{
    /**
     * Gets the data as json.
     *
     * @return - The Json data.
     */
    JsonObject getAsJson();

    /**
     * Gets the data as a String.
     *
     * @return - The string data.
     */
    String getAsString();

    /**
     * Gets the raw response as Json. It is the full package that has come in on the request.
     *
     * @return - The raw response.
     */
    T getRawResponse();

    /**
     * Gets the context that comes in on the response.
     *
     * @return - The Message context.
     */
    MessageContext getContext();
}
