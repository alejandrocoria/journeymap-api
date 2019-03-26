package journeymap.common.network.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.UUID;

import static journeymap.common.network.core.NetworkHandler.JOURNEYMAP_NETWORK_CHANNEL;

public abstract class MessageProcessor
{
    private static final String DATA = "data";
    private static final String MESSAGE_ID = "message_id";
    static final String CONTAINER_OBJECT = "container_object";

    protected static Gson gson = new GsonBuilder().serializeNulls().create();

    private JsonObject data;
    private String clazz;
    protected UUID id;
    protected Side side;
    protected EntityPlayer player;

    /**
     * Called when the message is received on the server.
     *
     * @param message - The Json message.
     * @param ctx     - The message context.
     * @return - The reply is returned and sent to the player's client.
     */
    protected abstract JsonObject onServer(JsonObject message, MessageContext ctx);

    /**
     * Called when the message is received on the client.
     *
     * @param message - The Json message.
     * @param ctx     - The message context.
     * @return - The reply is returned and sent to the server.
     */
    protected abstract JsonObject onClient(JsonObject message, MessageContext ctx);

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
        this.data.addProperty(MESSAGE_ID, getId().toString());
        this.data.addProperty(CONTAINER_OBJECT, this.clazz);
        this.data.add(DATA, data);
        if (side.isServer())
        {
            sendToPlayer((EntityPlayerMP) this.player);
        }
        else
        {
            send();
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
        JsonObject reply;
        this.side = ctx.side;
        this.data = message.get(DATA).getAsJsonObject();
        this.id = UUID.fromString(message.get(MESSAGE_ID).getAsString());
        this.clazz = message.get(CONTAINER_OBJECT).getAsString();

        if (side.isServer())
        {
            this.player = ctx.getServerHandler().player;
            reply = onServer(data, ctx);
        }
        else
        {
            reply = onClient(data, ctx);
        }

        if (reply != null)
        {
            reply(reply);
        }
    }

    /**
     * Sets the data to be sent.
     *
     * @param requestData - The data.
     */
    public void setRequest(JsonObject requestData)
    {
        this.data = new JsonObject();
        this.data.addProperty(MESSAGE_ID, getId().toString());
        this.data.addProperty(CONTAINER_OBJECT, this.getClass().getName());
        this.data.add(DATA, requestData);
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
    public void send()
    {
        verifyRequestData();
        JOURNEYMAP_NETWORK_CHANNEL.sendToServer(new Message(gson.toJson(data)));
    }

    /**
     * Sends the data package to the client of the player.
     *
     * @param player - The player.
     */
    @SideOnly(Side.SERVER)
    public void sendToPlayer(EntityPlayerMP player)
    {
        verifyRequestData();
        // Verify the player has forge and can receive forge packets.
        if (player.connection.getNetworkManager().channel().attr(NetworkRegistry.FML_MARKER).get())
        {
            JOURNEYMAP_NETWORK_CHANNEL.sendTo(new Message(gson.toJson(data)), player);
        }
    }

    /**
     * Checks if the data is null before sending a request.
     * If the data is null, it creates an empty json object so that empty requests can be made.
     */
    private void verifyRequestData()
    {
        if (data == null)
        {
            setRequest(new JsonObject());
        }
    }
}
