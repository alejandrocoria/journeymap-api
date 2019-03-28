package journeymap.common.network.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import journeymap.common.network.impl.utils.AsyncCallback;
import journeymap.common.network.impl.utils.CallbackService;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.UUID;

import static journeymap.common.network.impl.NetworkHandler.JOURNEYMAP_NETWORK_CHANNEL;

/**
 * Used primarily for JsonObject based network requests. Can and should be expanded for more data types.
 */
public abstract class MessageProcessor
{
    private static final String MESSAGE_KEY = "message_id";
    static final String OBJECT_KEY = "container_object";
    static final String DATA_KEY = "data";


    protected static Gson gson = new GsonBuilder().serializeNulls().create();

    private JsonObject data;
    private String clazz;
    protected UUID id;
    protected Side side;
    protected EntityPlayer player;

    /**
     * Called when the message is received on the server.
     *
     * @param response - The response.
     * @return - The reply is returned and sent to the player's client.
     */
    protected abstract JsonObject onServer(Response response);

    /**
     * Called when the message is received on the client.
     *
     * @param response - The response.
     * @return - The reply is returned and sent to the server.
     */
    protected abstract JsonObject onClient(Response response);

    /**
     * Replies the message.
     * If the message was received on the server it sends it back to the client.
     * If the message was received on the client it sends it back to the server.
     * <p>
     * Be careful of infinite loops!
     *
     * @param data - The new data.
     */
    private void reply(JsonObject data)
    {
        this.data = new JsonObject();
        this.data.addProperty(MESSAGE_KEY, getId().toString());
        this.data.addProperty(OBJECT_KEY, this.clazz);
        this.data.add(DATA_KEY, data);
        if (side.isServer())
        {
            sendToPlayer(data, (EntityPlayerMP) this.player);
        }
        else
        {
            send(data);
        }
    }

    /**
     * Process the response and prepares it for a reply.
     *
     * @param message - The response message.
     * @param ctx     - The message context.
     */
    void processResponse(JsonObject message, MessageContext ctx)
    {
        CallbackService callbackService = CallbackService.getInstance();
        JsonObject reply;
        this.side = ctx.side;
        this.data = message.get(DATA_KEY).getAsJsonObject();
        this.id = UUID.fromString(message.get(MESSAGE_KEY).getAsString());
        this.clazz = message.get(OBJECT_KEY).getAsString();

        JsonResponse response = new JsonResponse(message, ctx);
        if (side.isServer())
        {
            this.player = ctx.getServerHandler().player;
            reply = onServer(response);
        }
        else
        {
            reply = onClient(response);
        }

        if (reply != null)
        {
            reply(reply);
            return;
        }

        if (callbackService.getCallback(id) != null)
        {
            callbackService.getCallback(id).onSuccess(response);
            callbackService.removeCallback(id);
        }
    }

    /**
     * Sets the data to be sent.
     *
     * @param requestData - The data.
     */
    private void buildRequest(JsonObject requestData)
    {
        if (requestData == null)
        {
            requestData = new JsonObject();
        }
        this.data = new JsonObject();
        this.data.addProperty(MESSAGE_KEY, getId().toString());
        this.data.addProperty(OBJECT_KEY, this.getClass().getName());
        this.data.add(DATA_KEY, requestData);
    }

    private UUID getId()
    {
        if (id == null)
        {
            this.id = UUID.randomUUID();
            return id;
        }
        return this.id;
    }

    /**
     * Sends the data package to the server.
     */
    @SideOnly(Side.CLIENT)
    public void send(JsonObject requestData)
    {
        buildRequest(requestData);
        JOURNEYMAP_NETWORK_CHANNEL.sendToServer(new Message(gson.toJson(data)));
    }

    /**
     * Sends the data package to the server with callback.
     *
     * @param callback - The callback.
     */
    @SideOnly(Side.CLIENT)
    public void send(JsonObject requestData, AsyncCallback callback)
    {
        buildRequest(requestData);
        CallbackService.getInstance().saveCallback(this.id, callback);
        JOURNEYMAP_NETWORK_CHANNEL.sendToServer(new Message(gson.toJson(data)));
    }

    /**
     * Sends the data package to the client of the player.
     *
     * @param player - The player.
     */
    @SideOnly(Side.SERVER)
    public void sendToPlayer(JsonObject requestData, EntityPlayerMP player)
    {
        buildRequest(requestData);
        // Verify the player has forge and can receive forge packets.
        if (player.connection.getNetworkManager().channel().attr(NetworkRegistry.FML_MARKER).get())
        {
            JOURNEYMAP_NETWORK_CHANNEL.sendTo(new Message(gson.toJson(data)), player);
        }
    }

    /**
     * Sends the data package to the client of the player with callback.
     *
     * @param player   - The player.
     * @param callback - The callback.
     */
    @SideOnly(Side.SERVER)
    public void sendToPlayer(JsonObject requestData, EntityPlayerMP player, AsyncCallback callback)
    {
        buildRequest(requestData);
        CallbackService.getInstance().saveCallback(this.id, callback);
        // Verify the player has forge and can receive forge packets.
        if (player.connection.getNetworkManager().channel().attr(NetworkRegistry.FML_MARKER).get())
        {
            JOURNEYMAP_NETWORK_CHANNEL.sendTo(new Message(gson.toJson(data)), player);
        }
    }
}
