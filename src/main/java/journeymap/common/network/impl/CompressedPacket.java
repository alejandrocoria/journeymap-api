package journeymap.common.network.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import journeymap.common.network.impl.utils.CompressionUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.IOException;

/**
 * This packet is used for large data sets that may exceed netty's packet size.
 * Do not use for packets that require speed as the compression may slow things down.
 */
public abstract class CompressedPacket extends MessageProcessor
{
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();
    private static final String COMPRESSED_DATA = "COMPRESSED_DATA";

    @Override
    protected void buildRequest(JsonObject requestData)
    {
        if (requestData != null)
        {
            try
            {
                String dataAsString = requestData.toString();
                String compressedData = CompressionUtils.compress(dataAsString);
                requestData = new JsonObject();
                requestData.addProperty(COMPRESSED_DATA, compressedData);
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
        try
        {
            JsonObject data = message.get(DATA_KEY).getAsJsonObject();
            if (data.get(COMPRESSED_DATA) != null)
            {
                String dataAsCompressedString = data.get(COMPRESSED_DATA).getAsString();
                String dataString = CompressionUtils.decompress(dataAsCompressedString);
                JsonObject jsonObject = GSON.fromJson(dataString, JsonObject.class);
                message.add(DATA_KEY, jsonObject);
                System.out.println("decompressing packet");
            }
        }
        catch (IOException e)
        {
            NetworkHandler.getLogger().error("ERROR: Unable to decompress compressed json packet:", message.get(OBJECT_KEY).getAsString());
        }
        super.handleResponse(message, ctx);
    }
}
