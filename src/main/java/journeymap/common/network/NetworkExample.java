package journeymap.common.network;

import com.google.gson.JsonObject;
import journeymap.common.network.core.MessageProcessor;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * This is currently an example of the new networking system.
 * <p>
 * to send a request
 * NetworkExample example = new NetworkExample()
 * example.setRequest(JsonNode) (setting the request data is optional, if you send without setting the request it sends an empty json node)
 * example.send() //sends to server
 * example.sendToPlayer(PlayerEntityMP) // sends to the player's client
 */
public class NetworkExample extends MessageProcessor
{
    // Does this action when the message is received on the server.
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

    // Does this action when the message is received on the client.
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
