package journeymap.common.network.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import static journeymap.common.network.core.MessageProcessor.CONTAINER_OBJECT;

public class MessageListener implements IMessageHandler<Message, IMessage>
{
    @Override
    public IMessage onMessage(Message message, MessageContext ctx)
    {
        Gson gson = new GsonBuilder().serializeNulls().create();
        try
        {
            JsonObject response = gson.fromJson(message.getMessage(), JsonObject.class);
            String clazz = response.get(CONTAINER_OBJECT).getAsString();
            Class requestObject = Class.forName(clazz);
            MessageProcessor messageProcessor = (MessageProcessor) requestObject.newInstance();
            messageProcessor.processResponse(response, ctx);
        }
        catch (ClassNotFoundException e)
        {
            // TODO: Add loggers!
            e.printStackTrace();
        }
        catch (InstantiationException e)
        {
            // TODO: Add loggers!
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            // TODO: Add loggers!
            e.printStackTrace();
        }
        catch (NullPointerException e)
        {
            // TODO: Add loggers!
            e.printStackTrace();
        }
        return null;
    }
}