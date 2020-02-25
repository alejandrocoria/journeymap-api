package journeymap.common.network.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.apache.logging.log4j.Logger;

import static journeymap.common.network.impl.MessageProcessor.OBJECT_KEY;

public class MessageListener implements IMessageHandler<Message, IMessage>
{
    private Logger logger = NetworkHandler.getLogger();

    @Override
    public IMessage onMessage(Message message, MessageContext ctx)
    {
        Gson gson = new GsonBuilder().serializeNulls().create();
        try
        {
            JsonObject response = gson.fromJson(message.getMessage(), JsonObject.class);
            String clazz = response.get(OBJECT_KEY).getAsString();
            Class requestObject = Class.forName(clazz);
            if (requestObject.getSuperclass() == MessageProcessor.class || requestObject.getSuperclass() == CompressedPacket.class)
            {
                MessageProcessor.process(response, ctx, requestObject);
            }
            else
            {
                String error = String.format("Bad Network request: %s attempted to send an unqualified packet request.",
                        ctx.side.isClient() ? "THE SERVER" : ctx.getServerHandler().player.getName());
                logger.error(error);
            }
        }
        catch (ClassNotFoundException e)
        {
            logger.warn("Message processor not found: ", e);
        }
        catch (NullPointerException e)
        {
            logger.warn("Null Response: ", e);
        }
        return null;
    }
}