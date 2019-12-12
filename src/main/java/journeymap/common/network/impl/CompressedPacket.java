package journeymap.common.network.impl;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import journeymap.common.network.impl.utils.CompressionUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.IOException;

public abstract class CompressedPacket extends MessageProcessor
{
    @Override
    protected void buildRequest(JsonObject requestData)
    {
        System.out.println("huh");
        if (requestData != null)
        {
            System.out.println("attempting to compress packet.");
            try
            {
                String dataAsString = requestData.toString();
                CompressionUtils.compress(dataAsString);
                requestData.addProperty("COMPRESSED", dataAsString);
                System.out.println("compressing packet");
            }
            catch (IOException e)
            {
                NetworkHandler.getLogger().error("ERROR: Unable to compress compressed json packet");
            }
        }
        super.buildRequest(requestData);
    }

    @Override
    protected void handleResponse(JsonObject message, MessageContext ctx)
    {
        System.out.println("attempting to decompress packet.");
        try
        {
            JsonObject data = message.get(DATA_KEY).getAsJsonObject();
            if (data.get("COMPRESSED") != null)
            {
                String dataAsCompressedString = data.get("COMPRESSED").getAsString();
                String dataString = CompressionUtils.decompress(dataAsCompressedString);
                JsonObject jsonObject = new GsonBuilder().serializeNulls().create().fromJson(dataString, JsonObject.class);
                message.add(DATA_KEY, jsonObject);
                System.out.println("decompressing packet");
            }
        }
        catch (IOException e)
        {
           NetworkHandler.getLogger().error("ERROR: Unable to decompress compressed json packet");
        }
        super.handleResponse(message, ctx);
    }
}
