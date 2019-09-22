package journeymap.common.network.impl;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class Message implements IMessage
{
    private String message;

    // default constructor needed for forge's networking
    public Message()
    {
    }

    public Message(String message)
    {
        this.message = message;
    }


    public String getMessage()
    {
        return message;
    }


    @Override
    public void fromBytes(ByteBuf buf)
    {
        try
        {
            message = ByteBufUtils.readUTF8String(buf);
        }
        catch (Throwable t)
        {
            NetworkHandler.getLogger().error(String.format("Failed to read message: %s", t));
        }
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        try
        {
            if (message != null)
            {
                ByteBufUtils.writeUTF8String(buf, message);
            }
        }
        catch (Throwable t)
        {
            NetworkHandler.getLogger().error("[toBytes]Failed to read message: " + t);
        }
    }
}
