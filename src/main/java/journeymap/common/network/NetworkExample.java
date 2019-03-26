package journeymap.common.network;

import com.google.gson.JsonObject;
import journeymap.common.network.core.MessageProcessor;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * This is currently an example of the new networking system.
 * <p>
 * to send a request
 * PlayerLocation pl = new PlayerLocation()
 * pl.setRequest(JsonNode) (setting the request data is optional, if you send without setting the request it sends and empty json node)
 * pl.send() //sends to player
 * pl.sendToPlayer(PlayerEntityMP) // sends to the server
 */
public class NetworkExample extends MessageProcessor
{
    @Override
    protected JsonObject onServer(JsonObject message, MessageContext ctx)
    {
        System.out.println(message.get("message").getAsString());
        JsonObject test = new JsonObject();
        test.addProperty("message", "hello client");
        test.addProperty("action", "stop");

        // no reply, ignore!
        return null;
    }

    @Override
    protected JsonObject onClient(JsonObject message, MessageContext ctx)
    {
        System.out.println(message.get("message").getAsString());
        JsonObject test = new JsonObject();
        test.addProperty("message", "helloServer");

        if (message.get("action") != null && message.get("action").getAsString().equals("stop"))
        {
            return null;
        }
        // This is the reply to be sent back to the server.
        return test;
    }
}
