package journeymap.common.network.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import journeymap.common.Journeymap;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static journeymap.common.network.impl.MessageProcessor.OBJECT_KEY;

public class MessageListener implements IMessageHandler<Message, IMessage>
{
    private Logger logger = Journeymap.getLogger();

    @Override
    public IMessage onMessage(Message message, MessageContext ctx)
    {
        Gson gson = new GsonBuilder().serializeNulls().create();
        try
        {
            JsonObject response = gson.fromJson(message.getMessage(), JsonObject.class);
            String clazz = response.get(OBJECT_KEY).getAsString();
            Class requestObject = Class.forName(clazz);
            Method method = requestObject.getMethod("process", JsonObject.class, MessageContext.class, Class.class);
            method.invoke(null, response, ctx, requestObject);
        }
        catch (ClassNotFoundException e)
        {
            logger.warn("Message processor not found: ", e);
        }
        catch (IllegalAccessException e)
        {
            logger.warn("Cannot access message processor: ", e);
        }
        catch (NullPointerException e)
        {
            logger.warn("Null Response: ", e);
        }
        catch (NoSuchMethodException | InvocationTargetException e)
        {
            logger.warn("Unable to initiate message processing", e);
        }
        return null;
    }
}